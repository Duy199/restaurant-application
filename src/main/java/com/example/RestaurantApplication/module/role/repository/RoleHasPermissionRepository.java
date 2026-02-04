package com.example.RestaurantApplication.module.role.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RestaurantApplication.module.role.model.RoleHasPermission;

public interface RoleHasPermissionRepository extends JpaRepository<RoleHasPermission, Long> {

    @Query("SELECT rhp FROM RoleHasPermission rhp WHERE rhp.userRoleId = :roleId")
    List<RoleHasPermission> findByUserRoleId(@Param("roleId") Long roleId);

    @Query("SELECT rhp FROM RoleHasPermission rhp WHERE rhp.permissionId = :permissionId")
    List<RoleHasPermission> findByPermissionId(@Param("permissionId") Long permissionId);

    @Modifying
    @Query("DELETE FROM RoleHasPermission rhp WHERE rhp.userRoleId = :roleId")
    void deleteByUserRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("DELETE FROM RoleHasPermission rhp WHERE rhp.userRoleId = :roleId AND rhp.permissionId = :permissionId")
    void deleteByUserRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
