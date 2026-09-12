package com.agrocloud.porcinos.model.dto;

public class PorcinosEstablecimientoSolicitud {

    private String nombre;
    private String ubicacion;
    private String coordenadas;
    private Integer diasGestacion;
    private Integer diasLactancia;
    private Integer diasEntreCelos;
    private Boolean faenaHabilitada;
    private Integer capacidadCabezas;
    private Boolean activo;

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
}
