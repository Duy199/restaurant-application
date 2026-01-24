package com.example.RestaurantApplication.config.jwt;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.RestaurantApplication.config.redis.TokenBlacklistService;
import com.example.RestaurantApplication.module.user.model.enums.Role;
import com.example.RestaurantApplication.module.user.service.UserService;

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
        Role role;
        try {
            username = jwtService.extractUsername(token);
            role = jwtService.extractUserRole(token);
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

        // Check if the access token is not blacklisted
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
        

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.isTokenValid(token, username)) {
                var authorities = List.of(new SimpleGrantedAuthority(role.name()));
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    );
                auth.setDetails(Map.of("userName", username, "role", role.name(), "details", new WebAuthenticationDetailsSource().buildDetails(request)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
