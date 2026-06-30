package com.agrocloud.avicola.crianza.model.dto;

/**
 * Cierre manual de lote. Si quedan aves en plantel, requiere confirmación explícita.
 */
public class AvicolaCrianzaCierreLoteSolicitud {

    private Boolean confirmarConAvesPendientes;

    public Boolean getConfirmarConAvesPendientes() {
        return confirmarConAvesPendientes;
    }

    public void setConfirmarConAvesPendientes(Boolean confirmarConAvesPendientes) {
        this.confirmarConAvesPendientes = confirmarConAvesPendientes;
    }
}
