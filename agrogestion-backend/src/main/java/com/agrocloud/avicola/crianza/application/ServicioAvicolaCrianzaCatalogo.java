package com.agrocloud.avicola.crianza.application;

import com.agrocloud.avicola.crianza.model.dto.AvicolaEstablecimientoRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaEstablecimientoSolicitud;
import com.agrocloud.avicola.crianza.model.dto.AvicolaRazaRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaRazaSolicitud;
import com.agrocloud.avicola.crianza.model.entity.AvicolaEstablecimiento;
import com.agrocloud.avicola.crianza.model.entity.AvicolaRaza;
import com.agrocloud.avicola.crianza.model.enums.AvicolaModuloOrigen;
import com.agrocloud.avicola.crianza.repository.AvicolaEstablecimientoRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaRazaRepository;
import com.agrocloud.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaCrianzaCatalogo {

    private final AvicolaEstablecimientoRepository establecimientoRepository;
    private final AvicolaRazaRepository razaRepository;

    public ServicioAvicolaCrianzaCatalogo(
            AvicolaEstablecimientoRepository establecimientoRepository,
            AvicolaRazaRepository razaRepository) {
        this.establecimientoRepository = establecimientoRepository;
        this.razaRepository = razaRepository;
    }

    @Transactional(readOnly = true)
    public List<AvicolaEstablecimientoRespuesta> listarEstablecimientos(Long empresaId) {
        return listarEstablecimientos(empresaId, AvicolaModuloOrigen.AVICOLA_CRIANZA);
    }

    @Transactional(readOnly = true)
    public List<AvicolaEstablecimientoRespuesta> listarEstablecimientos(Long empresaId, AvicolaModuloOrigen modulo) {
        return establecimientoRepository.listarPorEmpresaIdYModulo(empresaId, modulo).stream()
                .map(this::aEstablecimientoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaEstablecimientoRespuesta crearEstablecimiento(Long empresaId, AvicolaEstablecimientoSolicitud solicitud) {
        return crearEstablecimiento(empresaId, AvicolaModuloOrigen.AVICOLA_CRIANZA, solicitud);
    }

    @Transactional
    public AvicolaEstablecimientoRespuesta crearEstablecimiento(
            Long empresaId, AvicolaModuloOrigen modulo, AvicolaEstablecimientoSolicitud solicitud) {
        AvicolaEstablecimiento e = new AvicolaEstablecimiento();
        e.setEmpresaId(empresaId);
        e.setModuloOrigen(modulo);
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
    public AvicolaEstablecimientoRespuesta actualizarEstablecimiento(Long empresaId, Long id, AvicolaEstablecimientoSolicitud solicitud) {
        return actualizarEstablecimiento(empresaId, id, AvicolaModuloOrigen.AVICOLA_CRIANZA, solicitud);
    }

    @Transactional
    public AvicolaEstablecimientoRespuesta actualizarEstablecimiento(
            Long empresaId, Long id, AvicolaModuloOrigen modulo, AvicolaEstablecimientoSolicitud solicitud) {
        AvicolaEstablecimiento e = establecimientoRepository.buscarPorIdYEmpresaIdYModulo(id, empresaId, modulo)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento avícola no encontrado"));
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
    public List<AvicolaRazaRespuesta> listarRazas(Long empresaId) {
        return listarRazas(empresaId, AvicolaModuloOrigen.AVICOLA_CRIANZA);
    }

    @Transactional(readOnly = true)
    public List<AvicolaRazaRespuesta> listarRazas(Long empresaId, AvicolaModuloOrigen modulo) {
        return razaRepository.listarPorEmpresaIdYModulo(empresaId, modulo).stream()
                .map(this::aRazaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaRazaRespuesta crearRaza(Long empresaId, AvicolaRazaSolicitud solicitud) {
        return crearRaza(empresaId, AvicolaModuloOrigen.AVICOLA_CRIANZA, solicitud);
    }

    @Transactional
    public AvicolaRazaRespuesta crearRaza(Long empresaId, AvicolaModuloOrigen modulo, AvicolaRazaSolicitud solicitud) {
        AvicolaRaza r = new AvicolaRaza();
        r.setEmpresaId(empresaId);
        r.setModuloOrigen(modulo);
        r.setNombre(solicitud.getNombre());
        if (solicitud.getActivo() != null) {
            r.setActivo(solicitud.getActivo());
        }
        return aRazaRespuesta(razaRepository.save(r));
    }

    @Transactional
    public AvicolaRazaRespuesta actualizarRaza(Long empresaId, Long id, AvicolaRazaSolicitud solicitud) {
        return actualizarRaza(empresaId, id, AvicolaModuloOrigen.AVICOLA_CRIANZA, solicitud);
    }

    @Transactional
    public AvicolaRazaRespuesta actualizarRaza(
            Long empresaId, Long id, AvicolaModuloOrigen modulo, AvicolaRazaSolicitud solicitud) {
        AvicolaRaza r = razaRepository.buscarPorIdYEmpresaIdYModulo(id, empresaId, modulo)
                .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));
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

    /** Cadena vacía borra coordenadas en base de datos. */
    private static String normalizarJsonCoordenadas(String json) {
        if (json == null) {
            return null;
        }
        String t = json.trim();
        return t.isEmpty() ? null : t;
    }

    private AvicolaEstablecimientoRespuesta aEstablecimientoRespuesta(AvicolaEstablecimiento e) {
        AvicolaEstablecimientoRespuesta dto = new AvicolaEstablecimientoRespuesta();
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

    private AvicolaRazaRespuesta aRazaRespuesta(AvicolaRaza r) {
        AvicolaRazaRespuesta dto = new AvicolaRazaRespuesta();
        dto.setId(r.getId());
        dto.setEmpresaId(r.getEmpresaId());
        dto.setNombre(r.getNombre());
        dto.setActivo(r.getActivo());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
