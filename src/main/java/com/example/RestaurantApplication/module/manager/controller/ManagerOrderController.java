package com.example.RestaurantApplication.module.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.order.dto.CreateOrderRequest;
import com.example.RestaurantApplication.module.order.dto.OrderSummaryDto;
import com.example.RestaurantApplication.module.order.model.Order;
import com.example.RestaurantApplication.module.order.service.OrderService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/manager/order")
public class ManagerOrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("")
    public ApiResponse<OrderSummaryDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ApiResponse.success("Order created successfully", "ORDER_CREATED", orderService.toSummaryDto(order));
    }
}