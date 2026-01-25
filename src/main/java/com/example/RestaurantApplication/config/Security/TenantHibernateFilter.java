package com.example.RestaurantApplication.config.Security;
import java.io.IOException;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Lazy
public class TenantHibernateFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        Session session = entityManager.unwrap(Session.class);

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // chưa login thì khỏi enable filter
            if (auth == null || !auth.isAuthenticated()) {
                chain.doFilter(request, response);
                return;
            }

            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                Long rid = null;

                // Nếu fen đã nhét rid vào auth.setDetails(Map.of(...)) thì lấy ở đây
                if (auth.getDetails() instanceof Map<?, ?> d) {
                    Object v = d.get("restaurant_id");
                    if (v instanceof Long) rid = (Long) v;
                    // nếu v là Integer/String thì tuỳ mình parse thêm
                    if (v instanceof Integer) rid = ((Integer) v).longValue();
                    if (v instanceof String s) rid = Long.valueOf(s);
                }

                if (rid != null) {
                    session.enableFilter("tenantFilter").setParameter("rid", rid);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("""
                    {"success":false,"code":"FORBIDDEN","message":"Missing restaurant scope"}
                    """);
                    return;
                }
            }

            chain.doFilter(request, response);

        } finally {
            session.disableFilter("tenantFilter");
        }
    }
}