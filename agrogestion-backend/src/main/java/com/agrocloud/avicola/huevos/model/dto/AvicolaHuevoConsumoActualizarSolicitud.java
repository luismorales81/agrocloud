package com.agrocloud.avicola.huevos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvicolaHuevoConsumoActualizarSolicitud {

    private LocalDate fecha;

    private BigDecimal cantidad;
    private String observaciones;

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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
