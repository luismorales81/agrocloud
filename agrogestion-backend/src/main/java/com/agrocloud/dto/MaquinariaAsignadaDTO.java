package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoMaquinaria;
import java.math.BigDecimal;

public class MaquinariaAsignadaDTO {
    
    private Long maquinariaId;
    private String descripcion;
    private String proveedor; // null si es propia, nombre del proveedor si es alquilada
    private TipoMaquinaria tipoMaquinaria;
    private BigDecimal horasUso;
    private BigDecimal costoTotal;
    private String observaciones;
    
    // Constructores
    public MaquinariaAsignadaDTO() {}
    
    public MaquinariaAsignadaDTO(String descripcion, String proveedor, BigDecimal costoTotal) {
        this.descripcion = descripcion;
        this.proveedor = proveedor;
        this.costoTotal = costoTotal;
    }
    
    // Getters y Setters
    public Long getMaquinariaId() {
        return maquinariaId;
    }
    
    public void setMaquinariaId(Long maquinariaId) {
        this.maquinariaId = maquinariaId;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    
    public TipoMaquinaria getTipoMaquinaria() {
        return tipoMaquinaria;
    }
    
    public void setTipoMaquinaria(TipoMaquinaria tipoMaquinaria) {
        this.tipoMaquinaria = tipoMaquinaria;
    }
    
    public BigDecimal getHorasUso() {
        return horasUso;
    }
    
    public void setHorasUso(BigDecimal horasUso) {
        this.horasUso = horasUso;
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
}
