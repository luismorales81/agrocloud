package com.agrocloud.avicola.carne.model.dto;

import java.math.BigDecimal;

/**
 * Indicadores de resumen para un lote avícola-carne (fórmulas de negocio del módulo).
 */
public class AvicolaCarneResumenRespuesta {

    private Long loteId;
    /** cantidadAnimales − suma(muertes). */
    private Integer cantidadDisponible;
    /** suma(muertes) / cantidadInicial × 100. */
    private BigDecimal mortalidadPct;
    /** totalConsumoKg / (pesoPromedioActual × cantidadDisponible). */
    private BigDecimal conversionAlimenticia;
    /** Días entre fecha de ingreso y hoy. */
    private long diasEnProduccion;

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public BigDecimal getMortalidadPct() {
        return mortalidadPct;
    }

    public void setMortalidadPct(BigDecimal mortalidadPct) {
        this.mortalidadPct = mortalidadPct;
    }

    public BigDecimal getConversionAlimenticia() {
        return conversionAlimenticia;
    }

    public void setConversionAlimenticia(BigDecimal conversionAlimenticia) {
        this.conversionAlimenticia = conversionAlimenticia;
    }

    public long getDiasEnProduccion() {
        return diasEnProduccion;
    }

    public void setDiasEnProduccion(long diasEnProduccion) {
        this.diasEnProduccion = diasEnProduccion;
    }
}
