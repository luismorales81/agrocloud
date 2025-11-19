package com.agrocloud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequest {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    private String token;
    
    private String newPassword;
    
    public PasswordResetRequest() {}
    
    public PasswordResetRequest(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
