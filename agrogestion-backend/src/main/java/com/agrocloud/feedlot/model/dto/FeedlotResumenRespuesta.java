package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;

public class FeedlotResumenRespuesta {

    private Long loteId;
    private Integer cabezasActuales;
    private Integer cabezasInicial;
    private Long diasEnFeedlot;
    private BigDecimal pesoActualKg;
    private BigDecimal gmd;
    private BigDecimal mortalidadPct;
    private BigDecimal totalAlimentoKg;
    private BigDecimal conversionAlimenticia;
    private BigDecimal consumoCabDia;

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Integer getCabezasActuales() {
        return cabezasActuales;
    }

    public void setCabezasActuales(Integer cabezasActuales) {
        this.cabezasActuales = cabezasActuales;
    }

    public Integer getCabezasInicial() {
        return cabezasInicial;
    }

    public void setCabezasInicial(Integer cabezasInicial) {
        this.cabezasInicial = cabezasInicial;
    }

    public Long getDiasEnFeedlot() {
        return diasEnFeedlot;
    }

    public void setDiasEnFeedlot(Long diasEnFeedlot) {
        this.diasEnFeedlot = diasEnFeedlot;
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

    public BigDecimal getMortalidadPct() {
        return mortalidadPct;
    }

    public void setMortalidadPct(BigDecimal mortalidadPct) {
        this.mortalidadPct = mortalidadPct;
    }

    public BigDecimal getTotalAlimentoKg() {
        return totalAlimentoKg;
    }

    public void setTotalAlimentoKg(BigDecimal totalAlimentoKg) {
        this.totalAlimentoKg = totalAlimentoKg;
    }

    public BigDecimal getConversionAlimenticia() {
        return conversionAlimenticia;
    }

    public void setConversionAlimenticia(BigDecimal conversionAlimenticia) {
        this.conversionAlimenticia = conversionAlimenticia;
    }

    public BigDecimal getConsumoCabDia() {
        return consumoCabDia;
    }

    public void setConsumoCabDia(BigDecimal consumoCabDia) {
        this.consumoCabDia = consumoCabDia;
    }
}
