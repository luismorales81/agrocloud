package com.agrocloud.avicola.crianza.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvicolaCrianzaCurvaPesoRespuesta {

    private Long loteId;
    private String nombreLote;
    private LocalDate fechaIngreso;
    private BigDecimal pesoPromedioIngreso;
    private List<PuntoPesada> serie = new ArrayList<>();

    public static class PuntoPesada {
        private LocalDate fecha;
        private BigDecimal pesoPromedio;
        private Integer cantidadPesada;
        private Long diasDesdeIngreso;

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public BigDecimal getPesoPromedio() {
            return pesoPromedio;
        }

        public void setPesoPromedio(BigDecimal pesoPromedio) {
            this.pesoPromedio = pesoPromedio;
        }

        public Integer getCantidadPesada() {
            return cantidadPesada;
        }

        public void setCantidadPesada(Integer cantidadPesada) {
            this.cantidadPesada = cantidadPesada;
        }

        public Long getDiasDesdeIngreso() {
            return diasDesdeIngreso;
        }

        public void setDiasDesdeIngreso(Long diasDesdeIngreso) {
            this.diasDesdeIngreso = diasDesdeIngreso;
        }
    }

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

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public BigDecimal getPesoPromedioIngreso() {
        return pesoPromedioIngreso;
    }

    public void setPesoPromedioIngreso(BigDecimal pesoPromedioIngreso) {
        this.pesoPromedioIngreso = pesoPromedioIngreso;
    }

    public List<PuntoPesada> getSerie() {
        return serie;
    }

    public void setSerie(List<PuntoPesada> serie) {
        this.serie = serie;
    }
}
