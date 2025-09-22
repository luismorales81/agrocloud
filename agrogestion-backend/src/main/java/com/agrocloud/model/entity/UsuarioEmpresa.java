package com.agrocloud.model.entity;

import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la relación entre usuarios y empresas en el sistema multiempresa.
 * Permite que un usuario pertenezca a múltiples empresas con diferentes roles.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Entity
@Table(name = "usuario_empresas", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "empresa_id"}))
@EntityListeners(AuditingEntityListener.class)
public class UsuarioEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"empresa", "empresas", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @NotNull(message = "La empresa es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"usuariosEmpresas", "campos", "lotes", "insumos", "maquinarias", "ingresos", "egresos", "labores", "cultivos"})
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolEmpresa rol = RolEmpresa.OPERARIO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoUsuarioEmpresa estado = EstadoUsuarioEmpresa.PENDIENTE;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    @JsonIgnoreProperties({"empresa", "empresas", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User creadoPor;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructors
    public UsuarioEmpresa() {}

    public UsuarioEmpresa(User usuario, Empresa empresa, RolEmpresa rol) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.rol = rol;
        this.estado = EstadoUsuarioEmpresa.ACTIVO;
        this.fechaInicio = LocalDate.now();
    }

    public UsuarioEmpresa(User usuario, Empresa empresa, RolEmpresa rol, EstadoUsuarioEmpresa estado) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.rol = rol;
        this.estado = estado;
        this.fechaInicio = LocalDate.now();
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

    public RolEmpresa getRol() {
        return rol;
    }

    public void setRol(RolEmpresa rol) {
        this.rol = rol;
    }

    public EstadoUsuarioEmpresa getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuarioEmpresa estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public User getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(User creadoPor) {
        this.creadoPor = creadoPor;
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

    // Métodos de utilidad
    public boolean isActivo() {
        return estado == EstadoUsuarioEmpresa.ACTIVO && 
               (fechaFin == null || fechaFin.isAfter(LocalDate.now()));
    }

    public boolean isAdministrador() {
        return rol == RolEmpresa.ADMINISTRADOR;
    }

    public boolean isAsesor() {
        return rol == RolEmpresa.ASESOR;
    }

    public boolean isOperario() {
        return rol == RolEmpresa.OPERARIO;
    }

    public boolean isContador() {
        return rol == RolEmpresa.CONTADOR;
    }

    public boolean isTecnico() {
        return rol == RolEmpresa.TECNICO;
    }

    public boolean isSoloLectura() {
        return rol == RolEmpresa.LECTURA;
    }

    public boolean tienePermisoEscritura() {
        return isActivo() && !isSoloLectura();
    }

    public boolean tienePermisoAdministracion() {
        return isActivo() && (isAdministrador() || isAsesor());
    }

    public boolean tienePermisoFinanciero() {
        return isActivo() && (isAdministrador() || isAsesor() || isContador());
    }
    
    // Métodos faltantes para los tests
    public void setRolNombre(String rolNombre) {
        try {
            this.rol = RolEmpresa.valueOf(rolNombre);
        } catch (IllegalArgumentException e) {
            // Si no se puede convertir, usar valor por defecto
            this.rol = RolEmpresa.OPERARIO;
        }
    }
    
    public String getRolNombre() {
        return this.rol != null ? this.rol.name() : "OPERARIO";
    }

    @Override
    public String toString() {
        return "UsuarioEmpresa{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getUsername() : "null") +
                ", empresa=" + (empresa != null ? empresa.getNombre() : "null") +
                ", rol=" + rol +
                ", estado=" + estado +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                '}';
    }
}
