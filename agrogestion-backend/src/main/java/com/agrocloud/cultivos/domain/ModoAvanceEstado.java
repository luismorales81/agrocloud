package com.agrocloud.cultivos.domain;

/**
 * Indica cómo avanza un estado configurado del lote.
 */
public enum ModoAvanceEstado {
    /** Derivado de eventos canónicos: siembra, cosecha, abandono. */
    EVENTO,
    /** Por días transcurridos desde la siembra (diasMinimos). */
    TIEMPO,
    /** Al completar tareas obligatorias del estado. */
    TAREAS,
    /** Combinación de tiempo, tareas y eventos. */
    MIXTO;

    public static ModoAvanceEstado desdeTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return MIXTO;
        }
        try {
            return ModoAvanceEstado.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MIXTO;
        }
    }
}
