package com.example.RestaurantApplication.module.user.dto.Login;

public class LoginResponse {
    private long userId;
    private String userName;
    private String token;
    private String refreshToken;

    public LoginResponse(long userId, String userName, String token, String refreshToken) {
        this.userId = userId;
        this.userName = userName;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
