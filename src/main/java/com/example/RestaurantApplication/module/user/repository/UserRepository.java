package com.example.RestaurantApplication.module.user.repository;

import com.example.RestaurantApplication.module.user.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);
    Boolean existsByUserName(String userName);
    Boolean existsByEmail(String email);

    /**
     * Find user by ID with Hibernate filter applied.
     * IMPORTANT: Dùng JPQL query thay vì findById() để trigger Hibernate filter!
     * findById() uses EntityManager.find() which bypasses filters.
     */
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdFiltered(@Param("id") Long id);

    /**
     * Find all users with a specific role ID.
     * Used for token revocation when role is updated/deleted.
     */
    @Query("SELECT u FROM User u WHERE u.userRoleId = :roleId")
    java.util.List<User> findByUserRoleId(@Param("roleId") Long roleId);
}
