package com.example.RestaurantApplication.module.user.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.RestaurantApplication.module.restaurant.repository.RestaurantRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.model.enums.Role;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public User loadUserByUsername(String username) {
        return userRepository.findByUserName(username)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found with username " + username, HttpStatus.NOT_FOUND));
    }

    public List<User> loadAllUsers() {
        return userRepository.findAll();
    }

    public User loadUserById(Long id) {
        // Dùng findByIdFiltered() thay vì findById() để trigger Hibernate filter
        return userRepository.findByIdFiltered(id)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public void addNewUser (String userName, String email, String password, String role) {
        // Lấy restaurant_id từ JWT (đã được parse trong JwtAuthenticationFilter)
        Long restaurantId = getAuthenticatedRestaurantId();

        // Validate username và email uniqueness
        if(userRepository.existsByUserName(userName)) {
            throw new BusinessException("USERNAME_EXISTS", "Username already exists: " + userName, HttpStatus.CONFLICT);
        }
        if(userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_EXISTS", "Email already exists: " + email, HttpStatus.CONFLICT);
        }

        // Validate restaurant exists
        if (restaurantRepository.findById(restaurantId).isEmpty()) {
            throw new BusinessException("RESTAURANT_NOT_FOUND", "Restaurant not found with id " + restaurantId, HttpStatus.NOT_FOUND);
        }

        // Create and save user
        User user = new User();
        user.setRestaurantId(restaurantId);
        user.setUserName(userName);
        user.setEmail(email);
        user.setRole(Role.valueOf(role));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private Long getAuthenticatedRestaurantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<?, ?> details = (Map<?, ?>) authentication.getDetails();
        return (Long) details.get("restaurant_id");
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

}
