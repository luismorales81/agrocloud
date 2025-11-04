package com.agrocloud.model.enums;

/**
 * Enum para tipos de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
public enum TipoAgroquimico {
    HERBICIDA("Herbicida"),
    INSECTICIDA("Insecticida"),
    FUNGICIDA("Fungicida"),
    COADYUVANTE("Coadyuvante"),
    DESCONOCIDO("Desconocido");

    private final String descripcion;

    TipoAgroquimico(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
