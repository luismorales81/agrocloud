package com.agrocloud.feedlot.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotConfiguracionCloseoutRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotConfiguracionCloseoutSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotConfiguracionEmpresa;
import com.agrocloud.feedlot.model.enums.FeedlotMetodoCloseout;
import com.agrocloud.feedlot.repository.FeedlotConfiguracionEmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioFeedlotConfiguracion {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final FeedlotConfiguracionEmpresaRepository configuracionRepository;

    public ServicioFeedlotConfiguracion(
            ServicioSeguridadContexto servicioSeguridadContexto,
            FeedlotConfiguracionEmpresaRepository configuracionRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional(readOnly = true)
    public FeedlotConfiguracionCloseoutRespuesta obtenerMetodoCloseout() {
        FeedlotMetodoCloseout metodo = resolverMetodoCloseout(servicioSeguridadContexto.obtenerEmpresaIdActual());
        FeedlotConfiguracionCloseoutRespuesta resp = new FeedlotConfiguracionCloseoutRespuesta();
        resp.setMetodoCloseout(metodo);
        return resp;
    }

    @Transactional
    public FeedlotConfiguracionCloseoutRespuesta actualizarMetodoCloseout(
            FeedlotConfiguracionCloseoutSolicitud solicitud) {
        if (solicitud.getMetodoCloseout() == null) {
            throw new IllegalArgumentException("El método closeout es obligatorio");
        }
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotConfiguracionEmpresa config = configuracionRepository.findById(empresaId)
                .orElseGet(() -> {
                    FeedlotConfiguracionEmpresa nueva = new FeedlotConfiguracionEmpresa();
                    nueva.setEmpresaId(empresaId);
                    return nueva;
                });
        config.setMetodoCloseout(solicitud.getMetodoCloseout());
        configuracionRepository.save(config);
        FeedlotConfiguracionCloseoutRespuesta resp = new FeedlotConfiguracionCloseoutRespuesta();
        resp.setMetodoCloseout(config.getMetodoCloseout());
        return resp;
    }

    @Transactional(readOnly = true)
    public FeedlotMetodoCloseout resolverMetodoCloseout(Long empresaId) {
        return configuracionRepository.findById(empresaId)
                .map(FeedlotConfiguracionEmpresa::getMetodoCloseout)
                .orElse(FeedlotMetodoCloseout.DEADS_IN);
    }
}
