package com.example.RestaurantApplication.module.permission.dto;

import java.util.List;

public class PermissionDetail {

    private Long id;
    private String code;
    private String name;
    private List<PermissionApiSummary> apis;

    public PermissionDetail() {
    }

    public PermissionDetail(Long id, String code, String name) {
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

    public List<PermissionApiSummary> getApis() {
        return apis;
    }

    public void setApis(List<PermissionApiSummary> apis) {
        this.apis = apis;
    }

    public static class PermissionApiSummary {
        private Long id;
        private String code;
        private String endpoint;
        private String method;

        public PermissionApiSummary() {
        }

        public PermissionApiSummary(Long id, String code, String endpoint, String method) {
            this.id = id;
            this.code = code;
            this.endpoint = endpoint;
            this.method = method;
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

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }
}
