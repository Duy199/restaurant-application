package com.example.RestaurantApplication.config.jwt;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;


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
    

    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + props.getAccessTokenExp()))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }


    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + props.getRefreshTokenExp()))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public long getRefreshTokenExpiration() {
        return props.getRefreshTokenExp();
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

    public boolean isTokenValid(String token, String userName) {
        return extractUsername(token).equals(userName)
            && !extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
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
