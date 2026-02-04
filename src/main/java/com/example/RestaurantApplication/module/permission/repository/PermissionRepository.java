package com.example.RestaurantApplication.module.permission.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RestaurantApplication.module.permission.model.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL")
    List<Permission> findAllActive();

    @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Permission> findByIdActive(@Param("id") Long id);

    @Query("SELECT p FROM Permission p WHERE p.code = :code AND p.deletedAt IS NULL")
    Optional<Permission> findByCodeActive(@Param("code") String code);

    @Query("SELECT p FROM Permission p LEFT JOIN FETCH p.permissionApis WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Permission> findByIdWithApis(@Param("id") Long id);

    Boolean existsByCode(String code);
}
