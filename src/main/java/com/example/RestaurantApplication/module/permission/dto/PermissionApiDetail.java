package com.example.RestaurantApplication.module.permission.dto;

public class PermissionApiDetail {

    private Long id;
    private String code;
    private String name;
    private String endpoint;
    private String method;

    public PermissionApiDetail() {
    }

    public PermissionApiDetail(Long id, String code, String name, String endpoint, String method) {
        this.id = id;
        this.code = code;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
