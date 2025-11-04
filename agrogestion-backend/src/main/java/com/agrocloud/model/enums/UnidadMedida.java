package com.agrocloud.model.enums;

/**
 * Enum para unidades de medida de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
public enum UnidadMedida {
    LTS("Litros"),
    KG("Kilogramos"),
    GR("Gramos"),
    ML("Mililitros");

    private final String descripcion;

    UnidadMedida(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
