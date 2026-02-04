package com.example.RestaurantApplication.config.Security;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.RestaurantApplication.config.jwt.JwtAccessDeniedHandler;
import com.example.RestaurantApplication.config.jwt.JwtAuthenticationEntryPoint;
import com.example.RestaurantApplication.config.jwt.JwtAuthenticationFilter;


@Configuration
// @EnableWebSecurity
// @RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public FilterRegistrationBean<OpenEntityManagerInViewFilter> openEntityManagerInViewFilter() {
      FilterRegistrationBean<OpenEntityManagerInViewFilter> registrationBean = new FilterRegistrationBean<>();
      OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
      registrationBean.setFilter(filter);
      registrationBean.setOrder(-100); // Chạy trước các filter khác
      return registrationBean;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http,
          JwtAuthenticationEntryPoint entryPoint,
          JwtAccessDeniedHandler deniedHandler,
          JwtAuthenticationFilter jwtFilter,
          TenantHibernateFilter tenantFilter,
          DynamicAuthorizationFilter dynamicAuthFilter
  ) throws Exception {

      return http
          .csrf(csrf -> csrf.disable())
          .exceptionHandling(ex -> ex
              .authenticationEntryPoint(entryPoint)
              .accessDeniedHandler(deniedHandler)
          )
          .authorizeHttpRequests(auth -> auth
              // Authentication endpoints
              .requestMatchers("/api/v1/auth/logout").authenticated()
              .requestMatchers("/api/v1/auth/**").permitAll()

              // Admin module - Global management (ADMIN only, hardcoded)
              .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")

              // All other endpoints - authenticated users with dynamic permission checking
              .anyRequest().authenticated()
          )
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
          .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class)
          .addFilterAfter(dynamicAuthFilter, TenantHibernateFilter.class)
          .build();
  }

}


