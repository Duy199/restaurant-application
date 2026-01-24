package com.example.RestaurantApplication.config.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String jti, long expirationTime, String tokenType) {
        long ttl = expirationTime - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(jti, tokenType + " revoked", ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlacklisted(String jti) {
        return redisTemplate.hasKey(jti);
    }
}