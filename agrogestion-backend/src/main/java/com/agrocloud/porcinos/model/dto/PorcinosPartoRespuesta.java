package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosPartoRespuesta {

    private Long id;
    private Long gestacionId;
    private Long madreId;
    private String madreCaravana;
    private LocalDate fecha;
    private Integer nacidosVivos;
    private Integer nacidosMuertos;
    private Integer momificados;
    private BigDecimal temperaturaAmbiente;
    private BigDecimal humedadAmbiente;
    private String observaciones;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGestacionId() {
        return gestacionId;
    }

    public void setGestacionId(Long gestacionId) {
        this.gestacionId = gestacionId;
    }

    public Long getMadreId() {
        return madreId;
    }

    public void setMadreId(Long madreId) {
        this.madreId = madreId;
    }

    public String getMadreCaravana() {
        return madreCaravana;
    }

    public void setMadreCaravana(String madreCaravana) {
        this.madreCaravana = madreCaravana;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getNacidosVivos() {
        return nacidosVivos;
    }

    public void setNacidosVivos(Integer nacidosVivos) {
        this.nacidosVivos = nacidosVivos;
    }

    public Integer getNacidosMuertos() {
        return nacidosMuertos;
    }

    public void setNacidosMuertos(Integer nacidosMuertos) {
        this.nacidosMuertos = nacidosMuertos;
    }

    public Integer getMomificados() {
        return momificados;
    }

    public void setMomificados(Integer momificados) {
        this.momificados = momificados;
    }

    public BigDecimal getTemperaturaAmbiente() {
        return temperaturaAmbiente;
    }

    public void setTemperaturaAmbiente(BigDecimal temperaturaAmbiente) {
        this.temperaturaAmbiente = temperaturaAmbiente;
    }

    public BigDecimal getHumedadAmbiente() {
        return humedadAmbiente;
    }

    public void setHumedadAmbiente(BigDecimal humedadAmbiente) {
        this.humedadAmbiente = humedadAmbiente;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
