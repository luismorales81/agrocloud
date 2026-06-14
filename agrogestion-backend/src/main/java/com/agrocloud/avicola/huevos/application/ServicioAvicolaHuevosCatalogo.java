package com.agrocloud.avicola.huevos.application;

import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoEstablecimientoRespuesta;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoEstablecimientoSolicitud;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoRazaRespuesta;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoRazaSolicitud;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoEstablecimiento;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoRaza;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoEstablecimientoRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoRazaRepository;
import com.agrocloud.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaHuevosCatalogo {

    private final AvicolaHuevoEstablecimientoRepository establecimientoRepository;
    private final AvicolaHuevoRazaRepository razaRepository;

    public ServicioAvicolaHuevosCatalogo(
            AvicolaHuevoEstablecimientoRepository establecimientoRepository,
            AvicolaHuevoRazaRepository razaRepository) {
        this.establecimientoRepository = establecimientoRepository;
        this.razaRepository = razaRepository;
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoEstablecimientoRespuesta> listarEstablecimientos(Long empresaId) {
        return establecimientoRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aEstablecimientoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaHuevoEstablecimientoRespuesta crearEstablecimiento(Long empresaId, AvicolaHuevoEstablecimientoSolicitud solicitud) {
        AvicolaHuevoEstablecimiento e = new AvicolaHuevoEstablecimiento();
        e.setEmpresaId(empresaId);
        e.setNombre(solicitud.getNombre());
        e.setUbicacion(normalizarTextoOpcional(solicitud.getUbicacion()));
        e.setCoordenadas(normalizarJsonCoordenadas(solicitud.getCoordenadas()));
        e.setObservaciones(solicitud.getObservaciones());
        if (solicitud.getActivo() != null) {
            e.setActivo(solicitud.getActivo());
        }
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional
    public AvicolaHuevoEstablecimientoRespuesta actualizarEstablecimiento(Long empresaId, Long id, AvicolaHuevoEstablecimientoSolicitud solicitud) {
        AvicolaHuevoEstablecimiento e = establecimientoRepository.buscarPorIdYEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento de huevos no encontrado"));
        if (solicitud.getNombre() != null) {
            e.setNombre(solicitud.getNombre());
        }
        if (solicitud.getUbicacion() != null) {
            e.setUbicacion(normalizarTextoOpcional(solicitud.getUbicacion()));
        }
        if (solicitud.getCoordenadas() != null) {
            e.setCoordenadas(normalizarJsonCoordenadas(solicitud.getCoordenadas()));
        }
        if (solicitud.getObservaciones() != null) {
            e.setObservaciones(solicitud.getObservaciones());
        }
        if (solicitud.getActivo() != null) {
            e.setActivo(solicitud.getActivo());
        }
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoRazaRespuesta> listarRazas(Long empresaId) {
        return razaRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aRazaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaHuevoRazaRespuesta crearRaza(Long empresaId, AvicolaHuevoRazaSolicitud solicitud) {
        AvicolaHuevoRaza r = new AvicolaHuevoRaza();
        r.setEmpresaId(empresaId);
        r.setNombre(solicitud.getNombre());
        if (solicitud.getActivo() != null) {
            r.setActivo(solicitud.getActivo());
        }
        return aRazaRespuesta(razaRepository.save(r));
    }

    @Transactional
    public AvicolaHuevoRazaRespuesta actualizarRaza(Long empresaId, Long id, AvicolaHuevoRazaSolicitud solicitud) {
        AvicolaHuevoRaza r = razaRepository.buscarPorIdYEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Raza de huevos no encontrada"));
        if (solicitud.getNombre() != null) {
            r.setNombre(solicitud.getNombre());
        }
        if (solicitud.getActivo() != null) {
            r.setActivo(solicitud.getActivo());
        }
        return aRazaRespuesta(razaRepository.save(r));
    }

    private static String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }

    /** Cadena vacía o solo espacios borra coordenadas en base de datos. */
    private static String normalizarJsonCoordenadas(String json) {
        if (json == null) {
            return null;
        }
        String t = json.trim();
        return t.isEmpty() ? null : t;
    }

    private AvicolaHuevoEstablecimientoRespuesta aEstablecimientoRespuesta(AvicolaHuevoEstablecimiento e) {
        AvicolaHuevoEstablecimientoRespuesta dto = new AvicolaHuevoEstablecimientoRespuesta();
        dto.setId(e.getId());
        dto.setEmpresaId(e.getEmpresaId());
        dto.setNombre(e.getNombre());
        dto.setUbicacion(e.getUbicacion());
        dto.setCoordenadas(e.getCoordenadas());
        dto.setObservaciones(e.getObservaciones());
        dto.setActivo(e.getActivo());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    private AvicolaHuevoRazaRespuesta aRazaRespuesta(AvicolaHuevoRaza r) {
        AvicolaHuevoRazaRespuesta dto = new AvicolaHuevoRazaRespuesta();
        dto.setId(r.getId());
        dto.setEmpresaId(r.getEmpresaId());
        dto.setNombre(r.getNombre());
        dto.setActivo(r.getActivo());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
