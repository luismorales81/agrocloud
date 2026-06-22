package com.agrocloud.dto;

import com.agrocloud.model.enums.EstadoLote;

/**
 * Solicitud de cambio manual de estado (enum legacy o estado configurado).
 */
public class ProponerCambioEstadoRequest {

    private Long loteId;
    private EstadoLote nuevoEstado;
    /** ID del estado destino en cultivo_estados_lote (prioritario si el lote usa configuración). */
    private Long estadoDestinoConfigId;
    private String motivo;
    private Long laborId;

    public ProponerCambioEstadoRequest() {}

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public EstadoLote getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(EstadoLote nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    public Long getEstadoDestinoConfigId() { return estadoDestinoConfigId; }
    public void setEstadoDestinoConfigId(Long estadoDestinoConfigId) { this.estadoDestinoConfigId = estadoDestinoConfigId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getLaborId() { return laborId; }
    public void setLaborId(Long laborId) { this.laborId = laborId; }
}
