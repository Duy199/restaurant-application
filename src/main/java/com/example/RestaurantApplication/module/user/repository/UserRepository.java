package com.example.RestaurantApplication.module.user.repository;

import com.example.RestaurantApplication.module.user.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);
    Boolean existsByUserName(String userName);
    Boolean existsByEmail(String email);
}
