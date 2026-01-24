package com.example.RestaurantApplication.config.jwt;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExp;
    private long refreshTokenExp;


    public long getRefreshTokenExp() {
        return refreshTokenExp;
    }
    public void setRefreshTokenExp(long refreshTokenExp) {
        this.refreshTokenExp = refreshTokenExp;
    }
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExp() {
        return accessTokenExp;
    }

    public void setAccessTokenExp(long accessTokenExp) {
        this.accessTokenExp = accessTokenExp;
    }
}

