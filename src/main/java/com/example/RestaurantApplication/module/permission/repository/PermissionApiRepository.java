package com.example.RestaurantApplication.module.permission.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RestaurantApplication.module.permission.model.PermissionApi;

public interface PermissionApiRepository extends JpaRepository<PermissionApi, Long> {

    @Query("SELECT pa FROM PermissionApi pa WHERE pa.deletedAt IS NULL")
    List<PermissionApi> findAllActive();

    @Query("SELECT pa FROM PermissionApi pa WHERE pa.id = :id AND pa.deletedAt IS NULL")
    Optional<PermissionApi> findByIdActive(@Param("id") Long id);

    @Query("SELECT pa FROM PermissionApi pa WHERE pa.endpoint = :endpoint AND pa.method = :method AND pa.deletedAt IS NULL")
    Optional<PermissionApi> findByEndpointAndMethod(@Param("endpoint") String endpoint, @Param("method") String method);

    @Query("""
        SELECT DISTINCT pa FROM PermissionApi pa
        JOIN PermissionHasPermissionApi phpa ON phpa.permissionApiId = pa.id
        WHERE phpa.permissionId IN :permissionIds
        AND pa.deletedAt IS NULL
        """)
    List<PermissionApi> findByPermissionIds(@Param("permissionIds") Set<Long> permissionIds);

    Boolean existsByCode(String code);
}
