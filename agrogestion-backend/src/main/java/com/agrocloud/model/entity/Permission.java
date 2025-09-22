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
 * Entidad que representa un permiso en el sistema.
 * Los permisos definen acciones específicas que pueden ser realizadas.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "permisos")
@EntityListeners(AuditingEntityListener.class)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre del permiso debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Size(max = 50, message = "El módulo no puede exceder 50 caracteres")
    @Column(name = "modulo", length = 50)
    private String modulo;

    @Size(max = 50, message = "La acción no puede exceder 50 caracteres")
    @Column(name = "accion", length = 50)
    private String accion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con RolePermission (tabla intermedia)
    @OneToMany(mappedBy = "permiso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RolePermission> rolePermissions = new ArrayList<>();

    // Constructors
    public Permission() {}

    public Permission(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public Permission(String nombre, String descripcion, String modulo, String accion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.modulo = modulo;
        this.accion = accion;
        this.activo = true;
    }

    // Métodos de utilidad
    public List<Role> getRoles() {
        return rolePermissions.stream()
                .map(RolePermission::getRol)
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean isAssignedToRole(String roleName) {
        return rolePermissions.stream()
                .anyMatch(rp -> rp.getRol().getNombre().equals(roleName));
    }

    public boolean isAssignedToRole(Long roleId) {
        return rolePermissions.stream()
                .anyMatch(rp -> rp.getRol().getId().equals(roleId));
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

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
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

    public List<RolePermission> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(List<RolePermission> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", modulo='" + modulo + '\'' +
                ", accion='" + accion + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
