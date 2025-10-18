package com.agrocloud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa las labores agropecuarias en el sistema.
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


    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @PositiveOrZero(message = "El costo debe ser un valor positivo o cero")
    @Column(name = "costo_total", precision = 10, scale = 2)
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLabor estado = EstadoLabor.PLANIFICADA;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Size(max = 255, message = "El responsable no puede exceder 255 caracteres")
    @Column(name = "responsable", length = 255)
    private String responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Plot lote;


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

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Campos de auditoría para anulación
    @Size(max = 1000, message = "El motivo de anulación no puede exceder 1000 caracteres")
    @Column(name = "motivo_anulacion", length = 1000)
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_anulacion_id")
    private User usuarioAnulacion;

    // Relaciones con entidades relacionadas
    @OneToMany(mappedBy = "labor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<LaborInsumo> insumosUsados;

    @OneToMany(mappedBy = "labor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<LaborMaquinaria> maquinariaAsignada;

    @OneToMany(mappedBy = "labor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<LaborManoObra> manoObra;

    // Enums
    public enum TipoLabor {
        SIEMBRA, FERTILIZACION, RIEGO, COSECHA, MANTENIMIENTO, PODA, CONTROL_PLAGAS, CONTROL_MALEZAS, ANALISIS_SUELO, OTROS
    }

    public enum EstadoLabor {
        PLANIFICADA,    // Labor planificada, aún no ejecutada
        EN_PROGRESO,    // Labor en ejecución
        COMPLETADA,     // Labor finalizada exitosamente
        CANCELADA,      // Labor cancelada antes de ejecutar (insumos restaurados)
        ANULADA         // Labor anulada después de ejecutar (requiere justificación)
    }

    // Constructors
    public Labor() {}

    public Labor(TipoLabor tipoLabor, LocalDate fechaInicio, User usuario) {
        this.tipoLabor = tipoLabor;
        this.fechaInicio = fechaInicio;
        this.usuario = usuario;
        this.estado = EstadoLabor.PLANIFICADA;
        this.activo = true;
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


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
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

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public Plot getLote() {
        return lote;
    }

    public void setLote(Plot lote) {
        this.lote = lote;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public java.util.List<LaborInsumo> getInsumosUsados() {
        return insumosUsados;
    }

    public void setInsumosUsados(java.util.List<LaborInsumo> insumosUsados) {
        this.insumosUsados = insumosUsados;
    }

    public java.util.List<LaborMaquinaria> getMaquinariaAsignada() {
        return maquinariaAsignada;
    }

    public void setMaquinariaAsignada(java.util.List<LaborMaquinaria> maquinariaAsignada) {
        this.maquinariaAsignada = maquinariaAsignada;
    }

    public java.util.List<LaborManoObra> getManoObra() {
        return manoObra;
    }

    public void setManoObra(java.util.List<LaborManoObra> manoObra) {
        this.manoObra = manoObra;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public User getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(User usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
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

    public boolean isAnulada() {
        return EstadoLabor.ANULADA.equals(estado);
    }

    public boolean puedeEliminarseDirectamente() {
        return EstadoLabor.PLANIFICADA.equals(estado);
    }

    public boolean requiereAnulacionFormal() {
        return EstadoLabor.EN_PROGRESO.equals(estado) || 
               EstadoLabor.COMPLETADA.equals(estado);
    }

    public boolean isPasada() {
        return fechaInicio != null && fechaInicio.isBefore(LocalDate.now());
    }

    public boolean isHoy() {
        return fechaInicio != null && fechaInicio.equals(LocalDate.now());
    }

    public boolean isFutura() {
        return fechaInicio != null && fechaInicio.isAfter(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Labor{" +
                "id=" + id +
                ", tipoLabor=" + tipoLabor +
                ", fechaInicio=" + fechaInicio +
                ", estado=" + estado +
                '}';
    }
}
