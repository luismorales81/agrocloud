package com.agrocloud.dto;

import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.model.enums.TipoAplicacion;
import com.agrocloud.model.enums.UnidadDosis;

import java.time.LocalDateTime;

public class DosisAgroquimicoResponse {
    private Long id;
    private TipoAplicacion tipoAplicacion;
    private FormaAplicacion formaAplicacion;
    private UnidadDosis unidad;
    private Double dosisRecomendadaPorHa;
    private Long insumoId;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }

    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }

    public FormaAplicacion getFormaAplicacion() {
        return formaAplicacion;
    }

    public void setFormaAplicacion(FormaAplicacion formaAplicacion) {
        this.formaAplicacion = formaAplicacion;
    }

    public UnidadDosis getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadDosis unidad) {
        this.unidad = unidad;
    }

    public Double getDosisRecomendadaPorHa() {
        return dosisRecomendadaPorHa;
    }

    public void setDosisRecomendadaPorHa(Double dosisRecomendadaPorHa) {
        this.dosisRecomendadaPorHa = dosisRecomendadaPorHa;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}

