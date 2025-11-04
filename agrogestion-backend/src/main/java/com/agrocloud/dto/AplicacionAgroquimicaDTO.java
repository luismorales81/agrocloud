package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para AplicacionAgroquimica
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class AplicacionAgroquimicaDTO {
    
    private Long id;
    
    @NotNull(message = "La labor es obligatoria")
    private Long laborId;
    
    private String laborNombre;
    
    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;
    
    private String insumoNombre;
    
    @NotNull(message = "El tipo de aplicación es obligatorio")
    private TipoAplicacion tipoAplicacion;
    
    @NotNull(message = "La cantidad total a aplicar es obligatoria")
    @Positive(message = "La cantidad total a aplicar debe ser un valor positivo")
    private BigDecimal cantidadTotalAplicar;
    
    private BigDecimal dosisAplicadaPorHa;
    
    private BigDecimal superficieAplicadaHa;
    
    private String unidadMedida;
    
    private String observaciones;
    
    private LocalDateTime fechaAplicacion;
    
    private LocalDateTime fechaRegistro;
    
    private Boolean activo;
    
    // Constructors
    public AplicacionAgroquimicaDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getLaborId() {
        return laborId;
    }
    
    public void setLaborId(Long laborId) {
        this.laborId = laborId;
    }
    
    public String getLaborNombre() {
        return laborNombre;
    }
    
    public void setLaborNombre(String laborNombre) {
        this.laborNombre = laborNombre;
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
    
    public BigDecimal getCantidadTotalAplicar() {
        return cantidadTotalAplicar;
    }
    
    public void setCantidadTotalAplicar(BigDecimal cantidadTotalAplicar) {
        this.cantidadTotalAplicar = cantidadTotalAplicar;
    }
    
    public BigDecimal getDosisAplicadaPorHa() {
        return dosisAplicadaPorHa;
    }
    
    public void setDosisAplicadaPorHa(BigDecimal dosisAplicadaPorHa) {
        this.dosisAplicadaPorHa = dosisAplicadaPorHa;
    }
    
    public BigDecimal getSuperficieAplicadaHa() {
        return superficieAplicadaHa;
    }
    
    public void setSuperficieAplicadaHa(BigDecimal superficieAplicadaHa) {
        this.superficieAplicadaHa = superficieAplicadaHa;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }
    
    public void setFechaAplicacion(LocalDateTime fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

