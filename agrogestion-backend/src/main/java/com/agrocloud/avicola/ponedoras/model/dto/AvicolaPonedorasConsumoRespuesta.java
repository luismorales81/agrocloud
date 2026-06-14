package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasConsumoTipo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvicolaPonedorasConsumoRespuesta {

    private Long id;
    private Long galponId;
    private Long empresaId;
    private Long insumoId;
    private LocalDate fecha;
    private BigDecimal cantidad;
    private AvicolaPonedorasConsumoTipo tipo;
    private String observaciones;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGalponId() {
        return galponId;
    }

    public void setGalponId(Long galponId) {
        this.galponId = galponId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public AvicolaPonedorasConsumoTipo getTipo() {
        return tipo;
    }

    public void setTipo(AvicolaPonedorasConsumoTipo tipo) {
        this.tipo = tipo;
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
