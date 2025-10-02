package com.agrocloud.dto;

import java.math.BigDecimal;

public class ManoObraDTO {
    
    private String descripcion;
    private Integer cantidadPersonas;
    private String proveedor;
    private BigDecimal costoTotal;
    private BigDecimal horasTrabajo;
    private String observaciones;
    
    // Constructores
    public ManoObraDTO() {}
    
    public ManoObraDTO(String descripcion, Integer cantidadPersonas, BigDecimal costoTotal) {
        this.descripcion = descripcion;
        this.cantidadPersonas = cantidadPersonas;
        this.costoTotal = costoTotal;
    }
    
    // Getters y Setters
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Integer getCantidadPersonas() {
        return cantidadPersonas;
    }
    
    public void setCantidadPersonas(Integer cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }
    
    public String getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    
    public BigDecimal getCostoTotal() {
        return costoTotal;
    }
    
    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }
    
    public BigDecimal getHorasTrabajo() {
        return horasTrabajo;
    }
    
    public void setHorasTrabajo(BigDecimal horasTrabajo) {
        this.horasTrabajo = horasTrabajo;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
