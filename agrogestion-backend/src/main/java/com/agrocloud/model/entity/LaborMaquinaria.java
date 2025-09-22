package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.agrocloud.model.enums.TipoMaquinaria;

@Entity
@Table(name = "labor_maquinaria")
public class LaborMaquinaria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_labor_maquinaria")
    private Long idLaborMaquinaria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_labor", nullable = false)
    @JsonIgnore
    private Labor labor;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_maquinaria", nullable = false)
    private TipoMaquinaria tipoMaquinaria;
    
    @Size(max = 255, message = "El proveedor no puede exceder 255 caracteres")
    @Column(name = "proveedor", length = 255)
    private String proveedor;
    
    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo debe ser mayor o igual a 0")
    @Column(name = "costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;
    
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
    public LaborMaquinaria() {}
    
    public LaborMaquinaria(Labor labor, String descripcion, TipoMaquinaria tipoMaquinaria,
                          String proveedor, BigDecimal costo, String observaciones) {
        this.labor = labor;
        this.descripcion = descripcion;
        this.tipoMaquinaria = tipoMaquinaria;
        this.proveedor = proveedor;
        this.costo = costo;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    public Long getIdLaborMaquinaria() { return idLaborMaquinaria; }
    public void setIdLaborMaquinaria(Long idLaborMaquinaria) { this.idLaborMaquinaria = idLaborMaquinaria; }
    
    public Labor getLabor() { return labor; }
    public void setLabor(Labor labor) { this.labor = labor; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public TipoMaquinaria getTipoMaquinaria() { return tipoMaquinaria; }
    public void setTipoMaquinaria(TipoMaquinaria tipoMaquinaria) { this.tipoMaquinaria = tipoMaquinaria; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
