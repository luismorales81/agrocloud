package com.agrocloud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para gestionar el inventario de granos cosechados.
 * Permite controlar stock disponible, costos y trazabilidad.
 */
@Entity
@Table(name = "inventario_granos")
@EntityListeners(AuditingEntityListener.class)
public class InventarioGrano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La cosecha es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cosecha_id", nullable = false)
    private HistorialCosecha cosecha;

    @NotNull(message = "El cultivo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @NotNull(message = "El lote es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Plot lote;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @NotNull
    @Positive(message = "La cantidad inicial debe ser positiva")
    @Column(name = "cantidad_inicial", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadInicial;

    @NotNull
    @Column(name = "cantidad_disponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadDisponible;

    @NotNull
    @Column(name = "unidad_medida", length = 10, nullable = false)
    private String unidadMedida; // kg, ton, qq

    @NotNull
    @Column(name = "costo_produccion_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal costoProduccionTotal = BigDecimal.ZERO;

    @NotNull
    @Column(name = "costo_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    @NotNull
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @NotNull
    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "DISPONIBLE"; // DISPONIBLE, RESERVADO, AGOTADO

    @Column(name = "variedad", length = 100)
    private String variedad;

    @Column(name = "calidad", length = 50)
    private String calidad;

    @Column(name = "ubicacion_almacenamiento", length = 200)
    private String ubicacionAlmacenamiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public InventarioGrano() {}

    public InventarioGrano(HistorialCosecha cosecha, Cultivo cultivo, Plot lote, User usuario,
                          BigDecimal cantidadInicial, String unidadMedida, 
                          BigDecimal costoProduccionTotal, LocalDate fechaIngreso) {
        this.cosecha = cosecha;
        this.cultivo = cultivo;
        this.lote = lote;
        this.usuario = usuario;
        this.cantidadInicial = cantidadInicial;
        this.cantidadDisponible = cantidadInicial;
        this.unidadMedida = unidadMedida;
        this.costoProduccionTotal = costoProduccionTotal;
        this.fechaIngreso = fechaIngreso;
        
        // Calcular costo unitario
        if (cantidadInicial != null && cantidadInicial.compareTo(BigDecimal.ZERO) > 0) {
            this.costoUnitario = costoProduccionTotal.divide(cantidadInicial, 2, java.math.RoundingMode.HALF_UP);
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HistorialCosecha getCosecha() {
        return cosecha;
    }

    public void setCosecha(HistorialCosecha cosecha) {
        this.cosecha = cosecha;
    }

    public Cultivo getCultivo() {
        return cultivo;
    }

    public void setCultivo(Cultivo cultivo) {
        this.cultivo = cultivo;
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

    public BigDecimal getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(BigDecimal cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public BigDecimal getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(BigDecimal cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
        // Actualizar estado automáticamente
        if (cantidadDisponible.compareTo(BigDecimal.ZERO) <= 0) {
            this.estado = "AGOTADO";
        }
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getCostoProduccionTotal() {
        return costoProduccionTotal;
    }

    public void setCostoProduccionTotal(BigDecimal costoProduccionTotal) {
        this.costoProduccionTotal = costoProduccionTotal;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getVariedad() {
        return variedad;
    }

    public void setVariedad(String variedad) {
        this.variedad = variedad;
    }

    public String getCalidad() {
        return calidad;
    }

    public void setCalidad(String calidad) {
        this.calidad = calidad;
    }

    public String getUbicacionAlmacenamiento() {
        return ubicacionAlmacenamiento;
    }

    public void setUbicacionAlmacenamiento(String ubicacionAlmacenamiento) {
        this.ubicacionAlmacenamiento = ubicacionAlmacenamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

