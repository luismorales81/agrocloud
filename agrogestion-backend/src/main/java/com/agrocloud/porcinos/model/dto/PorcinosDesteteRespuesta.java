package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosDesteteRespuesta {

    private Long id;
    private Long partoId;
    private LocalDate fecha;
    private Integer cantidadDestetados;
    private BigDecimal pesoPromedioKg;
    private Long loteId;
    private String loteNombre;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPartoId() {
        return partoId;
    }

    public void setPartoId(Long partoId) {
        this.partoId = partoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCantidadDestetados() {
        return cantidadDestetados;
    }

    public void setCantidadDestetados(Integer cantidadDestetados) {
        this.cantidadDestetados = cantidadDestetados;
    }

    public BigDecimal getPesoPromedioKg() {
        return pesoPromedioKg;
    }

    public void setPesoPromedioKg(BigDecimal pesoPromedioKg) {
        this.pesoPromedioKg = pesoPromedioKg;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public String getLoteNombre() {
        return loteNombre;
    }

    public void setLoteNombre(String loteNombre) {
        this.loteNombre = loteNombre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
