package com.example.RestaurantApplication.module.permission.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePermissionRequest {

    @NotBlank(message = "Permission code is required")
    private String code;

    @NotBlank(message = "Permission name is required")
    private String name;

    public UpdatePermissionRequest() {
    }

    public UpdatePermissionRequest(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
