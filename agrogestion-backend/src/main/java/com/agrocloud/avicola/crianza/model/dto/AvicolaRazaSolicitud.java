package com.agrocloud.avicola.crianza.model.dto;

/**
 * Datos para crear o actualizar una raza en el catálogo.
 */
public class AvicolaRazaSolicitud {

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
