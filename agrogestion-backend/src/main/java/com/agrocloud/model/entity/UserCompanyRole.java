package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad intermedia que relaciona un usuario con una empresa y un rol específico.
 * Permite que un usuario tenga diferentes roles en diferentes empresas.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "usuarios_empresas_roles", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "empresa_id", "rol_id"}))
@EntityListeners(AuditingEntityListener.class)
public class UserCompanyRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles"})
    private User usuario;

    @NotNull(message = "La empresa es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles"})
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
    public UserCompanyRole() {}

    public UserCompanyRole(User usuario, Empresa empresa, Role rol) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.rol = rol;
        this.activo = true;
    }

    public UserCompanyRole(User usuario, Empresa empresa, Role rol, Boolean activo) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.rol = rol;
        this.activo = activo;
    }

    // Métodos de utilidad
    public boolean isActive() {
        return activo != null && activo;
    }

    public String getRoleName() {
        return rol != null ? rol.getNombre() : null;
    }

    public String getCompanyName() {
        return empresa != null ? empresa.getNombre() : null;
    }

    public String getUsername() {
        return usuario != null ? usuario.getUsername() : null;
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
        return "UserCompanyRole{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getUsername() : "null") +
                ", empresa=" + (empresa != null ? empresa.getNombre() : "null") +
                ", rol=" + (rol != null ? rol.getNombre() : "null") +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCompanyRole that = (UserCompanyRole) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
