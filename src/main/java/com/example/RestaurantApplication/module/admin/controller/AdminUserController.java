package com.example.RestaurantApplication.module.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.user.dto.User.CreateUserRequest;
import com.example.RestaurantApplication.module.user.dto.User.PatchUserRequest;
import com.example.RestaurantApplication.module.user.dto.User.UpdateUserRequest;
import com.example.RestaurantApplication.module.user.dto.User.UserDetail;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.admin.service.AdminUserService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

import java.util.List;

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

@RestController
@RequestMapping("api/v1/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("")
    public ApiResponse<List<UserDetail>> getAllUsers() {
        List<User> users = adminUserService.loadAllUsersGlobal();
        return ApiResponse.success("Users fetched successfully", "USERS_FETCHED",
            users.stream()
                .map(user -> new UserDetail(user.getId(), user.getUserName(), user.getEmail()))
                .toList()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetail> getUserById(@PathVariable Long id) {
        User user = adminUserService.loadUserByIdGlobal(id);
        return ApiResponse.success("User fetched successfully", "USER_FETCHED",
            new UserDetail(user.getId(), user.getUserName(), user.getEmail()));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> createUser(@Valid @RequestBody CreateUserRequest request) {
        adminUserService.createUser(request.getUsername(), request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User created successfully", "USER_CREATED", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        adminUserService.updateUserGlobal(id, request.getUsername(), request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", "USER_UPDATED", null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> patchUser(
            @PathVariable Long id,
            @Valid @RequestBody PatchUserRequest request) {
        adminUserService.patchUserGlobal(id, request.getUsername(), request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", "USER_UPDATED", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUserGlobal(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "USER_DELETED", null));
    }

}