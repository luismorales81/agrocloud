package com.agrogestion.model.entity;

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
 * Entidad que representa las labores agrícolas en el sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "labores")
@EntityListeners(AuditingEntityListener.class)
public class Labor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de labor es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_labor", nullable = false)
    private TipoLabor tipoLabor;

    @NotBlank(message = "El nombre de la labor es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre de la labor debe tener entre 2 y 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "La fecha de la labor es obligatoria")
    @Column(name = "fecha_labor", nullable = false)
    private LocalDate fechaLabor;

    @Positive(message = "La superficie trabajada debe ser un valor positivo")
    @Column(name = "area_hectareas", precision = 10, scale = 2)
    private BigDecimal areaHectareas;

    @Positive(message = "El costo debe ser un valor positivo")
    @Column(name = "costo", precision = 15, scale = 2)
    private BigDecimal costo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLabor estado = EstadoLabor.PLANIFICADA;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Plot lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id")
    private Maquinaria maquinaria;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Enums
    public enum TipoLabor {
        SIEMBRA, FERTILIZACION, RIEGO, COSECHA, MANTENIMIENTO, PODA, CONTROL_PLAGAS, CONTROL_MALEZAS, ANALISIS_SUELO, OTROS
    }

    public enum EstadoLabor {
        PLANIFICADA, EN_PROGRESO, COMPLETADA, CANCELADA
    }

    // Constructors
    public Labor() {}

    public Labor(TipoLabor tipoLabor, String nombre, LocalDate fechaLabor, User usuario) {
        this.tipoLabor = tipoLabor;
        this.nombre = nombre;
        this.fechaLabor = fechaLabor;
        this.usuario = usuario;
        this.estado = EstadoLabor.PLANIFICADA;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoLabor getTipoLabor() {
        return tipoLabor;
    }

    public void setTipoLabor(TipoLabor tipoLabor) {
        this.tipoLabor = tipoLabor;
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

    public LocalDate getFechaLabor() {
        return fechaLabor;
    }

    public void setFechaLabor(LocalDate fechaLabor) {
        this.fechaLabor = fechaLabor;
    }

    public BigDecimal getAreaHectareas() {
        return areaHectareas;
    }

    public void setAreaHectareas(BigDecimal areaHectareas) {
        this.areaHectareas = areaHectareas;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public EstadoLabor getEstado() {
        return estado;
    }

    public void setEstado(EstadoLabor estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Plot getLote() {
        return lote;
    }

    public void setLote(Plot lote) {
        this.lote = lote;
    }

    public Maquinaria getMaquinaria() {
        return maquinaria;
    }

    public void setMaquinaria(Maquinaria maquinaria) {
        this.maquinaria = maquinaria;
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

    // Helper methods
    public boolean isPlanificada() {
        return EstadoLabor.PLANIFICADA.equals(estado);
    }

    public boolean isEnProgreso() {
        return EstadoLabor.EN_PROGRESO.equals(estado);
    }

    public boolean isCompletada() {
        return EstadoLabor.COMPLETADA.equals(estado);
    }

    public boolean isCancelada() {
        return EstadoLabor.CANCELADA.equals(estado);
    }

    public boolean isPasada() {
        return fechaLabor.isBefore(LocalDate.now());
    }

    public boolean isHoy() {
        return fechaLabor.equals(LocalDate.now());
    }

    public boolean isFutura() {
        return fechaLabor.isAfter(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Labor{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipoLabor=" + tipoLabor +
                ", fechaLabor=" + fechaLabor +
                ", estado=" + estado +
                '}';
    }
}
