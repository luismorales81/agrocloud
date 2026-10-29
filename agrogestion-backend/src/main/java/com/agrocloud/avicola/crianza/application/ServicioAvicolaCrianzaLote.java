package com.agrocloud.avicola.crianza.application;

import com.agrocloud.avicola.comun.ComposicionRazasLote;
import com.agrocloud.avicola.crianza.model.dto.AvicolaCrianzaCierreLoteSolicitud;
import com.agrocloud.avicola.crianza.model.dto.AvicolaLoteRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaLoteSolicitud;
import com.agrocloud.avicola.crianza.model.entity.AvicolaEstablecimiento;
import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.entity.AvicolaRaza;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.avicola.crianza.model.enums.AvicolaModuloOrigen;
import com.agrocloud.avicola.crianza.repository.AvicolaEstablecimientoRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaLoteRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaRazaRepository;
import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
        return listarLotes(empresaId, null, null, AvicolaModuloOrigen.AVICOLA_CRIANZA);
    }

    @Transactional(readOnly = true)
    public List<AvicolaLoteRespuesta> listarLotesPorEstado(Long empresaId, AvicolaLoteEstado estado) {
        return listarLotes(empresaId, estado, null, AvicolaModuloOrigen.AVICOLA_CRIANZA);
    }

    @Transactional(readOnly = true)
    public List<AvicolaLoteRespuesta> listarLotes(Long empresaId, AvicolaLoteEstado estado, Boolean delPeriodoActivo) {
        return listarLotes(empresaId, estado, delPeriodoActivo, AvicolaModuloOrigen.AVICOLA_CRIANZA);
    }

    @Transactional(readOnly = true)
    public List<AvicolaLoteRespuesta> listarLotes(
            Long empresaId, AvicolaLoteEstado estado, Boolean delPeriodoActivo, AvicolaModuloOrigen modulo) {
        List<AvicolaLote> lotes = estado != null
                ? loteRepository.listarPorEmpresaIdYEstadoYModulo(empresaId, estado, modulo)
                : loteRepository.listarPorEmpresaIdYModulo(empresaId, modulo);
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
        return obtenerLote(empresaId, loteId, AvicolaModuloOrigen.AVICOLA_CRIANZA);
    }

    @Transactional(readOnly = true)
    public AvicolaLoteRespuesta obtenerLote(Long empresaId, Long loteId, AvicolaModuloOrigen modulo) {
        AvicolaLote l = loteRepository.buscarPorIdYEmpresaIdYModulo(loteId, empresaId, modulo)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
        return aLoteRespuesta(l);
    }

    @Transactional
    public AvicolaLoteRespuesta crearLote(Long empresaId, AvicolaLoteSolicitud solicitud) {
        return crearLote(empresaId, AvicolaModuloOrigen.AVICOLA_CRIANZA, solicitud);
    }

    @Transactional
    public AvicolaLoteRespuesta crearLote(Long empresaId, AvicolaModuloOrigen modulo, AvicolaLoteSolicitud solicitud) {
        AvicolaEstablecimiento est = establecimientoRepository
                .buscarPorIdYEmpresaIdYModulo(solicitud.getEstablecimientoId(), empresaId, modulo)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado"));
        AvicolaLote l = new AvicolaLote();
        l.setEmpresaId(empresaId);
        l.setModuloOrigen(modulo);
        l.setEstablecimiento(est);
        asignarRazas(
                l,
                empresaId,
                modulo,
                ComposicionRazasLote.idsSolicitados(solicitud.getRazaIds(), solicitud.getRazaId(), true));
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
        return actualizarLote(empresaId, loteId, AvicolaModuloOrigen.AVICOLA_CRIANZA, solicitud);
    }

    @Transactional
    public AvicolaLoteRespuesta actualizarLote(
            Long empresaId, Long loteId, AvicolaModuloOrigen modulo, AvicolaLoteSolicitud solicitud) {
        AvicolaLote l = loteRepository.buscarPorIdYEmpresaIdYModulo(loteId, empresaId, modulo)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
        List<Long> idsRazas = ComposicionRazasLote.idsSolicitados(solicitud.getRazaIds(), solicitud.getRazaId(), false);
        if (l.getEstado() == AvicolaLoteEstado.CERRADO) {
            if (idsRazas == null) {
                throw new IllegalArgumentException("No se puede editar un lote cerrado");
            }
            asignarRazas(l, empresaId, modulo, idsRazas);
            return aLoteRespuesta(loteRepository.save(l));
        }
        if (idsRazas != null) {
            asignarRazas(l, empresaId, modulo, idsRazas);
        }
        if (solicitud.getEstablecimientoId() != null) {
            AvicolaEstablecimiento est = establecimientoRepository
                    .buscarPorIdYEmpresaIdYModulo(solicitud.getEstablecimientoId(), empresaId, modulo)
                    .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado"));
            l.setEstablecimiento(est);
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
        return obtenerEntidadLote(empresaId, loteId, AvicolaModuloOrigen.AVICOLA_CRIANZA);
    }

    @Transactional(readOnly = true)
    public AvicolaLote obtenerEntidadLote(Long empresaId, Long loteId, AvicolaModuloOrigen modulo) {
        return loteRepository.buscarPorIdYEmpresaIdYModulo(loteId, empresaId, modulo)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
    }

    @Transactional
    public AvicolaLote guardarLote(AvicolaLote lote) {
        return loteRepository.save(lote);
    }

    /**
     * Cierre manual de lote. Si quedan aves en plantel, exige confirmación explícita.
     */
    @Transactional
    public AvicolaLoteRespuesta cerrarLote(Long empresaId, Long loteId, AvicolaCrianzaCierreLoteSolicitud solicitud) {
        AvicolaLote lote = obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == AvicolaLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        int cantidad = lote.getCantidadAnimales() != null ? lote.getCantidadAnimales() : 0;
        if (cantidad > 0) {
            boolean confirmar = solicitud != null && Boolean.TRUE.equals(solicitud.getConfirmarConAvesPendientes());
            if (!confirmar) {
                throw new IllegalStateException(
                        "Quedan " + cantidad + " aves en el lote. Confirmá el cierre o registrá faena/venta.");
            }
        }
        lote.setEstado(AvicolaLoteEstado.CERRADO);
        lote.setFechaSalida(LocalDate.now());
        guardarLote(lote);
        return obtenerLote(empresaId, loteId);
    }

    private AvicolaLoteRespuesta aLoteRespuesta(AvicolaLote l) {
        AvicolaLoteRespuesta dto = new AvicolaLoteRespuesta();
        dto.setId(l.getId());
        dto.setEmpresaId(l.getEmpresaId());
        dto.setEstablecimientoId(l.getEstablecimiento() != null ? l.getEstablecimiento().getId() : null);
        dto.setEstablecimientoNombre(l.getEstablecimiento() != null ? l.getEstablecimiento().getNombre() : null);
        completarRazas(dto, l);
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

    private void asignarRazas(AvicolaLote lote, Long empresaId, AvicolaModuloOrigen modulo, List<Long> ids) {
        List<AvicolaRaza> encontradas = razaRepository.buscarPorIdsYEmpresaIdYModulo(ids, empresaId, modulo);
        if (encontradas.size() != ids.size()) {
            throw new ResourceNotFoundException("Raza no encontrada");
        }
        Set<Long> previas = lote.getRazas().stream().map(AvicolaRaza::getId).collect(Collectors.toSet());
        for (AvicolaRaza raza : encontradas) {
            boolean yaEstaba = previas.contains(raza.getId());
            if (!yaEstaba && !Boolean.TRUE.equals(raza.getActivo())) {
                throw new IllegalArgumentException("La raza " + raza.getNombre() + " no está activa");
            }
        }
        lote.getRazas().clear();
        lote.getRazas().addAll(encontradas);
        AvicolaRaza principal = encontradas.stream()
                .min(Comparator.comparing(
                        raza -> raza.getNombre() == null ? "" : raza.getNombre(), String.CASE_INSENSITIVE_ORDER))
                .orElseThrow(() -> new IllegalArgumentException("El lote debe tener al menos una raza"));
        lote.setRaza(principal);
    }

    private void completarRazas(AvicolaLoteRespuesta dto, AvicolaLote lote) {
        List<AvicolaRaza> lista = new ArrayList<>(lote.getRazas() != null ? lote.getRazas() : List.of());
        if (lista.isEmpty() && lote.getRaza() != null) {
            lista.add(lote.getRaza());
        }
        lista.sort(Comparator.comparing(
                raza -> raza.getNombre() == null ? "" : raza.getNombre(), String.CASE_INSENSITIVE_ORDER));
        dto.setRazaIds(lista.stream().map(AvicolaRaza::getId).toList());
        dto.setRazaNombre(ComposicionRazasLote.unirNombres(lista.stream().map(AvicolaRaza::getNombre).toList()));
        if (!lista.isEmpty()) {
            dto.setRazaId(lista.get(0).getId());
        }
    }
}
