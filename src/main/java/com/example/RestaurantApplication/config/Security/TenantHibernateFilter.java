package com.example.RestaurantApplication.config.Security;
import java.io.IOException;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantHibernateFilter extends OncePerRequestFilter {

    private final EntityManagerFactory entityManagerFactory;

    public TenantHibernateFilter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

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
                System.out.println(">>> TenantFilter - restaurant_id value: " + v + ", type: " + (v != null ? v.getClass() : "null"));
                if (v instanceof Long) rid = (Long) v;
                // nếu v là Integer/String thì tuỳ mình parse thêm
                if (v instanceof Integer) rid = ((Integer) v).longValue();
                if (v instanceof String s) rid = Long.valueOf(s);
                System.out.println(">>> TenantFilter - Final rid: " + rid);
            }

            if (rid != null) {
                // Lấy EntityManager từ transaction context (được bind bởi OpenEntityManagerInView)
                EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(entityManagerFactory);

                if (emHolder != null) {
                    EntityManager em = emHolder.getEntityManager();
                    Session session = em.unwrap(Session.class);
                    session.enableFilter("tenantFilter").setParameter("rid", rid);
                    System.out.println(">>> TenantFilter ENABLED (from holder) with rid: " + rid);
                } else {
                    System.out.println(">>> TenantFilter WARNING: No EntityManager bound to thread, storing rid in thread-local");
                    // Fallback: Store rid in request attribute để Service layer có thể enable filter
                    request.setAttribute("TENANT_RESTAURANT_ID", rid);
                }
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
    }
}