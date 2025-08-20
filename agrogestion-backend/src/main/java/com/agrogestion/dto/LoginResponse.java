package com.agrogestion.dto;

import java.time.LocalDateTime;

public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDto user;
    private LocalDateTime loginTime;
    
    // Constructores
    public LoginResponse() {
        this.loginTime = LocalDateTime.now();
    }
    
    public LoginResponse(String token, Long expiresIn, UserDto user) {
        this();
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    // Getters y Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", user=" + (user != null ? user.getEmail() : "null") +
                ", loginTime=" + loginTime +
                '}';
    }
}
