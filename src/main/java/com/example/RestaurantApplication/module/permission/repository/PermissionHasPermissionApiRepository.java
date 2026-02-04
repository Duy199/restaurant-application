package com.example.RestaurantApplication.module.permission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RestaurantApplication.module.permission.model.PermissionHasPermissionApi;

public interface PermissionHasPermissionApiRepository extends JpaRepository<PermissionHasPermissionApi, Long> {

    @Query("SELECT phpa FROM PermissionHasPermissionApi phpa WHERE phpa.permissionId = :permissionId")
    List<PermissionHasPermissionApi> findByPermissionId(@Param("permissionId") Long permissionId);

    @Query("SELECT phpa FROM PermissionHasPermissionApi phpa WHERE phpa.permissionApiId = :permissionApiId")
    List<PermissionHasPermissionApi> findByPermissionApiId(@Param("permissionApiId") Long permissionApiId);

    @Modifying
    @Query("DELETE FROM PermissionHasPermissionApi phpa WHERE phpa.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") Long permissionId);

    @Modifying
    @Query("DELETE FROM PermissionHasPermissionApi phpa WHERE phpa.permissionId = :permissionId AND phpa.permissionApiId = :permissionApiId")
    void deleteByPermissionIdAndPermissionApiId(@Param("permissionId") Long permissionId, @Param("permissionApiId") Long permissionApiId);
}
