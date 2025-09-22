package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "labor_mano_obra")
public class LaborManoObra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_labor_mano_obra")
    private Long idLaborManoObra;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_labor", nullable = false)
    @JsonIgnore
    private Labor labor;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;
    
    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "La cantidad de personas debe ser mayor a 0")
    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;
    
    @Size(max = 255, message = "El proveedor no puede exceder 255 caracteres")
    @Column(name = "proveedor", length = 255)
    private String proveedor;
    
    @NotNull(message = "El costo total es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo total debe ser mayor o igual a 0")
    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;
    
    @DecimalMin(value = "0.0", message = "Las horas de trabajo deben ser mayor o igual a 0")
    @Column(name = "horas_trabajo", precision = 8, scale = 2)
    private BigDecimal horasTrabajo;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
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
    public LaborManoObra() {}
    
    public LaborManoObra(Labor labor, String descripcion, Integer cantidadPersonas,
                        String proveedor, BigDecimal costoTotal, BigDecimal horasTrabajo,
                        String observaciones) {
        this.labor = labor;
        this.descripcion = descripcion;
        this.cantidadPersonas = cantidadPersonas;
        this.proveedor = proveedor;
        this.costoTotal = costoTotal;
        this.horasTrabajo = horasTrabajo;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    public Long getIdLaborManoObra() { return idLaborManoObra; }
    public void setIdLaborManoObra(Long idLaborManoObra) { this.idLaborManoObra = idLaborManoObra; }
    
    public Labor getLabor() { return labor; }
    public void setLabor(Labor labor) { this.labor = labor; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public Integer getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(Integer cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    
    public BigDecimal getHorasTrabajo() { return horasTrabajo; }
    public void setHorasTrabajo(BigDecimal horasTrabajo) { this.horasTrabajo = horasTrabajo; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
