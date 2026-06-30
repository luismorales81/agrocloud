package com.agrocloud.lecheria.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LecheriaVentaLecheSolicitud {

    private LocalDate fecha;
    private BigDecimal litros;
    private BigDecimal precioLitro;
    private String comprador;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getLitros() {
        return litros;
    }

    public void setLitros(BigDecimal litros) {
        this.litros = litros;
    }

    public BigDecimal getPrecioLitro() {
        return precioLitro;
    }

    public void setPrecioLitro(BigDecimal precioLitro) {
        this.precioLitro = precioLitro;
    }

    public String getComprador() {
        return comprador;
    }

    public void setComprador(String comprador) {
        this.comprador = comprador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
