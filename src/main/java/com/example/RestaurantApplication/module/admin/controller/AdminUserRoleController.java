package com.example.RestaurantApplication.module.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.admin.service.AdminUserRoleService;
import com.example.RestaurantApplication.module.role.dto.AssignPermissionsToRoleRequest;
import com.example.RestaurantApplication.module.role.dto.CreateUserRoleRequest;
import com.example.RestaurantApplication.module.role.dto.PatchUserRoleRequest;
import com.example.RestaurantApplication.module.role.dto.UpdateUserRoleRequest;
import com.example.RestaurantApplication.module.role.dto.UserRoleDetail;
import com.example.RestaurantApplication.module.role.model.UserRole;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/admin/role")
public class AdminUserRoleController {

    @Autowired
    private AdminUserRoleService adminUserRoleService;

    @GetMapping("")
    public ApiResponse<List<UserRoleDetail>> getAllRoles() {
        List<UserRole> roles = adminUserRoleService.getAllRoles();
        return ApiResponse.success("Roles fetched successfully", "ROLES_FETCHED",
            roles.stream()
                .map(r -> new UserRoleDetail(r.getId(), r.getName()))
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserRoleDetail> getRoleById(@PathVariable Long id) {
        UserRole role = adminUserRoleService.getRoleWithPermissions(id);
        UserRoleDetail detail = new UserRoleDetail(role.getId(), role.getName());
        detail.setPermissions(
            role.getPermissions().stream()
                .map(p -> new UserRoleDetail.PermissionSummary(p.getId(), p.getCode(), p.getName()))
                .collect(Collectors.toList())
        );
        return ApiResponse.success("Role fetched successfully", "ROLE_FETCHED", detail);
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<UserRoleDetail>> createRole(
            @Valid @RequestBody CreateUserRoleRequest request) {
        UserRole role = adminUserRoleService.createRole(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Role created successfully", "ROLE_CREATED",
                new UserRoleDetail(role.getId(), role.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRoleDetail>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        UserRole role = adminUserRoleService.updateRole(id, request.getName());
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", "ROLE_UPDATED",
            new UserRoleDetail(role.getId(), role.getName())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRoleDetail>> patchRole(
            @PathVariable Long id,
            @Valid @RequestBody PatchUserRoleRequest request) {
        UserRole role = adminUserRoleService.patchRole(id, request.getName());
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", "ROLE_UPDATED",
            new UserRoleDetail(role.getId(), role.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
        adminUserRoleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", "ROLE_DELETED", null));
    }

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<String>> assignPermissionsToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionsToRoleRequest request) {
        adminUserRoleService.assignPermissionsToRole(roleId, request.getPermissionIds());
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned successfully", "PERMISSIONS_ASSIGNED", null));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<String>> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        adminUserRoleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed successfully", "PERMISSION_REMOVED", null));
    }
}
