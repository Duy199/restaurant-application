package com.example.RestaurantApplication.module.user.service;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.example.RestaurantApplication.module.restaurant.repository.RestaurantRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    public User loadUserByUsername(String username) {
        return userRepository.findByUserName(username)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found with username " + username, HttpStatus.NOT_FOUND));
    }

    public List<User> loadAllUsers() {
        return userRepository.findAll();
    }

    public User loadUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public void addNewUser (Long restaurantId, String userName, String email, String password, String role) {
        if(userRepository.existsByUserName(userName)) {
            throw new BusinessException("USERNAME_EXISTS", "Username already exists: " + userName, HttpStatus.CONFLICT);
        }
        if(userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_EXISTS", "Email already exists: " + email, HttpStatus.CONFLICT);
        }
        if (restaurantRepository.findById(restaurantId).isEmpty()) {
            throw new BusinessException("RESTAURANT_NOT_FOUND", "Restaurant not found with id " + restaurantId, HttpStatus.NOT_FOUND);
        }
        
        User user = new User();
        user.setRestaurantId(restaurantId);
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(password);
        // Set role accordingly
        userRepository.save(user);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

}
