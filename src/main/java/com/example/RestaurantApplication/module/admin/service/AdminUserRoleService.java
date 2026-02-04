package com.example.RestaurantApplication.module.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RestaurantApplication.config.redis.TokenBlacklistService;
import com.example.RestaurantApplication.module.permission.repository.PermissionRepository;
import com.example.RestaurantApplication.module.role.model.RoleHasPermission;
import com.example.RestaurantApplication.module.role.model.UserRole;
import com.example.RestaurantApplication.module.role.repository.RoleHasPermissionRepository;
import com.example.RestaurantApplication.module.role.repository.UserRoleRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class AdminUserRoleService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleHasPermissionRepository roleHasPermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // TTL for token revocation (7 days = refresh token expiration)
    private static final long TOKEN_REVOCATION_TTL = 604800000L;

    public List<UserRole> getAllRoles() {
        return userRoleRepository.findAllActive();
    }

    public UserRole getRoleById(Long id) {
        return userRoleRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public UserRole getRoleWithPermissions(Long id) {
        return userRoleRepository.findByIdWithPermissions(id)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public UserRole createRole(String name) {
        if (userRoleRepository.existsByName(name)) {
            throw new BusinessException("ROLE_EXISTS",
                "Role already exists with name: " + name, HttpStatus.CONFLICT);
        }

        UserRole role = new UserRole();
        role.setName(name);
        return userRoleRepository.save(role);
    }

    @Transactional
    public UserRole updateRole(Long id, String name) {
        UserRole role = userRoleRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found with id " + id, HttpStatus.NOT_FOUND));

        if (!role.getName().equals(name) && userRoleRepository.existsByName(name)) {
            throw new BusinessException("ROLE_EXISTS",
                "Role already exists with name: " + name, HttpStatus.CONFLICT);
        }

        role.setName(name);
        UserRole savedRole = userRoleRepository.save(role);

        // Revoke tokens for all users with this role
        revokeTokensForUsersWithRole(id);

        return savedRole;
    }

    @Transactional
    public UserRole patchRole(Long id, String name) {
        UserRole role = userRoleRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found with id " + id, HttpStatus.NOT_FOUND));

        if (name != null && !name.isBlank()) {
            if (!role.getName().equals(name) && userRoleRepository.existsByName(name)) {
                throw new BusinessException("ROLE_EXISTS",
                    "Role already exists with name: " + name, HttpStatus.CONFLICT);
            }
            role.setName(name);
        }

        UserRole savedRole = userRoleRepository.save(role);

        // Revoke tokens for all users with this role
        revokeTokensForUsersWithRole(id);

        return savedRole;
    }

    @Transactional
    public void deleteRole(Long id) {
        UserRole role = userRoleRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found with id " + id, HttpStatus.NOT_FOUND));

        // Revoke tokens for all users with this role before deleting
        revokeTokensForUsersWithRole(id);

        role.setDeletedAt(LocalDateTime.now());
        userRoleRepository.save(role);
    }

    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        UserRole role = userRoleRepository.findByIdActive(roleId)
            .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND",
                "Role not found with id " + roleId, HttpStatus.NOT_FOUND));

        // Clear existing permissions
        roleHasPermissionRepository.deleteByUserRoleId(roleId);

        // Add new permissions
        for (Long permissionId : permissionIds) {
            if (!permissionRepository.findByIdActive(permissionId).isPresent()) {
                throw new BusinessException("PERMISSION_NOT_FOUND",
                    "Permission not found with id " + permissionId, HttpStatus.NOT_FOUND);
            }

            RoleHasPermission rhp = new RoleHasPermission();
            rhp.setUserRoleId(roleId);
            rhp.setPermissionId(permissionId);
            roleHasPermissionRepository.save(rhp);
        }

        // Revoke tokens for all users with this role (permissions changed)
        revokeTokensForUsersWithRole(roleId);
    }

    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        roleHasPermissionRepository.deleteByUserRoleIdAndPermissionId(roleId, permissionId);

        // Revoke tokens for all users with this role (permission removed)
        revokeTokensForUsersWithRole(roleId);
    }

    /**
     * Revoke all tokens for users with a specific role.
     * This forces affected users to login again.
     * Called when role is updated/deleted to ensure security.
     */
    private void revokeTokensForUsersWithRole(Long roleId) {
        List<User> affectedUsers = userRepository.findByUserRoleId(roleId);
        for (User user : affectedUsers) {
            tokenBlacklistService.revokeAllUserTokens(user.getId(), TOKEN_REVOCATION_TTL);
        }
    }
}
