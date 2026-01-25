package com.example.RestaurantApplication.module.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.admin.service.AdminRestaurantService;
import com.example.RestaurantApplication.module.restaurant.dto.PatchRestaurantRequest;
import com.example.RestaurantApplication.module.restaurant.dto.RestaurantCreate;
import com.example.RestaurantApplication.module.restaurant.dto.RestaurantDetail;
import com.example.RestaurantApplication.module.restaurant.dto.UpdateRestaurantRequest;
import com.example.RestaurantApplication.module.restaurant.model.Restaurant;
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
@RequestMapping("api/v1/admin/restaurant")
public class AdminRestaurantController {

    @Autowired
    private AdminRestaurantService adminRestaurantService;

    @GetMapping("")
    public ApiResponse<List<RestaurantDetail>> getAllRestaurants() {
        List<Restaurant> restaurants = adminRestaurantService.getAllRestaurantsGlobal();
        return ApiResponse.success("Restaurants fetched successfully", "RESTAURANTS_FETCHED",
            restaurants.stream()
                .map(restaurant -> new RestaurantDetail(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getAddress(),
                    restaurant.getCode()))
                .toList()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<RestaurantDetail> getRestaurantById(@PathVariable Long id) {
        Restaurant restaurant = adminRestaurantService.getRestaurantByIdGlobal(id);
        return ApiResponse.success("Restaurant fetched successfully", "RESTAURANT_FETCHED",
            new RestaurantDetail(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getCode()));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> createRestaurant(@Valid @RequestBody RestaurantCreate request) {
        adminRestaurantService.createRestaurant(request.getName(), request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Restaurant created successfully", "RESTAURANT_CREATED", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantRequest request) {
        adminRestaurantService.updateRestaurantGlobal(id, request.getName(), request.getAddress());
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", "RESTAURANT_UPDATED", null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> patchRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody PatchRestaurantRequest request) {
        adminRestaurantService.patchRestaurantGlobal(id, request.getName(), request.getAddress());
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", "RESTAURANT_UPDATED", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRestaurant(@PathVariable Long id) {
        adminRestaurantService.deleteRestaurantGlobal(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant deleted successfully", "RESTAURANT_DELETED", null));
    }

}