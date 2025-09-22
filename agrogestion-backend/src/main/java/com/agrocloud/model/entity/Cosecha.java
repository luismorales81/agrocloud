package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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
 * Entidad para representar las cosechas de cultivos
 * Permite registrar los rendimientos reales obtenidos y compararlos con las proyecciones
 */
@Entity
@Table(name = "cosechas")
@EntityListeners(AuditingEntityListener.class)
public class Cosecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El cultivo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultivo_id", nullable = false)
    @JsonBackReference("cosecha-cultivo")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cultivo cultivo;

    @NotNull(message = "La fecha de cosecha es obligatoria")
    @Column(name = "fecha_cosecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonBackReference("cosecha-lote")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Plot lote;

    @Column(name = "cantidad_toneladas", precision = 10, scale = 2)
    private BigDecimal cantidadToneladas;

    @Column(name = "precio_por_tonelada", precision = 10, scale = 2)
    private BigDecimal precioPorTonelada;

    @Column(name = "costo_total", precision = 10, scale = 2)
    private BigDecimal costoTotal;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonBackReference("cosecha-usuario")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User usuario;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public Cosecha() {}

    public Cosecha(Cultivo cultivo, Plot lote, LocalDate fecha, BigDecimal cantidadToneladas, 
                   BigDecimal precioPorTonelada, BigDecimal costoTotal, String observaciones, User usuario) {
        this.cultivo = cultivo;
        this.lote = lote;
        this.fecha = fecha;
        this.cantidadToneladas = cantidadToneladas;
        this.precioPorTonelada = precioPorTonelada;
        this.costoTotal = costoTotal;
        this.observaciones = observaciones;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cultivo getCultivo() {
        return cultivo;
    }

    public void setCultivo(Cultivo cultivo) {
        this.cultivo = cultivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Plot getLote() {
        return lote;
    }

    public void setLote(Plot lote) {
        this.lote = lote;
    }

    public BigDecimal getCantidadToneladas() {
        return cantidadToneladas;
    }

    public void setCantidadToneladas(BigDecimal cantidadToneladas) {
        this.cantidadToneladas = cantidadToneladas;
    }

    public BigDecimal getPrecioPorTonelada() {
        return precioPorTonelada;
    }

    public void setPrecioPorTonelada(BigDecimal precioPorTonelada) {
        this.precioPorTonelada = precioPorTonelada;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
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

    /**
     * Calcula el rendimiento total (cantidad de toneladas)
     */
    public BigDecimal getRendimientoTotal() {
        return cantidadToneladas != null ? cantidadToneladas : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Cosecha{" +
                "id=" + id +
                ", cultivo=" + (cultivo != null ? cultivo.getNombre() : "null") +
                ", lote=" + (lote != null ? lote.getNombre() : "null") +
                ", fecha=" + fecha +
                ", cantidadToneladas=" + cantidadToneladas +
                ", precioPorTonelada=" + precioPorTonelada +
                ", costoTotal=" + costoTotal +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}
