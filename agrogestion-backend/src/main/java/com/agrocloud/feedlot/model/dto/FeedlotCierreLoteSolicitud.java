package com.agrocloud.feedlot.model.dto;

public class FeedlotCierreLoteSolicitud {

    /** Obligatorio si el lote aún tiene cabezas actuales > 0. */
    private Boolean confirmarConCabezas;

    public Boolean getConfirmarConCabezas() {
        return confirmarConCabezas;
    }

    public void setConfirmarConCabezas(Boolean confirmarConCabezas) {
        this.confirmarConCabezas = confirmarConCabezas;
    }
}
