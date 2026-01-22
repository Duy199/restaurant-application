package com.example.RestaurantService.module.user.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantService.module.user.dto.LoginRequest;
import com.example.RestaurantService.module.user.dto.LoginResponse;
import com.example.RestaurantService.module.user.dto.RefreshTokenRequest;
import com.example.RestaurantService.module.user.dto.RefreshTokenResponse;
import com.example.RestaurantService.module.user.dto.RegisterRequest;
import com.example.RestaurantService.module.user.service.AuthService;
import com.example.RestaurantService.module.user.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;



@RestController
@RequestMapping("api/v1/auth")
public class AuthController {


    @Autowired
    AuthService authService;


    @PostMapping("register")
    public ResponseEntity <ApiResponse<String>> register(@Valid @RequestBody RegisterRequest user) {
        
        // Call the AuthService to register the user
        authService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
        
        // Return a success response
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", "200",  null));
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticateUser(request.getUserName(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Login successful", "200", response));   
    }

    @PostMapping("logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody RefreshTokenRequest request) {
        // Revoke access token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7).trim();
            authService.revokeUserTokens(accessToken, "Access");
        }
        // Revoke refresh token
        authService.revokeUserTokens(request.getRefreshToken(), "Refresh");
        return ResponseEntity.ok(ApiResponse.success("User logged out successfully", "200", null));
    }
    
    @PostMapping("refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        // Check if refresh token is blacklisted
        authService.checkRefreshTokenBlacklisted(request.getRefreshToken());

        // Give new tokens
        RefreshTokenResponse response = authService.getRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Refreshed token: " + request.getRefreshToken(), "200", response));
    }

}
