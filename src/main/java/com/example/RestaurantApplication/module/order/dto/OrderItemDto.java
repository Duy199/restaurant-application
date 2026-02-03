package com.example.RestaurantApplication.module.order.dto;

public class OrderItemDto {
    private Long id;
    private Long ingredientId;
    private String ingredientCode;
    private String ingredientName;
    private Integer price;
    private Integer quantity;
    private Integer subtotal;

    public OrderItemDto() {}

    public OrderItemDto(Long id, Long ingredientId, String ingredientCode, String ingredientName, Integer price, Integer quantity) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.ingredientCode = ingredientCode;
        this.ingredientName = ingredientName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientCode() {
        return ingredientCode;
    }

    public void setIngredientCode(String ingredientCode) {
        this.ingredientCode = ingredientCode;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Integer subtotal) {
        this.subtotal = subtotal;
    }
}