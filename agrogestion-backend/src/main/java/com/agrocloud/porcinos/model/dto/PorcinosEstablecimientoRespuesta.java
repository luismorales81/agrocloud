package com.agrocloud.porcinos.model.dto;

import java.time.LocalDateTime;

public class PorcinosEstablecimientoRespuesta {

    private Long id;
    private Long empresaId;
    private String nombre;
    private String ubicacion;
    private String coordenadas;
    private Double climaLatitud;
    private Double climaLongitud;
    private Integer diasGestacion;
    private Integer diasLactancia;
    private Integer diasEntreCelos;
    private Boolean faenaHabilitada;
    private Integer capacidadCabezas;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
    }

    public Double getClimaLatitud() {
        return climaLatitud;
    }

    public void setClimaLatitud(Double climaLatitud) {
        this.climaLatitud = climaLatitud;
    }

    public Double getClimaLongitud() {
        return climaLongitud;
    }

    public void setClimaLongitud(Double climaLongitud) {
        this.climaLongitud = climaLongitud;
    }

    public Integer getDiasGestacion() {
        return diasGestacion;
    }

    public void setDiasGestacion(Integer diasGestacion) {
        this.diasGestacion = diasGestacion;
    }

    public Integer getDiasLactancia() {
        return diasLactancia;
    }

    public void setDiasLactancia(Integer diasLactancia) {
        this.diasLactancia = diasLactancia;
    }

    public Integer getDiasEntreCelos() {
        return diasEntreCelos;
    }

    public void setDiasEntreCelos(Integer diasEntreCelos) {
        this.diasEntreCelos = diasEntreCelos;
    }

    public Boolean getFaenaHabilitada() {
        return faenaHabilitada;
    }

    public void setFaenaHabilitada(Boolean faenaHabilitada) {
        this.faenaHabilitada = faenaHabilitada;
    }

    public Integer getCapacidadCabezas() {
        return capacidadCabezas;
    }

    public void setCapacidadCabezas(Integer capacidadCabezas) {
        this.capacidadCabezas = capacidadCabezas;
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
