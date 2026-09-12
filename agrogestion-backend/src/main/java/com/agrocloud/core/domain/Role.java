package com.agrocloud.core.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.ModuleRolePermission;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UserCompanyRole;
import com.agrocloud.core.domain.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un rol en el sistema (Core).
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
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RolePermission> rolePermissions = new ArrayList<>();

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCompanyRole> userCompanyRoles = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ModuleRolePermission> moduleRolePermissions = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserRole> userRoles = new ArrayList<>();

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

    @JsonIgnore
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

    @JsonIgnore
    public List<User> getUsers() {
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getUsuario)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    @JsonIgnore
    public List<Empresa> getCompanies() {
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getEmpresa)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    @JsonIgnore
    public List<RolePermission> getRolePermissions() { return rolePermissions; }
    public void setRolePermissions(List<RolePermission> rolePermissions) { this.rolePermissions = rolePermissions; }
    public List<UserCompanyRole> getUserCompanyRoles() { return userCompanyRoles; }
    public void setUserCompanyRoles(List<UserCompanyRole> userCompanyRoles) { this.userCompanyRoles = userCompanyRoles; }
    public List<ModuleRolePermission> getModuleRolePermissions() { return moduleRolePermissions; }
    public void setModuleRolePermissions(List<ModuleRolePermission> moduleRolePermissions) { this.moduleRolePermissions = moduleRolePermissions; }
    public List<UserRole> getUserRoles() { return userRoles; }
    public void setUserRoles(List<UserRole> userRoles) { this.userRoles = userRoles; }
    public String getDescription() { return descripcion; }
    public void setDescription(String description) { this.descripcion = description; }
    public LocalDateTime getCreatedAt() { return fechaCreacion; }

    @Override
    public String toString() { return "Role{id=" + id + ", nombre='" + nombre + '\'' + ", descripcion='" + descripcion + '\'' + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion + '}'; }
}
