package com.agrocloud.avicola.huevos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos para gráficos de postura, edad del plantel, clima diario y costos estimados por consumos.
 */
public class AvicolaHuevosReporteAnalisisRespuesta {

    private Long loteId;
    private String nombreLote;
    private LocalDate fechaInicioLote;
    private Integer cantidadAvesActual;
    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal totalCostoConsumosRango = BigDecimal.ZERO;

    private List<PuntoSerieDiaria> seriePostura = new ArrayList<>();
    private List<PuntoGastoDia> serieGastos = new ArrayList<>();

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

    public LocalDate getFechaInicioLote() {
        return fechaInicioLote;
    }

    public void setFechaInicioLote(LocalDate fechaInicioLote) {
        this.fechaInicioLote = fechaInicioLote;
    }

    public Integer getCantidadAvesActual() {
        return cantidadAvesActual;
    }

    public void setCantidadAvesActual(Integer cantidadAvesActual) {
        this.cantidadAvesActual = cantidadAvesActual;
    }

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

    public BigDecimal getTotalCostoConsumosRango() {
        return totalCostoConsumosRango;
    }

    public void setTotalCostoConsumosRango(BigDecimal totalCostoConsumosRango) {
        this.totalCostoConsumosRango = totalCostoConsumosRango;
    }

    public List<PuntoSerieDiaria> getSeriePostura() {
        return seriePostura;
    }

    public void setSeriePostura(List<PuntoSerieDiaria> seriePostura) {
        this.seriePostura = seriePostura;
    }

    public List<PuntoGastoDia> getSerieGastos() {
        return serieGastos;
    }

    public void setSerieGastos(List<PuntoGastoDia> serieGastos) {
        this.serieGastos = serieGastos;
    }

    public static class PuntoSerieDiaria {
        private LocalDate fecha;
        private Integer totalHuevos;
        private BigDecimal temperatura;
        private BigDecimal humedad;
        private Long diasEdadLote;
        private BigDecimal huevosPorAve;

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public Integer getTotalHuevos() {
            return totalHuevos;
        }

        public void setTotalHuevos(Integer totalHuevos) {
            this.totalHuevos = totalHuevos;
        }

        public BigDecimal getTemperatura() {
            return temperatura;
        }

        public void setTemperatura(BigDecimal temperatura) {
            this.temperatura = temperatura;
        }

        public BigDecimal getHumedad() {
            return humedad;
        }

        public void setHumedad(BigDecimal humedad) {
            this.humedad = humedad;
        }

        public Long getDiasEdadLote() {
            return diasEdadLote;
        }

        public void setDiasEdadLote(Long diasEdadLote) {
            this.diasEdadLote = diasEdadLote;
        }

        public BigDecimal getHuevosPorAve() {
            return huevosPorAve;
        }

        public void setHuevosPorAve(BigDecimal huevosPorAve) {
            this.huevosPorAve = huevosPorAve;
        }
    }

    public static class PuntoGastoDia {
        private LocalDate fecha;
        private BigDecimal costoEstimado;

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public BigDecimal getCostoEstimado() {
            return costoEstimado;
        }

        public void setCostoEstimado(BigDecimal costoEstimado) {
            this.costoEstimado = costoEstimado;
        }
    }
}
