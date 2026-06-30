package com.agrocloud.lecheria.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LecheriaScoreCorporalSolicitud {

    private LocalDate fecha;
    private BigDecimal valor;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
