package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para DosisAplicacion
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class DosisAplicacionDTO {
    
    private Long id;
    
    private Long insumoId;
    
    private String insumoNombre;
    
    @NotNull(message = "El tipo de aplicación es obligatorio")
    private TipoAplicacion tipoAplicacion;
    
    @NotNull(message = "La dosis por hectárea es obligatoria")
    @Positive(message = "La dosis por hectárea debe ser un valor positivo")
    private BigDecimal dosisPorHa;
    
    private String unidadMedida;
    
    private String descripcion;
    
    private Boolean activo;
    
    // Constructors
    public DosisAplicacionDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }
    
    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }
    
    public BigDecimal getDosisPorHa() {
        return dosisPorHa;
    }
    
    public void setDosisPorHa(BigDecimal dosisPorHa) {
        this.dosisPorHa = dosisPorHa;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

