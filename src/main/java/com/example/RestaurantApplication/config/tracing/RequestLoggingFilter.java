package com.example.RestaurantApplication.config.tracing;

import java.io.IOException;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getDetails() instanceof Map<?, ?> details) {
            Object userId = details.get("user_id");
            Object userName = details.get("userName");
            Object role = details.get("role");

            if (userId != null) MDC.put("userId", userId.toString());
            if (userName != null) MDC.put("userName", userName.toString());
            if (role != null) MDC.put("role", role.toString());
        }

        filterChain.doFilter(request, response);
    }
}
