package com.agrocloud.dto;

import java.math.BigDecimal;

public class InsumoUsadoDTO {
    
    private Long insumoId;
    private String insumoNombre;
    private BigDecimal cantidadUsada;
    private String unidadMedida;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    
    // Constructores
    public InsumoUsadoDTO() {}
    
    public InsumoUsadoDTO(Long insumoId, BigDecimal cantidadUsada) {
        this.insumoId = insumoId;
        this.cantidadUsada = cantidadUsada;
    }
    
    // Getters y Setters
    public Long getInsumoId() {
        return insumoId;
    }
    
    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }
    
    public String getInsumoNombre() {
        return insumoNombre;
    }
    
    public void setInsumoNombre(String insumoNombre) {
        this.insumoNombre = insumoNombre;
    }
    
    public BigDecimal getCantidadUsada() {
        return cantidadUsada;
    }
    
    public void setCantidadUsada(BigDecimal cantidadUsada) {
        this.cantidadUsada = cantidadUsada;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
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
}
