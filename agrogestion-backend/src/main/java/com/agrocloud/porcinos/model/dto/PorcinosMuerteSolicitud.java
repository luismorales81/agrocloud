package com.agrocloud.porcinos.model.dto;

import java.time.LocalDate;

public class PorcinosMuerteSolicitud {

    private LocalDate fecha;
    private Integer cabezas;
    private Long causaMortalidadId;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCabezas() {
        return cabezas;
    }

    public void setCabezas(Integer cabezas) {
        this.cabezas = cabezas;
    }

    public Long getCausaMortalidadId() {
        return causaMortalidadId;
    }

    public void setCausaMortalidadId(Long causaMortalidadId) {
        this.causaMortalidadId = causaMortalidadId;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
