package com.agrocloud.model.enums;

/**
 * Enum para los estados de la relación usuario-empresa
 */
public enum EstadoUsuarioEmpresa {
    ACTIVO("Activo en la empresa"),
    INACTIVO("Inactivo en la empresa"),
    PENDIENTE("Pendiente de activación"),
    SUSPENDIDO("Suspendido temporalmente");
    
    private final String descripcion;
    
    EstadoUsuarioEmpresa(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}