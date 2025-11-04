package com.agrocloud.dto;

import java.util.List;

/**
 * DTO para ejecutar una labor con cantidades reales de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class EjecutarLaborRequest {
    
    private List<CantidadRealRequest> cantidadesReales;
    private String observacionesGenerales;
    private boolean confirmarEjecucion;

    // Constructors
    public EjecutarLaborRequest() {}

    public EjecutarLaborRequest(List<CantidadRealRequest> cantidadesReales) {
        this.cantidadesReales = cantidadesReales;
    }

    // Getters and Setters
    public List<CantidadRealRequest> getCantidadesReales() {
        return cantidadesReales;
    }

    public void setCantidadesReales(List<CantidadRealRequest> cantidadesReales) {
        this.cantidadesReales = cantidadesReales;
    }

    public String getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public void setObservacionesGenerales(String observacionesGenerales) {
        this.observacionesGenerales = observacionesGenerales;
    }

    public boolean isConfirmarEjecucion() {
        return confirmarEjecucion;
    }

    public void setConfirmarEjecucion(boolean confirmarEjecucion) {
        this.confirmarEjecucion = confirmarEjecucion;
    }
}
