package com.agrocloud.avicola.crianza.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para registrar un consumo de insumo.
 */
public class AvicolaConsumoSolicitud {

    private Long insumoId;
    private LocalDate fecha;
    private BigDecimal cantidad;
    private String tipo;
    private String observaciones;

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
