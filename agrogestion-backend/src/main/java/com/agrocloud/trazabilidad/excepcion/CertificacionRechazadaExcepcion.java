package com.agrocloud.trazabilidad.excepcion;

import com.agrocloud.trazabilidad.dto.IncidenciaValidacionTrazabilidad;
import java.util.List;

/**
 * Lanzada cuando el alcance no cumple las reglas: no se persiste reporte.
 */
public class CertificacionRechazadaExcepcion extends RuntimeException {
    private final List<IncidenciaValidacionTrazabilidad> incidencias;

    public CertificacionRechazadaExcepcion(String message, List<IncidenciaValidacionTrazabilidad> incidencias) {
        super(message);
        this.incidencias = incidencias;
    }

    public List<IncidenciaValidacionTrazabilidad> getIncidencias() { return incidencias; }
}
