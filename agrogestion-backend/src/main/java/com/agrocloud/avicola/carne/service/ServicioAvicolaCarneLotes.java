package com.agrocloud.avicola.carne.service;

import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaLote;
import com.agrocloud.avicola.crianza.model.dto.AvicolaLoteRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaLoteSolicitud;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión de lotes avícola-carne; la empresa se obtiene del contexto de seguridad.
 */
@Service
public class ServicioAvicolaCarneLotes {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioAvicolaCrianzaLote servicioLote;

    public ServicioAvicolaCarneLotes(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioAvicolaCrianzaLote servicioLote) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLote = servicioLote;
    }

    @Transactional(readOnly = true)
    public List<AvicolaLoteRespuesta> listarLotes(AvicolaLoteEstado estado) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        if (estado != null) {
            return servicioLote.listarLotesPorEstado(empresaId, estado);
        }
        return servicioLote.listarLotes(empresaId);
    }

    @Transactional(readOnly = true)
    public AvicolaLoteRespuesta obtenerLote(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioLote.obtenerLote(empresaId, loteId);
    }

    @Transactional
    public AvicolaLoteRespuesta crearLote(AvicolaLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioLote.crearLote(empresaId, solicitud);
    }

    @Transactional
    public AvicolaLoteRespuesta actualizarLote(Long loteId, AvicolaLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioLote.actualizarLote(empresaId, loteId, solicitud);
    }
}
