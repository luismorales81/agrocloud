package com.agrocloud.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para gestionar el alquiler de maquinaria
 */
@Entity
@Table(name = "alquiler_maquinaria")
public class AlquilerMaquinaria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;
    
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;
    
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;
    
    @Column(name = "costo_dia", precision = 10, scale = 2)
    private BigDecimal costoDia;
    
    @Column(name = "costo_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal costoTotal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Plot lote;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Constructor por defecto
    public AlquilerMaquinaria() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Constructor con campos obligatorios
    public AlquilerMaquinaria(Maquinaria maquinaria, LocalDate fechaInicio, LocalDate fechaFin, 
                              BigDecimal costoTotal, User user) {
        this();
        this.maquinaria = maquinaria;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costoTotal = costoTotal;
        this.user = user;
    }
    
    // Getters y Setters
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
    
    public BigDecimal getCostoDia() {
        return costoDia;
    }
    
    public void setCostoDia(BigDecimal costoDia) {
        this.costoDia = costoDia;
    }
    
    public BigDecimal getCostoTotal() {
        return costoTotal;
    }
    
    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }
    
    public Plot getLote() {
        return lote;
    }
    
    public void setLote(Plot lote) {
        this.lote = lote;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
    
    // Método para calcular el costo total basado en días y costo por día
    public void calcularCostoTotal() {
        if (this.fechaInicio != null && this.fechaFin != null && this.costoDia != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(this.fechaInicio, this.fechaFin) + 1;
            this.costoTotal = this.costoDia.multiply(BigDecimal.valueOf(dias));
        }
    }
    
    // Método para obtener la duración en días
    public long getDuracionDias() {
        if (this.fechaInicio != null && this.fechaFin != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(this.fechaInicio, this.fechaFin) + 1;
        }
        return 0;
    }
    
    // Método para verificar si el alquiler está activo
    public boolean isActivo() {
        LocalDate hoy = LocalDate.now();
        return this.fechaInicio != null && this.fechaFin != null &&
               !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
    
    // Método para actualizar fecha de modificación
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
