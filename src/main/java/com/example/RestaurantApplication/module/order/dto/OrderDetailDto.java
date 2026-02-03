package com.example.RestaurantApplication.module.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailDto {
    private Long id;
    private String code;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private Long userId;
    private Long restaurantId;
    private List<OrderItemDto> items;

    public OrderDetailDto() {}

    public OrderDetailDto(Long id, String code, BigDecimal totalAmount, LocalDateTime createdAt,
                          Long userId, Long restaurantId, List<OrderItemDto> items) {
        this.id = id;
        this.code = code;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.items = items;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
}