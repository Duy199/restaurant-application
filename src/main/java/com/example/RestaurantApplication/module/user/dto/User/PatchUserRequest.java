package com.example.RestaurantApplication.module.user.dto.User;

import jakarta.validation.constraints.Email;

public class PatchUserRequest {
    // All fields are optional for PATCH (partial update)
    // Users cannot change their role after creation
    private String username;

    @Email(message = "Email must be valid")
    private String email;

    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}