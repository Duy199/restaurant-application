package com.example.RestaurantApplication.module.restaurant.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.restaurant.dto.PatchRestaurantRequest;
import com.example.RestaurantApplication.module.restaurant.dto.RestaurantCreate;
import com.example.RestaurantApplication.module.restaurant.dto.RestaurantDetail;
import com.example.RestaurantApplication.module.restaurant.dto.UpdateRestaurantRequest;
import com.example.RestaurantApplication.module.restaurant.model.Restaurant;
import com.example.RestaurantApplication.module.restaurant.service.RestaurantService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("api/v1/restaurant")
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantService;
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<RestaurantDetail>>> getRestaurantList() {
        List <Restaurant> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(
            ApiResponse.success(
                "Restaurant list fetched successfully",
                "200",
                restaurants.stream().map(restaurant -> new RestaurantDetail(restaurant.getId(),restaurant.getName(), restaurant.getAddress(), restaurant.getCode())).toList()
            ));
    }
    
    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> addNewRestaurant(@Valid @RequestBody RestaurantCreate entity) {
        restaurantService.registerNewRestaurant(entity.getName(), entity.getAddress());
        return ResponseEntity.ok(ApiResponse.success("Restaurant created successfully", "200", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDetail>> getRestaurantById(@PathVariable Long id) {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Restaurant fetched successfully",
                "RESTAURANT_FETCHED",
                new RestaurantDetail(restaurant.getId(), restaurant.getName(), restaurant.getAddress(), restaurant.getCode())
            ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantRequest request) {
        restaurantService.updateRestaurant(id, request.getName(), request.getAddress());
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", "RESTAURANT_UPDATED", null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> patchRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody PatchRestaurantRequest request) {
        restaurantService.patchRestaurant(id, request.getName(), request.getAddress());
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", "RESTAURANT_UPDATED", null));
    }

}
