package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LaborManoObraDTO {
    private Long idLaborManoObra;
    private Long idLabor;
    private String descripcion;
    private Integer cantidadPersonas;
    private String proveedor;
    private BigDecimal costoTotal;
    private BigDecimal horasTrabajo;
    private BigDecimal costoPorHora;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructores
    public LaborManoObraDTO() {}
    
    public LaborManoObraDTO(Long idLaborManoObra, Long idLabor, String descripcion,
                          Integer cantidadPersonas, String proveedor, BigDecimal costoTotal,
                          BigDecimal horasTrabajo, BigDecimal costoPorHora, String observaciones,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idLaborManoObra = idLaborManoObra;
        this.idLabor = idLabor;
        this.descripcion = descripcion;
        this.cantidadPersonas = cantidadPersonas;
        this.proveedor = proveedor;
        this.costoTotal = costoTotal;
        this.horasTrabajo = horasTrabajo;
        this.costoPorHora = costoPorHora;
        this.observaciones = observaciones;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters y Setters
    public Long getIdLaborManoObra() { return idLaborManoObra; }
    public void setIdLaborManoObra(Long idLaborManoObra) { this.idLaborManoObra = idLaborManoObra; }
    
    public Long getIdLabor() { return idLabor; }
    public void setIdLabor(Long idLabor) { this.idLabor = idLabor; }
    
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
    
    public BigDecimal getCostoPorHora() { return costoPorHora; }
    public void setCostoPorHora(BigDecimal costoPorHora) { this.costoPorHora = costoPorHora; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
