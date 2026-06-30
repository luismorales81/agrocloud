package com.agrocloud.avicola.ponedoras.service;

import com.agrocloud.avicola.crianza.model.entity.AvicolaEstablecimiento;
import com.agrocloud.avicola.crianza.repository.AvicolaEstablecimientoRepository;
import com.agrocloud.avicola.ponedoras.model.dto.AvicolaPonedorasGalponRespuesta;
import com.agrocloud.avicola.ponedoras.model.dto.AvicolaPonedorasGalponSolicitud;
import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasGalpon;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasGalponEstado;
import com.agrocloud.avicola.ponedoras.repository.AvicolaPonedorasGalponRepository;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Alta, baja y consulta de galpones de ponedoras (empresa desde contexto de seguridad).
 */
@Service
public class ServicioAvicolaPonedorasGalpon {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final AvicolaPonedorasGalponRepository galponRepository;
    private final AvicolaEstablecimientoRepository establecimientoRepository;
    private final CampanaContextService campanaContextService;
    private final ObjectMapper objectMapper;

    public ServicioAvicolaPonedorasGalpon(
            ServicioSeguridadContexto servicioSeguridadContexto,
            AvicolaPonedorasGalponRepository galponRepository,
            AvicolaEstablecimientoRepository establecimientoRepository,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            ObjectMapper objectMapper) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.galponRepository = galponRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.campanaContextService = campanaContextService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasGalponRespuesta> listarGalpones(AvicolaPonedorasGalponEstado estado) {
        return listarGalpones(estado, null);
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasGalponRespuesta> listarGalpones(AvicolaPonedorasGalponEstado estado, Boolean delPeriodoActivo) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        List<AvicolaPonedorasGalpon> lista =
                estado != null
                        ? galponRepository.buscarPorEmpresaIdYEstado(empresaId, estado)
                        : galponRepository.buscarPorEmpresaId(empresaId);
        if (Boolean.TRUE.equals(delPeriodoActivo)) {
            Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
            lista = lista.stream().filter(g -> campanaId.equals(g.getCampanaId())).toList();
        }
        return lista.stream().map(this::aGalponRespuesta).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AvicolaPonedorasGalponRespuesta obtenerGalpon(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon g = galponRepository
                .buscarPorIdYEmpresaId(galponId, empresaId)
                .orElseThrow(() -> new IllegalStateException("Galpón no encontrado o no pertenece a la empresa"));
        return aGalponRespuesta(g);
    }

    @Transactional
    public AvicolaPonedorasGalponRespuesta crearGalpon(AvicolaPonedorasGalponSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        validarSolicitudGalponAlta(solicitud);
        AvicolaEstablecimiento est = establecimientoRepository
                .buscarPorIdYEmpresaId(solicitud.getEstablecimientoId(), empresaId)
                .orElseThrow(() -> new IllegalStateException("Establecimiento no encontrado o no pertenece a la empresa"));

        AvicolaPonedorasGalpon g = new AvicolaPonedorasGalpon();
        g.setEmpresaId(empresaId);
        g.setEstablecimiento(est);
        g.setNombre(solicitud.getNombre().trim());
        g.setRaza(solicitud.getRaza() != null ? solicitud.getRaza().trim() : "");
        g.setFechaIngreso(solicitud.getFechaIngreso());
        g.setCantidadInicial(solicitud.getCantidadInicial());
        g.setCantidadAves(solicitud.getCantidadAves() != null ? solicitud.getCantidadAves() : solicitud.getCantidadInicial());
        g.setEstado(solicitud.getEstado() != null ? solicitud.getEstado() : AvicolaPonedorasGalponEstado.ACTIVO);
        g.setFechaCierre(solicitud.getFechaCierre());
        g.setObservaciones(solicitud.getObservaciones());
        g.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        g = galponRepository.save(g);
        return aGalponRespuesta(g);
    }

    @Transactional
    public AvicolaPonedorasGalponRespuesta actualizarGalpon(Long galponId, AvicolaPonedorasGalponSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon g = galponRepository
                .buscarPorIdYEmpresaId(galponId, empresaId)
                .orElseThrow(() -> new IllegalStateException("Galpón no encontrado o no pertenece a la empresa"));

        if (solicitud.getEstablecimientoId() != null) {
            AvicolaEstablecimiento est = establecimientoRepository
                    .buscarPorIdYEmpresaId(solicitud.getEstablecimientoId(), empresaId)
                    .orElseThrow(() -> new IllegalStateException("Establecimiento no encontrado o no pertenece a la empresa"));
            g.setEstablecimiento(est);
        }
        if (solicitud.getNombre() != null) {
            g.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getRaza() != null) {
            g.setRaza(solicitud.getRaza().trim());
        }
        if (solicitud.getFechaIngreso() != null) {
            g.setFechaIngreso(solicitud.getFechaIngreso());
        }
        if (solicitud.getCantidadInicial() != null) {
            g.setCantidadInicial(solicitud.getCantidadInicial());
        }
        if (solicitud.getCantidadAves() != null) {
            g.setCantidadAves(solicitud.getCantidadAves());
        }
        if (solicitud.getEstado() != null) {
            g.setEstado(solicitud.getEstado());
        }
        if (solicitud.getFechaCierre() != null) {
            g.setFechaCierre(solicitud.getFechaCierre());
        }
        if (solicitud.getObservaciones() != null) {
            g.setObservaciones(solicitud.getObservaciones());
        }
        g = galponRepository.save(g);
        return aGalponRespuesta(g);
    }

    private static void validarSolicitudGalponAlta(AvicolaPonedorasGalponSolicitud solicitud) {
        if (solicitud.getEstablecimientoId() == null) {
            throw new IllegalStateException("El establecimiento es obligatorio");
        }
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalStateException("El nombre del galpón es obligatorio");
        }
        if (solicitud.getFechaIngreso() == null) {
            throw new IllegalStateException("La fecha de ingreso es obligatoria");
        }
        if (solicitud.getCantidadInicial() == null || solicitud.getCantidadInicial() <= 0) {
            throw new IllegalStateException("La cantidad inicial de aves debe ser mayor a cero");
        }
    }

    private AvicolaPonedorasGalponRespuesta aGalponRespuesta(AvicolaPonedorasGalpon g) {
        AvicolaPonedorasGalponRespuesta dto = new AvicolaPonedorasGalponRespuesta();
        dto.setId(g.getId());
        dto.setEmpresaId(g.getEmpresaId());
        dto.setEstablecimientoId(g.getEstablecimiento() != null ? g.getEstablecimiento().getId() : null);
        if (g.getEstablecimiento() != null) {
            dto.setEstablecimientoNombre(g.getEstablecimiento().getNombre());
            UtilCentroideCoordenadasCampo.calcularCentroide(g.getEstablecimiento().getCoordenadas(), objectMapper)
                    .ifPresent(xy -> {
                        dto.setClimaLatitud(xy[0]);
                        dto.setClimaLongitud(xy[1]);
                    });
        }
        dto.setNombre(g.getNombre());
        dto.setRaza(g.getRaza());
        dto.setFechaIngreso(g.getFechaIngreso());
        dto.setCantidadInicial(g.getCantidadInicial());
        dto.setCantidadAves(g.getCantidadAves());
        dto.setEstado(g.getEstado());
        dto.setFechaCierre(g.getFechaCierre());
        dto.setObservaciones(g.getObservaciones());
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());
        return dto;
    }
}
