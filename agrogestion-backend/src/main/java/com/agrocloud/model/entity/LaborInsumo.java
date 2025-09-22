package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa los insumos utilizados en una labor agrícola.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "labor_insumos")
public class LaborInsumo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_labor_insumo")
    private Long idLaborInsumo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_labor", nullable = false)
    @JsonIgnore
    private Labor labor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;
    
    @NotNull(message = "La cantidad usada es obligatoria")
    @DecimalMin(value = "0.0", message = "La cantidad usada debe ser mayor o igual a 0")
    @Column(name = "cantidad_usada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadUsada;
    
    @NotNull(message = "La cantidad planificada es obligatoria")
    @DecimalMin(value = "0.0", message = "La cantidad planificada debe ser mayor o igual a 0")
    @Column(name = "cantidad_planificada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadPlanificada;
    
    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo unitario debe ser mayor o igual a 0")
    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;
    
    @NotNull(message = "El costo total es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo total debe ser mayor o igual a 0")
    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(name = "observaciones", length = 500)
    private String observaciones;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructores
    public LaborInsumo() {}
    
    public LaborInsumo(Labor labor, Insumo insumo, BigDecimal cantidadUsada, 
                      BigDecimal cantidadPlanificada, BigDecimal costoUnitario, 
                      BigDecimal costoTotal, String observaciones) {
        this.labor = labor;
        this.insumo = insumo;
        this.cantidadUsada = cantidadUsada;
        this.cantidadPlanificada = cantidadPlanificada;
        this.costoUnitario = costoUnitario;
        this.costoTotal = costoTotal;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    public Long getIdLaborInsumo() { 
        return idLaborInsumo; 
    }
    
    public void setIdLaborInsumo(Long idLaborInsumo) { 
        this.idLaborInsumo = idLaborInsumo; 
    }
    
    public Labor getLabor() { 
        return labor; 
    }
    
    public void setLabor(Labor labor) { 
        this.labor = labor; 
    }
    
    public Insumo getInsumo() { 
        return insumo; 
    }
    
    public void setInsumo(Insumo insumo) { 
        this.insumo = insumo; 
    }
    
    public BigDecimal getCantidadUsada() { 
        return cantidadUsada; 
    }
    
    public void setCantidadUsada(BigDecimal cantidadUsada) { 
        this.cantidadUsada = cantidadUsada; 
    }
    
    public BigDecimal getCantidadPlanificada() { 
        return cantidadPlanificada; 
    }
    
    public void setCantidadPlanificada(BigDecimal cantidadPlanificada) { 
        this.cantidadPlanificada = cantidadPlanificada; 
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
