package com.agrocloud.feedlot.service.manga;

import com.agrocloud.core.domain.User;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.dto.FeedlotAccionDesconocidoSolicitud;
import com.agrocloud.feedlot.model.dto.FeedlotConfirmarSesionSolicitud;
import com.agrocloud.feedlot.model.dto.FeedlotLoteNuevoRecuentoSolicitud;
import com.agrocloud.feedlot.model.dto.FeedlotLoteRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotLoteSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotAccionDesconocido;
import com.agrocloud.feedlot.model.enums.FeedlotOrigenFilaRecuento;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.model.enums.FeedlotTipoTenencia;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConfirmacionRecuento implements EstrategiaConfirmacionManga {

    private final ConfirmacionEnrolamiento confirmacionEnrolamiento;
    private final ServicioFeedlotLotes servicioLotes;
    private final FeedlotSesionMangaFilaRepository filaRepository;

    public ConfirmacionRecuento(
            ConfirmacionEnrolamiento confirmacionEnrolamiento,
            ServicioFeedlotLotes servicioLotes,
            FeedlotSesionMangaFilaRepository filaRepository) {
        this.confirmacionEnrolamiento = confirmacionEnrolamiento;
        this.servicioLotes = servicioLotes;
        this.filaRepository = filaRepository;
    }

    @Override
    public FeedlotTipoSesionManga tipo() {
        return FeedlotTipoSesionManga.RECUENTO;
    }

    @Override
    public void confirmar(FeedlotSesionManga sesion, List<FeedlotSesionMangaFila> filas, ContextoConfirmacionManga contexto) {
        FeedlotConfirmarSesionSolicitud solicitud = contexto.getSolicitud();
        Map<Long, FeedlotAccionDesconocido> acciones = new HashMap<>();
        if (solicitud != null && solicitud.getAccionesDesconocidos() != null) {
            for (FeedlotAccionDesconocidoSolicitud a : solicitud.getAccionesDesconocidos()) {
                if (a.getFilaId() != null && a.getAccion() != null) {
                    acciones.put(a.getFilaId(), a.getAccion());
                }
            }
        }
        List<FeedlotSesionMangaFila> esteLote = new ArrayList<>();
        List<FeedlotSesionMangaFila> loteNuevo = new ArrayList<>();
        for (FeedlotSesionMangaFila fila : filas) {
            if (fila.getOrigenRecuento() != FeedlotOrigenFilaRecuento.DESCONOCIDO) {
                continue;
            }
            FeedlotAccionDesconocido accion = acciones.getOrDefault(fila.getId(), FeedlotAccionDesconocido.IGNORAR);
            fila.setAccionDesconocido(accion);
            if (accion == FeedlotAccionDesconocido.ENROLAR_EN_ESTE_LOTE) {
                esteLote.add(fila);
            } else if (accion == FeedlotAccionDesconocido.ENROLAR_EN_LOTE_NUEVO) {
                loteNuevo.add(fila);
            }
        }
        if (esteLote.isEmpty() && loteNuevo.isEmpty()) {
            filaRepository.saveAll(filas);
            return;
        }
        validarAdminEnrolamiento(contexto.getUsuario());
        if (!esteLote.isEmpty()) {
            confirmacionEnrolamiento.enrolarEnLote(sesion, sesion.getLote(), esteLote, contexto, true);
        }
        if (!loteNuevo.isEmpty()) {
            FeedlotLote creado = crearLoteNuevo(sesion, solicitud == null ? null : solicitud.getLoteNuevo(), loteNuevo.size());
            confirmacionEnrolamiento.enrolarEnLote(sesion, creado, loteNuevo, contexto, false);
            for (FeedlotSesionMangaFila fila : loteNuevo) {
                fila.setLoteDestinoId(creado.getId());
            }
        }
        filaRepository.saveAll(filas);
    }

    @Override
    public void revertir(FeedlotSesionManga sesion) {
        // RN-MG-R13: no deshace enrolamientos ni lote nuevo.
    }

    private FeedlotLote crearLoteNuevo(FeedlotSesionManga sesion, FeedlotLoteNuevoRecuentoSolicitud datos, int cabezas) {
        if (datos == null || datos.getCorralId() == null || datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("Para enrolar en lote nuevo hay que indicar corral y nombre");
        }
        FeedlotLote origen = sesion.getLote();
        Long categoriaId = datos.getCategoriaId();
        if (categoriaId == null && origen.getCategoria() != null) {
            categoriaId = origen.getCategoria().getId();
        }
        Long razaId = datos.getRazaId();
        if (razaId == null && origen.getRaza() != null) {
            razaId = origen.getRaza().getId();
        }
        FeedlotLoteSolicitud alta = new FeedlotLoteSolicitud();
        alta.setCorralId(datos.getCorralId());
        alta.setNombre(datos.getNombre().trim());
        alta.setCategoriaId(categoriaId);
        alta.setRazaId(razaId);
        alta.setFechaIngreso(sesion.getFecha());
        alta.setCabezasInicial(cabezas);
        alta.setTipoTenencia(origen.getTipoTenencia() != null ? origen.getTipoTenencia() : FeedlotTipoTenencia.PROPIO);
        BigDecimal peso = origen.getPesoPromedioIngresoKg();
        alta.setPesoPromedioIngresoKg(peso != null && peso.signum() > 0 ? peso : BigDecimal.ONE);
        alta.setObservaciones("Creado desde recuento de manga " + sesion.getId());
        FeedlotLoteRespuesta creado = servicioLotes.crearLote(alta);
        return servicioLotes.obtenerEntidadLote(sesion.getEmpresaId(), creado.getId());
    }

    static void validarAdminEnrolamiento(User usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new ResourceConflictException("Solo el administrador puede enrolar desde el recuento");
        }
        if (!esAdministrador(usuario)) {
            throw new ResourceConflictException("Solo el administrador puede enrolar desde el recuento");
        }
    }

    static boolean esAdministrador(User usuario) {
        if (usuario.getUsuarioEmpresas() != null) {
            try {
                boolean porEmpresa = usuario.getUsuarioEmpresas().stream()
                        .anyMatch(ue -> ue.getRol() != null
                                && ("ADMINISTRADOR".equals(ue.getRol().name())
                                || "SUPERADMIN".equals(ue.getRol().name())));
                if (porEmpresa) {
                    return true;
                }
            } catch (org.hibernate.LazyInitializationException ignored) {
                // SecurityContext
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String n = a.getAuthority() == null ? "" : a.getAuthority().toUpperCase();
            return n.contains("ADMINISTRADOR") || n.contains("SUPERADMIN") || "ROLE_ADMIN".equals(n);
        });
    }
}
