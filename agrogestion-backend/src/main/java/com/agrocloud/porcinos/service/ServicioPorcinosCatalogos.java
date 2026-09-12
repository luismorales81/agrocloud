package com.agrocloud.porcinos.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.porcinos.model.dto.*;
import com.agrocloud.porcinos.model.entity.*;
import com.agrocloud.porcinos.model.enums.PorcinosGalponEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioPorcinosCatalogos {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final PorcinosEstablecimientoRepository establecimientoRepository;
    private final PorcinosGalponRepository galponRepository;
    private final PorcinosRazaRepository razaRepository;
    private final PorcinosMotivoBajaRepository motivoBajaRepository;
    private final PorcinosCausaMortalidadRepository causaMortalidadRepository;
    private final PorcinosTipoServicioRepository tipoServicioRepository;
    private final ObjectMapper objectMapper;

    public ServicioPorcinosCatalogos(
            ServicioSeguridadContexto servicioSeguridadContexto,
            PorcinosEstablecimientoRepository establecimientoRepository,
            PorcinosGalponRepository galponRepository,
            PorcinosRazaRepository razaRepository,
            PorcinosMotivoBajaRepository motivoBajaRepository,
            PorcinosCausaMortalidadRepository causaMortalidadRepository,
            PorcinosTipoServicioRepository tipoServicioRepository,
            ObjectMapper objectMapper) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.establecimientoRepository = establecimientoRepository;
        this.galponRepository = galponRepository;
        this.razaRepository = razaRepository;
        this.motivoBajaRepository = motivoBajaRepository;
        this.causaMortalidadRepository = causaMortalidadRepository;
        this.tipoServicioRepository = tipoServicioRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<PorcinosEstablecimientoRespuesta> listarEstablecimientos() {
        return establecimientoRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aEstablecimientoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public PorcinosEstablecimientoRespuesta crearEstablecimiento(PorcinosEstablecimientoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        PorcinosEstablecimiento e = new PorcinosEstablecimiento();
        e.setEmpresaId(empresaActual());
        e.setNombre(solicitud.getNombre().trim());
        e.setUbicacion(normalizarOpcional(solicitud.getUbicacion()));
        e.setCoordenadas(normalizarJsonCoordenadas(solicitud.getCoordenadas()));
        if (solicitud.getDiasGestacion() != null) {
            e.setDiasGestacion(solicitud.getDiasGestacion());
        }
        if (solicitud.getDiasLactancia() != null) {
            e.setDiasLactancia(solicitud.getDiasLactancia());
        }
        if (solicitud.getDiasEntreCelos() != null) {
            e.setDiasEntreCelos(solicitud.getDiasEntreCelos());
        }
        if (solicitud.getFaenaHabilitada() != null) {
            e.setFaenaHabilitada(solicitud.getFaenaHabilitada());
        }
        e.setCapacidadCabezas(solicitud.getCapacidadCabezas());
        if (solicitud.getActivo() != null) {
            e.setActivo(solicitud.getActivo());
        }
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional
    public PorcinosEstablecimientoRespuesta actualizarEstablecimiento(
            Long id, PorcinosEstablecimientoSolicitud solicitud) {
        PorcinosEstablecimiento e = requerirEstablecimiento(id);
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            e.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getUbicacion() != null) {
            e.setUbicacion(normalizarOpcional(solicitud.getUbicacion()));
        }
        if (solicitud.getCoordenadas() != null) {
            e.setCoordenadas(normalizarJsonCoordenadas(solicitud.getCoordenadas()));
        }
        if (solicitud.getDiasGestacion() != null) {
            e.setDiasGestacion(solicitud.getDiasGestacion());
        }
        if (solicitud.getDiasLactancia() != null) {
            e.setDiasLactancia(solicitud.getDiasLactancia());
        }
        if (solicitud.getDiasEntreCelos() != null) {
            e.setDiasEntreCelos(solicitud.getDiasEntreCelos());
        }
        if (solicitud.getFaenaHabilitada() != null) {
            e.setFaenaHabilitada(solicitud.getFaenaHabilitada());
        }
        if (solicitud.getCapacidadCabezas() != null) {
            e.setCapacidadCabezas(solicitud.getCapacidadCabezas());
        }
        if (solicitud.getActivo() != null) {
            e.setActivo(solicitud.getActivo());
        }
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<PorcinosGalponRespuesta> listarGalpones(Long establecimientoId) {
        requerirEstablecimiento(establecimientoId);
        return galponRepository.listarPorEstablecimientoId(establecimientoId).stream()
                .map(this::aGalponRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public PorcinosGalponRespuesta crearGalpon(Long establecimientoId, PorcinosGalponSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        PorcinosEstablecimiento est = requerirEstablecimiento(establecimientoId);
        if (galponRepository.existeActivoConNombre(establecimientoId, solicitud.getNombre().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe un galpón activo con el nombre \"" + solicitud.getNombre().trim() + "\"");
        }
        PorcinosGalpon g = new PorcinosGalpon();
        g.setEstablecimiento(est);
        g.setNombre(solicitud.getNombre().trim());
        g.setCapacidadCabezas(solicitud.getCapacidadCabezas());
        g.setEstado(PorcinosGalponEstado.DISPONIBLE);
        if (solicitud.getActivo() != null) {
            g.setActivo(solicitud.getActivo());
        }
        return aGalponRespuesta(galponRepository.save(g));
    }

    @Transactional
    public PorcinosGalponRespuesta actualizarGalpon(
            Long establecimientoId, Long galponId, PorcinosGalponSolicitud solicitud) {
        requerirEstablecimiento(establecimientoId);
        PorcinosGalpon g = galponRepository.buscarPorIdYEmpresaId(galponId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Galpón no encontrado"));
        if (!g.getEstablecimiento().getId().equals(establecimientoId)) {
            throw new ResourceNotFoundException("Galpón no pertenece al establecimiento");
        }
        if (solicitud.getNombre() != null && !solicitud.getNombre().isBlank()) {
            g.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getCapacidadCabezas() != null) {
            g.setCapacidadCabezas(solicitud.getCapacidadCabezas());
        }
        if (solicitud.getActivo() != null) {
            g.setActivo(solicitud.getActivo());
        }
        return aGalponRespuesta(galponRepository.save(g));
    }

    @Transactional(readOnly = true)
    public List<PorcinosRazaRespuesta> listarRazas() {
        return razaRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aRazaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public PorcinosRazaRespuesta crearRaza(PorcinosRazaSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        PorcinosRaza r = new PorcinosRaza();
        r.setEmpresaId(empresaActual());
        r.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            r.setActivo(solicitud.getActivo());
        }
        return aRazaRespuesta(razaRepository.save(r));
    }

    @Transactional
    public PorcinosRazaRespuesta actualizarRaza(Long id, PorcinosRazaSolicitud solicitud) {
        PorcinosRaza r = razaRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            r.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) {
            r.setActivo(solicitud.getActivo());
        }
        return aRazaRespuesta(razaRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<PorcinosCatalogoRespuesta> listarMotivosBaja() {
        return motivoBajaRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aCatalogoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public PorcinosCatalogoRespuesta crearMotivoBaja(PorcinosCatalogoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        PorcinosMotivoBaja m = new PorcinosMotivoBaja();
        m.setEmpresaId(empresaActual());
        m.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            m.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuesta(motivoBajaRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<PorcinosCatalogoRespuesta> listarCausasMortalidad() {
        return causaMortalidadRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aCatalogoRespuestaCausa)
                .collect(Collectors.toList());
    }

    @Transactional
    public PorcinosCatalogoRespuesta crearCausaMortalidad(PorcinosCatalogoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        PorcinosCausaMortalidad c = new PorcinosCausaMortalidad();
        c.setEmpresaId(empresaActual());
        c.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            c.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuestaCausa(causaMortalidadRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<PorcinosCatalogoRespuesta> listarTiposServicio() {
        return tipoServicioRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aCatalogoRespuestaTipo)
                .collect(Collectors.toList());
    }

    @Transactional
    public PorcinosCatalogoRespuesta crearTipoServicio(PorcinosCatalogoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        PorcinosTipoServicio t = new PorcinosTipoServicio();
        t.setEmpresaId(empresaActual());
        t.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            t.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuestaTipo(tipoServicioRepository.save(t));
    }

    @Transactional(readOnly = true)
    public PorcinosEstablecimiento obtenerEntidadEstablecimiento(Long id) {
        return requerirEstablecimiento(id);
    }

    @Transactional(readOnly = true)
    public PorcinosGalpon obtenerEntidadGalpon(Long galponId) {
        return galponRepository.buscarPorIdYEmpresaId(galponId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Galpón no encontrado"));
    }

    public int resolverDiasGestacion(PorcinosMadre madre) {
        if (madre.getGalpon() != null && madre.getGalpon().getEstablecimiento() != null) {
            Integer dias = madre.getGalpon().getEstablecimiento().getDiasGestacion();
            if (dias != null && dias > 0) {
                return dias;
            }
        }
        return 114;
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private PorcinosEstablecimiento requerirEstablecimiento(Long id) {
        return establecimientoRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento porcinos no encontrado"));
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    private static String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }

    private static String normalizarJsonCoordenadas(String json) {
        if (json == null) {
            return null;
        }
        String t = json.trim();
        return t.isEmpty() ? null : t;
    }

    private PorcinosEstablecimientoRespuesta aEstablecimientoRespuesta(PorcinosEstablecimiento e) {
        PorcinosEstablecimientoRespuesta dto = new PorcinosEstablecimientoRespuesta();
        dto.setId(e.getId());
        dto.setEmpresaId(e.getEmpresaId());
        dto.setNombre(e.getNombre());
        dto.setUbicacion(e.getUbicacion());
        dto.setCoordenadas(e.getCoordenadas());
        UtilCentroideCoordenadasCampo.calcularCentroide(e.getCoordenadas(), objectMapper)
                .ifPresent(xy -> {
                    dto.setClimaLatitud(xy[0]);
                    dto.setClimaLongitud(xy[1]);
                });
        dto.setDiasGestacion(e.getDiasGestacion());
        dto.setDiasLactancia(e.getDiasLactancia());
        dto.setDiasEntreCelos(e.getDiasEntreCelos());
        dto.setFaenaHabilitada(e.getFaenaHabilitada());
        dto.setCapacidadCabezas(e.getCapacidadCabezas());
        dto.setActivo(e.getActivo());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    private PorcinosGalponRespuesta aGalponRespuesta(PorcinosGalpon g) {
        PorcinosGalponRespuesta dto = new PorcinosGalponRespuesta();
        dto.setId(g.getId());
        dto.setEstablecimientoId(g.getEstablecimiento().getId());
        dto.setNombre(g.getNombre());
        dto.setCapacidadCabezas(g.getCapacidadCabezas());
        dto.setEstado(g.getEstado());
        dto.setActivo(g.getActivo());
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());
        return dto;
    }

    private PorcinosRazaRespuesta aRazaRespuesta(PorcinosRaza r) {
        PorcinosRazaRespuesta dto = new PorcinosRazaRespuesta();
        dto.setId(r.getId());
        dto.setEmpresaId(r.getEmpresaId());
        dto.setNombre(r.getNombre());
        dto.setActivo(r.getActivo());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }

    private PorcinosCatalogoRespuesta aCatalogoRespuesta(PorcinosMotivoBaja m) {
        PorcinosCatalogoRespuesta dto = new PorcinosCatalogoRespuesta();
        dto.setId(m.getId());
        dto.setEmpresaId(m.getEmpresaId());
        dto.setNombre(m.getNombre());
        dto.setActivo(m.getActivo());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }

    private PorcinosCatalogoRespuesta aCatalogoRespuestaCausa(PorcinosCausaMortalidad c) {
        PorcinosCatalogoRespuesta dto = new PorcinosCatalogoRespuesta();
        dto.setId(c.getId());
        dto.setEmpresaId(c.getEmpresaId());
        dto.setNombre(c.getNombre());
        dto.setActivo(c.getActivo());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    private PorcinosCatalogoRespuesta aCatalogoRespuestaTipo(PorcinosTipoServicio t) {
        PorcinosCatalogoRespuesta dto = new PorcinosCatalogoRespuesta();
        dto.setId(t.getId());
        dto.setEmpresaId(t.getEmpresaId());
        dto.setNombre(t.getNombre());
        dto.setActivo(t.getActivo());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }
}
