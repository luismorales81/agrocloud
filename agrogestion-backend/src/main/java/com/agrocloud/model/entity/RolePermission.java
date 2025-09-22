package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad intermedia que relaciona un rol con un permiso específico.
 * Permite que un rol tenga múltiples permisos y un permiso pueda estar en múltiples roles.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "roles_permisos", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"rol_id", "permiso_id"}))
@EntityListeners(AuditingEntityListener.class)
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El rol es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    @JsonIgnoreProperties({"rolePermissions", "userCompanyRoles"})
    private Role rol;

    @NotNull(message = "El permiso es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permiso_id", nullable = false)
    @JsonIgnoreProperties({"rolePermissions"})
    private Permission permiso;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructors
    public RolePermission() {}

    public RolePermission(Role rol, Permission permiso) {
        this.rol = rol;
        this.permiso = permiso;
        this.activo = true;
    }

    public RolePermission(Role rol, Permission permiso, Boolean activo) {
        this.rol = rol;
        this.permiso = permiso;
        this.activo = activo;
    }

    // Métodos de utilidad
    public boolean isActive() {
        return activo != null && activo;
    }

    public String getRoleName() {
        return rol != null ? rol.getNombre() : null;
    }

    public String getPermissionName() {
        return permiso != null ? permiso.getNombre() : null;
    }

    public String getPermissionModule() {
        return permiso != null ? permiso.getModulo() : null;
    }

    public String getPermissionAction() {
        return permiso != null ? permiso.getAccion() : null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    public Permission getPermiso() {
        return permiso;
    }

    public void setPermiso(Permission permiso) {
        this.permiso = permiso;
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

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "RolePermission{" +
                "id=" + id +
                ", rol=" + (rol != null ? rol.getNombre() : "null") +
                ", permiso=" + (permiso != null ? permiso.getNombre() : "null") +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePermission that = (RolePermission) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
