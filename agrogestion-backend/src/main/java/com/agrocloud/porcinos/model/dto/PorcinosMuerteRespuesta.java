package com.agrocloud.porcinos.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosMuerteRespuesta {

    private Long id;
    private Long loteId;
    private Long empresaId;
    private LocalDate fecha;
    private Integer cabezas;
    private Long causaMortalidadId;
    private String causaMortalidadNombre;
    private String observaciones;
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

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

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

    public String getCausaMortalidadNombre() {
        return causaMortalidadNombre;
    }

    public void setCausaMortalidadNombre(String causaMortalidadNombre) {
        this.causaMortalidadNombre = causaMortalidadNombre;
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
