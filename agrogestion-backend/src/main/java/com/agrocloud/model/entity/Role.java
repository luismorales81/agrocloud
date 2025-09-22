package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un rol en el sistema.
 * Los roles definen las responsabilidades y permisos de los usuarios.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre del rol debe tener entre 2 y 50 caracteres")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación con RolePermission (tabla intermedia)
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RolePermission> rolePermissions = new ArrayList<>();

    // Relación con UserCompanyRole (tabla intermedia)
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCompanyRole> userCompanyRoles = new ArrayList<>();

    // Constructors
    public Role() {}

    public Role(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public Role(String nombre) {
        this.nombre = nombre;
        this.activo = true;
    }

    // Métodos de utilidad
    public List<Permission> getPermissions() {
        return rolePermissions.stream()
                .map(RolePermission::getPermiso)
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean hasPermission(String permissionName) {
        return rolePermissions.stream()
                .anyMatch(rp -> rp.getPermiso().getNombre().equals(permissionName));
    }

    public boolean hasPermission(Long permissionId) {
        return rolePermissions.stream()
                .anyMatch(rp -> rp.getPermiso().getId().equals(permissionId));
    }

    public List<User> getUsers() {
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getUsuario)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Empresa> getCompanies() {
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getEmpresa)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    public List<RolePermission> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(List<RolePermission> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }

    public List<UserCompanyRole> getUserCompanyRoles() {
        return userCompanyRoles;
    }

    public void setUserCompanyRoles(List<UserCompanyRole> userCompanyRoles) {
        this.userCompanyRoles = userCompanyRoles;
    }

    // Métodos de compatibilidad con el código existente
    public String getDescription() {
        return descripcion;
    }

    public void setDescription(String description) {
        this.descripcion = description;
    }

    public LocalDateTime getCreatedAt() {
        return fechaCreacion;
    }


    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}