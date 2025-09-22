package com.agrocloud.model.enums;

public enum RolEmpresa {
    SUPERADMIN("Super Administrador"),
    ADMINISTRADOR("Administrador de Empresa"),
    ASESOR("Asesor/Ingeniero Agrónomo"),
    OPERARIO("Operario"),
    TECNICO("Técnico"),
    CONTADOR("Contador"),
    LECTURA("Solo Lectura"),
    PRODUCTOR("Productor");

    private final String descripcion;

    RolEmpresa(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    /**
     * Verifica si este rol tiene el nivel de permisos requerido o superior
     */
    public boolean tieneNivelPermisos(RolEmpresa rolMinimo) {
        // Jerarquía de permisos (de mayor a menor)
        int nivelActual = getNivelPermisos();
        int nivelMinimo = rolMinimo.getNivelPermisos();
        return nivelActual >= nivelMinimo;
    }

    /**
     * Obtiene el nivel numérico de permisos del rol
     */
    private int getNivelPermisos() {
        return switch (this) {
            case SUPERADMIN -> 7;
            case ADMINISTRADOR -> 6;
            case PRODUCTOR -> 5;
            case ASESOR -> 4;
            case CONTADOR -> 3;
            case TECNICO -> 2;
            case OPERARIO -> 1;
            case LECTURA -> 0;
        };
    }

    /**
     * Verifica si el rol puede gestionar usuarios en la empresa
     */
    public boolean puedeGestionarUsuarios() {
        return this == SUPERADMIN || this == ADMINISTRADOR;
    }

    /**
     * Verifica si el rol tiene acceso a reportes financieros
     */
    public boolean tieneAccesoFinanciero() {
        return this == SUPERADMIN || this == ADMINISTRADOR || this == PRODUCTOR || this == CONTADOR;
    }

    /**
     * Verifica si el rol puede cargar datos de campo
     */
    public boolean puedeCargarDatos() {
        return this != LECTURA;
    }

    /**
     * Verifica si el rol puede validar datos
     */
    public boolean puedeValidarDatos() {
        return this == SUPERADMIN || this == ADMINISTRADOR || this == PRODUCTOR || this == ASESOR || this == TECNICO;
    }
}