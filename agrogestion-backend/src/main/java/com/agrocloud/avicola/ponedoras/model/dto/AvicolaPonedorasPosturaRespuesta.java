package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasHuevoCategoria;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvicolaPonedorasPosturaRespuesta {

    private Long id;
    private Long galponId;
    private Long empresaId;
    private LocalDate fecha;
    private AvicolaPonedorasHuevoCategoria categoriaHuevo;
    private Integer cantidad;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public AvicolaPonedorasHuevoCategoria getCategoriaHuevo() {
        return categoriaHuevo;
    }

    public void setCategoriaHuevo(AvicolaPonedorasHuevoCategoria categoriaHuevo) {
        this.categoriaHuevo = categoriaHuevo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
