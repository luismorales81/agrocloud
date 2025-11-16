package com.agrocloud.dto;

import java.time.LocalDateTime;

/**
 * DTO para el estado del EULA del usuario
 */
public class EulaEstadoResponse {
    
    private Boolean aceptado;
    private LocalDateTime fechaAceptacion;
    private String version;
    private String pdfUrl;
    
    public EulaEstadoResponse() {
    }
    
    public EulaEstadoResponse(Boolean aceptado, LocalDateTime fechaAceptacion, String version, String pdfUrl) {
        this.aceptado = aceptado;
        this.fechaAceptacion = fechaAceptacion;
        this.version = version;
        this.pdfUrl = pdfUrl;
    }
    
    public Boolean getAceptado() {
        return aceptado;
    }
    
    public void setAceptado(Boolean aceptado) {
        this.aceptado = aceptado;
    }
    
    public LocalDateTime getFechaAceptacion() {
        return fechaAceptacion;
    }
    
    public void setFechaAceptacion(LocalDateTime fechaAceptacion) {
        this.fechaAceptacion = fechaAceptacion;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getPdfUrl() {
        return pdfUrl;
    }
    
    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}

