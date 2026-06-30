package com.agrocloud.lecheria.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.lecheria.model.dto.LecheriaMovimientoSenasaRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaMovimientoSenasaSolicitud;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaMovimientoSenasa;
import com.agrocloud.lecheria.repository.LecheriaMovimientoSenasaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServicioLecheriaSenasa {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioLecheriaAnimales servicioAnimales;
    private final LecheriaMovimientoSenasaRepository movimientoRepository;

    public ServicioLecheriaSenasa(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioLecheriaAnimales servicioAnimales,
            LecheriaMovimientoSenasaRepository movimientoRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioAnimales = servicioAnimales;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional
    public LecheriaMovimientoSenasaRespuesta registrarMovimiento(LecheriaMovimientoSenasaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        if (solicitud.getTipoMovimiento() == null || solicitud.getFecha() == null) {
            throw new IllegalArgumentException("Tipo de movimiento y fecha son obligatorios");
        }
        LecheriaMovimientoSenasa m = new LecheriaMovimientoSenasa();
        m.setEmpresaId(empresaId);
        m.setTipoMovimiento(solicitud.getTipoMovimiento());
        m.setFecha(solicitud.getFecha());
        m.setExportado(false);
        if (solicitud.getAnimalId() != null) {
            LecheriaAnimal animal = servicioAnimales.obtenerEntidadAnimal(empresaId, solicitud.getAnimalId());
            m.setAnimal(animal);
        }
        m.setDatosJson(solicitud.getDatosJson() != null ? solicitud.getDatosJson() : generarBorradorJson(solicitud));
        return aRespuesta(movimientoRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<LecheriaMovimientoSenasaRespuesta> listarPendientesExportacion() {
        return movimientoRepository.listarPendientesExportacion(empresaActual()).stream()
                .map(this::aRespuesta).collect(Collectors.toList());
    }

    private String generarBorradorJson(LecheriaMovimientoSenasaSolicitud solicitud) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("tipoMovimiento", solicitud.getTipoMovimiento());
        datos.put("fecha", solicitud.getFecha().toString());
        datos.put("animalId", solicitud.getAnimalId());
        datos.put("formato", "SENASA_BORRADOR_v1");
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(datos);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private LecheriaMovimientoSenasaRespuesta aRespuesta(LecheriaMovimientoSenasa m) {
        LecheriaMovimientoSenasaRespuesta dto = new LecheriaMovimientoSenasaRespuesta();
        dto.setId(m.getId());
        dto.setAnimalId(m.getAnimal() != null ? m.getAnimal().getId() : null);
        dto.setTipoMovimiento(m.getTipoMovimiento());
        dto.setFecha(m.getFecha());
        dto.setDatosJson(m.getDatosJson());
        dto.setExportado(m.getExportado());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
