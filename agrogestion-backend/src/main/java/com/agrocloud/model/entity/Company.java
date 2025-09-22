package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una empresa en el sistema multiempresa.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "empresas")
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "^[0-9]{2}-[0-9]{8}-[0-9]{1}$", message = "El formato del CUIT debe ser XX-XXXXXXXX-X")
    @Column(name = "cuit", nullable = false, unique = true, length = 13)
    private String cuit;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Size(max = 100, message = "La dirección no puede exceder 100 caracteres")
    @Column(name = "direccion", length = 100)
    private String direccion;

    @Size(max = 50, message = "La ciudad no puede exceder 50 caracteres")
    @Column(name = "ciudad", length = 50)
    private String ciudad;

    @Size(max = 50, message = "La provincia no puede exceder 50 caracteres")
    @Column(name = "provincia", length = 50)
    private String provincia;

    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con UserCompanyRole (tabla intermedia)
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCompanyRole> userCompanyRoles = new ArrayList<>();

    // Constructors
    public Company() {}

    public Company(String nombre, String cuit) {
        this.nombre = nombre;
        this.cuit = cuit;
        this.activo = true;
    }

    public Company(String nombre, String cuit, String descripcion) {
        this.nombre = nombre;
        this.cuit = cuit;
        this.descripcion = descripcion;
        this.activo = true;
    }

    // Métodos de utilidad
    public List<User> getUsers() {
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getUsuario)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Role> getRoles() {
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getRol)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean hasUser(Long userId) {
        return userCompanyRoles.stream()
                .anyMatch(ucr -> ucr.getUsuario().getId().equals(userId));
    }

    public boolean hasUserWithRole(Long userId, String roleName) {
        return userCompanyRoles.stream()
                .anyMatch(ucr -> ucr.getUsuario().getId().equals(userId) && 
                               ucr.getRol().getNombre().equals(roleName));
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

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<UserCompanyRole> getUserCompanyRoles() {
        return userCompanyRoles;
    }

    public void setUserCompanyRoles(List<UserCompanyRole> userCompanyRoles) {
        this.userCompanyRoles = userCompanyRoles;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cuit='" + cuit + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
