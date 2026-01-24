package com.example.RestaurantApplication.module.restaurant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.example.RestaurantApplication.module.restaurant.model.Restaurant;
import com.example.RestaurantApplication.module.restaurant.repository.RestaurantRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public void registerNewRestaurant(String name, String address) { 
        // Registration logic here
        if (restaurantRepository.existsByName(name)) {
            throw new BusinessException("RESTAURANT_NAME_ALREADY_EXISTS", "Restaurant name already exists", HttpStatus.CONFLICT);
        }
        String code = NanoIdUtils.randomNanoId();
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setCode(code);
        restaurantRepository.save(restaurant);        
    }
}
