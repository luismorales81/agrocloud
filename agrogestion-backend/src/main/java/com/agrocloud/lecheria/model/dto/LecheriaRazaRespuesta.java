package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaEspecie;

import java.time.LocalDateTime;

public class LecheriaRazaRespuesta {

    private Long id;
    private Long empresaId;
    private String nombre;
    private LecheriaEspecie especie;
    private Boolean activo;
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

    public LecheriaEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(LecheriaEspecie especie) {
        this.especie = especie;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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
