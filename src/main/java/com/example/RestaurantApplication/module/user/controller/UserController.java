package com.example.RestaurantApplication.module.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.user.dto.User.CreateUserRequest;
import com.example.RestaurantApplication.module.user.dto.User.UserDetail;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.service.UserService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ApiResponse<List<UserDetail>> getAllUser() {
        List<User> users = userService.loadAllUsers();
        return ApiResponse.success("Users fetched successfully", "200",
            users.stream()
                .map(user -> new UserDetail(user.getId(), user.getUserName(), user.getEmail()))
                .toList()
        );
    }
    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> postMethodName(@Valid @RequestBody CreateUserRequest entity) {
        //TODO: process POST request
        userService.addNewUser(entity.getUsername(), entity.getEmail(), entity.getPassword(), entity.getRole());
        return ResponseEntity.ok(ApiResponse.success("User created successfully", "200", null));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<UserDetail> getUserProfile(@PathVariable Long id) {
        User user = userService.loadUserById(id);
        return ApiResponse.success("User fetched successfully", "200",
            new UserDetail(user.getId(), user.getUserName(), user.getEmail()));
    }
}
