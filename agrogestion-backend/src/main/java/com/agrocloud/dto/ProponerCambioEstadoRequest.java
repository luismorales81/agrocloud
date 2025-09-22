package com.agrocloud.dto;

import com.agrocloud.model.enums.EstadoLote;

/**
 * DTO para solicitar un cambio de estado de lote.
 * Contiene la información necesaria para proponer un cambio de estado.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class ProponerCambioEstadoRequest {
    
    private Long loteId;
    private EstadoLote nuevoEstado;
    private String motivo;
    private Long laborId; // ID de la labor que genera el cambio (opcional)
    
    // Constructores
    public ProponerCambioEstadoRequest() {}
    
    public ProponerCambioEstadoRequest(Long loteId, EstadoLote nuevoEstado, String motivo) {
        this.loteId = loteId;
        this.nuevoEstado = nuevoEstado;
        this.motivo = motivo;
    }
    
    public ProponerCambioEstadoRequest(Long loteId, EstadoLote nuevoEstado, String motivo, Long laborId) {
        this.loteId = loteId;
        this.nuevoEstado = nuevoEstado;
        this.motivo = motivo;
        this.laborId = laborId;
    }
    
    // Getters y Setters
    public Long getLoteId() {
        return loteId;
    }
    
    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }
    
    public EstadoLote getNuevoEstado() {
        return nuevoEstado;
    }
    
    public void setNuevoEstado(EstadoLote nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public Long getLaborId() {
        return laborId;
    }
    
    public void setLaborId(Long laborId) {
        this.laborId = laborId;
    }
}
