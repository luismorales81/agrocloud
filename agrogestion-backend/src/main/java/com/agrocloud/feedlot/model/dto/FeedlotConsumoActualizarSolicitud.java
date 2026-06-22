package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotConsumoActualizarSolicitud {

    private LocalDate fecha;
    private BigDecimal cantidadKg;
    private BigDecimal materiaSecaPct;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCantidadKg() {
        return cantidadKg;
    }

    public void setCantidadKg(BigDecimal cantidadKg) {
        this.cantidadKg = cantidadKg;
    }

    public BigDecimal getMateriaSecaPct() {
        return materiaSecaPct;
    }

    public void setMateriaSecaPct(BigDecimal materiaSecaPct) {
        this.materiaSecaPct = materiaSecaPct;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
