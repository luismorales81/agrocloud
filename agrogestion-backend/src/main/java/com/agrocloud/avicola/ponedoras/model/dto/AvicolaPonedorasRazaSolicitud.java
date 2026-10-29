package com.agrocloud.avicola.ponedoras.model.dto;

public class AvicolaPonedorasRazaSolicitud {

    private String nombre;
    private Boolean activo;

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
}
