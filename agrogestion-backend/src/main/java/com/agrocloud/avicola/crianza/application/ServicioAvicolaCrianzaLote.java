package com.agrocloud.avicola.crianza.application;

import com.agrocloud.avicola.crianza.model.dto.AvicolaLoteRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaLoteSolicitud;
import com.agrocloud.avicola.crianza.model.entity.AvicolaEstablecimiento;
import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.entity.AvicolaRaza;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.avicola.crianza.repository.AvicolaEstablecimientoRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaLoteRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaRazaRepository;
import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaCrianzaLote {

    private final AvicolaLoteRepository loteRepository;
    private final AvicolaEstablecimientoRepository establecimientoRepository;
    private final AvicolaRazaRepository razaRepository;
    private final ObjectMapper objectMapper;
    private final CampanaContextService campanaContextService;

    public ServicioAvicolaCrianzaLote(
            AvicolaLoteRepository loteRepository,
            AvicolaEstablecimientoRepository establecimientoRepository,
            AvicolaRazaRepository razaRepository,
            ObjectMapper objectMapper,
            CampanaContextService campanaContextService) {
        this.loteRepository = loteRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.razaRepository = razaRepository;
        this.objectMapper = objectMapper;
        this.campanaContextService = campanaContextService;
    }

    @Transactional(readOnly = true)
    public List<AvicolaLoteRespuesta> listarLotes(Long empresaId) {
        return listarLotes(empresaId, null, null);
    }

    @Transactional(readOnly = true)
    public List<AvicolaLoteRespuesta> listarLotesPorEstado(Long empresaId, AvicolaLoteEstado estado) {
        return listarLotes(empresaId, estado, null);
    }

    @Transactional(readOnly = true)
    public List<AvicolaLoteRespuesta> listarLotes(Long empresaId, AvicolaLoteEstado estado, Boolean delPeriodoActivo) {
        List<AvicolaLote> lotes = estado != null
                ? loteRepository.listarPorEmpresaIdYEstado(empresaId, estado)
                : loteRepository.listarPorEmpresaId(empresaId);
        return filtrarPorPeriodoActivo(empresaId, delPeriodoActivo, lotes).stream()
                .map(this::aLoteRespuesta)
                .collect(Collectors.toList());
    }

    private List<AvicolaLote> filtrarPorPeriodoActivo(Long empresaId, Boolean delPeriodoActivo, List<AvicolaLote> lotes) {
        if (!Boolean.TRUE.equals(delPeriodoActivo)) {
            return lotes;
        }
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
        return lotes.stream().filter(l -> campanaId.equals(l.getCampanaId())).toList();
    }

    @Transactional(readOnly = true)
    public AvicolaLoteRespuesta obtenerLote(Long empresaId, Long loteId) {
        AvicolaLote l = loteRepository.buscarPorIdYEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
        return aLoteRespuesta(l);
    }

    @Transactional
    public AvicolaLoteRespuesta crearLote(Long empresaId, AvicolaLoteSolicitud solicitud) {
        AvicolaEstablecimiento est = establecimientoRepository
                .buscarPorIdYEmpresaId(solicitud.getEstablecimientoId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado"));
        AvicolaRaza raza = razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));

        AvicolaLote l = new AvicolaLote();
        l.setEmpresaId(empresaId);
        l.setEstablecimiento(est);
        l.setRaza(raza);
        l.setNombre(solicitud.getNombre());
        l.setEspecie(solicitud.getEspecie());
        l.setOrigen(solicitud.getOrigen() != null ? solicitud.getOrigen() : "EXTERNO");
        l.setFechaIngreso(solicitud.getFechaIngreso());
        l.setCantidadInicial(solicitud.getCantidadInicial());
        int inicial = solicitud.getCantidadInicial();
        l.setCantidadAnimales(solicitud.getCantidadAnimales() != null ? solicitud.getCantidadAnimales() : inicial);
        l.setPesoPromedioIngreso(solicitud.getPesoPromedioIngreso());
        l.setObservaciones(solicitud.getObservaciones());
        l.setEstado(AvicolaLoteEstado.ACTIVO);
        l.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        return aLoteRespuesta(loteRepository.save(l));
    }

    @Transactional
    public AvicolaLoteRespuesta actualizarLote(Long empresaId, Long loteId, AvicolaLoteSolicitud solicitud) {
        AvicolaLote l = loteRepository.buscarPorIdYEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
        if (l.getEstado() == AvicolaLoteEstado.CERRADO) {
            throw new IllegalArgumentException("No se puede editar un lote cerrado");
        }
        if (solicitud.getEstablecimientoId() != null) {
            AvicolaEstablecimiento est = establecimientoRepository
                    .buscarPorIdYEmpresaId(solicitud.getEstablecimientoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado"));
            l.setEstablecimiento(est);
        }
        if (solicitud.getRazaId() != null) {
            AvicolaRaza raza = razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));
            l.setRaza(raza);
        }
        if (solicitud.getNombre() != null) {
            l.setNombre(solicitud.getNombre());
        }
        if (solicitud.getEspecie() != null) {
            l.setEspecie(solicitud.getEspecie());
        }
        if (solicitud.getOrigen() != null) {
            l.setOrigen(solicitud.getOrigen());
        }
        if (solicitud.getObservaciones() != null) {
            l.setObservaciones(solicitud.getObservaciones());
        }
        return aLoteRespuesta(loteRepository.save(l));
    }

    @Transactional(readOnly = true)
    public AvicolaLote obtenerEntidadLote(Long empresaId, Long loteId) {
        return loteRepository.buscarPorIdYEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
    }

    @Transactional
    public AvicolaLote guardarLote(AvicolaLote lote) {
        return loteRepository.save(lote);
    }

    private AvicolaLoteRespuesta aLoteRespuesta(AvicolaLote l) {
        AvicolaLoteRespuesta dto = new AvicolaLoteRespuesta();
        dto.setId(l.getId());
        dto.setEmpresaId(l.getEmpresaId());
        dto.setEstablecimientoId(l.getEstablecimiento() != null ? l.getEstablecimiento().getId() : null);
        dto.setEstablecimientoNombre(l.getEstablecimiento() != null ? l.getEstablecimiento().getNombre() : null);
        dto.setRazaId(l.getRaza() != null ? l.getRaza().getId() : null);
        dto.setRazaNombre(l.getRaza() != null ? l.getRaza().getNombre() : null);
        dto.setNombre(l.getNombre());
        dto.setEspecie(l.getEspecie());
        dto.setOrigen(l.getOrigen());
        dto.setFechaIngreso(l.getFechaIngreso());
        dto.setCantidadInicial(l.getCantidadInicial());
        dto.setCantidadAnimales(l.getCantidadAnimales());
        dto.setPesoPromedioIngreso(l.getPesoPromedioIngreso());
        dto.setEstado(l.getEstado());
        dto.setFechaSalida(l.getFechaSalida());
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
