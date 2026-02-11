package com.example.RestaurantApplication.config.tracing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import net.logstash.logback.marker.Markers;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestTracingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        try {
            MDC.put("correlation_id", NanoIdUtils.randomNanoId());
            MDC.put("method", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("clientIp", request.getRemoteAddr());

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info(Markers.append("duration_ms", duration).and(Markers.append("status", response.getStatus())),
                "Request completed: {} {}", request.getMethod(), request.getRequestURI());
            MDC.clear();
        }
    }
}
