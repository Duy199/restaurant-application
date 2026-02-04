package com.example.RestaurantApplication.module.permission.dto;

import jakarta.validation.constraints.Pattern;

public class PatchPermissionApiRequest {

    private String code;
    private String name;
    private String endpoint;

    @Pattern(regexp = "^(GET|POST|PUT|PATCH|DELETE)$", message = "Method must be GET, POST, PUT, PATCH, or DELETE")
    private String method;

    public PatchPermissionApiRequest() {
    }

    public PatchPermissionApiRequest(String code, String name, String endpoint, String method) {
        this.code = code;
        this.name = name;
        this.endpoint = endpoint;
        this.method = method;
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
