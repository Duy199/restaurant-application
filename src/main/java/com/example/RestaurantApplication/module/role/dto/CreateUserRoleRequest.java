package com.example.RestaurantApplication.module.role.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateUserRoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    public CreateUserRoleRequest() {
    }

    public CreateUserRoleRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
