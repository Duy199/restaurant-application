package com.example.RestaurantApplication.module.role.dto;

import java.util.List;

public class UserRoleDetail {

    private Long id;
    private String name;
    private List<PermissionSummary> permissions;

    public UserRoleDetail() {
    }

    public UserRoleDetail(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PermissionSummary> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionSummary> permissions) {
        this.permissions = permissions;
    }

    public static class PermissionSummary {
        private Long id;
        private String code;
        private String name;

        public PermissionSummary() {
        }

        public PermissionSummary(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
