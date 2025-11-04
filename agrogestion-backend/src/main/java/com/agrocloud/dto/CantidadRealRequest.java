package com.agrocloud.dto;

import java.math.BigDecimal;

/**
 * DTO para registrar la cantidad real utilizada de un agroquímico
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class CantidadRealRequest {
    
    private Long aplicacionId;
    private BigDecimal cantidadReal;
    private String motivoDesviacion;
    private String observaciones;

    // Constructors
    public CantidadRealRequest() {}

    public CantidadRealRequest(Long aplicacionId, BigDecimal cantidadReal) {
        this.aplicacionId = aplicacionId;
        this.cantidadReal = cantidadReal;
    }

    public CantidadRealRequest(Long aplicacionId, BigDecimal cantidadReal, String motivoDesviacion) {
        this.aplicacionId = aplicacionId;
        this.cantidadReal = cantidadReal;
        this.motivoDesviacion = motivoDesviacion;
    }

    // Getters and Setters
    public Long getAplicacionId() {
        return aplicacionId;
    }

    public void setAplicacionId(Long aplicacionId) {
        this.aplicacionId = aplicacionId;
    }

    public BigDecimal getCantidadReal() {
        return cantidadReal;
    }

    public void setCantidadReal(BigDecimal cantidadReal) {
        this.cantidadReal = cantidadReal;
    }

    public String getMotivoDesviacion() {
        return motivoDesviacion;
    }

    public void setMotivoDesviacion(String motivoDesviacion) {
        this.motivoDesviacion = motivoDesviacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
