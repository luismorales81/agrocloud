package com.agrocloud.model.enums;

public enum RolEmpresa {
    SUPERADMIN("Super Administrador"),
    ADMINISTRADOR("Administrador de Empresa"),
    JEFE_CAMPO("Jefe de Campo"),
    JEFE_FINANCIERO("Jefe Financiero"),
    OPERARIO("Operario de Campo"),
    CONSULTOR_EXTERNO("Consultor Externo"),
    
    // Roles legacy - mantener para retrocompatibilidad
    @Deprecated PRODUCTOR("Productor - usar JEFE_CAMPO"),
    @Deprecated ASESOR("Asesor - usar JEFE_CAMPO"),
    @Deprecated TECNICO("Técnico - usar JEFE_CAMPO"),
    @Deprecated CONTADOR("Contador - usar JEFE_FINANCIERO"),
    @Deprecated LECTURA("Solo Lectura - usar CONSULTOR_EXTERNO");

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
     * Mapea roles antiguos a roles nuevos
     */
    public RolEmpresa getRolActualizado() {
        return switch (this) {
            case PRODUCTOR, ASESOR, TECNICO -> JEFE_CAMPO;
            case CONTADOR -> JEFE_FINANCIERO;
            case LECTURA -> CONSULTOR_EXTERNO;
            default -> this;
        };
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
        RolEmpresa rolActualizado = getRolActualizado();
        return switch (rolActualizado) {
            case SUPERADMIN -> 5;
            case ADMINISTRADOR -> 4;
            case JEFE_CAMPO -> 3;
            case JEFE_FINANCIERO -> 2;
            case OPERARIO -> 1;
            case CONSULTOR_EXTERNO -> 0;
            default -> 0;
        };
    }

    /**
     * Verifica si el rol puede gestionar usuarios en la empresa
     */
    public boolean puedeGestionarUsuarios() {
        RolEmpresa rolActualizado = getRolActualizado();
        return rolActualizado == SUPERADMIN || rolActualizado == ADMINISTRADOR;
    }

    /**
     * Verifica si el rol tiene acceso a reportes financieros
     */
    public boolean tieneAccesoFinanciero() {
        RolEmpresa rolActualizado = getRolActualizado();
        return rolActualizado == SUPERADMIN || 
               rolActualizado == ADMINISTRADOR || 
               rolActualizado == JEFE_FINANCIERO;
    }

    /**
     * Verifica si el rol puede gestionar operaciones de campo
     */
    public boolean puedeGestionarCampo() {
        RolEmpresa rolActualizado = getRolActualizado();
        return rolActualizado == SUPERADMIN || 
               rolActualizado == ADMINISTRADOR || 
               rolActualizado == JEFE_CAMPO;
    }

    /**
     * Verifica si el rol puede cargar datos de campo
     */
    public boolean puedeCargarDatos() {
        RolEmpresa rolActualizado = getRolActualizado();
        return rolActualizado != CONSULTOR_EXTERNO;
    }

    /**
     * Verifica si el rol puede ejecutar labores
     */
    public boolean puedeEjecutarLabores() {
        RolEmpresa rolActualizado = getRolActualizado();
        return rolActualizado == SUPERADMIN || 
               rolActualizado == ADMINISTRADOR || 
               rolActualizado == JEFE_CAMPO || 
               rolActualizado == OPERARIO;
    }

    /**
     * Verifica si el rol es de solo lectura
     */
    public boolean esSoloLectura() {
        RolEmpresa rolActualizado = getRolActualizado();
        return rolActualizado == CONSULTOR_EXTERNO;
    }
}