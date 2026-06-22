package com.agrocloud.feedlot.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.FeedlotLecturaComederoRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotLecturaComederoSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotLecturaComedero;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.FeedlotLecturaComederoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioFeedlotBunk {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioFeedlotLotes servicioLotes;
    private final FeedlotLecturaComederoRepository lecturaRepository;

    public ServicioFeedlotBunk(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioFeedlotLotes servicioLotes,
            FeedlotLecturaComederoRepository lecturaRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.lecturaRepository = lecturaRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedlotLecturaComederoRespuesta> listarLecturas(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return lecturaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotLecturaComederoRespuesta registrarLectura(Long loteId, FeedlotLecturaComederoSolicitud solicitud) {
        validarSolicitud(solicitud);
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        validarLoteActivo(lote);
        if (solicitud.getFecha().isBefore(lote.getFechaIngreso())) {
            throw new IllegalArgumentException("La fecha no puede ser anterior al ingreso del lote");
        }

        FeedlotLecturaComedero lectura = new FeedlotLecturaComedero();
        lectura.setLote(lote);
        lectura.setEmpresaId(empresaId);
        lectura.setFecha(solicitud.getFecha());
        lectura.setBunkScore(solicitud.getBunkScore());
        lectura.setKgEntregados(solicitud.getKgEntregados());
        lectura.setObservaciones(solicitud.getObservaciones());
        return aRespuesta(lecturaRepository.save(lectura));
    }

    @Transactional
    public FeedlotLecturaComederoRespuesta actualizarLectura(
            Long loteId, Long lecturaId, FeedlotLecturaComederoSolicitud solicitud) {
        validarSolicitud(solicitud);
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        validarLoteActivo(lote);
        FeedlotLecturaComedero lectura = lecturaRepository.buscarPorIdYLoteIdYEmpresaId(lecturaId, loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura de comedero no encontrada"));
        lectura.setFecha(solicitud.getFecha());
        lectura.setBunkScore(solicitud.getBunkScore());
        lectura.setKgEntregados(solicitud.getKgEntregados());
        lectura.setObservaciones(solicitud.getObservaciones());
        return aRespuesta(lecturaRepository.save(lectura));
    }

    @Transactional
    public void eliminarLectura(Long loteId, Long lecturaId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        validarLoteActivo(lote);
        FeedlotLecturaComedero lectura = lecturaRepository.buscarPorIdYLoteIdYEmpresaId(lecturaId, loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura de comedero no encontrada"));
        lecturaRepository.delete(lectura);
    }

    private static void validarLoteActivo(FeedlotLote lote) {
        if (lote.getEstado() == FeedlotLoteEstado.CERRADO) {
            throw new IllegalStateException("No se pueden modificar lecturas de un lote cerrado");
        }
    }

    private static void validarSolicitud(FeedlotLecturaComederoSolicitud solicitud) {
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (solicitud.getBunkScore() == null) {
            throw new IllegalArgumentException("El bunk score es obligatorio");
        }
    }

    private FeedlotLecturaComederoRespuesta aRespuesta(FeedlotLecturaComedero l) {
        FeedlotLecturaComederoRespuesta dto = new FeedlotLecturaComederoRespuesta();
        dto.setId(l.getId());
        dto.setLoteId(l.getLote().getId());
        dto.setFecha(l.getFecha());
        dto.setBunkScore(l.getBunkScore());
        dto.setKgEntregados(l.getKgEntregados());
        dto.setObservaciones(l.getObservaciones());
        dto.setCreatedAt(l.getCreatedAt());
        return dto;
    }
}
