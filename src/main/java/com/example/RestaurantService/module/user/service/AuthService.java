package com.example.RestaurantService.module.user.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.RestaurantService.config.jwt.JwtService;
import com.example.RestaurantService.config.redis.TokenBlacklistService;
import com.example.RestaurantService.module.user.dto.LoginResponse;
import com.example.RestaurantService.module.user.dto.RefreshTokenResponse;
import com.example.RestaurantService.module.user.model.User;
import com.example.RestaurantService.module.user.repository.UserRepository;
import com.example.RestaurantService.module.user.utils.Exceptions.BusinessException;


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

        String accessToken = jwtService.generateToken(user.getUserName());
        String refreshToken = jwtService.generateRefreshToken(user.getUserName());

        return new LoginResponse(user.getId(), user.getUserName(), accessToken, refreshToken);
    }

    public RefreshTokenResponse getRefreshToken (String refreshToken) {

        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new BusinessException("REFRESH_TOKEN_INVALID", "Refresh token signature is invalid", HttpStatus.UNAUTHORIZED);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BusinessException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired", HttpStatus.UNAUTHORIZED);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new BusinessException("REFRESH_TOKEN_INVALID", "Refresh token is invalid", HttpStatus.UNAUTHORIZED);
        }

        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    public void revokeUserTokens (String token, String tokenType) {
        String jti;
        try {
            jti = jwtService.extractJtiString(token);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new BusinessException("TOKEN_INVALID", tokenType + " token is invalid", HttpStatus.UNAUTHORIZED);
        }

        long expirationTime = jwtService.extractExpiration(token).getTime();
        // Add the token's JTI to the blacklist
        tokenBlacklistService.addToBlacklist(jti, expirationTime, tokenType);
    }

    public void checkRefreshTokenBlacklisted(String refreshToken) {
        String jti = jwtService.extractJtiString(refreshToken);
        if (tokenBlacklistService.isTokenBlacklisted(jti)) {
            throw new BusinessException("REFRESH_TOKEN_REVOKED", "Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
        }

    }
}
    