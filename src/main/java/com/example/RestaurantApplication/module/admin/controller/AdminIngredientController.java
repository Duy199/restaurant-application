package com.example.RestaurantApplication.module.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.ingredient.dto.CreateIngredientRequest;
import com.example.RestaurantApplication.module.ingredient.dto.IngredientDetail;
import com.example.RestaurantApplication.module.ingredient.dto.PatchIngredientRequest;
import com.example.RestaurantApplication.module.ingredient.dto.UpdateIngredientRequest;
import com.example.RestaurantApplication.module.ingredient.model.Ingredient;
import com.example.RestaurantApplication.module.ingredient.service.IngredientService;
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
@RequestMapping("api/v1/admin/ingredient")
public class AdminIngredientController {

    @Autowired
    private IngredientService ingredientService;

    @GetMapping("")
    public ApiResponse<List<IngredientDetail>> getAllIngredients() {
        List<Ingredient> ingredients = ingredientService.getAllIngredients();
        return ApiResponse.success("Ingredients fetched successfully", "INGREDIENTS_FETCHED",
            ingredients.stream()
                .map(i -> new IngredientDetail(i.getId(), i.getCode(), i.getName(), i.getPrice(), i.getStock()))
                .toList()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredientDetail> getIngredientById(@PathVariable Long id) {
        Ingredient ingredient = ingredientService.getIngredientById(id);
        return ApiResponse.success("Ingredient fetched successfully", "INGREDIENT_FETCHED",
            new IngredientDetail(ingredient.getId(), ingredient.getCode(), ingredient.getName(),
                ingredient.getPrice(), ingredient.getStock()));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<IngredientDetail>> createIngredient(
            @Valid @RequestBody CreateIngredientRequest request) {
        Ingredient ingredient = ingredientService.createIngredient(
            request.getName(),
            request.getPrice(),
            request.getStock()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Ingredient created successfully", "INGREDIENT_CREATED",
                new IngredientDetail(ingredient.getId(), ingredient.getCode(), ingredient.getName(),
                    ingredient.getPrice(), ingredient.getStock())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IngredientDetail>> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngredientRequest request) {
        Ingredient ingredient = ingredientService.updateIngredient(
            id,
            request.getName(),
            request.getPrice(),
            request.getStock()
        );
        return ResponseEntity.ok(ApiResponse.success("Ingredient updated successfully", "INGREDIENT_UPDATED",
            new IngredientDetail(ingredient.getId(), ingredient.getCode(), ingredient.getName(),
                ingredient.getPrice(), ingredient.getStock())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<IngredientDetail>> patchIngredient(
            @PathVariable Long id,
            @Valid @RequestBody PatchIngredientRequest request) {
        Ingredient ingredient = ingredientService.patchIngredient(
            id,
            request.getName(),
            request.getPrice(),
            request.getStock()
        );
        return ResponseEntity.ok(ApiResponse.success("Ingredient updated successfully", "INGREDIENT_UPDATED",
            new IngredientDetail(ingredient.getId(), ingredient.getCode(), ingredient.getName(),
                ingredient.getPrice(), ingredient.getStock())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteIngredient(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.ok(ApiResponse.success("Ingredient deleted successfully", "INGREDIENT_DELETED", null));
    }
}