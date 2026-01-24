package com.example.RestaurantApplication.utils.ResponseWrapper;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;

    public ApiResponse(boolean success, String message, String code, T data) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public static <T> ApiResponse<T> success (String message, String code, T data) {
        return new ApiResponse<T>(true, message, code, data);
    }

    public static <T> ApiResponse<T> error (String message, String code, T data) {
        return new ApiResponse<T>(false, message, code, data);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
