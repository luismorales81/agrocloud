package com.agrocloud.feedlot.model.dto;

public class FeedlotEstablecimientoSolicitud {

    private String nombre;
    private String ubicacion;
    private String coordenadas;
    private Integer capacidadTotalCabezas;
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

    public Integer getCapacidadTotalCabezas() {
        return capacidadTotalCabezas;
    }

    public void setCapacidadTotalCabezas(Integer capacidadTotalCabezas) {
        this.capacidadTotalCabezas = capacidadTotalCabezas;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
