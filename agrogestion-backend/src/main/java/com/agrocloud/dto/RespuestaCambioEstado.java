package com.agrocloud.dto;

import com.agrocloud.model.enums.EstadoLote;
import java.util.List;

/**
 * DTO para respuestas de cambio de estado de lotes.
 * Contiene información sobre la propuesta de cambio y si requiere confirmación.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class RespuestaCambioEstado {
    
    private boolean requiereConfirmacion;
    private String mensaje;
    private EstadoLote estadoActual;
    private EstadoLote estadoPropuesto;
    private String accionRequerida;
    private List<String> consecuencias;
    private boolean puedeCancelar;
    private Long loteId;
    private String loteNombre;
    private String motivo;
    
    // Constructores
    public RespuestaCambioEstado() {}
    
    public RespuestaCambioEstado(boolean requiereConfirmacion, String mensaje) {
        this.requiereConfirmacion = requiereConfirmacion;
        this.mensaje = mensaje;
    }
    
    public RespuestaCambioEstado(boolean requiereConfirmacion, String mensaje, 
                               EstadoLote estadoActual, EstadoLote estadoPropuesto,
                               String accionRequerida, List<String> consecuencias,
                               boolean puedeCancelar, Long loteId, String loteNombre, String motivo) {
        this.requiereConfirmacion = requiereConfirmacion;
        this.mensaje = mensaje;
        this.estadoActual = estadoActual;
        this.estadoPropuesto = estadoPropuesto;
        this.accionRequerida = accionRequerida;
        this.consecuencias = consecuencias;
        this.puedeCancelar = puedeCancelar;
        this.loteId = loteId;
        this.loteNombre = loteNombre;
        this.motivo = motivo;
    }
    
    // Getters y Setters
    public boolean isRequiereConfirmacion() {
        return requiereConfirmacion;
    }
    
    public void setRequiereConfirmacion(boolean requiereConfirmacion) {
        this.requiereConfirmacion = requiereConfirmacion;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
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
    
    public String getAccionRequerida() {
        return accionRequerida;
    }
    
    public void setAccionRequerida(String accionRequerida) {
        this.accionRequerida = accionRequerida;
    }
    
    public List<String> getConsecuencias() {
        return consecuencias;
    }
    
    public void setConsecuencias(List<String> consecuencias) {
        this.consecuencias = consecuencias;
    }
    
    public boolean isPuedeCancelar() {
        return puedeCancelar;
    }
    
    public void setPuedeCancelar(boolean puedeCancelar) {
        this.puedeCancelar = puedeCancelar;
    }
    
    public Long getLoteId() {
        return loteId;
    }
    
    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }
    
    public String getLoteNombre() {
        return loteNombre;
    }
    
    public void setLoteNombre(String loteNombre) {
        this.loteNombre = loteNombre;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
