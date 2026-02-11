package com.example.RestaurantApplication.module.ingredient.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.example.RestaurantApplication.module.ingredient.model.Ingredient;
import com.example.RestaurantApplication.module.ingredient.repository.IngredientRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.RestaurantApplication.config.tracing.LogHelper;

@Service
public class IngredientService {

    private static final Logger log = LoggerFactory.getLogger(IngredientService.class);

    @Autowired
    private IngredientRepository ingredientRepository;

    /**
     * Get all active ingredients (not soft deleted)
     */
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAllActive();
    }

    /**
     * Get ingredient by ID
     */
    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findByIdActive(id)
            .orElseThrow(() -> {
                log.warn("[{}] INGREDIENT_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("INGREDIENT_NOT_FOUND",
                    "Ingredient not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });
    }

    /**
     * Create new ingredient with auto-generated code using NanoId
     */
    public Ingredient createIngredient(String name, Integer price, Integer stock) {
        Ingredient ingredient = new Ingredient();
        ingredient.setCode(NanoIdUtils.randomNanoId());
        ingredient.setName(name);
        ingredient.setPrice(price);
        ingredient.setStock(stock);

        log.info("[{}] Ingredient created: name={}", LogHelper.loc(), name);
        return ingredientRepository.save(ingredient);
    }

    /**
     * Update ingredient (full update)
     */
    public Ingredient updateIngredient(Long id, String name, Integer price, Integer stock) {
        Ingredient ingredient = ingredientRepository.findByIdActive(id)
            .orElseThrow(() -> {
                log.warn("[{}] INGREDIENT_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("INGREDIENT_NOT_FOUND",
                    "Ingredient not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });

        ingredient.setName(name);
        ingredient.setPrice(price);
        ingredient.setStock(stock);

        return ingredientRepository.save(ingredient);
    }

    /**
     * Partially update ingredient
     */
    public Ingredient patchIngredient(Long id, String name, Integer price, Integer stock) {
        Ingredient ingredient = ingredientRepository.findByIdActive(id)
            .orElseThrow(() -> {
                log.warn("[{}] INGREDIENT_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("INGREDIENT_NOT_FOUND",
                    "Ingredient not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });

        if (name != null && !name.isBlank()) {
            ingredient.setName(name);
        }
        if (price != null) {
            ingredient.setPrice(price);
        }
        if (stock != null) {
            ingredient.setStock(stock);
        }

        return ingredientRepository.save(ingredient);
    }

    /**
     * Soft delete ingredient
     */
    public void deleteIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findByIdActive(id)
            .orElseThrow(() -> {
                log.warn("[{}] INGREDIENT_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("INGREDIENT_NOT_FOUND",
                    "Ingredient not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });

        ingredient.setDeletedAt(LocalDateTime.now());
        ingredientRepository.save(ingredient);
        log.info("[{}] Ingredient deleted: id={}", LogHelper.loc(), id);
    }
}