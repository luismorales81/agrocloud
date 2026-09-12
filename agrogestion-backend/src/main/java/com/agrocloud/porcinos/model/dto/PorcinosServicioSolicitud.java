package com.agrocloud.porcinos.model.dto;

import java.time.LocalDate;

public class PorcinosServicioSolicitud {

    private Long padrilloId;
    private Long tipoServicioId;
    private LocalDate fecha;
    private String observaciones;

    public Long getPadrilloId() {
        return padrilloId;
    }

    public void setPadrilloId(Long padrilloId) {
        this.padrilloId = padrilloId;
    }

    public Long getTipoServicioId() {
        return tipoServicioId;
    }

    public void setTipoServicioId(Long tipoServicioId) {
        this.tipoServicioId = tipoServicioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
