package com.agrogestion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotNull(message = "El rol es obligatorio")
    private Long roleId;
    
    private boolean sendInvitationEmail = true;
    
    // Constructores
    public CreateUserRequest() {}
    
    public CreateUserRequest(String name, String email, String password, Long roleId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
    }
    
    // Getters y Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Long getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
    
    public boolean isSendInvitationEmail() {
        return sendInvitationEmail;
    }
    
    public void setSendInvitationEmail(boolean sendInvitationEmail) {
        this.sendInvitationEmail = sendInvitationEmail;
    }
    
    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", roleId=" + roleId +
                ", sendInvitationEmail=" + sendInvitationEmail +
                '}';
    }
}
