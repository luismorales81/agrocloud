package com.agrocloud.avicola.ponedoras.model.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Indicadores agregados de un galpón de ponedoras (postura, mortalidad, huevos).
 */
public class AvicolaPonedorasResumenRespuesta {

    private Long galponId;
    private Integer cantidadDisponible;
    private BigDecimal mortalidadPct;
    private Long totalHuevos;
    private long diasEnProduccion;
    private BigDecimal porcentajePostura;
    private BigDecimal huevosPorAvePorDia;
    private Map<String, Long> distribucionCategorias = new LinkedHashMap<>();

    public Long getGalponId() {
        return galponId;
    }

    public void setGalponId(Long galponId) {
        this.galponId = galponId;
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

    public Long getTotalHuevos() {
        return totalHuevos;
    }

    public void setTotalHuevos(Long totalHuevos) {
        this.totalHuevos = totalHuevos;
    }

    public long getDiasEnProduccion() {
        return diasEnProduccion;
    }

    public void setDiasEnProduccion(long diasEnProduccion) {
        this.diasEnProduccion = diasEnProduccion;
    }

    public BigDecimal getPorcentajePostura() {
        return porcentajePostura;
    }

    public void setPorcentajePostura(BigDecimal porcentajePostura) {
        this.porcentajePostura = porcentajePostura;
    }

    public BigDecimal getHuevosPorAvePorDia() {
        return huevosPorAvePorDia;
    }

    public void setHuevosPorAvePorDia(BigDecimal huevosPorAvePorDia) {
        this.huevosPorAvePorDia = huevosPorAvePorDia;
    }

    public Map<String, Long> getDistribucionCategorias() {
        return distribucionCategorias;
    }

    public void setDistribucionCategorias(Map<String, Long> distribucionCategorias) {
        this.distribucionCategorias = distribucionCategorias != null ? distribucionCategorias : new LinkedHashMap<>();
    }
}
