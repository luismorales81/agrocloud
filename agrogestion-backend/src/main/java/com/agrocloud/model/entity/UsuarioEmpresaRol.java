package com.agrocloud.model.entity;

import com.agrocloud.model.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad intermedia que permite asignar múltiples roles RolEmpresa a un usuario en una empresa.
 * Esta entidad extiende la funcionalidad de UsuarioEmpresa para soportar múltiples roles.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "usuarios_empresas_roles", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "empresa_id", "rol_id"}))
@EntityListeners(AuditingEntityListener.class)
public class UsuarioEmpresaRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"usuarioEmpresaRoles", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @NotNull(message = "La empresa es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"usuarioEmpresaRoles", "campos", "lotes", "insumos", "maquinarias", "ingresos", "egresos", "labores", "cultivos"})
    private Empresa empresa;

    @NotNull(message = "El rol es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles", "rolePermissions"})
    private Role rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructors
    public UsuarioEmpresaRol() {}

    public UsuarioEmpresaRol(User usuario, Empresa empresa, Role rol) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.rol = rol;
        this.activo = true;
    }

    public UsuarioEmpresaRol(User usuario, Empresa empresa, Role rol, Boolean activo) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.rol = rol;
        this.activo = activo;
    }
    
    /**
     * Obtiene el RolEmpresa equivalente desde el Role
     */
    public com.agrocloud.model.enums.RolEmpresa getRolEmpresa() {
        if (rol == null || rol.getNombre() == null) {
            return null;
        }
        try {
            return com.agrocloud.model.enums.RolEmpresa.valueOf(rol.getNombre());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Métodos de utilidad
    public boolean isActive() {
        return activo != null && activo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
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
        return "UsuarioEmpresaRol{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getUsername() : "null") +
                ", empresa=" + (empresa != null ? empresa.getNombre() : "null") +
                ", rol=" + rol +
                ", activo=" + activo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioEmpresaRol that = (UsuarioEmpresaRol) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

