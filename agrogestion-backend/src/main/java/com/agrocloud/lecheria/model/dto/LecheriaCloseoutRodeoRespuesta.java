package com.agrocloud.lecheria.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LecheriaCloseoutRodeoRespuesta {

    private Long id;
    private Long rodeoId;
    private String rodeoNombre;
    private Long campanaId;
    private LocalDate fechaCierre;
    private BigDecimal litrosTotales;
    private BigDecimal costoAlimentacion;
    private BigDecimal ingresosLeche;
    private BigDecimal margen;
    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRodeoId() {
        return rodeoId;
    }

    public void setRodeoId(Long rodeoId) {
        this.rodeoId = rodeoId;
    }

    public String getRodeoNombre() {
        return rodeoNombre;
    }

    public void setRodeoNombre(String rodeoNombre) {
        this.rodeoNombre = rodeoNombre;
    }

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getLitrosTotales() {
        return litrosTotales;
    }

    public void setLitrosTotales(BigDecimal litrosTotales) {
        this.litrosTotales = litrosTotales;
    }

    public BigDecimal getCostoAlimentacion() {
        return costoAlimentacion;
    }

    public void setCostoAlimentacion(BigDecimal costoAlimentacion) {
        this.costoAlimentacion = costoAlimentacion;
    }

    public BigDecimal getIngresosLeche() {
        return ingresosLeche;
    }

    public void setIngresosLeche(BigDecimal ingresosLeche) {
        this.ingresosLeche = ingresosLeche;
    }

    public BigDecimal getMargen() {
        return margen;
    }

    public void setMargen(BigDecimal margen) {
        this.margen = margen;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
