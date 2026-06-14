package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasConsumoTipo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvicolaPonedorasConsumoSolicitud {

    private Long insumoId;
    private LocalDate fecha;
    private BigDecimal cantidad;
    private AvicolaPonedorasConsumoTipo tipo;
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

    public AvicolaPonedorasConsumoTipo getTipo() {
        return tipo;
    }

    public void setTipo(AvicolaPonedorasConsumoTipo tipo) {
        this.tipo = tipo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
