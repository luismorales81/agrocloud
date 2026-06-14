package com.agrocloud.trazabilidad.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.HechosExpedienteTrazabilidad;
import com.agrocloud.trazabilidad.dto.SolicitudGeneracionExpedienteTrazabilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta la generación del expediente de ciclo de vida (sin reglas de rechazo).
 */
@Service
@Transactional
public class TrazabilidadExpedienteOrquestador {

    @Autowired
    private TrazabilidadExpedienteConstruccionService construccionService;
    @Autowired
    private ReporteTrazabilidadService reporteTrazabilidadService;

    public TrazabilidadReporte solicitarExpediente(
            SolicitudGeneracionExpedienteTrazabilidad solicitud, User usuario, Empresa empresa) {
        String tipo = solicitud.getEntidadTipo().trim().toUpperCase();
        HechosExpedienteTrazabilidad hechos = construccionService.construir(tipo, solicitud.getEntidadId(), empresa);
        TrazabilidadReporte.TipoEntidadAlcance alcance = TrazabilidadReporte.TipoEntidadAlcance.valueOf(tipo);
        return reporteTrazabilidadService.generarExpedienteYPersistir(
                alcance, solicitud.getEntidadId(), hechos, usuario, empresa);
    }
}
