package com.agrocloud.dto;

import com.agrocloud.model.enums.EstadoLote;

/**
 * DTO para confirmación de cambio de estado de lotes.
 * Contiene la información necesaria para confirmar o cancelar un cambio de estado.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class ConfirmacionCambioEstado {
    
    private Long loteId;
    private EstadoLote estadoActual;
    private EstadoLote estadoPropuesto;
    private String motivo;
    private boolean confirmado;
    private String observaciones;
    private Long laborId; // ID de la labor que generó el cambio (opcional)
    
    // Constructores
    public ConfirmacionCambioEstado() {}
    
    public ConfirmacionCambioEstado(Long loteId, EstadoLote estadoActual, EstadoLote estadoPropuesto,
                                  String motivo, boolean confirmado, String observaciones) {
        this.loteId = loteId;
        this.estadoActual = estadoActual;
        this.estadoPropuesto = estadoPropuesto;
        this.motivo = motivo;
        this.confirmado = confirmado;
        this.observaciones = observaciones;
    }
    
    public ConfirmacionCambioEstado(Long loteId, EstadoLote estadoActual, EstadoLote estadoPropuesto,
                                  String motivo, boolean confirmado, String observaciones, Long laborId) {
        this.loteId = loteId;
        this.estadoActual = estadoActual;
        this.estadoPropuesto = estadoPropuesto;
        this.motivo = motivo;
        this.confirmado = confirmado;
        this.observaciones = observaciones;
        this.laborId = laborId;
    }
    
    // Getters y Setters
    public Long getLoteId() {
        return loteId;
    }
    
    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }
    
    public EstadoLote getEstadoActual() {
        return estadoActual;
    }
    
    public void setEstadoActual(EstadoLote estadoActual) {
        this.estadoActual = estadoActual;
    }
    
    public EstadoLote getEstadoPropuesto() {
        return estadoPropuesto;
    }
    
    public void setEstadoPropuesto(EstadoLote estadoPropuesto) {
        this.estadoPropuesto = estadoPropuesto;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public Long getLaborId() {
        return laborId;
    }
    
    public void setLaborId(Long laborId) {
        this.laborId = laborId;
    }
}
