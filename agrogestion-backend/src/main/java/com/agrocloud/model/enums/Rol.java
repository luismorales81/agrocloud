package com.agrocloud.model.enums;

public enum Rol {
    SUPERADMIN("Super administrador"),
    ADMINISTRADOR("Administrador de empresa"),
    PRODUCTOR("Productor agropecuario"),
    TECNICO("Técnico agropecuario"),
    ASESOR("Asesor agropecuario"),
    OPERARIO("Operario de campo"),
    INVITADO("Usuario invitado con acceso limitado");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
