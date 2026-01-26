package com.example.RestaurantApplication.module.user.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.RestaurantApplication.config.jwt.JwtService;
import com.example.RestaurantApplication.config.redis.TokenBlacklistService;
import com.example.RestaurantApplication.module.user.dto.Login.LoginResponse;
import com.example.RestaurantApplication.module.user.dto.Token.RefreshTokenResponse;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.model.enums.Role;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

import io.jsonwebtoken.Claims;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final TokenBlacklistService tokenBlacklistService;
    
    public AuthService(
        JwtService jwtService,
        TokenBlacklistService tokenBlacklistService
    ) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }


    public User registerUser(String userName, String email, String password) {
        // Registration logic here
        User user = new User();
        
        if (userRepository.existsByUserName(userName)) {
            throw new BusinessException("USER_ALREADY_EXISTS", "Username already exists", HttpStatus.CONFLICT);
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.CONFLICT);
        }
        
        user.setUserName(userName);
        user.setEmail(email);
        user.setRole(Role.ROLE_STAFF);
        
        // Encode the password before saving
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        
        // Save the user to the database
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error saving user: " + e.getMessage());
        }
        
        return user;
    }

    public LoginResponse authenticateUser(String userName, String password) {
        // Authentication logic here
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        // No need to clear revocation - timestamp approach handles this automatically
        // New tokens will have iat > revoked_at, so they will pass

        String accessToken = jwtService.generateToken(user.getUserName(), user.getRole(), user.getRestaurantId(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getUserName(), user.getRole(), user.getRestaurantId(), user.getId());

        return new LoginResponse(user.getId(), user.getUserName(), accessToken, refreshToken);
    }

    public RefreshTokenResponse getRefreshToken (String refreshToken) {

        String username;
        Role role;
        Long restaurantId;
        Long userId;
        try {
            Claims claims = jwtService.extractClaims(refreshToken);
            username = claims.getSubject();
            role = Role.valueOf((String) claims.get("role"));
            restaurantId = claims.get("restaurantId", Long.class);
            userId = claims.get("userId", Long.class);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new BusinessException("REFRESH_TOKEN_INVALID", "Refresh token signature is invalid", HttpStatus.UNAUTHORIZED);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BusinessException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired", HttpStatus.UNAUTHORIZED);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new BusinessException("REFRESH_TOKEN_INVALID", "Refresh token is invalid", HttpStatus.UNAUTHORIZED);
        }

        String newAccessToken = jwtService.generateToken(username, role, restaurantId, userId);
        String newRefreshToken = jwtService.generateRefreshToken(username, role, restaurantId, userId);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    public void revokeUserTokens (String token, String tokenType) {
        try {
            Claims claims = jwtService.extractClaims(token);
            String jti = claims.getId();
            long expirationTime = claims.getExpiration().getTime();

            // Add the token's JTI to the blacklist
            tokenBlacklistService.addToBlacklist(jti, expirationTime, tokenType);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new BusinessException("TOKEN_INVALID", tokenType + " token is invalid", HttpStatus.UNAUTHORIZED);
        }
    }

    public void checkRefreshTokenBlacklisted(String refreshToken) {
        Claims claims = jwtService.extractClaims(refreshToken);
        String jti = claims.getId();

        if (tokenBlacklistService.isTokenBlacklisted(jti)) {
            throw new BusinessException("REFRESH_TOKEN_REVOKED", "Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Revoke all tokens for a user (force logout from all devices).
     * Used when password is changed or admin wants to force logout.
     *
     * @param userId The user ID to revoke tokens for
     */
    public void revokeAllUserTokens(Long userId) {
        // TTL = refresh token expiration time (7 days)
        long ttl = jwtService.getRefreshTokenExpiration();
        tokenBlacklistService.revokeAllUserTokens(userId, ttl);
    }
}
