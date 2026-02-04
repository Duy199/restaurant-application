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

import com.example.RestaurantApplication.module.admin.service.AdminPermissionApiService;
import com.example.RestaurantApplication.module.permission.dto.CreatePermissionApiRequest;
import com.example.RestaurantApplication.module.permission.dto.PatchPermissionApiRequest;
import com.example.RestaurantApplication.module.permission.dto.PermissionApiDetail;
import com.example.RestaurantApplication.module.permission.dto.UpdatePermissionApiRequest;
import com.example.RestaurantApplication.module.permission.model.PermissionApi;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/admin/permission-api")
public class AdminPermissionApiController {

    @Autowired
    private AdminPermissionApiService adminPermissionApiService;

    @GetMapping("")
    public ApiResponse<List<PermissionApiDetail>> getAllPermissionApis() {
        List<PermissionApi> apis = adminPermissionApiService.getAllPermissionApis();
        return ApiResponse.success("Permission APIs fetched successfully", "PERMISSION_APIS_FETCHED",
            apis.stream()
                .map(api -> new PermissionApiDetail(api.getId(), api.getCode(),
                    api.getName(), api.getEndpoint(), api.getMethod()))
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionApiDetail> getPermissionApiById(@PathVariable Long id) {
        PermissionApi api = adminPermissionApiService.getPermissionApiById(id);
        return ApiResponse.success("Permission API fetched successfully", "PERMISSION_API_FETCHED",
            new PermissionApiDetail(api.getId(), api.getCode(), api.getName(),
                api.getEndpoint(), api.getMethod()));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<PermissionApiDetail>> createPermissionApi(
            @Valid @RequestBody CreatePermissionApiRequest request) {
        PermissionApi api = adminPermissionApiService.createPermissionApi(
            request.getCode(), request.getName(), request.getEndpoint(), request.getMethod());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Permission API created successfully", "PERMISSION_API_CREATED",
                new PermissionApiDetail(api.getId(), api.getCode(), api.getName(),
                    api.getEndpoint(), api.getMethod())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionApiDetail>> updatePermissionApi(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionApiRequest request) {
        PermissionApi api = adminPermissionApiService.updatePermissionApi(
            id, request.getCode(), request.getName(), request.getEndpoint(), request.getMethod());
        return ResponseEntity.ok(ApiResponse.success("Permission API updated successfully", "PERMISSION_API_UPDATED",
            new PermissionApiDetail(api.getId(), api.getCode(), api.getName(),
                api.getEndpoint(), api.getMethod())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionApiDetail>> patchPermissionApi(
            @PathVariable Long id,
            @Valid @RequestBody PatchPermissionApiRequest request) {
        PermissionApi api = adminPermissionApiService.patchPermissionApi(
            id, request.getCode(), request.getName(), request.getEndpoint(), request.getMethod());
        return ResponseEntity.ok(ApiResponse.success("Permission API updated successfully", "PERMISSION_API_UPDATED",
            new PermissionApiDetail(api.getId(), api.getCode(), api.getName(),
                api.getEndpoint(), api.getMethod())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePermissionApi(@PathVariable Long id) {
        adminPermissionApiService.deletePermissionApi(id);
        return ResponseEntity.ok(ApiResponse.success("Permission API deleted successfully", "PERMISSION_API_DELETED", null));
    }
}
