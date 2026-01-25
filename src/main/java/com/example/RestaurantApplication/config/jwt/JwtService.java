package com.example.RestaurantApplication.config.jwt;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.RestaurantApplication.module.user.model.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


@Service
public class JwtService {
    
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }
    

    public String generateToken(String username, Role role, Long restaurantId, Long userId) {
        return Jwts.builder()
            .setSubject(username)
            .claim("role", role.name())
            .claim("restaurantId", restaurantId)
            .claim("userId", userId)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + props.getAccessTokenExp()))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }


    public String generateRefreshToken(String username, Role role, Long restaurantId, Long userId) {
        return Jwts.builder()
            .setSubject(username)
            .claim("role", role.name())
            .claim("restaurantId", restaurantId)
            .claim("userId", userId)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + props.getRefreshTokenExp()))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public long getRefreshTokenExpiration() {
        return props.getRefreshTokenExp();
    }

    public Role extractUserRole(String token) {
        String role = (String) extractClaims(token).get("role");
        return Role.valueOf(role);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractJtiString(String token) {
        return extractClaims(token).getId();
    }

    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    public Long extractRestaurantId(String token) {
        Integer value = (Integer) extractClaims(token).get("restaurantId");
        return value != null ? value.longValue() : null;
    }

    public Long extractUserId(String token) {
        Integer value = (Integer) extractClaims(token).get("userId");
        return value != null ? value.longValue() : null;
    }

    public boolean isTokenValid(String token, String userName) {
        Claims claims = extractClaims(token);
        return claims.getSubject().equals(userName)
            && !claims.getExpiration().before(new Date());
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes());
    }
}
