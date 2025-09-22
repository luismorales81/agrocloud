package com.agrocloud.model.enums;

/**
 * Enum para los estados de empresa en el sistema
 */
public enum EstadoEmpresa {
    TRIAL("Período de prueba"),
    ACTIVO("Empresa activa"),
    SUSPENDIDO("Empresa suspendida"),
    INACTIVO("Empresa inactiva");
    
    private final String descripcion;
    
    EstadoEmpresa(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}