package com.example.RestaurantApplication.module.role.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    public UpdateUserRoleRequest() {
    }

    public UpdateUserRoleRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
