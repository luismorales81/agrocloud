package com.agrocloud.model.entity;

/**
 * Enum para los estados de usuario en el sistema
 */
public enum EstadoUsuario {
    PENDIENTE("Pendiente de activación"),
    ACTIVO("Usuario activo"),
    SUSPENDIDO("Usuario suspendido"),
    ELIMINADO("Usuario eliminado");

    private final String descripcion;

    EstadoUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
