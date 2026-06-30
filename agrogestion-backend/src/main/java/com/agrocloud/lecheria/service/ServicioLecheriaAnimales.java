package com.agrocloud.lecheria.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.lecheria.model.dto.*;
import com.agrocloud.lecheria.model.entity.*;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioLecheriaAnimales {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final LecheriaAnimalRepository animalRepository;
    private final LecheriaRazaRepository razaRepository;
    private final LecheriaRodeoRepository rodeoRepository;
    private final LecheriaLactanciaRepository lactanciaRepository;
    private final ObjectMapper objectMapper;

    public ServicioLecheriaAnimales(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            LecheriaAnimalRepository animalRepository,
            LecheriaRazaRepository razaRepository,
            LecheriaRodeoRepository rodeoRepository,
            LecheriaLactanciaRepository lactanciaRepository,
            ObjectMapper objectMapper) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.animalRepository = animalRepository;
        this.razaRepository = razaRepository;
        this.rodeoRepository = rodeoRepository;
        this.lactanciaRepository = lactanciaRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<LecheriaAnimalRespuesta> listarAnimales() {
        return animalRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aAnimalRespuestaBasica).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LecheriaAnimalRespuesta obtenerAnimal(Long id) {
        LecheriaAnimal animal = obtenerEntidadAnimal(empresaActual(), id);
        return aAnimalRespuestaDetalle(animal);
    }

    @Transactional
    public LecheriaAnimalRespuesta crearAnimal(LecheriaAnimalSolicitud solicitud) {
        Long empresaId = empresaActual();
        validarAlta(solicitud, empresaId);
        LecheriaAnimal a = new LecheriaAnimal();
        a.setEmpresaId(empresaId);
        a.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        aplicarSolicitud(a, solicitud, empresaId);
        return aAnimalRespuestaDetalle(animalRepository.save(a));
    }

    @Transactional
    public LecheriaAnimalRespuesta actualizarAnimal(Long id, LecheriaAnimalSolicitud solicitud) {
        Long empresaId = empresaActual();
        LecheriaAnimal a = obtenerEntidadAnimal(empresaId, id);
        if (!a.getActivo()) {
            throw new IllegalStateException("No se puede modificar un animal dado de baja");
        }
        aplicarSolicitud(a, solicitud, empresaId);
        return aAnimalRespuestaDetalle(animalRepository.save(a));
    }

    public LecheriaAnimal obtenerEntidadAnimal(Long empresaId, Long animalId) {
        return animalRepository.buscarPorIdYEmpresaId(animalId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal no encontrado"));
    }

    private void validarAlta(LecheriaAnimalSolicitud solicitud, Long empresaId) {
        if (solicitud.getIdentificacion() == null || solicitud.getIdentificacion().isBlank()) {
            throw new IllegalArgumentException("La identificación es obligatoria");
        }
        if (solicitud.getEspecie() == null || solicitud.getSexo() == null) {
            throw new IllegalArgumentException("Especie y sexo son obligatorios");
        }
        if (solicitud.getFechaIngreso() == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria");
        }
        if (animalRepository.existsByEmpresaIdAndIdentificacion(empresaId, solicitud.getIdentificacion().trim())) {
            throw new ResourceConflictException("Ya existe un animal con esa identificación");
        }
    }

    private void aplicarSolicitud(LecheriaAnimal a, LecheriaAnimalSolicitud solicitud, Long empresaId) {
        if (solicitud.getIdentificacion() != null) a.setIdentificacion(solicitud.getIdentificacion().trim());
        if (solicitud.getEspecie() != null) a.setEspecie(solicitud.getEspecie());
        if (solicitud.getSexo() != null) a.setSexo(solicitud.getSexo());
        if (solicitud.getEstado() != null) a.setEstado(solicitud.getEstado());
        if (solicitud.getFechaNacimiento() != null) a.setFechaNacimiento(solicitud.getFechaNacimiento());
        if (solicitud.getFechaIngreso() != null) a.setFechaIngreso(solicitud.getFechaIngreso());
        if (solicitud.getObservaciones() != null) a.setObservaciones(solicitud.getObservaciones());
        if (solicitud.getRazaId() != null) {
            LecheriaRaza raza = razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));
            a.setRaza(raza);
        }
        if (solicitud.getRodeoId() != null) {
            LecheriaRodeo rodeo = rodeoRepository.buscarPorIdYEmpresaId(solicitud.getRodeoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rodeo no encontrado"));
            a.setRodeo(rodeo);
        }
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private LecheriaAnimalRespuesta aAnimalRespuestaBasica(LecheriaAnimal a) {
        LecheriaAnimalRespuesta dto = new LecheriaAnimalRespuesta();
        completarCamposBasicos(dto, a);
        return dto;
    }

    private LecheriaAnimalRespuesta aAnimalRespuestaDetalle(LecheriaAnimal a) {
        LecheriaAnimalRespuesta dto = aAnimalRespuestaBasica(a);
        lactanciaRepository.buscarActivaPorAnimalId(a.getId()).ifPresent(l -> {
            dto.setLactanciaActiva(aLactanciaRespuesta(l));
            dto.setDim(calcularDim(l));
        });
        resolverClima(dto, a);
        return dto;
    }

    private void resolverClima(LecheriaAnimalRespuesta dto, LecheriaAnimal a) {
        if (a.getRodeo() != null && a.getRodeo().getEstablecimiento() != null) {
            UtilCentroideCoordenadasCampo.calcularCentroide(
                    a.getRodeo().getEstablecimiento().getCoordenadas(), objectMapper)
                    .ifPresent(coords -> {
                        dto.setClimaLatitud(coords[0]);
                        dto.setClimaLongitud(coords[1]);
                    });
        }
    }

    private void completarCamposBasicos(LecheriaAnimalRespuesta dto, LecheriaAnimal a) {
        dto.setId(a.getId());
        dto.setEmpresaId(a.getEmpresaId());
        dto.setIdentificacion(a.getIdentificacion());
        dto.setEspecie(a.getEspecie());
        dto.setSexo(a.getSexo());
        dto.setEstado(a.getEstado());
        if (a.getRaza() != null) {
            dto.setRazaId(a.getRaza().getId());
            dto.setRazaNombre(a.getRaza().getNombre());
        }
        if (a.getRodeo() != null) {
            dto.setRodeoId(a.getRodeo().getId());
            dto.setRodeoNombre(a.getRodeo().getNombre());
        }
        dto.setCampanaId(a.getCampanaId());
        dto.setFechaNacimiento(a.getFechaNacimiento());
        dto.setFechaIngreso(a.getFechaIngreso());
        dto.setFechaBaja(a.getFechaBaja());
        dto.setActivo(a.getActivo());
        dto.setObservaciones(a.getObservaciones());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
    }

    LecheriaLactanciaRespuesta aLactanciaRespuesta(LecheriaLactancia l) {
        LecheriaLactanciaRespuesta dto = new LecheriaLactanciaRespuesta();
        dto.setId(l.getId());
        dto.setAnimalId(l.getAnimal().getId());
        dto.setNumeroLactancia(l.getNumeroLactancia());
        dto.setFechaParto(l.getFechaParto());
        dto.setFechaSecado(l.getFechaSecado());
        dto.setActiva(l.getActiva());
        dto.setObservaciones(l.getObservaciones());
        dto.setDim(calcularDim(l));
        return dto;
    }

    static Integer calcularDim(LecheriaLactancia l) {
        if (l == null || l.getFechaParto() == null) return null;
        LocalDate fin = l.getFechaSecado() != null ? l.getFechaSecado() : LocalDate.now();
        return (int) ChronoUnit.DAYS.between(l.getFechaParto(), fin);
    }
}
