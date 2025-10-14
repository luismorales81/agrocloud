package com.agrocloud.dto;

import com.agrocloud.model.entity.EstadoUsuario;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para la administración de usuarios con información completa
 */
public class AdminUsuarioDTO {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private EstadoUsuario estado;
    private Boolean activo;
    private Boolean emailVerified;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Información de auditoría
    private Long creadoPorId;
    private String creadoPorNombre;
    private String creadoPorEmail;
    
    // Roles del usuario
    private Set<RoleDTO> roles;
    private Set<Long> roleIds; // Para recibir/enviar solo los IDs de los roles
    
    // Información de jerarquía
    private Long parentUserId;
    private String parentUserName;
    private Integer childUsersCount;
    
    // Constructor
    public AdminUsuarioDTO() {}
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public EstadoUsuario getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getCreadoPorId() {
        return creadoPorId;
    }
    
    public void setCreadoPorId(Long creadoPorId) {
        this.creadoPorId = creadoPorId;
    }
    
    public String getCreadoPorNombre() {
        return creadoPorNombre;
    }
    
    public void setCreadoPorNombre(String creadoPorNombre) {
        this.creadoPorNombre = creadoPorNombre;
    }
    
    public String getCreadoPorEmail() {
        return creadoPorEmail;
    }
    
    public void setCreadoPorEmail(String creadoPorEmail) {
        this.creadoPorEmail = creadoPorEmail;
    }
    
    public Set<RoleDTO> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }
    
    public Set<Long> getRoleIds() {
        return roleIds;
    }
    
    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
    
    public Long getParentUserId() {
        return parentUserId;
    }
    
    public void setParentUserId(Long parentUserId) {
        this.parentUserId = parentUserId;
    }
    
    public String getParentUserName() {
        return parentUserName;
    }
    
    public void setParentUserName(String parentUserName) {
        this.parentUserName = parentUserName;
    }
    
    public Integer getChildUsersCount() {
        return childUsersCount;
    }
    
    public void setChildUsersCount(Integer childUsersCount) {
        this.childUsersCount = childUsersCount;
    }
    
    @Override
    public String toString() {
        return "AdminUsuarioDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", estado=" + estado +
                ", activo=" + activo +
                ", roles=" + roles +
                '}';
    }
}
