package com.agrocloud.porcinos.model.dto;

public class PorcinosGalponSolicitud {

    private String nombre;
    private Integer capacidadCabezas;
    private Boolean activo;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
