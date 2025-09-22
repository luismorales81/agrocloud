package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para gestionar los egresos del sistema agropecuario
 */
@Entity
@Table(name = "egresos")
public class Egreso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoEgreso tipo;
    
    @Column(name = "referencia_id")
    private Long referenciaId; // FK opcional hacia insumo o maquinaria
    
    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;
    
    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;
    
    @Column(name = "costo_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoTotal;
    
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEgreso estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    @JsonIgnore
    private Plot lote;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    // Enum para tipos de egreso
    public enum TipoEgreso {
        INSUMO,
        MAQUINARIA_COMPRA,
        MAQUINARIA_ALQUILER,
        SERVICIO,
        OTROS
    }
    
    // Enum para estados de egreso
    public enum EstadoEgreso {
        REGISTRADO,
        CONFIRMADO,
        PAGADO,
        CANCELADO
    }
    
    // Constructor por defecto
    public Egreso() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = EstadoEgreso.REGISTRADO;
        this.activo = true;
    }
    
    // Constructor con campos obligatorios
    public Egreso(TipoEgreso tipo, BigDecimal costoTotal, LocalDate fecha, User user) {
        this();
        this.tipo = tipo;
        this.costoTotal = costoTotal;
        this.fecha = fecha;
        this.user = user;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TipoEgreso getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoEgreso tipo) {
        this.tipo = tipo;
    }
    
    public Long getReferenciaId() {
        return referenciaId;
    }
    
    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }
    
    public BigDecimal getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }
    
    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
    
    public BigDecimal getCostoTotal() {
        return costoTotal;
    }
    
    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public EstadoEgreso getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoEgreso estado) {
        this.estado = estado;
    }
    
    public Plot getLote() {
        return lote;
    }
    
    public void setLote(Plot lote) {
        this.lote = lote;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
    
    // Método para calcular costo total automáticamente
    public void calcularCostoTotal() {
        if (this.cantidad != null && this.costoUnitario != null) {
            this.costoTotal = this.cantidad.multiply(this.costoUnitario);
        }
    }
    
    // Método para actualizar fecha de modificación
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
