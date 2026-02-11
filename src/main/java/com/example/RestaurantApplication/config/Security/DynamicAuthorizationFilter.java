package com.example.RestaurantApplication.config.Security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.RestaurantApplication.config.tracing.LogHelper;

import com.example.RestaurantApplication.module.permission.model.PermissionApi;
import com.example.RestaurantApplication.module.permission.repository.PermissionApiRepository;
import com.example.RestaurantApplication.module.role.model.RoleHasPermission;
import com.example.RestaurantApplication.module.role.repository.RoleHasPermissionRepository;
import com.example.RestaurantApplication.module.role.repository.UserRoleRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DynamicAuthorizationFilter.class);
    private final UserRoleRepository userRoleRepository;
    private final RoleHasPermissionRepository roleHasPermissionRepository;
    private final PermissionApiRepository permissionApiRepository;
    private final AntPathMatcher pathMatcher;

    public DynamicAuthorizationFilter(
            UserRoleRepository userRoleRepository,
            RoleHasPermissionRepository roleHasPermissionRepository,
            PermissionApiRepository permissionApiRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleHasPermissionRepository = roleHasPermissionRepository;
        this.permissionApiRepository = permissionApiRepository;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip authentication endpoints and admin endpoints (already handled by SecurityConfig)
        return path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/admin/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If not authenticated, let Spring Security handle it
        if (auth == null || !auth.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        // Skip permission check for ROLE_ADMIN (full access)
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            chain.doFilter(request, response);
            return;
        }

        // Get user's role name from authentication
        String roleName = null;
        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object roleObj = details.get("role");
            if (roleObj instanceof String) {
                roleName = (String) roleObj;
            }
        }

        if (roleName == null) {
            sendForbiddenResponse(response, "Missing role information");
            return;
        }

        // Get user's role from database
        var userRoleOpt = userRoleRepository.findByNameActive(roleName);
        if (userRoleOpt.isEmpty()) {
            sendForbiddenResponse(response, "Role not found");
            return;
        }

        Long userRoleId = userRoleOpt.get().getId();

        // Get all permission IDs for this role
        List<RoleHasPermission> rolePermissions = roleHasPermissionRepository.findByUserRoleId(userRoleId);
        if (rolePermissions.isEmpty()) {
            // Role has no permissions assigned
            sendForbiddenResponse(response, "No permissions assigned to this role");
            return;
        }

        Set<Long> permissionIds = rolePermissions.stream()
                .map(RoleHasPermission::getPermissionId)
                .collect(Collectors.toSet());

        // Get all PermissionApis for these permissions
        List<PermissionApi> permissionApis = permissionApiRepository.findByPermissionIds(permissionIds);

        // Get current request path and method
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();

        // Check if any permission API matches the current request
        boolean hasPermission = permissionApis.stream()
                .anyMatch(api -> matchesEndpoint(api, requestPath, requestMethod));

        if (!hasPermission) {
            sendForbiddenResponse(response, "Access denied: Insufficient permissions for this endpoint");
            return;
        }

        // User has permission, proceed
        chain.doFilter(request, response);
    }

    /**
     * Check if a PermissionApi matches the current request using AntPathMatcher
     *
     * @param api PermissionApi entity
     * @param requestPath Current request path (e.g., "/api/v1/user/123")
     * @param requestMethod Current HTTP method (e.g., "GET")
     * @return true if matches
     */
    private boolean matchesEndpoint(PermissionApi api, String requestPath, String requestMethod) {
        // Check HTTP method match
        if (!api.getMethod().equalsIgnoreCase(requestMethod)) {
            return false;
        }

        // Use AntPathMatcher for pattern matching
        // Supports: exact match, wildcards (*, **), path variables ({id})
        // Examples:
        // - "/api/v1/user" matches "/api/v1/user" (exact)
        // - "/api/v1/user/*" matches "/api/v1/user/123"
        // - "/api/v1/user/**" matches "/api/v1/user/123/profile"
        // - "/api/v1/user/{id}" matches "/api/v1/user/123"
        return pathMatcher.match(api.getEndpoint(), requestPath);
    }

    /**
     * Send 403 Forbidden response with JSON error message
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        log.warn("[{}] RBAC_DENIED: {}", LogHelper.loc(), message);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(String.format("""
                {
                  "success": false,
                  "code": "FORBIDDEN",
                  "message": "%s"
                }
                """, message));
    }
}