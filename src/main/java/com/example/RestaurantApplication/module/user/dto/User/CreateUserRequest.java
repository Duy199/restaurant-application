package com.example.RestaurantApplication.module.user.dto.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class CreateUserRequest {
    @NotNull(message = "Restaurant ID is required")
    @Positive(message = "Restaurant ID must be greater than 0")
    private long restaurantId;
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ROLE_MANAGER|ROLE_STAFF", message = "Role must be either MANAGER or STAFF")
    private String role;
    // Getters and Setters
    public long getRestaurantId() {
        return restaurantId;
    }
    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }
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

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
