package com.agrocloud.lecheria.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.lecheria.model.dto.LecheriaRegistroOrdeneRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaRegistroOrdeneSolicitud;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaLactancia;
import com.agrocloud.lecheria.model.entity.LecheriaRegistroOrdene;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.repository.LecheriaLactanciaRepository;
import com.agrocloud.lecheria.repository.LecheriaRegistroOrdeneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioLecheriaOrdene {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioLecheriaAnimales servicioAnimales;
    private final LecheriaLactanciaRepository lactanciaRepository;
    private final LecheriaRegistroOrdeneRepository ordeneRepository;

    public ServicioLecheriaOrdene(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioLecheriaAnimales servicioAnimales,
            LecheriaLactanciaRepository lactanciaRepository,
            LecheriaRegistroOrdeneRepository ordeneRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioAnimales = servicioAnimales;
        this.lactanciaRepository = lactanciaRepository;
        this.ordeneRepository = ordeneRepository;
    }

    @Transactional(readOnly = true)
    public List<LecheriaRegistroOrdeneRespuesta> listarOrdenes(Long animalId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        return ordeneRepository.listarPorAnimalId(animalId).stream()
                .map(this::aOrdeneRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaRegistroOrdeneRespuesta registrarOrdene(Long animalId, LecheriaRegistroOrdeneSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        LecheriaAnimal animal = servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        validarAnimalParaOrdene(animal);
        validarSolicitud(solicitud, animalId);

        LecheriaLactancia lactancia = lactanciaRepository.buscarActivaPorAnimalId(animalId)
                .orElseThrow(() -> new IllegalStateException("El animal no tiene lactancia activa"));

        LecheriaRegistroOrdene o = new LecheriaRegistroOrdene();
        o.setAnimal(animal);
        o.setLactancia(lactancia);
        o.setEmpresaId(empresaId);
        o.setFecha(solicitud.getFecha());
        o.setTurno(solicitud.getTurno());
        o.setLitros(solicitud.getLitros());
        o.setGrasaPct(solicitud.getGrasaPct());
        o.setProteinaPct(solicitud.getProteinaPct());
        o.setRcs(solicitud.getRcs());
        o.setTemperaturaAmbiente(solicitud.getTemperaturaAmbiente());
        o.setHumedadAmbiente(solicitud.getHumedadAmbiente());
        o.setObservaciones(solicitud.getObservaciones());
        return aOrdeneRespuesta(ordeneRepository.save(o));
    }

    private void validarAnimalParaOrdene(LecheriaAnimal animal) {
        if (!Boolean.TRUE.equals(animal.getActivo())) {
            throw new IllegalStateException("El animal está dado de baja");
        }
        if (animal.getEstado() != LecheriaEstadoAnimal.LACTANDO) {
            throw new IllegalStateException("Solo se puede registrar ordeñe en animales LACTANDO");
        }
    }

    private void validarSolicitud(LecheriaRegistroOrdeneSolicitud solicitud, Long animalId) {
        if (solicitud.getFecha() == null || solicitud.getTurno() == null) {
            throw new IllegalArgumentException("Fecha y turno son obligatorios");
        }
        if (solicitud.getLitros() == null || solicitud.getLitros().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Los litros deben ser mayores a cero");
        }
        if (ordeneRepository.existsByAnimalIdAndFechaAndTurno(animalId, solicitud.getFecha(), solicitud.getTurno())) {
            throw new ResourceConflictException("Ya existe un registro de ordeñe para ese animal, fecha y turno");
        }
    }

    private LecheriaRegistroOrdeneRespuesta aOrdeneRespuesta(LecheriaRegistroOrdene o) {
        LecheriaRegistroOrdeneRespuesta dto = new LecheriaRegistroOrdeneRespuesta();
        dto.setId(o.getId());
        dto.setAnimalId(o.getAnimal().getId());
        dto.setLactanciaId(o.getLactancia().getId());
        dto.setFecha(o.getFecha());
        dto.setTurno(o.getTurno());
        dto.setLitros(o.getLitros());
        dto.setGrasaPct(o.getGrasaPct());
        dto.setProteinaPct(o.getProteinaPct());
        dto.setRcs(o.getRcs());
        dto.setTemperaturaAmbiente(o.getTemperaturaAmbiente());
        dto.setHumedadAmbiente(o.getHumedadAmbiente());
        dto.setObservaciones(o.getObservaciones());
        dto.setCreatedAt(o.getCreatedAt());
        return dto;
    }
}
