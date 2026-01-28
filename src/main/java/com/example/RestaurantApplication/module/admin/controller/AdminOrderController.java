package com.example.RestaurantApplication.module.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestaurantApplication.module.order.dto.CreateOrderRequest;
import com.example.RestaurantApplication.module.order.dto.OrderDetailDto;
import com.example.RestaurantApplication.module.order.dto.OrderSummaryDto;
import com.example.RestaurantApplication.module.order.model.Order;
import com.example.RestaurantApplication.module.order.service.OrderService;
import com.example.RestaurantApplication.utils.ResponseWrapper.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/admin/order")
public class AdminOrderController {

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

    @PostMapping("")
    public ApiResponse<OrderSummaryDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ApiResponse.success("Order created successfully", "ORDER_CREATED", orderService.toSummaryDto(order));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success("Order deleted successfully", "ORDER_DELETED", null);
    }
}