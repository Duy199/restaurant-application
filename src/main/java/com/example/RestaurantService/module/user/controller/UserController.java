package com.example.RestaurantService.module.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantService.module.user.dto.UserDetail;
import com.example.RestaurantService.module.user.model.User;
import com.example.RestaurantService.module.user.service.RestaurantService;
import com.example.RestaurantService.module.user.utils.ResponseWrapper.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("api/v1/user")
public class UserController {

    @Autowired
    private RestaurantService RestaurantService;

    @GetMapping("/{id}")
    public ApiResponse<UserDetail> getUserProfile(@PathVariable Long id) {
        User user = RestaurantService.loadUserById(id);
        return ApiResponse.success("User fetched successfully", "200",
            new UserDetail(user.getId(), user.getUserName(), user.getEmail()));
    }
}
