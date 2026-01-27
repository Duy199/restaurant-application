package com.example.RestaurantApplication.module.ingredient.dto;

import jakarta.validation.constraints.Positive;

public class PatchIngredientRequest {
    // Tất cả fields optional cho PATCH

    private String name;

    @Positive(message = "Price must be positive")
    private Integer price;

    @Positive(message = "Stock must be positive")
    private Integer stock;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}