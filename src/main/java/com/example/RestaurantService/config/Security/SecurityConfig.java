package com.example.RestaurantService.config.Security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.RestaurantService.config.jwt.JwtAccessDeniedHandler;
import com.example.RestaurantService.config.jwt.JwtAuthenticationEntryPoint;
import com.example.RestaurantService.config.jwt.JwtAuthenticationFilter;


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
              .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
              .requestMatchers(HttpMethod.DELETE,"/api/v1/restaurants/**").hasRole("ADMIN")
              .requestMatchers(HttpMethod.POST,"/api/v1/restaurants/**").hasAnyRole("MANAGER","ADMIN")
              .requestMatchers(HttpMethod.PUT,"/api/v1/restaurants/**").hasAnyRole("MANAGER","ADMIN")
              .requestMatchers(HttpMethod.PATCH,"/api/v1/restaurants/**").hasAnyRole("MANAGER","ADMIN")
              .requestMatchers(HttpMethod.POST, "/api/v1/orders/**").hasAnyRole("MANAGER","ADMIN")
              .requestMatchers(HttpMethod.GET,"/api/v1/orders/**").hasAnyRole("MANAGER", "STAFF","ADMIN")
              .anyRequest().authenticated()
          )
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
          .build();
  }

}


