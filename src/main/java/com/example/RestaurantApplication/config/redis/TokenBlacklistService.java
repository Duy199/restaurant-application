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

    /**
     * Revoke all tokens for a specific user by storing revocation timestamp.
     * This forces the user to login again from all devices.
     * Any token issued BEFORE this timestamp will be rejected, even after user logs in again.
     *
     * Security: This prevents old tokens from working even when user logs in from another device.
     *
     * @param userId The user ID to revoke tokens for
     * @param ttl Time to live in milliseconds (should be max token expiration time, e.g., 7 days for refresh token)
     */
    public void revokeAllUserTokens(Long userId, long ttl) {
        String key = "user:" + userId + ":revoked_at";
        long currentTimestamp = System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, String.valueOf(currentTimestamp), ttl, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Get the timestamp when user tokens were revoked.
     * Returns null if user has never been revoked or revocation has expired.
     *
     * @param userId The user ID to check
     * @return Revoked timestamp in milliseconds, or null if not revoked
     */
    public Long getUserRevokedTimestamp(Long userId) {
        String key = "user:" + userId + ":revoked_at";
        String timestamp = redisTemplate.opsForValue().get(key);
        return timestamp != null ? Long.parseLong(timestamp) : null;
    }
}