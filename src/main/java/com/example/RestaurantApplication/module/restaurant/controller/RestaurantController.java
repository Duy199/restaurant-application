package com.example.RestaurantApplication.module.restaurant.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.restaurant.dto.RestaurantCreate;
import com.example.RestaurantApplication.module.restaurant.service.RestaurantService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/v1/restaurant")
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantService;
    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> addNewRestaurant(@Valid @RequestBody RestaurantCreate entity) {
        restaurantService.registerNewRestaurant(entity.getName(), entity.getAddress());
        return ResponseEntity.ok(ApiResponse.success("Restaurant created successfully", "200", null));
    }
    
}
