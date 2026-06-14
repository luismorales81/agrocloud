package com.agrocloud.core.domain;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.model.enums.EstadoEmpresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una empresa en el sistema multiempresa.
 * Ubicada en core: no mantiene relaciones con entidades de Cultivos (Field, Cultivo).
 *
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Entity
@Table(name = "empresas")
@EntityListeners(AuditingEntityListener.class)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Size(max = 20, message = "El CUIT no puede exceder 20 caracteres")
    @Column(name = "cuit", unique = true, length = 20)
    private String cuit;

    @Email(message = "El email de contacto debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEmpresa estado = EstadoEmpresa.TRIAL;

    @Column(name = "fecha_inicio_trial")
    private LocalDate fechaInicioTrial;

    @Column(name = "fecha_fin_trial")
    private LocalDate fechaFinTrial;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    @JsonIgnoreProperties({"empresa", "empresas", "creadoPor", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User creadoPor;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UsuarioEmpresa> usuariosEmpresas = new HashSet<>();

    public Empresa() {}

    public Empresa(String nombre, String cuit, String emailContacto) {
        this.nombre = nombre;
        this.cuit = cuit;
        this.emailContacto = emailContacto;
        this.estado = EstadoEmpresa.TRIAL;
        this.activo = true;
        this.fechaInicioTrial = LocalDate.now();
        this.fechaFinTrial = LocalDate.now().plusDays(30);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoEmpresa getEstado() { return estado; }
    public void setEstado(EstadoEmpresa estado) { this.estado = estado; }
    public LocalDate getFechaInicioTrial() { return fechaInicioTrial; }
    public void setFechaInicioTrial(LocalDate fechaInicioTrial) { this.fechaInicioTrial = fechaInicioTrial; }
    public LocalDate getFechaFinTrial() { return fechaFinTrial; }
    public void setFechaFinTrial(LocalDate fechaFinTrial) { this.fechaFinTrial = fechaFinTrial; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public User getCreadoPor() { return creadoPor; }
    public void setCreadoPor(User creadoPor) { this.creadoPor = creadoPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Set<UsuarioEmpresa> getUsuariosEmpresas() { return usuariosEmpresas; }
    public void setUsuariosEmpresas(Set<UsuarioEmpresa> usuariosEmpresas) { this.usuariosEmpresas = usuariosEmpresas; }

    public boolean isTrialActivo() {
        return estado == EstadoEmpresa.TRIAL && fechaFinTrial != null && fechaFinTrial.isAfter(LocalDate.now());
    }
    public boolean isActiva() {
        return activo && (estado == EstadoEmpresa.ACTIVO || isTrialActivo());
    }
    public int getDiasRestantesTrial() {
        if (estado != EstadoEmpresa.TRIAL || fechaFinTrial == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaFinTrial);
    }

    @Override
    public String toString() {
        return "Empresa{" + "id=" + id + ", nombre='" + nombre + '\'' + ", cuit='" + cuit + '\'' + ", estado=" + estado + ", activo=" + activo + '}';
    }
}
