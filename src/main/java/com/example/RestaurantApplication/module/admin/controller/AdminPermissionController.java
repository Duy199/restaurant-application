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

import com.example.RestaurantApplication.module.admin.service.AdminPermissionService;
import com.example.RestaurantApplication.module.permission.dto.AssignApisToPermissionRequest;
import com.example.RestaurantApplication.module.permission.dto.CreatePermissionRequest;
import com.example.RestaurantApplication.module.permission.dto.PatchPermissionRequest;
import com.example.RestaurantApplication.module.permission.dto.PermissionApiDetail;
import com.example.RestaurantApplication.module.permission.dto.PermissionDetail;
import com.example.RestaurantApplication.module.permission.dto.UpdatePermissionRequest;
import com.example.RestaurantApplication.module.permission.model.Permission;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/admin/permission")
public class AdminPermissionController {

    @Autowired
    private AdminPermissionService adminPermissionService;

    @GetMapping("")
    public ApiResponse<List<PermissionDetail>> getAllPermissions() {
        List<Permission> permissions = adminPermissionService.getAllPermissions();
        return ApiResponse.success("Permissions fetched successfully", "PERMISSIONS_FETCHED",
            permissions.stream()
                .map(p -> new PermissionDetail(p.getId(), p.getCode(), p.getName()))
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionDetail> getPermissionById(@PathVariable Long id) {
        Permission permission = adminPermissionService.getPermissionWithApis(id);
        PermissionDetail detail = new PermissionDetail(permission.getId(), permission.getCode(), permission.getName());
        detail.setApis(
            permission.getPermissionApis().stream()
                .map(api -> new PermissionDetail.PermissionApiSummary(
                    api.getId(), api.getCode(), api.getEndpoint(), api.getMethod()))
                .collect(Collectors.toList())
        );
        return ApiResponse.success("Permission fetched successfully", "PERMISSION_FETCHED", detail);
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<PermissionDetail>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        Permission permission = adminPermissionService.createPermission(request.getCode(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Permission created successfully", "PERMISSION_CREATED",
                new PermissionDetail(permission.getId(), permission.getCode(), permission.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionDetail>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        Permission permission = adminPermissionService.updatePermission(id, request.getCode(), request.getName());
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", "PERMISSION_UPDATED",
            new PermissionDetail(permission.getId(), permission.getCode(), permission.getName())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionDetail>> patchPermission(
            @PathVariable Long id,
            @Valid @RequestBody PatchPermissionRequest request) {
        Permission permission = adminPermissionService.patchPermission(id, request.getCode(), request.getName());
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", "PERMISSION_UPDATED",
            new PermissionDetail(permission.getId(), permission.getCode(), permission.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePermission(@PathVariable Long id) {
        adminPermissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully", "PERMISSION_DELETED", null));
    }

    @GetMapping("/{permissionId}/apis")
    public ApiResponse<List<PermissionApiDetail>> getPermissionApis(@PathVariable Long permissionId) {
        List<PermissionApiDetail> apis = adminPermissionService.getPermissionApis(permissionId);
        return ApiResponse.success("Permission APIs fetched successfully", "PERMISSION_APIS_FETCHED", apis);
    }

    @PostMapping("/{permissionId}/apis")
    public ResponseEntity<ApiResponse<String>> assignApisToPermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody AssignApisToPermissionRequest request) {
        adminPermissionService.assignApisToPermission(permissionId, request.getApiIds());
        return ResponseEntity.ok(ApiResponse.success("APIs assigned successfully", "APIS_ASSIGNED", null));
    }

    @DeleteMapping("/{permissionId}/apis/{apiId}")
    public ResponseEntity<ApiResponse<String>> removeApiFromPermission(
            @PathVariable Long permissionId,
            @PathVariable Long apiId) {
        adminPermissionService.removeApiFromPermission(permissionId, apiId);
        return ResponseEntity.ok(ApiResponse.success("API removed successfully", "API_REMOVED", null));
    }
}
