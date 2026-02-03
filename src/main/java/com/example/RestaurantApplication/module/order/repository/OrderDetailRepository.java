package com.example.RestaurantApplication.module.order.repository;

import com.example.RestaurantApplication.module.order.model.OrderDetail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Find all order details by order ID
    @Query("SELECT od FROM OrderDetail od WHERE od.orderId = :orderId AND od.deletedAt IS NULL")
    List<OrderDetail> findByOrderIdActive(@Param("orderId") Long orderId);

    // Find all order details by order ID with ingredient info
    @Query("SELECT od FROM OrderDetail od JOIN FETCH od.ingredient WHERE od.orderId = :orderId AND od.deletedAt IS NULL")
    List<OrderDetail> findByOrderIdWithIngredient(@Param("orderId") Long orderId);
}