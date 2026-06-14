package com.agrocloud.avicola.crianza.model.dto;

import java.math.BigDecimal;

/**
 * KPIs agregados del lote (crianza).
 */
public class AvicolaCrianzaResumenRespuesta {

    private Long loteId;
    private Integer cantidadAnimalesRegistrada;
    private Long sumaMuertes;
    private Integer cantidadDisponible;
    private BigDecimal mortalidadPorcentaje;
    private BigDecimal sumaConsumos;
    private BigDecimal pesoPromedioReferencia;
    private BigDecimal conversionAlimenticia;
    private Long diasEnProduccion;

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Integer getCantidadAnimalesRegistrada() {
        return cantidadAnimalesRegistrada;
    }

    public void setCantidadAnimalesRegistrada(Integer cantidadAnimalesRegistrada) {
        this.cantidadAnimalesRegistrada = cantidadAnimalesRegistrada;
    }

    public Long getSumaMuertes() {
        return sumaMuertes;
    }

    public void setSumaMuertes(Long sumaMuertes) {
        this.sumaMuertes = sumaMuertes;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public BigDecimal getMortalidadPorcentaje() {
        return mortalidadPorcentaje;
    }

    public void setMortalidadPorcentaje(BigDecimal mortalidadPorcentaje) {
        this.mortalidadPorcentaje = mortalidadPorcentaje;
    }

    public BigDecimal getSumaConsumos() {
        return sumaConsumos;
    }

    public void setSumaConsumos(BigDecimal sumaConsumos) {
        this.sumaConsumos = sumaConsumos;
    }

    public BigDecimal getPesoPromedioReferencia() {
        return pesoPromedioReferencia;
    }

    public void setPesoPromedioReferencia(BigDecimal pesoPromedioReferencia) {
        this.pesoPromedioReferencia = pesoPromedioReferencia;
    }

    public BigDecimal getConversionAlimenticia() {
        return conversionAlimenticia;
    }

    public void setConversionAlimenticia(BigDecimal conversionAlimenticia) {
        this.conversionAlimenticia = conversionAlimenticia;
    }

    public Long getDiasEnProduccion() {
        return diasEnProduccion;
    }

    public void setDiasEnProduccion(Long diasEnProduccion) {
        this.diasEnProduccion = diasEnProduccion;
    }
}
