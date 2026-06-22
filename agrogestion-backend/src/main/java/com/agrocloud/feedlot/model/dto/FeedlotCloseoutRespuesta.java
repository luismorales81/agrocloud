package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotCloseoutRespuesta {

    private Long loteId;
    private String loteNombre;
    private LocalDate fechaIngreso;
    private LocalDate fechaReferencia;
    private Long diasEnFeedlot;
    private Integer cabezasInicial;
    private Integer cabezasActuales;
    private Integer totalMuertes;
    private BigDecimal mortalidadPct;
    private BigDecimal pesoIngresoKg;
    private BigDecimal pesoActualKg;
    private BigDecimal gmd;
    private BigDecimal kgGanados;
    private BigDecimal totalAlimentoKg;
    private BigDecimal totalAlimentoMsKg;
    private BigDecimal conversionAlimenticia;
    private BigDecimal headDays;
    private BigDecimal costoCompra;
    private BigDecimal costoAlimento;
    private BigDecimal costoHoteleria;
    private BigDecimal costoAcumulado;
    private BigDecimal ingresosVentas;
    private BigDecimal margen;
    private BigDecimal breakevenKg;
    private String metodoCloseout = "DEADS_IN";

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

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaReferencia() {
        return fechaReferencia;
    }

    public void setFechaReferencia(LocalDate fechaReferencia) {
        this.fechaReferencia = fechaReferencia;
    }

    public Long getDiasEnFeedlot() {
        return diasEnFeedlot;
    }

    public void setDiasEnFeedlot(Long diasEnFeedlot) {
        this.diasEnFeedlot = diasEnFeedlot;
    }

    public Integer getCabezasInicial() {
        return cabezasInicial;
    }

    public void setCabezasInicial(Integer cabezasInicial) {
        this.cabezasInicial = cabezasInicial;
    }

    public Integer getCabezasActuales() {
        return cabezasActuales;
    }

    public void setCabezasActuales(Integer cabezasActuales) {
        this.cabezasActuales = cabezasActuales;
    }

    public Integer getTotalMuertes() {
        return totalMuertes;
    }

    public void setTotalMuertes(Integer totalMuertes) {
        this.totalMuertes = totalMuertes;
    }

    public BigDecimal getMortalidadPct() {
        return mortalidadPct;
    }

    public void setMortalidadPct(BigDecimal mortalidadPct) {
        this.mortalidadPct = mortalidadPct;
    }

    public BigDecimal getPesoIngresoKg() {
        return pesoIngresoKg;
    }

    public void setPesoIngresoKg(BigDecimal pesoIngresoKg) {
        this.pesoIngresoKg = pesoIngresoKg;
    }

    public BigDecimal getPesoActualKg() {
        return pesoActualKg;
    }

    public void setPesoActualKg(BigDecimal pesoActualKg) {
        this.pesoActualKg = pesoActualKg;
    }

    public BigDecimal getGmd() {
        return gmd;
    }

    public void setGmd(BigDecimal gmd) {
        this.gmd = gmd;
    }

    public BigDecimal getKgGanados() {
        return kgGanados;
    }

    public void setKgGanados(BigDecimal kgGanados) {
        this.kgGanados = kgGanados;
    }

    public BigDecimal getTotalAlimentoKg() {
        return totalAlimentoKg;
    }

    public void setTotalAlimentoKg(BigDecimal totalAlimentoKg) {
        this.totalAlimentoKg = totalAlimentoKg;
    }

    public BigDecimal getTotalAlimentoMsKg() {
        return totalAlimentoMsKg;
    }

    public void setTotalAlimentoMsKg(BigDecimal totalAlimentoMsKg) {
        this.totalAlimentoMsKg = totalAlimentoMsKg;
    }

    public BigDecimal getConversionAlimenticia() {
        return conversionAlimenticia;
    }

    public void setConversionAlimenticia(BigDecimal conversionAlimenticia) {
        this.conversionAlimenticia = conversionAlimenticia;
    }

    public BigDecimal getHeadDays() {
        return headDays;
    }

    public void setHeadDays(BigDecimal headDays) {
        this.headDays = headDays;
    }

    public BigDecimal getCostoCompra() {
        return costoCompra;
    }

    public void setCostoCompra(BigDecimal costoCompra) {
        this.costoCompra = costoCompra;
    }

    public BigDecimal getCostoAlimento() {
        return costoAlimento;
    }

    public void setCostoAlimento(BigDecimal costoAlimento) {
        this.costoAlimento = costoAlimento;
    }

    public BigDecimal getCostoHoteleria() {
        return costoHoteleria;
    }

    public void setCostoHoteleria(BigDecimal costoHoteleria) {
        this.costoHoteleria = costoHoteleria;
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

    public BigDecimal getBreakevenKg() {
        return breakevenKg;
    }

    public void setBreakevenKg(BigDecimal breakevenKg) {
        this.breakevenKg = breakevenKg;
    }

    public String getMetodoCloseout() {
        return metodoCloseout;
    }

    public void setMetodoCloseout(String metodoCloseout) {
        this.metodoCloseout = metodoCloseout;
    }
}
