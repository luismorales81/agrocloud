package com.agrocloud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa los mantenimientos de maquinaria en el sistema.
 * 
 * @deprecated Esta entidad está deprecada. La tabla será eliminada en V1_13.
 * Razón: Funcionalidad nunca implementada, sin servicio ni controlador.
 * Nota: Puede reimplementarse en el futuro si se requiere gestión de mantenimientos.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Deprecated
@Entity
@Table(name = "mantenimientos_maquinaria")
@EntityListeners(AuditingEntityListener.class)
public class MantenimientoMaquinaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La maquinaria es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;

    @NotNull(message = "El tipo de mantenimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mantenimiento", nullable = false)
    private TipoMantenimiento tipoMantenimiento;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @NotNull(message = "La fecha programada es obligatoria")
    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "fecha_realizada")
    private LocalDate fechaRealizada;

    @Positive(message = "Las horas de máquina deben ser un valor positivo")
    @Column(name = "horas_maquina", precision = 10, scale = 2)
    private BigDecimal horasMaquina;

    @Positive(message = "El costo debe ser un valor positivo")
    @Column(name = "costo", precision = 15, scale = 2)
    private BigDecimal costo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoMantenimiento estado = EstadoMantenimiento.PROGRAMADO;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "proximo_mantenimiento")
    private LocalDate proximoMantenimiento;

    @Positive(message = "Las horas del próximo mantenimiento deben ser un valor positivo")
    @Column(name = "horas_proximo_mantenimiento", precision = 10, scale = 2)
    private BigDecimal horasProximoMantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Enums
    public enum TipoMantenimiento {
        PREVENTIVO, CORRECTIVO, REPARACION, INSPECCION
    }

    public enum EstadoMantenimiento {
        PROGRAMADO, EN_PROGRESO, COMPLETADO, CANCELADO
    }

    // Constructors
    public MantenimientoMaquinaria() {}

    public MantenimientoMaquinaria(Maquinaria maquinaria, TipoMantenimiento tipoMantenimiento, 
                                  String descripcion, LocalDate fechaProgramada) {
        this.maquinaria = maquinaria;
        this.tipoMantenimiento = tipoMantenimiento;
        this.descripcion = descripcion;
        this.fechaProgramada = fechaProgramada;
        this.estado = EstadoMantenimiento.PROGRAMADO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Maquinaria getMaquinaria() {
        return maquinaria;
    }

    public void setMaquinaria(Maquinaria maquinaria) {
        this.maquinaria = maquinaria;
    }

    public TipoMantenimiento getTipoMantenimiento() {
        return tipoMantenimiento;
    }

    public void setTipoMantenimiento(TipoMantenimiento tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public LocalDate getFechaRealizada() {
        return fechaRealizada;
    }

    public void setFechaRealizada(LocalDate fechaRealizada) {
        this.fechaRealizada = fechaRealizada;
    }

    public BigDecimal getHorasMaquina() {
        return horasMaquina;
    }

    public void setHorasMaquina(BigDecimal horasMaquina) {
        this.horasMaquina = horasMaquina;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public EstadoMantenimiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoMantenimiento estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getProximoMantenimiento() {
        return proximoMantenimiento;
    }

    public void setProximoMantenimiento(LocalDate proximoMantenimiento) {
        this.proximoMantenimiento = proximoMantenimiento;
    }

    public BigDecimal getHorasProximoMantenimiento() {
        return horasProximoMantenimiento;
    }

    public void setHorasProximoMantenimiento(BigDecimal horasProximoMantenimiento) {
        this.horasProximoMantenimiento = horasProximoMantenimiento;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
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
        return "MantenimientoMaquinaria{" +
                "id=" + id +
                ", maquinaria=" + (maquinaria != null ? maquinaria.getNombre() : "null") +
                ", tipoMantenimiento=" + tipoMantenimiento +
                ", fechaProgramada=" + fechaProgramada +
                ", estado=" + estado +
                '}';
    }
}
