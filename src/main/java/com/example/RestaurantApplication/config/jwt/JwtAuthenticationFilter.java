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
import com.example.RestaurantApplication.config.tracing.LogHelper;

import io.jsonwebtoken.Claims;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
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

        // Parse JWT 1 lần duy nhất → lấy hết fields từ Claims
        Claims claims;
        try {
            claims = jwtService.extractClaims(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("[{}] TOKEN_EXPIRED: {}", LogHelper.loc(), e.getMessage());
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
            log.warn("[{}] INVALID_TOKEN: {}", LogHelper.loc(), e.getMessage());
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

        String username = claims.getSubject();
        String roleName = (String) claims.get("role");
        Integer restaurantIdInt = (Integer) claims.get("restaurantId");
        Long restaurantId = restaurantIdInt != null ? restaurantIdInt.longValue() : null;
        Integer userIdInt = (Integer) claims.get("userId");
        Long userId = userIdInt != null ? userIdInt.longValue() : null;
        String jti = claims.getId();
        long tokenIssuedAt = claims.getIssuedAt() != null ? claims.getIssuedAt().getTime() : 0;
        

        // Check if the access token is not blacklisted (JTI check)
        if (tokenBlacklistService.isTokenBlacklisted(jti)) {
            log.warn("[{}] TOKEN_REVOKED: jti={}", LogHelper.loc(), jti);
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
        if (!roleName.equalsIgnoreCase("ROLE_ADMIN")) {
            Long revokedAt = tokenBlacklistService.getUserRevokedTimestamp(userId);
            if (revokedAt != null && tokenIssuedAt < revokedAt) {
                log.warn("[{}] USER_TOKENS_REVOKED: userId={}", LogHelper.loc(), userId);
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
            var authorities = List.of(new SimpleGrantedAuthority(roleName));
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
                );

            Map<String, Object> details = new HashMap<>();
            details.put("userName", username);
            details.put("role", roleName);
            details.put("restaurant_id", restaurantId);
            details.put("user_id", userId);
            details.put("web_details", new WebAuthenticationDetailsSource().buildDetails(request));

            auth.setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
