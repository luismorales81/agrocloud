package com.agrocloud.avicola.crianza.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para registrar una pesada.
 */
public class AvicolaPesadaSolicitud {

    private LocalDate fecha;
    private BigDecimal pesoPromedio;
    private Integer cantidadPesada;
    private String observaciones;
    private BigDecimal temperaturaAmbiente;
    private BigDecimal humedadAmbiente;

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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getTemperaturaAmbiente() {
        return temperaturaAmbiente;
    }

    public void setTemperaturaAmbiente(BigDecimal temperaturaAmbiente) {
        this.temperaturaAmbiente = temperaturaAmbiente;
    }

    public BigDecimal getHumedadAmbiente() {
        return humedadAmbiente;
    }

    public void setHumedadAmbiente(BigDecimal humedadAmbiente) {
        this.humedadAmbiente = humedadAmbiente;
    }
}
