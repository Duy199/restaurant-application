package com.example.RestaurantApplication.module.restaurant.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.example.RestaurantApplication.module.restaurant.model.Restaurant;
import com.example.RestaurantApplication.module.restaurant.repository.RestaurantRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.RestaurantApplication.config.tracing.LogHelper;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    public void registerNewRestaurant(String name, String address) { 
        // Registration logic here
        if (restaurantRepository.existsByName(name)) {
            log.warn("[{}] RESTAURANT_NAME_ALREADY_EXISTS: {}", LogHelper.loc(), name);
            throw new BusinessException("RESTAURANT_NAME_ALREADY_EXISTS", "Restaurant name already exists", HttpStatus.CONFLICT);
        }
        String code = NanoIdUtils.randomNanoId();
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setCode(code);
        restaurantRepository.save(restaurant);
        log.info("[{}] Restaurant created: name={}", LogHelper.loc(), name);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Get restaurant by ID.
     * Staff/Manager can only view their own restaurant.
     */
    public Restaurant getRestaurantById(Long id) {
        Long authenticatedRestaurantId = getAuthenticatedRestaurantId();

        // Validate that the requested restaurant matches the authenticated user's restaurant
        if (!id.equals(authenticatedRestaurantId)) {
            log.warn("[{}] ACCESS_DENIED: requested={} but authenticated={}", LogHelper.loc(), id, authenticatedRestaurantId);
            throw new BusinessException("ACCESS_DENIED",
                "You can only view your own restaurant",
                HttpStatus.FORBIDDEN);
        }

        return restaurantRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("[{}] RESTAURANT_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("RESTAURANT_NOT_FOUND",
                    "Restaurant not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });
    }

    /**
     * Update restaurant (full update).
     * Only MANAGER can update their restaurant.
     */
    public void updateRestaurant(Long id, String name, String address) {
        Long authenticatedRestaurantId = getAuthenticatedRestaurantId();

        // Validate that the target restaurant matches the authenticated user's restaurant
        if (!id.equals(authenticatedRestaurantId)) {
            log.warn("[{}] ACCESS_DENIED: requested={} but authenticated={}", LogHelper.loc(), id, authenticatedRestaurantId);
            throw new BusinessException("ACCESS_DENIED",
                "You can only update your own restaurant",
                HttpStatus.FORBIDDEN);
        }

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("[{}] RESTAURANT_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("RESTAURANT_NOT_FOUND",
                    "Restaurant not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });

        // Check name uniqueness if changed
        if (!restaurant.getName().equals(name) && restaurantRepository.existsByName(name)) {
            log.warn("[{}] RESTAURANT_NAME_ALREADY_EXISTS: {}", LogHelper.loc(), name);
            throw new BusinessException("RESTAURANT_NAME_ALREADY_EXISTS",
                "Restaurant name already exists: " + name,
                HttpStatus.CONFLICT);
        }

        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurantRepository.save(restaurant);
        log.info("[{}] Restaurant updated: id={}", LogHelper.loc(), id);
    }

    /**
     * Partially update restaurant.
     * Only MANAGER can update their restaurant.
     */
    public void patchRestaurant(Long id, String name, String address) {
        Long authenticatedRestaurantId = getAuthenticatedRestaurantId();

        // Validate that the target restaurant matches the authenticated user's restaurant
        if (!id.equals(authenticatedRestaurantId)) {
            log.warn("[{}] ACCESS_DENIED: requested={} but authenticated={}", LogHelper.loc(), id, authenticatedRestaurantId);
            throw new BusinessException("ACCESS_DENIED",
                "You can only update your own restaurant",
                HttpStatus.FORBIDDEN);
        }

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("[{}] RESTAURANT_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("RESTAURANT_NOT_FOUND",
                    "Restaurant not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });

        // Partial update - only update provided fields
        if (name != null && !name.isBlank()) {
            if (!restaurant.getName().equals(name) && restaurantRepository.existsByName(name)) {
                log.warn("[{}] RESTAURANT_NAME_ALREADY_EXISTS: {}", LogHelper.loc(), name);
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

    private Long getAuthenticatedRestaurantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<?, ?> details = (Map<?, ?>) authentication.getDetails();
        return (Long) details.get("restaurant_id");
    }
}
