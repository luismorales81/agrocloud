package com.agrocloud.lecheria.model.dto;

public class LecheriaEstablecimientoSolicitud {

    private String nombre;
    private String ubicacion;
    private String coordenadas;
    private Integer capacidadAnimales;
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

    public Integer getCapacidadAnimales() {
        return capacidadAnimales;
    }

    public void setCapacidadAnimales(Integer capacidadAnimales) {
        this.capacidadAnimales = capacidadAnimales;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
