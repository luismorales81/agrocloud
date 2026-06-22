package com.agrocloud.feedlot.model.dto;

import java.time.LocalDate;

public class FeedlotAjustePlantelSolicitud {

    private LocalDate fecha;
    private Integer cabezasDespues;
    private String motivo;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCabezasDespues() {
        return cabezasDespues;
    }

    public void setCabezasDespues(Integer cabezasDespues) {
        this.cabezasDespues = cabezasDespues;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
