package com.agrocloud.lecheria.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.lecheria.model.dto.LecheriaEventoReproductivoRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaEventoReproductivoSolicitud;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaEventoReproductivo;
import com.agrocloud.lecheria.model.entity.LecheriaLactancia;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaTipoEventoReproductivo;
import com.agrocloud.lecheria.repository.LecheriaEventoReproductivoRepository;
import com.agrocloud.lecheria.repository.LecheriaLactanciaRepository;
import com.agrocloud.lecheria.repository.LecheriaParametroEspecieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioLecheriaReproduccion {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioLecheriaAnimales servicioAnimales;
    private final LecheriaEventoReproductivoRepository eventoRepository;
    private final LecheriaLactanciaRepository lactanciaRepository;
    private final LecheriaParametroEspecieRepository parametroEspecieRepository;

    public ServicioLecheriaReproduccion(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioLecheriaAnimales servicioAnimales,
            LecheriaEventoReproductivoRepository eventoRepository,
            LecheriaLactanciaRepository lactanciaRepository,
            LecheriaParametroEspecieRepository parametroEspecieRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioAnimales = servicioAnimales;
        this.eventoRepository = eventoRepository;
        this.lactanciaRepository = lactanciaRepository;
        this.parametroEspecieRepository = parametroEspecieRepository;
    }

    @Transactional(readOnly = true)
    public List<LecheriaEventoReproductivoRespuesta> listarEventos(Long animalId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        return eventoRepository.listarPorAnimalId(animalId).stream()
                .map(this::aEventoRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaEventoReproductivoRespuesta registrarEvento(Long animalId, LecheriaEventoReproductivoSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        LecheriaAnimal animal = servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        if (!Boolean.TRUE.equals(animal.getActivo())) {
            throw new IllegalStateException("El animal está dado de baja");
        }
        if (solicitud.getTipo() == null || solicitud.getFecha() == null) {
            throw new IllegalArgumentException("Tipo y fecha son obligatorios");
        }

        LecheriaEventoReproductivo e = new LecheriaEventoReproductivo();
        e.setAnimal(animal);
        e.setEmpresaId(empresaId);
        e.setTipo(solicitud.getTipo());
        e.setFecha(solicitud.getFecha());
        e.setResultado(solicitud.getResultado());
        e.setToroPajuela(solicitud.getToroPajuela());
        e.setFechaPrevistaParto(solicitud.getFechaPrevistaParto());
        e.setObservaciones(solicitud.getObservaciones());

        aplicarEfectosEvento(animal, e);
        eventoRepository.save(e);
        return aEventoRespuesta(e);
    }

    private void aplicarEfectosEvento(LecheriaAnimal animal, LecheriaEventoReproductivo e) {
        switch (e.getTipo()) {
            case PARTO -> crearLactancia(animal, e.getFecha());
            case SECADO -> cerrarLactanciaActiva(animal, e.getFecha());
            case TACTO -> {
                if ("POSITIVO".equalsIgnoreCase(e.getResultado())) {
                    animal.setEstado(LecheriaEstadoAnimal.PRENADA);
                    if (e.getFechaPrevistaParto() == null) {
                        parametroEspecieRepository.findById(animal.getEspecie()).ifPresent(p ->
                                e.setFechaPrevistaParto(e.getFecha().plusDays(p.getDiasGestacion())));
                    }
                }
            }
            case SERVICIO -> {
                if (e.getFechaPrevistaParto() == null) {
                    parametroEspecieRepository.findById(animal.getEspecie()).ifPresent(p ->
                            e.setFechaPrevistaParto(e.getFecha().plusDays(p.getDiasGestacion())));
                }
            }
            case ABORTO -> animal.setEstado(LecheriaEstadoAnimal.SECA);
            default -> { }
        }
    }

    private void crearLactancia(LecheriaAnimal animal, LocalDate fechaParto) {
        lactanciaRepository.buscarActivaPorAnimalId(animal.getId()).ifPresent(l -> {
            throw new ResourceConflictException("El animal ya tiene una lactancia activa");
        });
        Integer numero = lactanciaRepository.contarPorAnimalId(animal.getId());
        LecheriaLactancia lact = new LecheriaLactancia();
        lact.setAnimal(animal);
        lact.setEmpresaId(animal.getEmpresaId());
        lact.setNumeroLactancia(numero + 1);
        lact.setFechaParto(fechaParto);
        lact.setActiva(true);
        lactanciaRepository.save(lact);
        animal.setEstado(LecheriaEstadoAnimal.LACTANDO);
    }

    private void cerrarLactanciaActiva(LecheriaAnimal animal, LocalDate fechaSecado) {
        LecheriaLactancia lact = lactanciaRepository.buscarActivaPorAnimalId(animal.getId())
                .orElseThrow(() -> new IllegalStateException("No hay lactancia activa para secar"));
        lact.setActiva(false);
        lact.setFechaSecado(fechaSecado);
        lactanciaRepository.save(lact);
        animal.setEstado(animal.getEstado() == LecheriaEstadoAnimal.PRENADA
                ? LecheriaEstadoAnimal.PRENADA : LecheriaEstadoAnimal.SECA);
    }

    private LecheriaEventoReproductivoRespuesta aEventoRespuesta(LecheriaEventoReproductivo e) {
        LecheriaEventoReproductivoRespuesta dto = new LecheriaEventoReproductivoRespuesta();
        dto.setId(e.getId());
        dto.setAnimalId(e.getAnimal().getId());
        dto.setTipo(e.getTipo());
        dto.setFecha(e.getFecha());
        dto.setResultado(e.getResultado());
        dto.setToroPajuela(e.getToroPajuela());
        dto.setFechaPrevistaParto(e.getFechaPrevistaParto());
        dto.setObservaciones(e.getObservaciones());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
