package com.agrocloud.trazabilidad.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.HechosTrazabilidadDocumento;
import com.agrocloud.trazabilidad.dto.IncidenciaValidacionTrazabilidad;
import com.agrocloud.trazabilidad.excepcion.CertificacionIncompletaExcepcion;
import com.agrocloud.trazabilidad.excepcion.CertificacionRechazadaExcepcion;
import com.agrocloud.trazabilidad.dto.SolicitudGeneracionReporteTrazabilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta consulta, validación y emisión; no contiene reglas fijas.
 */
@Service
@Transactional
public class TrazabilidadComercialOrquestador {

    @Autowired
    private CertificacionTrazabilidadService certificacionTrazabilidadService;
    @Autowired
    private TrazabilidadQueryService trazabilidadQueryService;
    @Autowired
    private ValidacionTrazabilidadService validacionTrazabilidadService;
    @Autowired
    private ReporteTrazabilidadService reporteTrazabilidadService;

    public TrazabilidadReporte solicitarReporte(SolicitudGeneracionReporteTrazabilidad s, User usuario, Empresa empresa) {
        TrazabilidadCertificacion cert = certificacionTrazabilidadService
                .obtenerCertificacionConReglas(s.getCertificacion().trim());
        HechosTrazabilidadDocumento hechos = trazabilidadQueryService.construirHechos(
                s.getEntidadTipo().trim().toUpperCase(), s.getEntidadId(), empresa, cert);
        List<IncidenciaValidacionTrazabilidad> inc = new ArrayList<>();
        TrazabilidadReporte.ResultadoReporte r = validacionTrazabilidadService.validarOIncumple(hechos, cert, inc);
        if (r == TrazabilidadReporte.ResultadoReporte.INVALIDO) {
            throw new CertificacionRechazadaExcepcion("No se cumple la certificacion: " + cert.getCodigo(), inc);
        }
        if (r == TrazabilidadReporte.ResultadoReporte.INCOMPLETO) {
            throw new CertificacionIncompletaExcepcion("Datos insuficientes o reglas inaplicables para: " + cert.getCodigo(), inc);
        }
        TrazabilidadReporte.TipoEntidadAlcance t = TrazabilidadReporte.TipoEntidadAlcance.valueOf(s.getEntidadTipo().trim().toUpperCase());
        return reporteTrazabilidadService.generarYPersistirSiValido(
                t, s.getEntidadId(), cert, hechos, r, inc, usuario, empresa);
    }

}
