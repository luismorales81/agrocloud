package com.agrocloud.porcinos.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.porcinos.model.dto.*;
import com.agrocloud.porcinos.model.entity.PorcinosDieta;
import com.agrocloud.porcinos.model.entity.PorcinosDietaFase;
import com.agrocloud.porcinos.repository.PorcinosDietaFaseRepository;
import com.agrocloud.porcinos.repository.PorcinosDietaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioPorcinosDietas {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final PorcinosDietaRepository dietaRepository;
    private final PorcinosDietaFaseRepository faseRepository;

    public ServicioPorcinosDietas(
            ServicioSeguridadContexto servicioSeguridadContexto,
            PorcinosDietaRepository dietaRepository,
            PorcinosDietaFaseRepository faseRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.dietaRepository = dietaRepository;
        this.faseRepository = faseRepository;
    }

    @Transactional(readOnly = true)
    public List<PorcinosDietaRespuesta> listarDietas() {
        return dietaRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aDietaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PorcinosDietaRespuesta obtenerDieta(Long dietaId) {
        return aDietaRespuesta(obtenerEntidadDieta(dietaId));
    }

    @Transactional
    public PorcinosDietaRespuesta crearDieta(PorcinosDietaSolicitud solicitud) {
        validarNombreDieta(solicitud);
        PorcinosDieta dieta = new PorcinosDieta();
        dieta.setEmpresaId(empresaActual());
        dieta.setNombre(solicitud.getNombre().trim());
        dieta.setActivo(solicitud.getActivo() != null ? solicitud.getActivo() : Boolean.TRUE);
        return aDietaRespuesta(dietaRepository.save(dieta));
    }

    @Transactional
    public PorcinosDietaRespuesta actualizarDieta(Long dietaId, PorcinosDietaSolicitud solicitud) {
        PorcinosDieta dieta = obtenerEntidadDieta(dietaId);
        if (solicitud.getNombre() != null && !solicitud.getNombre().isBlank()) {
            dieta.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) {
            dieta.setActivo(solicitud.getActivo());
        }
        return aDietaRespuesta(dietaRepository.save(dieta));
    }

    @Transactional
    public PorcinosDietaFaseRespuesta crearFase(Long dietaId, PorcinosDietaFaseSolicitud solicitud) {
        PorcinosDieta dieta = obtenerEntidadDieta(dietaId);
        validarFase(solicitud);
        PorcinosDietaFase fase = new PorcinosDietaFase();
        fase.setDieta(dieta);
        fase.setNombreFase(solicitud.getNombreFase().trim());
        fase.setDiasDesdeIngreso(solicitud.getDiasDesdeIngreso() != null ? solicitud.getDiasDesdeIngreso() : 0);
        fase.setKgCabezaDia(solicitud.getKgCabezaDia());
        fase.setInsumoId(solicitud.getInsumoId());
        return aFaseRespuesta(faseRepository.save(fase));
    }

    @Transactional
    public PorcinosDietaFaseRespuesta actualizarFase(Long dietaId, Long faseId, PorcinosDietaFaseSolicitud solicitud) {
        PorcinosDietaFase fase = faseRepository.buscarPorIdYDietaIdYEmpresaId(faseId, dietaId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Fase de dieta no encontrada"));
        if (solicitud.getNombreFase() != null && !solicitud.getNombreFase().isBlank()) {
            fase.setNombreFase(solicitud.getNombreFase().trim());
        }
        if (solicitud.getDiasDesdeIngreso() != null) {
            fase.setDiasDesdeIngreso(solicitud.getDiasDesdeIngreso());
        }
        if (solicitud.getKgCabezaDia() != null) {
            fase.setKgCabezaDia(solicitud.getKgCabezaDia());
        }
        if (solicitud.getInsumoId() != null) {
            fase.setInsumoId(solicitud.getInsumoId());
        }
        return aFaseRespuesta(faseRepository.save(fase));
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private PorcinosDieta obtenerEntidadDieta(Long dietaId) {
        return dietaRepository.buscarPorIdYEmpresaId(dietaId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Dieta no encontrada"));
    }

    private static void validarNombreDieta(PorcinosDietaSolicitud solicitud) {
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la dieta es obligatorio");
        }
    }

    private static void validarFase(PorcinosDietaFaseSolicitud solicitud) {
        if (solicitud.getNombreFase() == null || solicitud.getNombreFase().isBlank()) {
            throw new IllegalArgumentException("El nombre de la fase es obligatorio");
        }
        if (solicitud.getKgCabezaDia() == null || solicitud.getKgCabezaDia().signum() <= 0) {
            throw new IllegalArgumentException("Los kg/cabeza/día deben ser mayores a cero");
        }
    }

    private PorcinosDietaRespuesta aDietaRespuesta(PorcinosDieta d) {
        PorcinosDietaRespuesta dto = new PorcinosDietaRespuesta();
        dto.setId(d.getId());
        dto.setEmpresaId(d.getEmpresaId());
        dto.setNombre(d.getNombre());
        dto.setActivo(d.getActivo());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        dto.setFases(d.getFases().stream().map(this::aFaseRespuesta).collect(Collectors.toList()));
        return dto;
    }

    private PorcinosDietaFaseRespuesta aFaseRespuesta(PorcinosDietaFase f) {
        PorcinosDietaFaseRespuesta dto = new PorcinosDietaFaseRespuesta();
        dto.setId(f.getId());
        dto.setDietaId(f.getDieta() != null ? f.getDieta().getId() : null);
        dto.setNombreFase(f.getNombreFase());
        dto.setDiasDesdeIngreso(f.getDiasDesdeIngreso());
        dto.setKgCabezaDia(f.getKgCabezaDia());
        dto.setInsumoId(f.getInsumoId());
        dto.setCreatedAt(f.getCreatedAt());
        dto.setUpdatedAt(f.getUpdatedAt());
        return dto;
    }
}
