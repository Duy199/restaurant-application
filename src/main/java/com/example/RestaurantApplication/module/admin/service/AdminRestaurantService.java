package com.example.RestaurantApplication.module.admin.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.example.RestaurantApplication.config.tracing.LogHelper;
import com.example.RestaurantApplication.module.restaurant.model.Restaurant;
import com.example.RestaurantApplication.module.restaurant.repository.RestaurantRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class AdminRestaurantService {

    private static final Logger log = LoggerFactory.getLogger(AdminRestaurantService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    /**
     * Get all restaurants globally.
     * Admin has access to all restaurants.
     */
    public List<Restaurant> getAllRestaurantsGlobal() {
        return restaurantRepository.findAll();
    }

    /**
     * Get restaurant by ID globally.
     * Admin can access any restaurant.
     */
    public Restaurant getRestaurantByIdGlobal(Long id) {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new BusinessException("RESTAURANT_NOT_FOUND",
                "Restaurant not found with id " + id,
                HttpStatus.NOT_FOUND));
    }

    /**
     * Create a new restaurant.
     * Admin can create restaurants.
     */
    public void createRestaurant(String name, String address) {
        if (restaurantRepository.existsByName(name)) {
            log.warn("[{}] RESTAURANT_NAME_ALREADY_EXISTS: name={}", LogHelper.loc(), name);
            throw new BusinessException("RESTAURANT_NAME_ALREADY_EXISTS",
                "Restaurant name already exists: " + name,
                HttpStatus.CONFLICT);
        }

        String code = NanoIdUtils.randomNanoId();
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setCode(code);
        restaurantRepository.save(restaurant);
        log.info("[{}] Admin created restaurant: name={}", LogHelper.loc(), name);
    }

    /**
     * Update restaurant globally (full update).
     * Admin can update any restaurant.
     */
    public void updateRestaurantGlobal(Long id, String name, String address) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new BusinessException("RESTAURANT_NOT_FOUND",
                "Restaurant not found with id " + id,
                HttpStatus.NOT_FOUND));

        // Check name uniqueness if changed
        if (!restaurant.getName().equals(name) && restaurantRepository.existsByName(name)) {
            log.warn("[{}] RESTAURANT_NAME_ALREADY_EXISTS: name={}", LogHelper.loc(), name);
            throw new BusinessException("RESTAURANT_NAME_ALREADY_EXISTS",
                "Restaurant name already exists: " + name,
                HttpStatus.CONFLICT);
        }

        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurantRepository.save(restaurant);
        log.info("[{}] Admin updated restaurant: id={}", LogHelper.loc(), id);
    }

    /**
     * Partially update restaurant globally.
     * Admin can update any restaurant.
     */
    public void patchRestaurantGlobal(Long id, String name, String address) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new BusinessException("RESTAURANT_NOT_FOUND",
                "Restaurant not found with id " + id,
                HttpStatus.NOT_FOUND));

        // Partial update - only update provided fields
        if (name != null && !name.isBlank()) {
            if (!restaurant.getName().equals(name) && restaurantRepository.existsByName(name)) {
                log.warn("[{}] RESTAURANT_NAME_ALREADY_EXISTS: name={}", LogHelper.loc(), name);
                throw new BusinessException("RESTAURANT_NAME_ALREADY_EXISTS",
                    "Restaurant name already exists: " + name,
                    HttpStatus.CONFLICT);
            }
            restaurant.setName(name);
        }

        if (address != null && !address.isBlank()) {
            restaurant.setAddress(address);
        }

        restaurantRepository.save(restaurant);
    }

    /**
     * Delete restaurant globally.
     * Admin can delete any restaurant.
     */
    public void deleteRestaurantGlobal(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new BusinessException("RESTAURANT_NOT_FOUND",
                "Restaurant not found with id " + id,
                HttpStatus.NOT_FOUND));

        restaurantRepository.delete(restaurant);
        log.info("[{}] Admin deleted restaurant: id={}", LogHelper.loc(), id);
    }
}