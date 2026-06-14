package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasDescarteMotivo;

import java.time.LocalDate;

public class AvicolaPonedorasDescarteAvesSolicitud {

    private LocalDate fecha;
    private Integer cantidad;
    private AvicolaPonedorasDescarteMotivo motivo;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public AvicolaPonedorasDescarteMotivo getMotivo() {
        return motivo;
    }

    public void setMotivo(AvicolaPonedorasDescarteMotivo motivo) {
        this.motivo = motivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
