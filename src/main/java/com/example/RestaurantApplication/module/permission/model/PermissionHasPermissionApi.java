package com.example.RestaurantApplication.module.permission.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "permission_has_permission_api")
public class PermissionHasPermissionApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private Permission permission;

    @Column(name = "permission_id")
    private Long permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_api_id", insertable = false, updatable = false)
    private PermissionApi permissionApi;

    @Column(name = "permission_api_id")
    private Long permissionApiId;

    public PermissionHasPermissionApi() {
    }

    public PermissionHasPermissionApi(Long permissionId, Long permissionApiId) {
        this.permissionId = permissionId;
        this.permissionApiId = permissionApiId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public PermissionApi getPermissionApi() {
        return permissionApi;
    }

    public void setPermissionApi(PermissionApi permissionApi) {
        this.permissionApi = permissionApi;
    }

    public Long getPermissionApiId() {
        return permissionApiId;
    }

    public void setPermissionApiId(Long permissionApiId) {
        this.permissionApiId = permissionApiId;
    }
}
