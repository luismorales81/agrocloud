package com.agrocloud.porcinos.model.dto;

import com.agrocloud.porcinos.model.enums.PorcinosEventoSanitarioTipo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosEventoSanitarioRespuesta {

    private Long id;
    private Long loteId;
    private String loteNombre;
    private Long empresaId;
    private LocalDate fecha;
    private PorcinosEventoSanitarioTipo tipo;
    private String descripcion;
    private Long insumoId;
    private Integer diasRetiro;
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

    public String getLoteNombre() {
        return loteNombre;
    }

    public void setLoteNombre(String loteNombre) {
        this.loteNombre = loteNombre;
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

    public PorcinosEventoSanitarioTipo getTipo() {
        return tipo;
    }

    public void setTipo(PorcinosEventoSanitarioTipo tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public Integer getDiasRetiro() {
        return diasRetiro;
    }

    public void setDiasRetiro(Integer diasRetiro) {
        this.diasRetiro = diasRetiro;
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
