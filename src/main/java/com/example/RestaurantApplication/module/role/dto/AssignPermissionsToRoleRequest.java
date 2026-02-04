package com.example.RestaurantApplication.module.role.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class AssignPermissionsToRoleRequest {

    @NotEmpty(message = "Permission IDs list cannot be empty")
    private List<Long> permissionIds;

    public AssignPermissionsToRoleRequest() {
    }

    public AssignPermissionsToRoleRequest(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
