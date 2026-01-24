package com.example.RestaurantApplication.module.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.user.dto.User.UserDetail;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.service.UserService;
import com.example.RestaurantApplication.module.user.utils.ResponseWrapper.ApiResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



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
    
    @GetMapping("/{id}")
    public ApiResponse<UserDetail> getUserProfile(@PathVariable Long id) {
        User user = userService.loadUserById(id);
        return ApiResponse.success("User fetched successfully", "200",
            new UserDetail(user.getId(), user.getUserName(), user.getEmail()));
    }
}
