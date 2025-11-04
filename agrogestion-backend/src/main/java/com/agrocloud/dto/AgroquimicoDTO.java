package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para agroquímico con sus dosis
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class AgroquimicoDTO {

    private Long id;
    private Long insumoId;
    private String nombreInsumo;
    private String principioActivo;
    private String concentracion;
    private String claseQuimica;
    private String categoriaToxicologica;
    private String modoAccion;
    private Integer periodoCarenciaDias;
    private BigDecimal dosisMinimaPorHa;
    private BigDecimal dosisMaximaPorHa;
    private String unidadDosis;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    // Lista de dosis eliminada - simplificada

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

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public String getClaseQuimica() {
        return claseQuimica;
    }

    public void setClaseQuimica(String claseQuimica) {
        this.claseQuimica = claseQuimica;
    }

    public String getCategoriaToxicologica() {
        return categoriaToxicologica;
    }

    public void setCategoriaToxicologica(String categoriaToxicologica) {
        this.categoriaToxicologica = categoriaToxicologica;
    }

    public String getModoAccion() {
        return modoAccion;
    }

    public void setModoAccion(String modoAccion) {
        this.modoAccion = modoAccion;
    }

    public Integer getPeriodoCarenciaDias() {
        return periodoCarenciaDias;
    }

    public void setPeriodoCarenciaDias(Integer periodoCarenciaDias) {
        this.periodoCarenciaDias = periodoCarenciaDias;
    }

    public BigDecimal getDosisMinimaPorHa() {
        return dosisMinimaPorHa;
    }

    public void setDosisMinimaPorHa(BigDecimal dosisMinimaPorHa) {
        this.dosisMinimaPorHa = dosisMinimaPorHa;
    }

    public BigDecimal getDosisMaximaPorHa() {
        return dosisMaximaPorHa;
    }

    public void setDosisMaximaPorHa(BigDecimal dosisMaximaPorHa) {
        this.dosisMaximaPorHa = dosisMaximaPorHa;
    }

    public String getUnidadDosis() {
        return unidadDosis;
    }

    public void setUnidadDosis(String unidadDosis) {
        this.unidadDosis = unidadDosis;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    // Métodos de dosis eliminados - simplificados
}


