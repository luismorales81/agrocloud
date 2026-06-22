package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotBunkScore;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotLecturaComederoSolicitud {

    private LocalDate fecha;
    private FeedlotBunkScore bunkScore;
    private BigDecimal kgEntregados;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public FeedlotBunkScore getBunkScore() {
        return bunkScore;
    }

    public void setBunkScore(FeedlotBunkScore bunkScore) {
        this.bunkScore = bunkScore;
    }

    public BigDecimal getKgEntregados() {
        return kgEntregados;
    }

    public void setKgEntregados(BigDecimal kgEntregados) {
        this.kgEntregados = kgEntregados;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
