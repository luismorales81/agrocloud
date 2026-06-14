package com.agrocloud.avicola.huevos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reporte agregado de postura y consumos por empresa en un rango de fechas.
 */
public class AvicolaHuevosReporteResumenRespuesta {

    private LocalDate desde;
    private LocalDate hasta;
    private Long totalHuevosRegistrados;
    private List<FilaResumenLoteHuevos> porLote = new ArrayList<>();

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public Long getTotalHuevosRegistrados() {
        return totalHuevosRegistrados;
    }

    public void setTotalHuevosRegistrados(Long totalHuevosRegistrados) {
        this.totalHuevosRegistrados = totalHuevosRegistrados;
    }

    public List<FilaResumenLoteHuevos> getPorLote() {
        return porLote;
    }

    public void setPorLote(List<FilaResumenLoteHuevos> porLote) {
        this.porLote = porLote;
    }

    public static class FilaResumenLoteHuevos {
        private Long loteId;
        private String nombreLote;
        private Long totalHuevos;
        private BigDecimal consumoInsumoEnRango;
        private Double temperaturaPromedio;
        private Double humedadPromedio;

        public Long getLoteId() {
            return loteId;
        }

        public void setLoteId(Long loteId) {
            this.loteId = loteId;
        }

        public String getNombreLote() {
            return nombreLote;
        }

        public void setNombreLote(String nombreLote) {
            this.nombreLote = nombreLote;
        }

        public Long getTotalHuevos() {
            return totalHuevos;
        }

        public void setTotalHuevos(Long totalHuevos) {
            this.totalHuevos = totalHuevos;
        }

        public BigDecimal getConsumoInsumoEnRango() {
            return consumoInsumoEnRango;
        }

        public void setConsumoInsumoEnRango(BigDecimal consumoInsumoEnRango) {
            this.consumoInsumoEnRango = consumoInsumoEnRango;
        }

        public Double getTemperaturaPromedio() {
            return temperaturaPromedio;
        }

        public void setTemperaturaPromedio(Double temperaturaPromedio) {
            this.temperaturaPromedio = temperaturaPromedio;
        }

        public Double getHumedadPromedio() {
            return humedadPromedio;
        }

        public void setHumedadPromedio(Double humedadPromedio) {
            this.humedadPromedio = humedadPromedio;
        }
    }
}
