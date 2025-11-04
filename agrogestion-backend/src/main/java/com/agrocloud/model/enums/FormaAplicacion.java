package com.agrocloud.model.enums;

/**
 * Enum para formas de aplicación de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
public enum FormaAplicacion {
    TERRESTRE("Terrestre"),
    AEREA("Aérea"),
    MANUAL("Manual");

    private final String descripcion;

    FormaAplicacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
