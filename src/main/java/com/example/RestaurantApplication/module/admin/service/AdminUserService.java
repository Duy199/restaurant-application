package com.example.RestaurantApplication.module.admin.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.RestaurantApplication.config.tracing.LogHelper;
import com.example.RestaurantApplication.module.role.model.UserRole;
import com.example.RestaurantApplication.module.role.repository.UserRoleRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.module.user.service.AuthService;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

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
     * Create a new user. Admin must specify the restaurant_id for MANAGER/STAFF users.
     */
    public void createUser(String userName, String email, String password, String role, Long restaurantId) {
        // Validate username and email uniqueness
        if (userRepository.existsByUserName(userName)) {
            log.warn("[{}] USERNAME_EXISTS: {}", LogHelper.loc(), userName);
            throw new BusinessException("USERNAME_EXISTS",
                "Username already exists: " + userName,
                HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("[{}] EMAIL_EXISTS: {}", LogHelper.loc(), email);
            throw new BusinessException("EMAIL_EXISTS",
                "Email already exists: " + email,
                HttpStatus.CONFLICT);
        }

        // Find role by name
        UserRole userRole = userRoleRepository.findByNameActive(role)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found: " + role,
                HttpStatus.NOT_FOUND));

        // Validate restaurantId based on role
        if ("ROLE_ADMIN".equals(role)) {
            // ADMIN must NOT have restaurantId
            if (restaurantId != null) {
                log.warn("[{}] INVALID_RESTAURANT_ID: ADMIN cannot have restaurantId", LogHelper.loc());
                throw new BusinessException("INVALID_RESTAURANT_ID",
                    "ROLE_ADMIN users cannot be assigned to a restaurant",
                    HttpStatus.BAD_REQUEST);
            }
        } else {
            // MANAGER/STAFF must have restaurantId
            if (restaurantId == null) {
                log.warn("[{}] RESTAURANT_ID_REQUIRED: role={}", LogHelper.loc(), role);
                throw new BusinessException("RESTAURANT_ID_REQUIRED",
                    "Restaurant ID is required for " + role + " users",
                    HttpStatus.BAD_REQUEST);
            }
        }

        // Create user with restaurant_id
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setUserRoleId(userRole.getId());
        user.setRestaurantId(restaurantId);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        log.info("[{}] Admin created user: username={}, role={}", LogHelper.loc(), userName, role);
    }

    /**
     * Update user globally (can update any user from any restaurant).
     */
    public void updateUserGlobal(Long userId, String username, String email, String password, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + userId,
                HttpStatus.NOT_FOUND));

        // Check username uniqueness if changed
        if (!user.getUserName().equals(username) && userRepository.existsByUserName(username)) {
            log.warn("[{}] USERNAME_EXISTS: {}", LogHelper.loc(), username);
            throw new BusinessException("USERNAME_EXISTS",
                "Username already exists: " + username,
                HttpStatus.CONFLICT);
        }

        // Check email uniqueness if changed
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            log.warn("[{}] EMAIL_EXISTS: {}", LogHelper.loc(), email);
            throw new BusinessException("EMAIL_EXISTS",
                "Email already exists: " + email,
                HttpStatus.CONFLICT);
        }

        // Find new role by name
        UserRole newUserRole = userRoleRepository.findByNameActive(role)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found: " + role,
                HttpStatus.NOT_FOUND));

        // Update fields
        user.setUserName(username);
        user.setEmail(email);

        // Update password only if provided
        boolean passwordChanged = false;
        boolean roleChanged = false;

        if (!user.getUserRoleId().equals(newUserRole.getId())) {
            user.setUserRoleId(newUserRole.getId());
            roleChanged = true;
        }

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
            passwordChanged = true;
        }

        userRepository.save(user);
        log.info("[{}] Admin updated user: userId={}", LogHelper.loc(), userId);

        // Revoke all tokens if password changed (force re-login from all devices)
        if (passwordChanged || roleChanged) {
            authService.revokeAllUserTokens(userId);
            log.info("[{}] Tokens revoked for user: userId={}", LogHelper.loc(), userId);
        }
    }

    /**
     * Partially update user globally.
     */
    public void patchUserGlobal(Long userId, String username, String email, String password, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User not found with id " + userId,
                HttpStatus.NOT_FOUND));

        // Partial update - only update provided fields
        if (username != null && !username.isBlank()) {
            if (!user.getUserName().equals(username) && userRepository.existsByUserName(username)) {
                log.warn("[{}] USERNAME_EXISTS: {}", LogHelper.loc(), username);
                throw new BusinessException("USERNAME_EXISTS",
                    "Username already exists: " + username,
                    HttpStatus.CONFLICT);
            }
            user.setUserName(username);
        }

        if (email != null && !email.isBlank()) {
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                log.warn("[{}] EMAIL_EXISTS: {}", LogHelper.loc(), email);
                throw new BusinessException("EMAIL_EXISTS",
                    "Email already exists: " + email,
                    HttpStatus.CONFLICT);
            }
            user.setEmail(email);
        }

        boolean roleChanged = false;
        if (role != null && !role.isBlank()) {
            UserRole newUserRole = userRoleRepository.findByNameActive(role)
                .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                    "Role not found: " + role,
                    HttpStatus.NOT_FOUND));

            if (!user.getUserRoleId().equals(newUserRole.getId())) {
                user.setUserRoleId(newUserRole.getId());
                roleChanged = true;
            }
        }

        boolean passwordChanged = false;
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
            passwordChanged = true;
        }

        userRepository.save(user);
        log.info("[{}] Admin patched user: userId={}", LogHelper.loc(), userId);

        // Revoke all tokens if password changed (force re-login from all devices)
        if (passwordChanged || roleChanged) {
            authService.revokeAllUserTokens(userId);
            log.info("[{}] Tokens revoked for user: userId={}", LogHelper.loc(), userId);
        }
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
        log.info("[{}] Admin deleted user: userId={}", LogHelper.loc(), userId);
    }
}