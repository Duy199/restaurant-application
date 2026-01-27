package com.example.RestaurantApplication.module.ingredient.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.ingredient.dto.IngredientDetail;
import com.example.RestaurantApplication.module.ingredient.model.Ingredient;
import com.example.RestaurantApplication.module.ingredient.service.IngredientService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/v1/ingredient")
public class IngredientController {

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
}