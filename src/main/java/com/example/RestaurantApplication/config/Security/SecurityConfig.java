package com.example.RestaurantApplication.config.Security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
  SecurityFilterChain filterChain(HttpSecurity http,
          JwtAuthenticationEntryPoint entryPoint,
          JwtAccessDeniedHandler deniedHandler,
          JwtAuthenticationFilter jwtFilter
  ) throws Exception {

      return http
          .csrf(csrf -> csrf.disable())
          .exceptionHandling(ex -> ex
              .authenticationEntryPoint(entryPoint)
              .accessDeniedHandler(deniedHandler)
          )
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/v1/auth/logout").authenticated()
              .requestMatchers("/api/v1/auth/**").permitAll()
              .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
              .requestMatchers(HttpMethod.DELETE,"/api/v1/restaurant/**").hasAuthority("ROLE_ADMIN")
              .requestMatchers(HttpMethod.POST,"/api/v1/restaurant/**").hasAnyAuthority("ROLE_MANAGER","ROLE_ADMIN")
              .requestMatchers(HttpMethod.PUT,"/api/v1/restaurant/**").hasAnyAuthority("ROLE_MANAGER","ROLE_ADMIN")
              .requestMatchers(HttpMethod.PATCH,"/api/v1/restaurant/**").hasAnyAuthority("ROLE_MANAGER","ROLE_ADMIN")
              .requestMatchers(HttpMethod.POST, "/api/v1/orders/**").hasAnyAuthority("ROLE_MANAGER","ROLE_ADMIN")
              .requestMatchers(HttpMethod.GET,"/api/v1/orders/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_STAFF","ROLE_ADMIN")
              .anyRequest().authenticated()
          )
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
          .build();
  }

}


