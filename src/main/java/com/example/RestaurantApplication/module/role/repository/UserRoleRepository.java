package com.example.RestaurantApplication.module.role.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RestaurantApplication.module.role.model.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("SELECT ur FROM UserRole ur WHERE ur.deletedAt IS NULL")
    List<UserRole> findAllActive();

    @Query("SELECT ur FROM UserRole ur WHERE ur.id = :id AND ur.deletedAt IS NULL")
    Optional<UserRole> findByIdActive(@Param("id") Long id);

    @Query("SELECT ur FROM UserRole ur WHERE ur.name = :name AND ur.deletedAt IS NULL")
    Optional<UserRole> findByNameActive(@Param("name") String name);

    @Query("SELECT ur FROM UserRole ur LEFT JOIN FETCH ur.permissions WHERE ur.id = :id AND ur.deletedAt IS NULL")
    Optional<UserRole> findByIdWithPermissions(@Param("id") Long id);

    Boolean existsByName(String name);
}
