package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotBunkScore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeedlotLecturaComederoRespuesta {

    private Long id;
    private Long loteId;
    private LocalDate fecha;
    private FeedlotBunkScore bunkScore;
    private BigDecimal kgEntregados;
    private String observaciones;
    private BigDecimal temperaturaDia;
    private BigDecimal humedadDia;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

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

    public BigDecimal getTemperaturaDia() {
        return temperaturaDia;
    }

    public void setTemperaturaDia(BigDecimal temperaturaDia) {
        this.temperaturaDia = temperaturaDia;
    }

    public BigDecimal getHumedadDia() {
        return humedadDia;
    }

    public void setHumedadDia(BigDecimal humedadDia) {
        this.humedadDia = humedadDia;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
