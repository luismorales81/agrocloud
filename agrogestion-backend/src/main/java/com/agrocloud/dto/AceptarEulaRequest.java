package com.agrocloud.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para aceptar el EULA
 */
public class AceptarEulaRequest {
    
    @NotNull(message = "Debe aceptar el EULA")
    private Boolean aceptado;

    /** Ticket de corta vida emitido al detectar que falta el EULA. */
    private String ticket;

    private String ipAddress;
    private String userAgent;
    
    public AceptarEulaRequest() {
    }
    
    public AceptarEulaRequest(Boolean aceptado, String ipAddress, String userAgent) {
        this.aceptado = aceptado;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public Boolean getAceptado() {
        return aceptado;
    }
    
    public void setAceptado(Boolean aceptado) {
        this.aceptado = aceptado;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}

