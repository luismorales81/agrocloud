package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotAccionDesconocido;

public class FeedlotAccionDesconocidoSolicitud {

    private Long filaId;
    private FeedlotAccionDesconocido accion;

    public Long getFilaId() { return filaId; }
    public void setFilaId(Long filaId) { this.filaId = filaId; }
    public FeedlotAccionDesconocido getAccion() { return accion; }
    public void setAccion(FeedlotAccionDesconocido accion) { this.accion = accion; }
}
