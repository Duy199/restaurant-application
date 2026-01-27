package com.example.RestaurantApplication.module.ingredient.repository;

import com.example.RestaurantApplication.module.ingredient.model.Ingredient;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByCode(String code);

    Boolean existsByCode(String code);

    // Find all active ingredients (not soft deleted)
    @Query("SELECT i FROM Ingredient i WHERE i.deletedAt IS NULL")
    List<Ingredient> findAllActive();

    // Find active ingredient by ID
    @Query("SELECT i FROM Ingredient i WHERE i.id = :id AND i.deletedAt IS NULL")
    Optional<Ingredient> findByIdActive(@Param("id") Long id);
}