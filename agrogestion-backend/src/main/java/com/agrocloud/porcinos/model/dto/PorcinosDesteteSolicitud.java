package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PorcinosDesteteSolicitud {

    private LocalDate fecha;
    private Integer cantidadDestetados;
    private BigDecimal pesoPromedioKg;
    private Long galponId;
    private String loteNombre;

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

    public Long getGalponId() {
        return galponId;
    }

    public void setGalponId(Long galponId) {
        this.galponId = galponId;
    }

    public String getLoteNombre() {
        return loteNombre;
    }

    public void setLoteNombre(String loteNombre) {
        this.loteNombre = loteNombre;
    }
}
