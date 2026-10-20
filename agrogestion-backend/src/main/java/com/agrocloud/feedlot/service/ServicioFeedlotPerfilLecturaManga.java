package com.agrocloud.feedlot.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.FeedlotAliasColumnasPerfil;
import com.agrocloud.feedlot.model.dto.FeedlotEncabezadosMuestra;
import com.agrocloud.feedlot.model.dto.FeedlotPerfilLecturaMangaRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotPerfilLecturaMangaSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotPerfilLecturaManga;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.repository.FeedlotPerfilLecturaMangaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ServicioFeedlotPerfilLecturaManga {

    private final ServicioSeguridadContexto seguridad;
    private final FeedlotPerfilLecturaMangaRepository repositorio;
    private final ServicioFeedlotImportacionManga importacion;
    private final ObjectMapper objectMapper;

    public ServicioFeedlotPerfilLecturaManga(
            ServicioSeguridadContexto seguridad,
            FeedlotPerfilLecturaMangaRepository repositorio,
            ServicioFeedlotImportacionManga importacion,
            ObjectMapper objectMapper) {
        this.seguridad = seguridad;
        this.repositorio = repositorio;
        this.importacion = importacion;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<FeedlotPerfilLecturaMangaRespuesta> listar(FeedlotTipoSesionManga tipo) {
        Long empresaId = seguridad.obtenerEmpresaIdActual();
        List<FeedlotPerfilLecturaManga> filas = tipo == null
                ? repositorio.listarPorEmpresaId(empresaId)
                : repositorio.listarPorEmpresaYTipo(empresaId, tipo);
        return filas.stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public FeedlotPerfilLecturaMangaRespuesta obtener(Long id) {
        return aRespuesta(entidad(id));
    }

    @Transactional
    public FeedlotPerfilLecturaMangaRespuesta crear(FeedlotPerfilLecturaMangaSolicitud solicitud) {
        validar(solicitud, true);
        FeedlotPerfilLecturaManga p = new FeedlotPerfilLecturaManga();
        p.setEmpresaId(seguridad.obtenerEmpresaIdActual());
        aplicar(p, solicitud);
        p = repositorio.save(p);
        repositorio.flush();
        marcarPredeterminado(p);
        return aRespuesta(p);
    }

    @Transactional
    public FeedlotPerfilLecturaMangaRespuesta actualizar(Long id, FeedlotPerfilLecturaMangaSolicitud solicitud) {
        validar(solicitud, false);
        FeedlotPerfilLecturaManga p = entidad(id);
        aplicar(p, solicitud);
        p = repositorio.save(p);
        repositorio.flush();
        marcarPredeterminado(p);
        return aRespuesta(p);
    }

    @Transactional
    public void eliminar(Long id) {
        repositorio.delete(entidad(id));
    }

    public FeedlotEncabezadosMuestra extraerMuestra(MultipartFile archivo) {
        return importacion.extraerEncabezados(archivo);
    }

    public FeedlotPerfilLecturaManga entidad(Long id) {
        return repositorio.buscarPorIdYEmpresaId(id, seguridad.obtenerEmpresaIdActual())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de lectura no encontrado"));
    }

    public FeedlotAliasColumnasPerfil aliasDe(FeedlotPerfilLecturaManga perfil) {
        try {
            FeedlotAliasColumnasPerfil a = objectMapper.readValue(perfil.getMapeoAliasJson(), FeedlotAliasColumnasPerfil.class);
            return a == null ? new FeedlotAliasColumnasPerfil() : a;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer el mapeo del perfil");
        }
    }

    private void aplicar(FeedlotPerfilLecturaManga p, FeedlotPerfilLecturaMangaSolicitud s) {
        if (s.getNombre() != null) {
            p.setNombre(s.getNombre().trim());
        }
        if (s.getTipo() != null) {
            p.setTipo(s.getTipo());
        }
        if (s.getPredeterminado() != null) {
            p.setPredeterminado(s.getPredeterminado());
        }
        if (s.getAlias() != null) {
            try {
                p.setMapeoAliasJson(objectMapper.writeValueAsString(s.getAlias()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("El mapeo de columnas no es válido");
            }
        }
        p.setInsumoId(s.getInsumoId());
    }

    private void marcarPredeterminado(FeedlotPerfilLecturaManga p) {
        if (Boolean.TRUE.equals(p.getPredeterminado()) && p.getId() != null && p.getTipo() != null) {
            repositorio.quitarPredeterminados(p.getEmpresaId(), p.getTipo(), p.getId());
        }
    }

    private void validar(FeedlotPerfilLecturaMangaSolicitud s, boolean crear) {
        if (crear && (s.getNombre() == null || s.getNombre().isBlank())) {
            throw new IllegalArgumentException("El nombre del perfil es obligatorio");
        }
        if (crear && s.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de evento del perfil es obligatorio");
        }
        FeedlotAliasColumnasPerfil alias = s.getAlias();
        if (crear && alias == null) {
            throw new IllegalArgumentException("Hay que indicar qué columnas leer");
        }
        if (alias == null) {
            return;
        }
        boolean identidad = !vacia(alias.getEid()) || !vacia(alias.getVid());
        if (!identidad) {
            throw new IllegalArgumentException("El perfil necesita al menos EID o caravana visual");
        }
        FeedlotTipoSesionManga tipo = s.getTipo();
        if (tipo == FeedlotTipoSesionManga.PESADA && vacia(alias.getPeso())) {
            throw new IllegalArgumentException("Un perfil de pesada necesita la columna de peso");
        }
    }

    private static boolean vacia(List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return true;
        }
        return valores.stream().noneMatch(v -> v != null && !v.isBlank());
    }

    private FeedlotPerfilLecturaMangaRespuesta aRespuesta(FeedlotPerfilLecturaManga p) {
        FeedlotPerfilLecturaMangaRespuesta r = new FeedlotPerfilLecturaMangaRespuesta();
        r.setId(p.getId());
        r.setNombre(p.getNombre());
        r.setTipo(p.getTipo());
        r.setPredeterminado(p.getPredeterminado());
        r.setAlias(aliasDe(p));
        r.setInsumoId(p.getInsumoId());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
