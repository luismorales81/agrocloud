package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;

public class PorcinosResumenEconomicoRespuesta {

    private Long loteId;
    private String loteNombre;
    private BigDecimal costoAlimento;
    private BigDecimal costoSanidad;
    private BigDecimal costoAcumulado;
    private BigDecimal ingresosVentas;
    private BigDecimal margen;
    private BigDecimal totalAlimentoKg;

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public String getLoteNombre() {
        return loteNombre;
    }

    public void setLoteNombre(String loteNombre) {
        this.loteNombre = loteNombre;
    }

    public BigDecimal getCostoAlimento() {
        return costoAlimento;
    }

    public void setCostoAlimento(BigDecimal costoAlimento) {
        this.costoAlimento = costoAlimento;
    }

    public BigDecimal getCostoSanidad() {
        return costoSanidad;
    }

    public void setCostoSanidad(BigDecimal costoSanidad) {
        this.costoSanidad = costoSanidad;
    }

    public BigDecimal getCostoAcumulado() {
        return costoAcumulado;
    }

    public void setCostoAcumulado(BigDecimal costoAcumulado) {
        this.costoAcumulado = costoAcumulado;
    }

    public BigDecimal getIngresosVentas() {
        return ingresosVentas;
    }

    public void setIngresosVentas(BigDecimal ingresosVentas) {
        this.ingresosVentas = ingresosVentas;
    }

    public BigDecimal getMargen() {
        return margen;
    }

    public void setMargen(BigDecimal margen) {
        this.margen = margen;
    }

    public BigDecimal getTotalAlimentoKg() {
        return totalAlimentoKg;
    }

    public void setTotalAlimentoKg(BigDecimal totalAlimentoKg) {
        this.totalAlimentoKg = totalAlimentoKg;
    }
}
