package com.agrocloud.avicola.huevos.application;

import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoAjustePlantelRespuesta;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoAjustePlantelSolicitud;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoLoteRespuesta;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoLoteSolicitud;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoAjustePlantel;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoEstablecimiento;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoLote;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoRaza;
import com.agrocloud.avicola.huevos.model.enums.AvicolaHuevoLoteEstado;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoAjustePlantelRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoEstablecimientoRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoLoteRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoRazaRepository;
import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.agrocloud.core.domain.User;
import com.agrocloud.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaHuevosLote {

    private final AvicolaHuevoLoteRepository loteRepository;
    private final AvicolaHuevoEstablecimientoRepository establecimientoRepository;
    private final AvicolaHuevoRazaRepository razaRepository;
    private final AvicolaHuevoAjustePlantelRepository ajustePlantelRepository;
    private final ObjectMapper objectMapper;

    public ServicioAvicolaHuevosLote(
            AvicolaHuevoLoteRepository loteRepository,
            AvicolaHuevoEstablecimientoRepository establecimientoRepository,
            AvicolaHuevoRazaRepository razaRepository,
            AvicolaHuevoAjustePlantelRepository ajustePlantelRepository,
            ObjectMapper objectMapper) {
        this.loteRepository = loteRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.razaRepository = razaRepository;
        this.ajustePlantelRepository = ajustePlantelRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoLoteRespuesta> listarLotes(Long empresaId) {
        return loteRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aLoteRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoLoteRespuesta> listarLotesPorEstado(Long empresaId, AvicolaHuevoLoteEstado estado) {
        return loteRepository.listarPorEmpresaIdYEstado(empresaId, estado).stream()
                .map(this::aLoteRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AvicolaHuevoLoteRespuesta obtenerLote(Long empresaId, Long loteId) {
        return aLoteRespuesta(obtenerEntidadLote(empresaId, loteId));
    }

    @Transactional
    public AvicolaHuevoLoteRespuesta crearLote(Long empresaId, AvicolaHuevoLoteSolicitud solicitud) {
        AvicolaHuevoEstablecimiento est = establecimientoRepository
                .buscarPorIdYEmpresaId(solicitud.getEstablecimientoId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado"));
        AvicolaHuevoRaza raza = razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));

        AvicolaHuevoLote l = new AvicolaHuevoLote();
        l.setEmpresaId(empresaId);
        l.setEstablecimiento(est);
        l.setRaza(raza);
        l.setNombre(solicitud.getNombre());
        l.setFechaInicio(solicitud.getFechaInicio());
        l.setCantidadAvesInicial(solicitud.getCantidadAvesInicial());
        int inicial = solicitud.getCantidadAvesInicial();
        l.setCantidadAvesActual(solicitud.getCantidadAvesActual() != null ? solicitud.getCantidadAvesActual() : inicial);
        l.setObservaciones(solicitud.getObservaciones());
        l.setEstado(AvicolaHuevoLoteEstado.ACTIVO);
        return aLoteRespuesta(loteRepository.save(l));
    }

    @Transactional
    public AvicolaHuevoLoteRespuesta actualizarLote(Long empresaId, Long loteId, AvicolaHuevoLoteSolicitud solicitud) {
        AvicolaHuevoLote l = obtenerEntidadLote(empresaId, loteId);
        if (l.getEstado() == AvicolaHuevoLoteEstado.CERRADO) {
            throw new IllegalArgumentException("No se puede editar un lote cerrado");
        }
        if (solicitud.getEstablecimientoId() != null) {
            AvicolaHuevoEstablecimiento est = establecimientoRepository
                    .buscarPorIdYEmpresaId(solicitud.getEstablecimientoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado"));
            l.setEstablecimiento(est);
        }
        if (solicitud.getRazaId() != null) {
            AvicolaHuevoRaza raza = razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));
            l.setRaza(raza);
        }
        if (solicitud.getNombre() != null) {
            l.setNombre(solicitud.getNombre());
        }
        if (solicitud.getObservaciones() != null) {
            l.setObservaciones(solicitud.getObservaciones());
        }
        if (solicitud.getCantidadAvesActual() != null) {
            l.setCantidadAvesActual(solicitud.getCantidadAvesActual());
        }
        return aLoteRespuesta(loteRepository.save(l));
    }

    /**
     * Ajuste explícito de plantel con motivo (auditoría). No usar para ediciones simples del formulario de lote.
     */
    @Transactional
    public AvicolaHuevoLoteRespuesta registrarAjustePlantel(
            Long empresaId, Long loteId, AvicolaHuevoAjustePlantelSolicitud solicitud, User usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuario obligatorio para ajuste de plantel");
        }
        if (solicitud.getCantidadAvesNueva() == null || solicitud.getCantidadAvesNueva() < 0) {
            throw new IllegalArgumentException("La cantidad de aves debe ser un número no negativo");
        }
        if (solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()) {
            throw new IllegalArgumentException("El motivo del ajuste es obligatorio");
        }
        AvicolaHuevoLote l = obtenerEntidadLote(empresaId, loteId);
        if (l.getEstado() == AvicolaHuevoLoteEstado.CERRADO) {
            throw new IllegalArgumentException("No se puede ajustar plantel en un lote cerrado");
        }
        int anterior = l.getCantidadAvesActual() != null ? l.getCantidadAvesActual() : 0;
        int nuevo = solicitud.getCantidadAvesNueva();
        AvicolaHuevoAjustePlantel a = new AvicolaHuevoAjustePlantel();
        a.setLote(l);
        a.setEmpresaId(empresaId);
        a.setUsuario(usuario);
        a.setFechaHora(LocalDateTime.now());
        a.setCantidadAvesAnterior(anterior);
        a.setCantidadAvesNueva(nuevo);
        a.setMotivo(solicitud.getMotivo().trim());
        ajustePlantelRepository.save(a);
        l.setCantidadAvesActual(nuevo);
        return aLoteRespuesta(loteRepository.save(l));
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoAjustePlantelRespuesta> listarAjustesPlantel(Long empresaId, Long loteId) {
        obtenerEntidadLote(empresaId, loteId);
        return ajustePlantelRepository.listarPorLoteYEmpresa(loteId, empresaId).stream()
                .map(this::aAjustePlantelRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaHuevoLoteRespuesta cerrarLote(Long empresaId, Long loteId) {
        AvicolaHuevoLote l = obtenerEntidadLote(empresaId, loteId);
        if (l.getEstado() == AvicolaHuevoLoteEstado.CERRADO) {
            throw new IllegalArgumentException("El lote ya está cerrado");
        }
        l.setEstado(AvicolaHuevoLoteEstado.CERRADO);
        l.setFechaCierre(LocalDate.now());
        return aLoteRespuesta(loteRepository.save(l));
    }

    @Transactional(readOnly = true)
    public AvicolaHuevoLote obtenerEntidadLote(Long empresaId, Long loteId) {
        return loteRepository.buscarPorIdYEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote de huevos no encontrado"));
    }

    @Transactional
    public AvicolaHuevoLote guardarLote(AvicolaHuevoLote lote) {
        return loteRepository.save(lote);
    }

    private AvicolaHuevoAjustePlantelRespuesta aAjustePlantelRespuesta(AvicolaHuevoAjustePlantel a) {
        AvicolaHuevoAjustePlantelRespuesta dto = new AvicolaHuevoAjustePlantelRespuesta();
        dto.setId(a.getId());
        dto.setLoteId(a.getLote() != null ? a.getLote().getId() : null);
        dto.setFechaHora(a.getFechaHora());
        dto.setCantidadAvesAnterior(a.getCantidadAvesAnterior());
        dto.setCantidadAvesNueva(a.getCantidadAvesNueva());
        dto.setMotivo(a.getMotivo());
        dto.setUsuarioId(a.getUsuario() != null ? a.getUsuario().getId() : null);
        return dto;
    }

    private AvicolaHuevoLoteRespuesta aLoteRespuesta(AvicolaHuevoLote l) {
        AvicolaHuevoLoteRespuesta dto = new AvicolaHuevoLoteRespuesta();
        dto.setId(l.getId());
        dto.setEmpresaId(l.getEmpresaId());
        dto.setEstablecimientoId(l.getEstablecimiento() != null ? l.getEstablecimiento().getId() : null);
        dto.setEstablecimientoNombre(l.getEstablecimiento() != null ? l.getEstablecimiento().getNombre() : null);
        dto.setRazaId(l.getRaza() != null ? l.getRaza().getId() : null);
        dto.setRazaNombre(l.getRaza() != null ? l.getRaza().getNombre() : null);
        dto.setNombre(l.getNombre());
        dto.setFechaInicio(l.getFechaInicio());
        dto.setCantidadAvesInicial(l.getCantidadAvesInicial());
        dto.setCantidadAvesActual(l.getCantidadAvesActual());
        dto.setEstado(l.getEstado());
        dto.setFechaCierre(l.getFechaCierre());
        dto.setObservaciones(l.getObservaciones());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setUpdatedAt(l.getUpdatedAt());
        if (l.getEstablecimiento() != null) {
            UtilCentroideCoordenadasCampo.calcularCentroide(l.getEstablecimiento().getCoordenadas(), objectMapper)
                    .ifPresent(xy -> {
                        dto.setClimaLatitud(xy[0]);
                        dto.setClimaLongitud(xy[1]);
                    });
        }
        return dto;
    }
}
