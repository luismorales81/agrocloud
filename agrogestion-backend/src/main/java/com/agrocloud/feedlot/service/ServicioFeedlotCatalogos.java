package com.agrocloud.feedlot.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.*;
import com.agrocloud.feedlot.model.entity.*;
import com.agrocloud.feedlot.model.enums.FeedlotCorralEstado;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioFeedlotCatalogos {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final FeedlotEstablecimientoRepository establecimientoRepository;
    private final FeedlotCorralRepository corralRepository;
    private final FeedlotCategoriaRepository categoriaRepository;
    private final FeedlotRazaRepository razaRepository;
    private final FeedlotMotivoMuerteRepository motivoMuerteRepository;
    private final FeedlotProveedorOrigenRepository proveedorRepository;
    private final FeedlotLoteRepository loteRepository;

    public ServicioFeedlotCatalogos(
            ServicioSeguridadContexto servicioSeguridadContexto,
            FeedlotEstablecimientoRepository establecimientoRepository,
            FeedlotCorralRepository corralRepository,
            FeedlotCategoriaRepository categoriaRepository,
            FeedlotRazaRepository razaRepository,
            FeedlotMotivoMuerteRepository motivoMuerteRepository,
            FeedlotProveedorOrigenRepository proveedorRepository,
            FeedlotLoteRepository loteRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.establecimientoRepository = establecimientoRepository;
        this.corralRepository = corralRepository;
        this.categoriaRepository = categoriaRepository;
        this.razaRepository = razaRepository;
        this.motivoMuerteRepository = motivoMuerteRepository;
        this.proveedorRepository = proveedorRepository;
        this.loteRepository = loteRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedlotEstablecimientoRespuesta> listarEstablecimientos() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return establecimientoRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aEstablecimientoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotEstablecimientoRespuesta crearEstablecimiento(FeedlotEstablecimientoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotEstablecimiento e = new FeedlotEstablecimiento();
        e.setEmpresaId(empresaId);
        e.setNombre(solicitud.getNombre().trim());
        e.setUbicacion(normalizarOpcional(solicitud.getUbicacion()));
        e.setCoordenadas(normalizarJsonCoordenadas(solicitud.getCoordenadas()));
        e.setCapacidadTotalCabezas(solicitud.getCapacidadTotalCabezas());
        if (solicitud.getActivo() != null) {
            e.setActivo(solicitud.getActivo());
        }
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional
    public FeedlotEstablecimientoRespuesta actualizarEstablecimiento(Long id, FeedlotEstablecimientoSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotEstablecimiento e = establecimientoRepository.buscarPorIdYEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento feedlot no encontrado"));
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
        if (solicitud.getCapacidadTotalCabezas() != null) {
            e.setCapacidadTotalCabezas(solicitud.getCapacidadTotalCabezas());
        }
        if (solicitud.getActivo() != null) {
            e.setActivo(solicitud.getActivo());
        }
        return aEstablecimientoRespuesta(establecimientoRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<FeedlotCorralRespuesta> listarCorrales(Long establecimientoId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirEstablecimiento(empresaId, establecimientoId);
        return corralRepository.listarPorEstablecimientoId(establecimientoId).stream()
                .map(this::aCorralRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotCorralRespuesta crearCorral(Long establecimientoId, FeedlotCorralSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotEstablecimiento est = requerirEstablecimiento(empresaId, establecimientoId);
        FeedlotCorral c = new FeedlotCorral();
        c.setEstablecimiento(est);
        c.setNombre(solicitud.getNombre().trim());
        c.setCapacidadCabezas(solicitud.getCapacidadCabezas());
        c.setEstado(FeedlotCorralEstado.DISPONIBLE);
        if (solicitud.getActivo() != null) {
            c.setActivo(solicitud.getActivo());
        }
        return aCorralRespuesta(corralRepository.save(c));
    }

    @Transactional
    public FeedlotCorralRespuesta actualizarCorral(Long establecimientoId, Long corralId, FeedlotCorralSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirEstablecimiento(empresaId, establecimientoId);
        FeedlotCorral c = corralRepository.buscarPorIdYEstablecimientoId(corralId, establecimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Corral no encontrado"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            c.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getCapacidadCabezas() != null) {
            c.setCapacidadCabezas(solicitud.getCapacidadCabezas());
        }
        if (solicitud.getActivo() != null && Boolean.FALSE.equals(solicitud.getActivo())) {
            if (loteRepository.existsByCorralIdAndEstado(corralId, FeedlotLoteEstado.ACTIVO)) {
                throw new ResourceConflictException("No se puede inactivar un corral con lote activo");
            }
            c.setActivo(false);
            c.setEstado(FeedlotCorralEstado.INACTIVO);
        } else if (solicitud.getActivo() != null) {
            c.setActivo(true);
            if (c.getEstado() == FeedlotCorralEstado.INACTIVO) {
                c.setEstado(FeedlotCorralEstado.DISPONIBLE);
            }
        }
        return aCorralRespuesta(corralRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<FeedlotCatalogoRespuesta> listarCategorias() {
        return listarCatalogoSimple(categoriaRepository.listarPorEmpresaId(empresaActual()));
    }

    @Transactional
    public FeedlotCatalogoRespuesta crearCategoria(FeedlotCatalogoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        FeedlotCategoria c = new FeedlotCategoria();
        c.setEmpresaId(empresaActual());
        c.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            c.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuesta(categoriaRepository.save(c));
    }

    @Transactional
    public FeedlotCatalogoRespuesta actualizarCategoria(Long id, FeedlotCatalogoSolicitud solicitud) {
        FeedlotCategoria c = categoriaRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            c.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) {
            c.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuesta(categoriaRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<FeedlotCatalogoRespuesta> listarRazas() {
        return listarCatalogoSimple(razaRepository.listarPorEmpresaId(empresaActual()));
    }

    @Transactional
    public FeedlotCatalogoRespuesta crearRaza(FeedlotCatalogoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        FeedlotRaza r = new FeedlotRaza();
        r.setEmpresaId(empresaActual());
        r.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            r.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuesta(razaRepository.save(r));
    }

    @Transactional
    public FeedlotCatalogoRespuesta actualizarRaza(Long id, FeedlotCatalogoSolicitud solicitud) {
        FeedlotRaza r = razaRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            r.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) {
            r.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuesta(razaRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<FeedlotCatalogoRespuesta> listarMotivosMuerte() {
        return listarCatalogoSimple(motivoMuerteRepository.listarPorEmpresaId(empresaActual()));
    }

    @Transactional
    public FeedlotCatalogoRespuesta crearMotivoMuerte(FeedlotCatalogoSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        FeedlotMotivoMuerte m = new FeedlotMotivoMuerte();
        m.setEmpresaId(empresaActual());
        m.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            m.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuesta(motivoMuerteRepository.save(m));
    }

    @Transactional
    public FeedlotCatalogoRespuesta actualizarMotivoMuerte(Long id, FeedlotCatalogoSolicitud solicitud) {
        FeedlotMotivoMuerte m = motivoMuerteRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Motivo de muerte no encontrado"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            m.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) {
            m.setActivo(solicitud.getActivo());
        }
        return aCatalogoRespuesta(motivoMuerteRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<FeedlotProveedorOrigenRespuesta> listarProveedores() {
        return proveedorRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aProveedorRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotProveedorOrigenRespuesta crearProveedor(FeedlotProveedorOrigenSolicitud solicitud) {
        validarNombre(solicitud.getNombre());
        FeedlotProveedorOrigen p = new FeedlotProveedorOrigen();
        p.setEmpresaId(empresaActual());
        p.setNombre(solicitud.getNombre().trim());
        if (solicitud.getTipo() != null) {
            p.setTipo(solicitud.getTipo());
        }
        if (solicitud.getActivo() != null) {
            p.setActivo(solicitud.getActivo());
        }
        return aProveedorRespuesta(proveedorRepository.save(p));
    }

    @Transactional
    public FeedlotProveedorOrigenRespuesta actualizarProveedor(Long id, FeedlotProveedorOrigenSolicitud solicitud) {
        FeedlotProveedorOrigen p = proveedorRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        if (solicitud.getNombre() != null) {
            validarNombre(solicitud.getNombre());
            p.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getTipo() != null) {
            p.setTipo(solicitud.getTipo());
        }
        if (solicitud.getActivo() != null) {
            p.setActivo(solicitud.getActivo());
        }
        return aProveedorRespuesta(proveedorRepository.save(p));
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private FeedlotEstablecimiento requerirEstablecimiento(Long empresaId, Long establecimientoId) {
        return establecimientoRepository.buscarPorIdYEmpresaId(establecimientoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento feedlot no encontrado"));
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

    private FeedlotEstablecimientoRespuesta aEstablecimientoRespuesta(FeedlotEstablecimiento e) {
        FeedlotEstablecimientoRespuesta dto = new FeedlotEstablecimientoRespuesta();
        dto.setId(e.getId());
        dto.setEmpresaId(e.getEmpresaId());
        dto.setNombre(e.getNombre());
        dto.setUbicacion(e.getUbicacion());
        dto.setCoordenadas(e.getCoordenadas());
        dto.setCapacidadTotalCabezas(e.getCapacidadTotalCabezas());
        dto.setActivo(e.getActivo());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    private FeedlotCorralRespuesta aCorralRespuesta(FeedlotCorral c) {
        FeedlotCorralRespuesta dto = new FeedlotCorralRespuesta();
        dto.setId(c.getId());
        dto.setEstablecimientoId(c.getEstablecimiento().getId());
        dto.setNombre(c.getNombre());
        dto.setCapacidadCabezas(c.getCapacidadCabezas());
        dto.setEstado(c.getEstado());
        dto.setActivo(c.getActivo());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    private FeedlotProveedorOrigenRespuesta aProveedorRespuesta(FeedlotProveedorOrigen p) {
        FeedlotProveedorOrigenRespuesta dto = new FeedlotProveedorOrigenRespuesta();
        dto.setId(p.getId());
        dto.setEmpresaId(p.getEmpresaId());
        dto.setNombre(p.getNombre());
        dto.setTipo(p.getTipo());
        dto.setActivo(p.getActivo());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    private List<FeedlotCatalogoRespuesta> listarCatalogoSimple(List<?> entidades) {
        return entidades.stream().map(e -> {
            if (e instanceof FeedlotCategoria c) {
                return aCatalogoRespuesta(c);
            }
            if (e instanceof FeedlotRaza r) {
                return aCatalogoRespuesta(r);
            }
            if (e instanceof FeedlotMotivoMuerte m) {
                return aCatalogoRespuesta(m);
            }
            throw new IllegalStateException("Tipo de catálogo no soportado");
        }).collect(Collectors.toList());
    }

    private FeedlotCatalogoRespuesta aCatalogoRespuesta(FeedlotCategoria c) {
        FeedlotCatalogoRespuesta dto = new FeedlotCatalogoRespuesta();
        dto.setId(c.getId());
        dto.setEmpresaId(c.getEmpresaId());
        dto.setNombre(c.getNombre());
        dto.setActivo(c.getActivo());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    private FeedlotCatalogoRespuesta aCatalogoRespuesta(FeedlotRaza r) {
        FeedlotCatalogoRespuesta dto = new FeedlotCatalogoRespuesta();
        dto.setId(r.getId());
        dto.setEmpresaId(r.getEmpresaId());
        dto.setNombre(r.getNombre());
        dto.setActivo(r.getActivo());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }

    private FeedlotCatalogoRespuesta aCatalogoRespuesta(FeedlotMotivoMuerte m) {
        FeedlotCatalogoRespuesta dto = new FeedlotCatalogoRespuesta();
        dto.setId(m.getId());
        dto.setEmpresaId(m.getEmpresaId());
        dto.setNombre(m.getNombre());
        dto.setActivo(m.getActivo());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }
}
