package com.agrocloud.porcinos.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.porcinos.model.dto.*;
import com.agrocloud.porcinos.model.entity.PorcinosGalpon;
import com.agrocloud.porcinos.model.entity.PorcinosLote;
import com.agrocloud.porcinos.model.enums.PorcinosGalponEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEtapa;
import com.agrocloud.porcinos.model.enums.PorcinosLoteOrigen;
import com.agrocloud.porcinos.repository.PorcinosGalponRepository;
import com.agrocloud.porcinos.repository.PorcinosLoteRepository;
import com.agrocloud.porcinos.repository.PorcinosMuerteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioPorcinosLotes {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final PorcinosLoteRepository loteRepository;
    private final PorcinosGalponRepository galponRepository;
    private final PorcinosMuerteRepository muerteRepository;
    private final ObjectMapper objectMapper;

    public ServicioPorcinosLotes(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            PorcinosLoteRepository loteRepository,
            PorcinosGalponRepository galponRepository,
            PorcinosMuerteRepository muerteRepository,
            ObjectMapper objectMapper) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.loteRepository = loteRepository;
        this.galponRepository = galponRepository;
        this.muerteRepository = muerteRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<PorcinosLoteRespuesta> listarLotes(PorcinosLoteEstado estado, Boolean delPeriodoActivo, Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        List<PorcinosLote> lotes;
        if (galponId != null) {
            lotes = loteRepository.listarPorEmpresaIdYGalponId(empresaId, galponId);
        } else if (estado != null) {
            lotes = loteRepository.listarPorEmpresaIdYEstado(empresaId, estado);
        } else {
            lotes = loteRepository.listarPorEmpresaId(empresaId);
        }
        if (Boolean.TRUE.equals(delPeriodoActivo)) {
            Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
            lotes = lotes.stream().filter(l -> campanaId.equals(l.getCampanaId())).toList();
        }
        return lotes.stream().map(l -> aLoteRespuesta(l, empresaId)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PorcinosLoteRespuesta obtenerLote(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return aLoteRespuesta(obtenerEntidadLote(empresaId, loteId), empresaId);
    }

    @Transactional
    public PorcinosLoteRespuesta crearLote(PorcinosLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        validarAlta(solicitud);

        PorcinosGalpon galpon = galponRepository.buscarPorIdYEmpresaId(solicitud.getGalponId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Galpón no encontrado"));
        if (galpon.getEstado() != PorcinosGalponEstado.DISPONIBLE) {
            throw new ResourceConflictException("El galpón no está disponible");
        }
        if (loteRepository.existsByGalponIdAndEstado(galpon.getId(), PorcinosLoteEstado.ACTIVO)) {
            throw new ResourceConflictException("El galpón ya tiene un lote activo");
        }

        PorcinosLote lote = new PorcinosLote();
        lote.setEmpresaId(empresaId);
        lote.setGalpon(galpon);
        lote.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        lote.setNombre(solicitud.getNombre().trim());
        lote.setOrigen(solicitud.getOrigen() != null ? solicitud.getOrigen() : PorcinosLoteOrigen.EXTERNO);
        lote.setFechaIngreso(solicitud.getFechaIngreso());
        lote.setCabezasInicial(solicitud.getCabezasInicial());
        lote.setCabezasActuales(solicitud.getCabezasInicial());
        lote.setPesoPromedioIngresoKg(solicitud.getPesoPromedioIngresoKg());
        lote.setEtapa(solicitud.getEtapa() != null ? solicitud.getEtapa() : PorcinosLoteEtapa.RECRIA);
        lote.setObservaciones(solicitud.getObservaciones());
        lote.setEstado(PorcinosLoteEstado.ACTIVO);

        galpon.setEstado(PorcinosGalponEstado.OCUPADO);
        galponRepository.save(galpon);
        return aLoteRespuesta(loteRepository.save(lote), empresaId);
    }

    @Transactional
    public PorcinosLoteRespuesta actualizarLote(Long loteId, PorcinosLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        PorcinosLote lote = obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == PorcinosLoteEstado.CERRADO) {
            throw new IllegalStateException("No se puede editar un lote cerrado");
        }
        if (solicitud.getNombre() != null) {
            lote.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getEtapa() != null) {
            lote.setEtapa(solicitud.getEtapa());
        }
        if (solicitud.getObservaciones() != null) {
            lote.setObservaciones(solicitud.getObservaciones());
        }
        return aLoteRespuesta(loteRepository.save(lote), empresaId);
    }

    @Transactional
    public PorcinosLoteRespuesta cerrarLote(Long loteId, PorcinosCierreLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        PorcinosLote lote = obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == PorcinosLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        int cabezas = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        if (cabezas > 0 && !Boolean.TRUE.equals(solicitud != null ? solicitud.getConfirmarConCabezas() : null)) {
            throw new IllegalStateException(
                    "El lote aún tiene cabezas activas. Confirme el cierre con confirmarConCabezas=true");
        }
        cerrarLoteYLiberarGalpon(lote);
        return aLoteRespuesta(lote, empresaId);
    }

    @Transactional(readOnly = true)
    public PorcinosLote obtenerEntidadLote(Long empresaId, Long loteId) {
        return loteRepository.buscarPorIdYEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote porcinos no encontrado"));
    }

    @Transactional
    public PorcinosLote guardarLote(PorcinosLote lote) {
        return loteRepository.save(lote);
    }

    @Transactional
    public void cerrarLoteYLiberarGalpon(PorcinosLote lote) {
        lote.setEstado(PorcinosLoteEstado.CERRADO);
        lote.setFechaCierre(LocalDate.now());
        if (lote.getCabezasActuales() != null && lote.getCabezasActuales() < 0) {
            lote.setCabezasActuales(0);
        }
        loteRepository.save(lote);
        PorcinosGalpon galpon = lote.getGalpon();
        if (galpon != null && galpon.getEstado() == PorcinosGalponEstado.OCUPADO) {
            galpon.setEstado(PorcinosGalponEstado.DISPONIBLE);
            galponRepository.save(galpon);
        }
    }

    @Transactional(readOnly = true)
    public int calcularCabezasDisponibles(PorcinosLote lote) {
        return lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
    }

    @Transactional
    public PorcinosLote crearLoteDesdeDestete(
            PorcinosGalpon galpon,
            Long empresaId,
            String nombre,
            LocalDate fechaIngreso,
            Integer cabezas,
            BigDecimal pesoPromedioKg) {
        if (galpon.getEstado() != PorcinosGalponEstado.DISPONIBLE) {
            throw new ResourceConflictException("El galpón no está disponible para el lote de destete");
        }
        if (loteRepository.existsByGalponIdAndEstado(galpon.getId(), PorcinosLoteEstado.ACTIVO)) {
            throw new ResourceConflictException("El galpón ya tiene un lote activo");
        }
        PorcinosLote lote = new PorcinosLote();
        lote.setEmpresaId(empresaId);
        lote.setGalpon(galpon);
        lote.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        lote.setNombre(nombre);
        lote.setOrigen(PorcinosLoteOrigen.DESTETE);
        lote.setFechaIngreso(fechaIngreso);
        lote.setCabezasInicial(cabezas);
        lote.setCabezasActuales(cabezas);
        lote.setPesoPromedioIngresoKg(pesoPromedioKg);
        lote.setEtapa(PorcinosLoteEtapa.RECRIA);
        lote.setEstado(PorcinosLoteEstado.ACTIVO);
        galpon.setEstado(PorcinosGalponEstado.OCUPADO);
        galponRepository.save(galpon);
        return loteRepository.save(lote);
    }

    private static void validarAlta(PorcinosLoteSolicitud solicitud) {
        if (solicitud.getGalponId() == null) {
            throw new IllegalArgumentException("El galpón es obligatorio");
        }
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del lote es obligatorio");
        }
        if (solicitud.getFechaIngreso() == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria");
        }
        if (solicitud.getCabezasInicial() == null || solicitud.getCabezasInicial() <= 0) {
            throw new IllegalArgumentException("Las cabezas iniciales deben ser mayores a cero");
        }
        if (solicitud.getPesoPromedioIngresoKg() == null
                || solicitud.getPesoPromedioIngresoKg().signum() <= 0) {
            throw new IllegalArgumentException("El peso promedio de ingreso debe ser mayor a cero");
        }
    }

    private PorcinosLoteRespuesta aLoteRespuesta(PorcinosLote l, Long empresaId) {
        PorcinosLoteRespuesta dto = new PorcinosLoteRespuesta();
        dto.setId(l.getId());
        dto.setEmpresaId(l.getEmpresaId());
        if (l.getGalpon() != null) {
            dto.setGalponId(l.getGalpon().getId());
            dto.setGalponNombre(l.getGalpon().getNombre());
            dto.setGalponEstado(l.getGalpon().getEstado());
            if (l.getGalpon().getEstablecimiento() != null) {
                dto.setEstablecimientoId(l.getGalpon().getEstablecimiento().getId());
                dto.setEstablecimientoNombre(l.getGalpon().getEstablecimiento().getNombre());
                UtilCentroideCoordenadasCampo.calcularCentroide(
                                l.getGalpon().getEstablecimiento().getCoordenadas(), objectMapper)
                        .ifPresent(xy -> {
                            dto.setClimaLatitud(xy[0]);
                            dto.setClimaLongitud(xy[1]);
                        });
            }
        }
        dto.setCampanaId(l.getCampanaId());
        dto.setNombre(l.getNombre());
        dto.setOrigen(l.getOrigen());
        if (l.getDestete() != null) {
            dto.setDesteteId(l.getDestete().getId());
        }
        dto.setFechaIngreso(l.getFechaIngreso());
        dto.setFechaCierre(l.getFechaCierre());
        dto.setCabezasInicial(l.getCabezasInicial());
        dto.setCabezasActuales(l.getCabezasActuales());
        dto.setCabezasDisponibles(calcularCabezasDisponibles(l));
        dto.setPesoPromedioIngresoKg(l.getPesoPromedioIngresoKg());
        dto.setEtapa(l.getEtapa());
        dto.setEstado(l.getEstado());
        dto.setObservaciones(l.getObservaciones());
        LocalDate fin = l.getFechaCierre() != null ? l.getFechaCierre() : LocalDate.now();
        if (l.getFechaIngreso() != null) {
            dto.setDiasEnLote((int) ChronoUnit.DAYS.between(l.getFechaIngreso(), fin));
        }
        Integer muertes = muerteRepository.sumarCabezasPorLoteId(l.getId(), empresaId);
        if (l.getCabezasInicial() != null && l.getCabezasInicial() > 0 && muertes != null) {
            dto.setMortalidadPct(BigDecimal.valueOf(muertes)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(l.getCabezasInicial()), 4, RoundingMode.HALF_UP));
        }
        dto.setCreatedAt(l.getCreatedAt());
        dto.setUpdatedAt(l.getUpdatedAt());
        return dto;
    }
}
