package com.example.RestaurantApplication.module.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.model.enums.Role;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Load all users from all restaurants.
     * Since the caller has ROLE_ADMIN, the TenantHibernateFilter won't enable the filter,
     * so findAll() will return all users globally.
     */
    public List<User> loadAllUsersGlobal() {
        return userRepository.findAll();
    }

    /**
     * Load user by ID from any restaurant.
     * Since the caller has ROLE_ADMIN, no filter is enabled, so findById() has global access.
     */
    public User loadUserByIdGlobal(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + id,
                HttpStatus.NOT_FOUND));
    }

    /**
     * Create a new user. Admin must specify the restaurant_id for the user.
     */
    public void createUser(String userName, String email, String password, String role) {
        // Validate username and email uniqueness
        if (userRepository.existsByUserName(userName)) {
            throw new BusinessException("USERNAME_EXISTS",
                "Username already exists: " + userName,
                HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_EXISTS",
                "Email already exists: " + email,
                HttpStatus.CONFLICT);
        }

        // Create user without restaurant_id (or add it as parameter later if needed)
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setRole(Role.valueOf(role));
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
    }

    /**
     * Update user globally (can update any user from any restaurant).
     */
    public void updateUserGlobal(Long userId, String username, String email, String password) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + userId,
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

    /**
     * Partially update user globally.
     */
    public void patchUserGlobal(Long userId, String username, String email, String password) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + userId,
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

    /**
     * Delete user globally (can delete any user from any restaurant).
     */
    public void deleteUserGlobal(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + userId,
                HttpStatus.NOT_FOUND));

        userRepository.delete(user);
    }
}