package com.agrocloud.porcinos.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PorcinosDietaRespuesta {

    private Long id;
    private Long empresaId;
    private String nombre;
    private Boolean activo;
    private List<PorcinosDietaFaseRespuesta> fases = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<PorcinosDietaFaseRespuesta> getFases() {
        return fases;
    }

    public void setFases(List<PorcinosDietaFaseRespuesta> fases) {
        this.fases = fases;
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
