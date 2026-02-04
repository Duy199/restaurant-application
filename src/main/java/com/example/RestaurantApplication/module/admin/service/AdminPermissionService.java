package com.example.RestaurantApplication.module.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RestaurantApplication.config.redis.TokenBlacklistService;
import com.example.RestaurantApplication.module.permission.model.Permission;
import com.example.RestaurantApplication.module.permission.model.PermissionHasPermissionApi;
import com.example.RestaurantApplication.module.permission.repository.PermissionApiRepository;
import com.example.RestaurantApplication.module.permission.repository.PermissionHasPermissionApiRepository;
import com.example.RestaurantApplication.module.permission.repository.PermissionRepository;
import com.example.RestaurantApplication.module.role.model.RoleHasPermission;
import com.example.RestaurantApplication.module.role.repository.RoleHasPermissionRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

@Service
public class AdminPermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionApiRepository permissionApiRepository;

    @Autowired
    private PermissionHasPermissionApiRepository permissionHasPermissionApiRepository;

    @Autowired
    private RoleHasPermissionRepository roleHasPermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // TTL for token revocation (7 days = refresh token expiration)
    private static final long TOKEN_REVOCATION_TTL = 604800000L;

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllActive();
    }

    public Permission getPermissionById(Long id) {
        return permissionRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND",
                "Permission not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public Permission getPermissionWithApis(Long id) {
        return permissionRepository.findByIdWithApis(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND",
                "Permission not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public Permission createPermission(String code, String name) {
        if (permissionRepository.existsByCode(code)) {
            throw new BusinessException("PERMISSION_EXISTS",
                "Permission already exists with code: " + code, HttpStatus.CONFLICT);
        }

        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission updatePermission(Long id, String code, String name) {
        Permission permission = permissionRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND",
                "Permission not found with id " + id, HttpStatus.NOT_FOUND));

        if (!permission.getCode().equals(code) && permissionRepository.existsByCode(code)) {
            throw new BusinessException("PERMISSION_EXISTS",
                "Permission already exists with code: " + code, HttpStatus.CONFLICT);
        }

        permission.setCode(code);
        permission.setName(name);
        Permission savedPermission = permissionRepository.save(permission);

        // Check if this permission is assigned to any roles, then revoke
        revokeTokensIfPermissionIsAssigned(id);

        return savedPermission;
    }

    @Transactional
    public Permission patchPermission(Long id, String code, String name) {
        Permission permission = permissionRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND",
                "Permission not found with id " + id, HttpStatus.NOT_FOUND));

        if (code != null && !code.isBlank()) {
            if (!permission.getCode().equals(code) && permissionRepository.existsByCode(code)) {
                throw new BusinessException("PERMISSION_EXISTS",
                    "Permission already exists with code: " + code, HttpStatus.CONFLICT);
            }
            permission.setCode(code);
        }

        if (name != null && !name.isBlank()) {
            permission.setName(name);
        }

        Permission savedPermission = permissionRepository.save(permission);

        // Check if this permission is assigned to any roles, then revoke
        revokeTokensIfPermissionIsAssigned(id);

        return savedPermission;
    }

    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findByIdActive(id)
            .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND",
                "Permission not found with id " + id, HttpStatus.NOT_FOUND));

        // Revoke tokens for all users affected by this permission deletion
        revokeTokensForUsersWithPermission(id);

        permission.setDeletedAt(LocalDateTime.now());
        permissionRepository.save(permission);
    }

    @Transactional
    public void assignApisToPermission(Long permissionId, List<Long> apiIds) {
        Permission permission = permissionRepository.findByIdActive(permissionId)
            .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND",
                "Permission not found with id " + permissionId, HttpStatus.NOT_FOUND));

        // Clear existing API mappings
        permissionHasPermissionApiRepository.deleteByPermissionId(permissionId);

        // Add new API mappings
        for (Long apiId : apiIds) {
            if (!permissionApiRepository.findByIdActive(apiId).isPresent()) {
                throw new BusinessException("PERMISSION_API_NOT_FOUND",
                    "Permission API not found with id " + apiId, HttpStatus.NOT_FOUND);
            }

            PermissionHasPermissionApi phpa = new PermissionHasPermissionApi();
            phpa.setPermissionId(permissionId);
            phpa.setPermissionApiId(apiId);
            permissionHasPermissionApiRepository.save(phpa);
        }

        // Revoke tokens for all users affected by this permission change
        revokeTokensForUsersWithPermission(permissionId);
    }

    @Transactional
    public void removeApiFromPermission(Long permissionId, Long apiId) {
        permissionHasPermissionApiRepository.deleteByPermissionIdAndPermissionApiId(permissionId, apiId);

        // Revoke tokens for all users affected by this permission change
        revokeTokensForUsersWithPermission(permissionId);
    }

    /**
     * Revoke all tokens for users affected by permission changes.
     * Flow: Permission → Roles with that Permission → Users with those Roles
     */
    private void revokeTokensForUsersWithPermission(Long permissionId) {
        // Find all roles that have this permission
        List<RoleHasPermission> rolePermissions = roleHasPermissionRepository.findByPermissionId(permissionId);

        // For each role, revoke tokens for all users with that role
        for (RoleHasPermission rolePermission : rolePermissions) {
            List<User> affectedUsers = userRepository.findByUserRoleId(rolePermission.getUserRoleId());
            for (User user : affectedUsers) {
                tokenBlacklistService.revokeAllUserTokens(user.getId(), TOKEN_REVOCATION_TTL);
            }
        }
    }

    /**
     * Check if Permission is assigned to any Roles.
     * Only revoke tokens if it's actually being used.
     * Flow: Permission → Roles using it → Users
     */
    private void revokeTokensIfPermissionIsAssigned(Long permissionId) {
        // Check if this permission is assigned to any roles
        List<RoleHasPermission> rolePermissions = roleHasPermissionRepository.findByPermissionId(permissionId);

        // Only proceed if permission is actually being used
        if (rolePermissions.isEmpty()) {
            return; // Not assigned anywhere, no need to revoke
        }

        // Permission is assigned, revoke tokens for all affected users
        for (RoleHasPermission rolePermission : rolePermissions) {
            List<User> affectedUsers = userRepository.findByUserRoleId(rolePermission.getUserRoleId());
            for (User user : affectedUsers) {
                tokenBlacklistService.revokeAllUserTokens(user.getId(), TOKEN_REVOCATION_TTL);
            }
        }
    }
}
