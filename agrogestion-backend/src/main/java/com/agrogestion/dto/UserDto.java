package com.agrogestion.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDto {
    
    private Long id;
    private String name;
    private String email;
    private String roleName;
    private Set<String> permissions;
    private boolean active;
    private boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    
    // Constructores
    public UserDto() {}
    
    public UserDto(Long id, String name, String email, String roleName, Set<String> permissions, 
                   boolean active, boolean emailVerified, LocalDateTime lastLogin, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.roleName = roleName;
        this.permissions = permissions;
        this.active = active;
        this.emailVerified = emailVerified;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }
    
    // Getters y Setters
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Métodos de utilidad
    public boolean hasPermission(String permission) {
        return this.permissions != null && this.permissions.contains(permission);
    }
    
    public boolean isAdmin() {
        return "ADMINISTRADOR".equals(this.roleName);
    }
    
    public boolean isOperario() {
        return "OPERARIO".equals(this.roleName);
    }
    
    public boolean isIngenieroAgronomo() {
        return "INGENIERO_AGRONOMO".equals(this.roleName);
    }
    
    public boolean isInvitado() {
        return "INVITADO".equals(this.roleName);
    }
    
    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", roleName='" + roleName + '\'' +
                ", active=" + active +
                ", emailVerified=" + emailVerified +
                ", createdAt=" + createdAt +
                '}';
    }
}
