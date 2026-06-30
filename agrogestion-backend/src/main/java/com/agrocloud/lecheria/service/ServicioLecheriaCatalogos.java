package com.agrocloud.lecheria.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.lecheria.model.dto.*;
import com.agrocloud.lecheria.model.entity.*;
import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import com.agrocloud.lecheria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioLecheriaCatalogos {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final LecheriaEstablecimientoRepository establecimientoRepository;
    private final LecheriaRodeoRepository rodeoRepository;
    private final LecheriaRazaRepository razaRepository;
    private final LecheriaMotivoBajaRepository motivoBajaRepository;

    public ServicioLecheriaCatalogos(
            ServicioSeguridadContexto servicioSeguridadContexto,
            LecheriaEstablecimientoRepository establecimientoRepository,
            LecheriaRodeoRepository rodeoRepository,
            LecheriaRazaRepository razaRepository,
            LecheriaMotivoBajaRepository motivoBajaRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.establecimientoRepository = establecimientoRepository;
        this.rodeoRepository = rodeoRepository;
        this.razaRepository = razaRepository;
        this.motivoBajaRepository = motivoBajaRepository;
    }

    @Transactional(readOnly = true)
    public List<LecheriaEstablecimientoRespuesta> listarEstablecimientos() {
        return establecimientoRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aEstablecimientoRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaEstablecimientoRespuesta crearEstablecimiento(LecheriaEstablecimientoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        LecheriaEstablecimiento e = new LecheriaEstablecimiento();
        e.setEmpresaId(empresaActual());
        e.setNombre(solicitud.getNombre().trim());
        e.setUbicacion(normalizarOpcional(solicitud.getUbicacion()));
        e.setCoordenadas(normalizarJsonCoordenadas(solicitud.getCoordenadas()));
        e.setCapacidadAnimales(solicitud.getCapacidadAnimales());
        if (solicitud.getActivo() != null) e.setActivo(solicitud.getActivo());
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional
    public LecheriaEstablecimientoRespuesta actualizarEstablecimiento(Long id, LecheriaEstablecimientoSolicitud solicitud) {
        LecheriaEstablecimiento e = requerirEstablecimiento(id);
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            e.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getUbicacion() != null) e.setUbicacion(normalizarOpcional(solicitud.getUbicacion()));
        if (solicitud.getCoordenadas() != null) e.setCoordenadas(normalizarJsonCoordenadas(solicitud.getCoordenadas()));
        if (solicitud.getCapacidadAnimales() != null) e.setCapacidadAnimales(solicitud.getCapacidadAnimales());
        if (solicitud.getActivo() != null) e.setActivo(solicitud.getActivo());
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<LecheriaRodeoRespuesta> listarRodeos(Long establecimientoId) {
        requerirEstablecimiento(establecimientoId);
        return rodeoRepository.listarPorEstablecimientoId(establecimientoId).stream()
                .map(this::aRodeoRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaRodeoRespuesta crearRodeo(Long establecimientoId, LecheriaRodeoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        LecheriaEstablecimiento est = requerirEstablecimiento(establecimientoId);
        LecheriaRodeo r = new LecheriaRodeo();
        r.setEstablecimiento(est);
        r.setEmpresaId(empresaActual());
        r.setNombre(solicitud.getNombre().trim());
        r.setEspecie(solicitud.getEspecie());
        if (solicitud.getActivo() != null) r.setActivo(solicitud.getActivo());
        return aRodeoRespuesta(rodeoRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<LecheriaRazaRespuesta> listarRazas(LecheriaEspecie especie) {
        List<LecheriaRaza> razas = especie != null
                ? razaRepository.listarPorEmpresaIdYEspecie(empresaActual(), especie)
                : razaRepository.listarPorEmpresaId(empresaActual());
        return razas.stream().map(this::aRazaRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaRazaRespuesta crearRaza(LecheriaRazaSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        LecheriaRaza r = new LecheriaRaza();
        r.setEmpresaId(empresaActual());
        r.setNombre(solicitud.getNombre().trim());
        r.setEspecie(solicitud.getEspecie());
        if (solicitud.getActivo() != null) r.setActivo(solicitud.getActivo());
        return aRazaRespuesta(razaRepository.save(r));
    }

    @Transactional
    public LecheriaRazaRespuesta actualizarRaza(Long id, LecheriaRazaSolicitud solicitud) {
        LecheriaRaza r = razaRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            r.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getEspecie() != null) r.setEspecie(solicitud.getEspecie());
        if (solicitud.getActivo() != null) r.setActivo(solicitud.getActivo());
        return aRazaRespuesta(razaRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<LecheriaMotivoBajaRespuesta> listarMotivosBaja() {
        return motivoBajaRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aMotivoBajaRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaMotivoBajaRespuesta crearMotivoBaja(LecheriaMotivoBajaSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        LecheriaMotivoBaja m = new LecheriaMotivoBaja();
        m.setEmpresaId(empresaActual());
        m.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) m.setActivo(solicitud.getActivo());
        return aMotivoBajaRespuesta(motivoBajaRepository.save(m));
    }

    @Transactional
    public LecheriaMotivoBajaRespuesta actualizarMotivoBaja(Long id, LecheriaMotivoBajaSolicitud solicitud) {
        LecheriaMotivoBaja m = motivoBajaRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Motivo de baja no encontrado"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            m.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) m.setActivo(solicitud.getActivo());
        return aMotivoBajaRespuesta(motivoBajaRepository.save(m));
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private LecheriaEstablecimiento requerirEstablecimiento(Long id) {
        return establecimientoRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento lechería no encontrado"));
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    private static String normalizarOpcional(String valor) {
        if (valor == null) return null;
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }

    private static String normalizarJsonCoordenadas(String json) {
        if (json == null) return null;
        String t = json.trim();
        return t.isEmpty() ? null : t;
    }

    private LecheriaEstablecimientoRespuesta aEstablecimientoRespuesta(LecheriaEstablecimiento e) {
        LecheriaEstablecimientoRespuesta dto = new LecheriaEstablecimientoRespuesta();
        dto.setId(e.getId());
        dto.setEmpresaId(e.getEmpresaId());
        dto.setNombre(e.getNombre());
        dto.setUbicacion(e.getUbicacion());
        dto.setCoordenadas(e.getCoordenadas());
        dto.setCapacidadAnimales(e.getCapacidadAnimales());
        dto.setActivo(e.getActivo());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    private LecheriaRodeoRespuesta aRodeoRespuesta(LecheriaRodeo r) {
        LecheriaRodeoRespuesta dto = new LecheriaRodeoRespuesta();
        dto.setId(r.getId());
        dto.setEstablecimientoId(r.getEstablecimiento().getId());
        dto.setEmpresaId(r.getEmpresaId());
        dto.setNombre(r.getNombre());
        dto.setEspecie(r.getEspecie());
        dto.setActivo(r.getActivo());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }

    private LecheriaRazaRespuesta aRazaRespuesta(LecheriaRaza r) {
        LecheriaRazaRespuesta dto = new LecheriaRazaRespuesta();
        dto.setId(r.getId());
        dto.setEmpresaId(r.getEmpresaId());
        dto.setNombre(r.getNombre());
        dto.setEspecie(r.getEspecie());
        dto.setActivo(r.getActivo());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }

    private LecheriaMotivoBajaRespuesta aMotivoBajaRespuesta(LecheriaMotivoBaja m) {
        LecheriaMotivoBajaRespuesta dto = new LecheriaMotivoBajaRespuesta();
        dto.setId(m.getId());
        dto.setEmpresaId(m.getEmpresaId());
        dto.setNombre(m.getNombre());
        dto.setActivo(m.getActivo());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
