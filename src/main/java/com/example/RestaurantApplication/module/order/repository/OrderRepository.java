package com.example.RestaurantApplication.module.order.repository;

import com.example.RestaurantApplication.module.order.model.Order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCode(String code);

    // Find all active orders (not soft deleted) - Hibernate Filter will apply
    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL ORDER BY o.createdAt DESC")
    List<Order> findAllActive();

    // Find active order by ID - Hibernate Filter will apply
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.deletedAt IS NULL")
    Optional<Order> findByIdActive(@Param("id") Long id);

    // Find orders by restaurant (for admin queries without filter)
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.deletedAt IS NULL ORDER BY o.createdAt DESC")
    List<Order> findByRestaurantIdActive(@Param("restaurantId") Long restaurantId);
}