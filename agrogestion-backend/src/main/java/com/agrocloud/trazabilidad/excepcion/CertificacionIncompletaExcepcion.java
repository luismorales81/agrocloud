package com.agrocloud.trazabilidad.excepcion;

import com.agrocloud.trazabilidad.dto.IncidenciaValidacionTrazabilidad;
import java.util.List;

public class CertificacionIncompletaExcepcion extends RuntimeException {
    private final List<IncidenciaValidacionTrazabilidad> incidencias;

    public CertificacionIncompletaExcepcion(String message, List<IncidenciaValidacionTrazabilidad> incidencias) {
        super(message);
        this.incidencias = incidencias;
    }

    public List<IncidenciaValidacionTrazabilidad> getIncidencias() { return incidencias; }
}
