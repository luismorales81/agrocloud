package com.agrocloud.avicola.huevos.model.dto;

import java.math.BigDecimal;

/**
 * KPIs agregados del lote de postura (huevos).
 */
public class AvicolaHuevosResumenRespuesta {

    private Long loteId;
    private Integer cantidadAvesActual;
    private Long totalHuevosProducidos;
    private BigDecimal sumaConsumosInsumo;
    private Long diasEnPostura;
    private BigDecimal huevosPromedioPorAveYdia;

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Integer getCantidadAvesActual() {
        return cantidadAvesActual;
    }

    public void setCantidadAvesActual(Integer cantidadAvesActual) {
        this.cantidadAvesActual = cantidadAvesActual;
    }

    public Long getTotalHuevosProducidos() {
        return totalHuevosProducidos;
    }

    public void setTotalHuevosProducidos(Long totalHuevosProducidos) {
        this.totalHuevosProducidos = totalHuevosProducidos;
    }

    public BigDecimal getSumaConsumosInsumo() {
        return sumaConsumosInsumo;
    }

    public void setSumaConsumosInsumo(BigDecimal sumaConsumosInsumo) {
        this.sumaConsumosInsumo = sumaConsumosInsumo;
    }

    public Long getDiasEnPostura() {
        return diasEnPostura;
    }

    public void setDiasEnPostura(Long diasEnPostura) {
        this.diasEnPostura = diasEnPostura;
    }

    public BigDecimal getHuevosPromedioPorAveYdia() {
        return huevosPromedioPorAveYdia;
    }

    public void setHuevosPromedioPorAveYdia(BigDecimal huevosPromedioPorAveYdia) {
        this.huevosPromedioPorAveYdia = huevosPromedioPorAveYdia;
    }
}
