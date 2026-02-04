package com.example.RestaurantApplication.config.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.RestaurantApplication.config.redis.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.equals("/api/v1/auth/logout")) {
            return false;
        }
        return path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        
        if (token.isEmpty()
            || "null".equalsIgnoreCase(token)
            || "undefined".equalsIgnoreCase(token)
            || token.chars().filter(ch -> ch == '.').count() != 2) {
            filterChain.doFilter(request, response);
            return;
        }

        String username;
        String roleName;
        Long restaurantId;
        Long userId;
        try {
            username = jwtService.extractUsername(token);
            roleName = jwtService.extractUserRole(token);
            restaurantId = jwtService.extractRestaurantId(token);
            userId = jwtService.extractUserId(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
            {
            "success": false,
            "code": "TOKEN_EXPIRED",
            "message": "Access token expired"
            }
            """);
            return;

        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            // token sai, bị sửa, không hợp lệ
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
            {
            "success": false,
            "code": "INVALID_TOKEN",
            "message": "Invalid access token"
            }
            """);
            return;
        }

        // Check if the access token is not blacklisted (JTI check)
        String jti = jwtService.extractJtiString(token);
        if (tokenBlacklistService.isTokenBlacklisted(jti)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
            {
            "success": false,
            "code": "TOKEN_REVOKED",
            "message": "Access token has been revoked"
            }
            """);
            return;
        }

        // Check if token was issued before user revocation (timestamp check)
        Long revokedAt = tokenBlacklistService.getUserRevokedTimestamp(userId);
        if (revokedAt != null) {
            long tokenIssuedAt = jwtService.extractIssuedAt(token);
            if (tokenIssuedAt < revokedAt) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                {
                "success": false,
                "code": "USER_TOKENS_REVOKED",
                "message": "All tokens for this user have been revoked. Please login again."
                }
                """);
                return;
            }
        }


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.isTokenValid(token, username)) {
                var authorities = List.of(new SimpleGrantedAuthority(roleName));
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    );

                // Dùng HashMap thay vì Map.of() để support null values (cho ADMIN users)
                Map<String, Object> details = new HashMap<>();
                details.put("userName", username);
                details.put("role", roleName);
                details.put("restaurant_id", restaurantId);  // Có thể null cho ADMIN
                details.put("user_id", userId);
                details.put("web_details", new WebAuthenticationDetailsSource().buildDetails(request));

                auth.setDetails(details);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
