package com.example.RestaurantApplication.module.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.user.dto.User.CreateUserRequest;
import com.example.RestaurantApplication.module.user.dto.User.PatchUserRequest;
import com.example.RestaurantApplication.module.user.dto.User.UpdateUserRequest;
import com.example.RestaurantApplication.module.user.dto.User.UserDetail;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.service.UserService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ApiResponse<List<UserDetail>> getAllUser() {
        List<User> users = userService.loadAllUsers();
        return ApiResponse.success("Users fetched successfully", "USERS_FETCHED",
            users.stream()
                .map(user -> new UserDetail(user.getId(), user.getUserName(), user.getEmail()))
                .toList()
        );
    }
    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> postMethodName(@Valid @RequestBody CreateUserRequest entity) {
        userService.addNewUser(entity.getUsername(), entity.getEmail(), entity.getPassword(), entity.getRole());
        return ResponseEntity.ok(ApiResponse.success("User created successfully", "USER_CREATED", null));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetail> getUserProfile(@PathVariable Long id) {
        User user = userService.loadUserById(id);
        return ApiResponse.success("User fetched successfully", "USER_FETCHED",
            new UserDetail(user.getId(), user.getUserName(), user.getEmail()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", "USER_UPDATED", null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> patchUser(
            @PathVariable Long id,
            @Valid @RequestBody PatchUserRequest request) {
        userService.patchUser(id, request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", "USER_UPDATED", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "USER_DELETED", null));
    }
}
