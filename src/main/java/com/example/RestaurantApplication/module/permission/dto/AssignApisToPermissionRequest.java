package com.example.RestaurantApplication.module.permission.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class AssignApisToPermissionRequest {

    @NotEmpty(message = "API IDs list cannot be empty")
    private List<Long> apiIds;

    public AssignApisToPermissionRequest() {
    }

    public AssignApisToPermissionRequest(List<Long> apiIds) {
        this.apiIds = apiIds;
    }

    public List<Long> getApiIds() {
        return apiIds;
    }

    public void setApiIds(List<Long> apiIds) {
        this.apiIds = apiIds;
    }
}
