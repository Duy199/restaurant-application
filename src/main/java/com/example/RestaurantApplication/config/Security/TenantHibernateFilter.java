package com.example.RestaurantApplication.config.Security;
import java.io.IOException;
import java.util.Map;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.RestaurantApplication.config.tracing.LogHelper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantHibernateFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantHibernateFilter.class);
    private final EntityManagerFactory entityManagerFactory;

    public TenantHibernateFilter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If not login then skip this filter
        if (auth == null || !auth.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }
        

        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Long rid = null;

            if (auth.getDetails() instanceof Map<?, ?> d) {
                Object v = d.get("restaurant_id");
                if (v instanceof Long) rid = (Long) v;
                if (v instanceof Integer) rid = ((Integer) v).longValue();
                if (v instanceof String s) rid = Long.valueOf(s);
            }

            if (rid != null) {
                EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(entityManagerFactory);

                if (emHolder != null) {
                    EntityManager em = emHolder.getEntityManager();
                    Session session = em.unwrap(Session.class);
                    session.enableFilter("tenantFilter").setParameter("rid", rid);
                    log.info("[{}] Tenant filter enabled: rid={}", LogHelper.loc(), rid);
                } else {
                    log.warn("[{}] No EntityManager bound, storing rid in request attribute: rid={}", LogHelper.loc(), rid);
                    // Fallback: Store rid in request attribute để Service layer có thể enable filter
                    request.setAttribute("TENANT_RESTAURANT_ID", rid);
                }
            } else {
                log.warn("[{}] MISSING_RESTAURANT_SCOPE: no restaurant_id for user", LogHelper.loc());
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