package com.example.RestaurantApplication.module.admin.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RestaurantApplication.config.redis.TokenBlacklistService;
import com.example.RestaurantApplication.config.tracing.LogHelper;
import com.example.RestaurantApplication.module.permission.model.PermissionApi;
import com.example.RestaurantApplication.module.permission.model.PermissionHasPermissionApi;
import com.example.RestaurantApplication.module.permission.repository.PermissionApiRepository;
import com.example.RestaurantApplication.module.permission.repository.PermissionHasPermissionApiRepository;
import com.example.RestaurantApplication.module.role.model.RoleHasPermission;
import com.example.RestaurantApplication.module.role.repository.RoleHasPermissionRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class AdminPermissionApiService {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionApiService.class);

    @Autowired
    private PermissionApiRepository permissionApiRepository;

    @Autowired
    private PermissionHasPermissionApiRepository permissionHasPermissionApiRepository;

    @Autowired
    private RoleHasPermissionRepository roleHasPermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // TTL for token revocation (7 days = refresh token expiration)
    private static final long TOKEN_REVOCATION_TTL = 604800000L;

    public List<PermissionApi> getAllPermissionApis() {
        return permissionApiRepository.findAllActive();
    }

    public PermissionApi getPermissionApiById(Long id) {
        return permissionApiRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_API_NOT_FOUND",
                "Permission API not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public PermissionApi createPermissionApi(String code, String name, String endpoint, String method) {
        if (permissionApiRepository.existsByCode(code)) {
            log.warn("[{}] PERMISSION_API_EXISTS: code={}", LogHelper.loc(), code);
            throw new BusinessException("PERMISSION_API_EXISTS",
                "Permission API already exists with code: " + code, HttpStatus.CONFLICT);
        }

        PermissionApi permissionApi = new PermissionApi();
        permissionApi.setCode(code);
        permissionApi.setName(name);
        permissionApi.setEndpoint(endpoint);
        permissionApi.setMethod(method.toUpperCase());
        PermissionApi saved = permissionApiRepository.save(permissionApi);
        log.info("[{}] PermissionApi created: code={}", LogHelper.loc(), code);
        return saved;
    }

    @Transactional
    public PermissionApi updatePermissionApi(Long id, String code, String name, String endpoint, String method) {
        PermissionApi permissionApi = permissionApiRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_API_NOT_FOUND",
                "Permission API not found with id " + id, HttpStatus.NOT_FOUND));

        if (!permissionApi.getCode().equals(code) && permissionApiRepository.existsByCode(code)) {
            log.warn("[{}] PERMISSION_API_EXISTS: code={}", LogHelper.loc(), code);
            throw new BusinessException("PERMISSION_API_EXISTS",
                "Permission API already exists with code: " + code, HttpStatus.CONFLICT);
        }

        permissionApi.setCode(code);
        permissionApi.setName(name);
        permissionApi.setEndpoint(endpoint);
        permissionApi.setMethod(method.toUpperCase());
        PermissionApi savedApi = permissionApiRepository.save(permissionApi);

        // Check if this API is assigned to any permissions, then revoke
        revokeTokensIfPermissionApiIsAssigned(id);
        log.info("[{}] PermissionApi updated: id={}", LogHelper.loc(), id);

        return savedApi;
    }

    @Transactional
    public PermissionApi patchPermissionApi(Long id, String code, String name, String endpoint, String method) {
        PermissionApi permissionApi = permissionApiRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_API_NOT_FOUND",
                "Permission API not found with id " + id, HttpStatus.NOT_FOUND));

        if (code != null && !code.isBlank()) {
            if (!permissionApi.getCode().equals(code) && permissionApiRepository.existsByCode(code)) {
                log.warn("[{}] PERMISSION_API_EXISTS: code={}", LogHelper.loc(), code);
                throw new BusinessException("PERMISSION_API_EXISTS",
                    "Permission API already exists with code: " + code, HttpStatus.CONFLICT);
            }
            permissionApi.setCode(code);
        }

        if (name != null && !name.isBlank()) {
            permissionApi.setName(name);
        }

        if (endpoint != null && !endpoint.isBlank()) {
            permissionApi.setEndpoint(endpoint);
        }

        if (method != null && !method.isBlank()) {
            permissionApi.setMethod(method.toUpperCase());
        }

        PermissionApi savedApi = permissionApiRepository.save(permissionApi);

        // Check if this API is assigned to any permissions, then revoke
        revokeTokensIfPermissionApiIsAssigned(id);

        return savedApi;
    }

    @Transactional
    public void deletePermissionApi(Long id) {
        PermissionApi permissionApi = permissionApiRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_API_NOT_FOUND",
                "Permission API not found with id " + id, HttpStatus.NOT_FOUND));

        // Check if this API is assigned to any permissions, then revoke before deleting
        revokeTokensIfPermissionApiIsAssigned(id);

        permissionApi.setDeletedAt(LocalDateTime.now());
        permissionApiRepository.save(permissionApi);
        log.info("[{}] PermissionApi deleted: id={}", LogHelper.loc(), id);
    }

    /**
     * Check if PermissionApi is assigned to any Permissions.
     * Only revoke tokens if it's actually being used.
     * Flow: PermissionApi → Permissions using it → Roles with those Permissions → Users
     */
    private void revokeTokensIfPermissionApiIsAssigned(Long permissionApiId) {
        // Check if this API is assigned to any permissions
        List<PermissionHasPermissionApi> assignments =
            permissionHasPermissionApiRepository.findByPermissionApiId(permissionApiId);

        // Only proceed if API is actually being used
        if (assignments.isEmpty()) {
            return; // Not assigned anywhere, no need to revoke
        }

        // Collect all affected users (avoid duplicates with Set)
        Set<Long> affectedUserIds = new HashSet<>();

        for (PermissionHasPermissionApi assignment : assignments) {
            Long permissionId = assignment.getPermissionId();

            // Find roles that have this permission
            List<RoleHasPermission> rolePermissions =
                roleHasPermissionRepository.findByPermissionId(permissionId);

            for (RoleHasPermission rolePermission : rolePermissions) {
                // Find users with this role
                List<User> users = userRepository.findByUserRoleId(rolePermission.getUserRoleId());
                for (User user : users) {
                    affectedUserIds.add(user.getId());
                }
            }
        }

        // Revoke tokens for all affected users
        for (Long userId : affectedUserIds) {
            tokenBlacklistService.revokeAllUserTokens(userId, TOKEN_REVOCATION_TTL);
        }
        log.info("[{}] Tokens revoked for API change: apiId={}, affectedUsers={}", LogHelper.loc(), permissionApiId, affectedUserIds.size());
    }
}
