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
import com.example.RestaurantApplication.module.role.model.UserRole;
import com.example.RestaurantApplication.module.role.repository.UserRoleRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.RestaurantApplication.config.tracing.LogHelper;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;

    public User loadUserByUsername(String username) {
        return userRepository.findByUserName(username)
            .orElseThrow(() -> {
                log.warn("[{}] USER_NOT_FOUND: username={}", LogHelper.loc(), username);
                return new BusinessException("USER_NOT_FOUND", "User not found with username " + username, HttpStatus.NOT_FOUND);
            });
    }

    public List<User> loadAllUsers() {
        return userRepository.findAll();
    }

    public User loadUserById(Long id) {
        // Dùng findByIdFiltered() thay vì findById() để trigger Hibernate filter
        return userRepository.findByIdFiltered(id)
            .orElseThrow(() -> {
                log.warn("[{}] USER_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("USER_NOT_FOUND", "User not found with id " + id, HttpStatus.NOT_FOUND);
            });
    }

    public void addNewUser (String userName, String email, String password, String role) {
        // Lấy restaurant_id từ JWT (đã được parse trong JwtAuthenticationFilter)
        Long restaurantId = getAuthenticatedRestaurantId();

        // Validate username và email uniqueness
        if(userRepository.existsByUserName(userName)) {
            log.warn("[{}] USERNAME_EXISTS: {}", LogHelper.loc(), userName);
            throw new BusinessException("USERNAME_EXISTS", "Username already exists: " + userName, HttpStatus.CONFLICT);
        }
        if(userRepository.existsByEmail(email)) {
            log.warn("[{}] EMAIL_EXISTS: {}", LogHelper.loc(), email);
            throw new BusinessException("EMAIL_EXISTS", "Email already exists: " + email, HttpStatus.CONFLICT);
        }

        // Validate restaurant exists
        if (restaurantRepository.findById(restaurantId).isEmpty()) {
            log.warn("[{}] RESTAURANT_NOT_FOUND: id={}", LogHelper.loc(), restaurantId);
            throw new BusinessException("RESTAURANT_NOT_FOUND", "Restaurant not found with id " + restaurantId, HttpStatus.NOT_FOUND);
        }

        // Find role by name
        UserRole userRole = userRoleRepository.findByNameActive(role)
            .orElseThrow(() -> {
                log.warn("[{}] ROLE_NOT_FOUND: {}", LogHelper.loc(), role);
                return new BusinessException("ROLE_NOT_FOUND",
                    "Role not found: " + role,
                    HttpStatus.NOT_FOUND);
            });

        // Create and save user
        User user = new User();
        user.setRestaurantId(restaurantId);
        user.setUserName(userName);
        user.setEmail(email);
        user.setUserRoleId(userRole.getId());
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("[{}] User created: username={}", LogHelper.loc(), userName);
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
            log.warn("[{}] ACCESS_DENIED: userId={} tried to update userId={}", LogHelper.loc(), currentUserId, targetUserId);
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

        // Update fields
        user.setUserName(username);
        user.setEmail(email);

        // Update password only if provided
        boolean passwordChanged = false;
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
            passwordChanged = true;
        }

        userRepository.save(user);
        log.info("[{}] User updated: userId={}", LogHelper.loc(), targetUserId);

        // Revoke all tokens if password changed (force re-login from all devices)
        if (passwordChanged) {
            authService.revokeAllUserTokens(targetUserId);
            log.info("[{}] Password changed, tokens revoked: userId={}", LogHelper.loc(), targetUserId);
        }
    }

    public void patchUser(Long targetUserId, String username, String email, String password) {
        Long currentUserId = getCurrentUserId();

        // Staff/Manager can only update themselves
        if (!currentUserId.equals(targetUserId)) {
            log.warn("[{}] ACCESS_DENIED: userId={} tried to patch userId={}", LogHelper.loc(), currentUserId, targetUserId);
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

        boolean passwordChanged = false;
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
            passwordChanged = true;
        }

        userRepository.save(user);
        log.info("[{}] User patched: userId={}", LogHelper.loc(), targetUserId);

        // Revoke all tokens if password changed (force re-login from all devices)
        if (passwordChanged) {
            authService.revokeAllUserTokens(targetUserId);
            log.info("[{}] Password changed, tokens revoked: userId={}", LogHelper.loc(), targetUserId);
        }
    }

    public void deleteUser(Long targetUserId) {

        // Load user with filter applied (admin can access all restaurants)
        User user = userRepository.findByIdFiltered(targetUserId)
            .orElseThrow(() -> {
                log.warn("[{}] USER_NOT_FOUND: id={}", LogHelper.loc(), targetUserId);
                return new BusinessException("USER_NOT_FOUND",
                    "User not found with id " + targetUserId,
                    HttpStatus.NOT_FOUND);
            });

        userRepository.delete(user);
        log.info("[{}] User deleted: userId={}", LogHelper.loc(), targetUserId);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

}
