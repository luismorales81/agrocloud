package com.agrocloud.model.enums;

public enum Rol {
    SUPERADMIN("Super administrador"),
    ADMINISTRADOR("Administrador de empresa"),
    ADMIN("Administrador del sistema"),
    PRODUCTOR("Productor agrícola"),
    TECNICO("Técnico agrícola"),
    ASESOR("Asesor agrícola"),
    OPERARIO("Operario de campo"),
    INVITADO("Usuario invitado con acceso limitado"),
    USUARIO_REGISTRADO("Usuario común que puede loguearse y acceder a empresas");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
