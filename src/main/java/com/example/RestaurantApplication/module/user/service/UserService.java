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

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<?, ?> details = (Map<?, ?>) authentication.getDetails();
        return (Long) details.get("user_id");
    }

    public void updateUser(Long targetUserId, String username, String email, String password) {
        Long currentUserId = getCurrentUserId();

        // Staff/Manager can only update themselves
        if (!currentUserId.equals(targetUserId)) {
            throw new BusinessException("ACCESS_DENIED",
                "You can only update your own profile",
                HttpStatus.FORBIDDEN);
        }

        // Load user with filter applied (ensures same restaurant)
        User user = userRepository.findByIdFiltered(targetUserId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + targetUserId,
                HttpStatus.NOT_FOUND));

        // Check username uniqueness if changed
        if (!user.getUserName().equals(username) && userRepository.existsByUserName(username)) {
            throw new BusinessException("USERNAME_EXISTS",
                "Username already exists: " + username,
                HttpStatus.CONFLICT);
        }

        // Check email uniqueness if changed
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_EXISTS",
                "Email already exists: " + email,
                HttpStatus.CONFLICT);
        }

        // Update fields
        user.setUserName(username);
        user.setEmail(email);

        // Update password only if provided
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }

    public void patchUser(Long targetUserId, String username, String email, String password) {
        Long currentUserId = getCurrentUserId();

        // Staff/Manager can only update themselves
        if (!currentUserId.equals(targetUserId)) {
            throw new BusinessException("ACCESS_DENIED",
                "You can only update your own profile",
                HttpStatus.FORBIDDEN);
        }

        // Load user with filter applied
        User user = userRepository.findByIdFiltered(targetUserId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + targetUserId,
                HttpStatus.NOT_FOUND));

        // Partial update - only update provided fields
        if (username != null && !username.isBlank()) {
            if (!user.getUserName().equals(username) && userRepository.existsByUserName(username)) {
                throw new BusinessException("USERNAME_EXISTS",
                    "Username already exists: " + username,
                    HttpStatus.CONFLICT);
            }
            user.setUserName(username);
        }

        if (email != null && !email.isBlank()) {
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new BusinessException("EMAIL_EXISTS",
                    "Email already exists: " + email,
                    HttpStatus.CONFLICT);
            }
            user.setEmail(email);
        }

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }

    public void deleteUser(Long targetUserId) {

        // Load user with filter applied (admin can access all restaurants)
        User user = userRepository.findByIdFiltered(targetUserId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + targetUserId,
                HttpStatus.NOT_FOUND));

        userRepository.delete(user);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

}
