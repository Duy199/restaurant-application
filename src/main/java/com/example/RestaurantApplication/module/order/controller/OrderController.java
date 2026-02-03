package com.example.RestaurantApplication.module.order.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.order.dto.OrderDetailDto;
import com.example.RestaurantApplication.module.order.dto.OrderSummaryDto;
import com.example.RestaurantApplication.module.order.model.Order;
import com.example.RestaurantApplication.module.order.service.OrderService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/v1/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public ApiResponse<List<OrderSummaryDto>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ApiResponse.success("Orders fetched successfully", "ORDERS_FETCHED",
            orders.stream()
                .map(orderService::toSummaryDto)
                .toList()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailDto> getOrderById(@PathVariable Long id) {
        OrderDetailDto orderDetail = orderService.getOrderDetailDto(id);
        return ApiResponse.success("Order fetched successfully", "ORDER_FETCHED", orderDetail);
    }
}