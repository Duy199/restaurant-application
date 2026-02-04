package com.example.RestaurantApplication.module.role.dto;

public class PatchUserRoleRequest {

    private String name;

    public PatchUserRoleRequest() {
    }

    public PatchUserRoleRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
