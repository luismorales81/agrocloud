package com.agrocloud.model.enums;

/**
 * Enum que representa los estados del ciclo de vida de un lote.
 * Define las transiciones válidas entre estados y sus descripciones.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public enum EstadoLote {
    
    // Estados iniciales
    DISPONIBLE("Disponible para siembra", "El lote está disponible para comenzar un nuevo ciclo de cultivo"),
    PREPARADO("Preparado para siembra", "El lote ha sido preparado y está listo para la siembra"),
    
    // Estados de cultivo
    SEMBRADO("Con cultivo sembrado", "El lote tiene cultivo sembrado y en desarrollo inicial"),
    EN_CRECIMIENTO("Cultivo en crecimiento", "El cultivo está en fase de crecimiento vegetativo"),
    EN_FLORACION("Cultivo en floración", "El cultivo está en fase de floración"),
    EN_FRUTIFICACION("Cultivo en fructificación", "El cultivo está en fase de fructificación"),
    
    // Estados de cosecha
    LISTO_PARA_COSECHA("Listo para cosechar", "El cultivo está listo para ser cosechado"),
    EN_COSECHA("Cosecha en progreso", "La cosecha está siendo realizada"),
    COSECHADO("Cosecha completada", "La cosecha ha sido completada exitosamente"),
    
    // Estados de post-cosecha
    EN_DESCANSO("Lote en descanso", "El lote está en período de descanso después de la cosecha"),
    EN_PREPARACION("Preparando para nuevo ciclo", "El lote está siendo preparado para un nuevo ciclo"),
    
    // Estados especiales
    ENFERMO("Cultivo con problemas", "El cultivo presenta problemas de salud o plagas"),
    ABANDONADO("Lote abandonado", "El lote ha sido abandonado temporalmente");
    
    private final String descripcion;
    private final String detalle;
    
    EstadoLote(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public String getDetalle() {
        return detalle;
    }
    
    /**
     * Verifica si el lote puede ser sembrado desde este estado
     */
    public boolean puedeSembrar() {
        return this == DISPONIBLE || this == PREPARADO || this == EN_PREPARACION;
    }
    
    /**
     * Verifica si el lote puede ser cosechado desde este estado
     * Permite cosecha desde múltiples estados para casos de:
     * - Cosecha normal (LISTO_PARA_COSECHA)
     * - Cosecha anticipada por problemas (SEMBRADO, EN_CRECIMIENTO)
     * - Conversión a forraje (EN_FLORACION, EN_FRUTIFICACION)
     */
    public boolean puedeCosechar() {
        return this == SEMBRADO || 
               this == EN_CRECIMIENTO || 
               this == EN_FLORACION || 
               this == EN_FRUTIFICACION || 
               this == LISTO_PARA_COSECHA;
    }
    
    /**
     * Verifica si el lote está en período de cultivo activo
     */
    public boolean esCultivoActivo() {
        return this == SEMBRADO || this == EN_CRECIMIENTO || 
               this == EN_FLORACION || this == EN_FRUTIFICACION;
    }
    
    /**
     * Verifica si el lote está en período de descanso
     */
    public boolean esDescanso() {
        return this == EN_DESCANSO || this == EN_PREPARACION;
    }
    
    /**
     * Verifica si el lote requiere atención especial
     */
    public boolean requiereAtencion() {
        return this == ENFERMO || this == ABANDONADO;
    }
    
    /**
     * Obtiene el color asociado al estado para la UI
     */
    public String getColor() {
        switch (this) {
            case DISPONIBLE:
            case PREPARADO:
                return "green";
            case SEMBRADO:
            case EN_CRECIMIENTO:
            case EN_FLORACION:
            case EN_FRUTIFICACION:
                return "blue";
            case LISTO_PARA_COSECHA:
                return "orange";
            case EN_COSECHA:
            case COSECHADO:
                return "purple";
            case EN_DESCANSO:
            case EN_PREPARACION:
                return "gray";
            case ENFERMO:
                return "red";
            case ABANDONADO:
                return "black";
            default:
                return "gray";
        }
    }
}
