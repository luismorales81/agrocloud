package com.agrocloud.feedlot.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.*;
import com.agrocloud.feedlot.model.entity.*;
import com.agrocloud.feedlot.model.enums.FeedlotCorralEstado;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.model.enums.FeedlotTipoTenencia;
import com.agrocloud.feedlot.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioFeedlotLotes {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final FeedlotLoteRepository loteRepository;
    private final FeedlotCorralRepository corralRepository;
    private final FeedlotCategoriaRepository categoriaRepository;
    private final FeedlotRazaRepository razaRepository;
    private final FeedlotProveedorOrigenRepository proveedorRepository;
    private final FeedlotDietaRepository dietaRepository;
    private final FeedlotAjustePlantelRepository ajustePlantelRepository;

    public ServicioFeedlotLotes(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            FeedlotLoteRepository loteRepository,
            FeedlotCorralRepository corralRepository,
            FeedlotCategoriaRepository categoriaRepository,
            FeedlotRazaRepository razaRepository,
            FeedlotProveedorOrigenRepository proveedorRepository,
            FeedlotDietaRepository dietaRepository,
            FeedlotAjustePlantelRepository ajustePlantelRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.loteRepository = loteRepository;
        this.corralRepository = corralRepository;
        this.categoriaRepository = categoriaRepository;
        this.razaRepository = razaRepository;
        this.proveedorRepository = proveedorRepository;
        this.dietaRepository = dietaRepository;
        this.ajustePlantelRepository = ajustePlantelRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedlotLoteRespuesta> listarLotes(FeedlotLoteEstado estado, Boolean delPeriodoActivo, Long corralId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        List<FeedlotLote> lotes;
        if (corralId != null) {
            lotes = loteRepository.listarPorEmpresaIdYCorralId(empresaId, corralId);
        } else if (estado != null) {
            lotes = loteRepository.listarPorEmpresaIdYEstado(empresaId, estado);
        } else {
            lotes = loteRepository.listarPorEmpresaId(empresaId);
        }
        if (Boolean.TRUE.equals(delPeriodoActivo)) {
            Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
            lotes = lotes.stream().filter(l -> campanaId.equals(l.getCampanaId())).toList();
        }
        return lotes.stream().map(this::aLoteRespuesta).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeedlotLoteRespuesta obtenerLote(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return aLoteRespuesta(obtenerEntidadLote(empresaId, loteId));
    }

    @Transactional
    public FeedlotLoteRespuesta crearLote(FeedlotLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        validarAlta(solicitud);

        FeedlotCorral corral = corralRepository.buscarPorIdYEmpresaId(solicitud.getCorralId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Corral no encontrado"));
        if (corral.getEstado() != FeedlotCorralEstado.DISPONIBLE) {
            throw new ResourceConflictException("El corral no está disponible");
        }
        if (loteRepository.existsByCorralIdAndEstado(corral.getId(), FeedlotLoteEstado.ACTIVO)) {
            throw new ResourceConflictException("El corral ya tiene un lote activo");
        }

        FeedlotCategoria categoria = categoriaRepository.buscarPorIdYEmpresaId(solicitud.getCategoriaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        FeedlotLote lote = new FeedlotLote();
        lote.setEmpresaId(empresaId);
        lote.setCorral(corral);
        lote.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        lote.setNombre(solicitud.getNombre().trim());
        lote.setCategoria(categoria);
        if (solicitud.getRazaId() != null) {
            lote.setRaza(razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada")));
        }
        if (solicitud.getProveedorId() != null) {
            lote.setProveedor(proveedorRepository.buscarPorIdYEmpresaId(solicitud.getProveedorId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado")));
        }
        FeedlotTipoTenencia tenencia = solicitud.getTipoTenencia() != null
                ? solicitud.getTipoTenencia() : FeedlotTipoTenencia.PROPIO;
        lote.setTipoTenencia(tenencia);
        lote.setFechaIngreso(solicitud.getFechaIngreso());
        lote.setCabezasInicial(solicitud.getCabezasInicial());
        lote.setCabezasActuales(solicitud.getCabezasInicial());
        lote.setPesoPromedioIngresoKg(solicitud.getPesoPromedioIngresoKg());
        lote.setPrecioCompraKg(solicitud.getPrecioCompraKg());
        lote.setCostoHoteleriaDia(solicitud.getCostoHoteleriaDia());
        lote.setObservaciones(solicitud.getObservaciones());
        asignarDietaSiInformada(lote, solicitud.getDietaId(), empresaId);
        lote.setEstado(FeedlotLoteEstado.ACTIVO);

        corral.setEstado(FeedlotCorralEstado.OCUPADO);
        corralRepository.save(corral);
        return aLoteRespuesta(loteRepository.save(lote));
    }

    @Transactional
    public FeedlotLoteRespuesta actualizarLote(Long loteId, FeedlotLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == FeedlotLoteEstado.CERRADO) {
            throw new IllegalStateException("No se puede editar un lote cerrado");
        }
        if (solicitud.getNombre() != null) {
            lote.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getCategoriaId() != null) {
            lote.setCategoria(categoriaRepository.buscarPorIdYEmpresaId(solicitud.getCategoriaId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada")));
        }
        if (solicitud.getRazaId() != null) {
            lote.setRaza(razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada")));
        }
        if (solicitud.getProveedorId() != null) {
            lote.setProveedor(proveedorRepository.buscarPorIdYEmpresaId(solicitud.getProveedorId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado")));
        }
        if (solicitud.getTipoTenencia() != null) {
            lote.setTipoTenencia(solicitud.getTipoTenencia());
        }
        if (solicitud.getPrecioCompraKg() != null) {
            lote.setPrecioCompraKg(solicitud.getPrecioCompraKg());
        }
        if (solicitud.getCostoHoteleriaDia() != null) {
            lote.setCostoHoteleriaDia(solicitud.getCostoHoteleriaDia());
        }
        if (solicitud.getObservaciones() != null) {
            lote.setObservaciones(solicitud.getObservaciones());
        }
        if (solicitud.getDietaId() != null) {
            asignarDietaSiInformada(lote, solicitud.getDietaId(), empresaId);
        }
        return aLoteRespuesta(loteRepository.save(lote));
    }

    @Transactional
    public FeedlotLoteRespuesta cerrarLote(Long loteId, FeedlotCierreLoteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == FeedlotLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        int cabezas = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        if (cabezas > 0 && !Boolean.TRUE.equals(solicitud != null ? solicitud.getConfirmarConCabezas() : null)) {
            throw new IllegalStateException(
                    "El lote aún tiene cabezas activas. Confirme el cierre con confirmarConCabezas=true");
        }
        cerrarLoteYLiberarCorral(lote);
        return aLoteRespuesta(lote);
    }

    @Transactional
    public FeedlotAjustePlantelRespuesta registrarAjustePlantel(Long loteId, FeedlotAjustePlantelSolicitud solicitud, User usuario) {
        validarRolAjuste(usuario);
        if (solicitud.getCabezasDespues() == null || solicitud.getCabezasDespues() < 0) {
            throw new IllegalArgumentException("Las cabezas después del ajuste deben ser un número no negativo");
        }
        if (solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()) {
            throw new IllegalArgumentException("El motivo del ajuste es obligatorio");
        }
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == FeedlotLoteEstado.CERRADO) {
            throw new IllegalStateException("No se puede ajustar plantel en un lote cerrado");
        }
        int antes = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        int despues = solicitud.getCabezasDespues();

        FeedlotAjustePlantel ajuste = new FeedlotAjustePlantel();
        ajuste.setLote(lote);
        ajuste.setEmpresaId(empresaId);
        ajuste.setUsuario(usuario);
        ajuste.setFecha(solicitud.getFecha() != null ? solicitud.getFecha() : LocalDate.now());
        ajuste.setCabezasAntes(antes);
        ajuste.setCabezasDespues(despues);
        ajuste.setMotivo(solicitud.getMotivo().trim());
        ajustePlantelRepository.save(ajuste);

        lote.setCabezasActuales(despues);
        if (despues <= 0) {
            cerrarLoteYLiberarCorral(lote);
        } else {
            loteRepository.save(lote);
        }
        return aAjusteRespuesta(ajuste);
    }

    @Transactional(readOnly = true)
    public List<FeedlotAjustePlantelRespuesta> listarAjustesPlantel(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        obtenerEntidadLote(empresaId, loteId);
        return ajustePlantelRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aAjusteRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cerrarLoteYLiberarCorral(FeedlotLote lote) {
        lote.setEstado(FeedlotLoteEstado.CERRADO);
        lote.setFechaCierre(LocalDate.now());
        if (lote.getCabezasActuales() != null && lote.getCabezasActuales() < 0) {
            lote.setCabezasActuales(0);
        }
        loteRepository.save(lote);
        FeedlotCorral corral = lote.getCorral();
        if (corral != null && corral.getEstado() == FeedlotCorralEstado.OCUPADO) {
            corral.setEstado(FeedlotCorralEstado.DISPONIBLE);
            corralRepository.save(corral);
        }
    }

    @Transactional(readOnly = true)
    public FeedlotLote obtenerEntidadLote(Long empresaId, Long loteId) {
        return loteRepository.buscarPorIdYEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote feedlot no encontrado"));
    }

    @Transactional
    public FeedlotLote guardarLote(FeedlotLote lote) {
        return loteRepository.save(lote);
    }

    private static void validarAlta(FeedlotLoteSolicitud solicitud) {
        if (solicitud.getCorralId() == null) {
            throw new IllegalArgumentException("El corral es obligatorio");
        }
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del lote es obligatorio");
        }
        if (solicitud.getCategoriaId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (solicitud.getFechaIngreso() == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria");
        }
        if (solicitud.getCabezasInicial() == null || solicitud.getCabezasInicial() <= 0) {
            throw new IllegalArgumentException("Las cabezas iniciales deben ser mayores a cero");
        }
        if (solicitud.getPesoPromedioIngresoKg() == null
                || solicitud.getPesoPromedioIngresoKg().signum() <= 0) {
            throw new IllegalArgumentException("El peso promedio de ingreso debe ser mayor a cero");
        }
    }

    private static void validarRolAjuste(User usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuario obligatorio para ajuste de plantel");
        }
        boolean permitido = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(r -> {
                    String nombre = r.getNombre();
                    if (nombre == null) {
                        return false;
                    }
                    String upper = nombre.toUpperCase();
                    return upper.contains("ADMIN") || upper.contains("SUPERVISOR")
                            || "JEFE_CAMPO".equals(upper) || "ADMINISTRADOR".equals(upper);
                });
        if (!permitido) {
            throw new IllegalStateException("Solo administradores o supervisores pueden ajustar plantel");
        }
    }

    private FeedlotLoteRespuesta aLoteRespuesta(FeedlotLote l) {
        FeedlotLoteRespuesta dto = new FeedlotLoteRespuesta();
        dto.setId(l.getId());
        dto.setEmpresaId(l.getEmpresaId());
        if (l.getCorral() != null) {
            dto.setCorralId(l.getCorral().getId());
            dto.setCorralNombre(l.getCorral().getNombre());
            dto.setCorralEstado(l.getCorral().getEstado());
            if (l.getCorral().getEstablecimiento() != null) {
                dto.setEstablecimientoId(l.getCorral().getEstablecimiento().getId());
                dto.setEstablecimientoNombre(l.getCorral().getEstablecimiento().getNombre());
            }
        }
        dto.setCampanaId(l.getCampanaId());
        dto.setNombre(l.getNombre());
        if (l.getCategoria() != null) {
            dto.setCategoriaId(l.getCategoria().getId());
            dto.setCategoriaNombre(l.getCategoria().getNombre());
        }
        if (l.getRaza() != null) {
            dto.setRazaId(l.getRaza().getId());
            dto.setRazaNombre(l.getRaza().getNombre());
        }
        if (l.getProveedor() != null) {
            dto.setProveedorId(l.getProveedor().getId());
            dto.setProveedorNombre(l.getProveedor().getNombre());
        }
        dto.setTipoTenencia(l.getTipoTenencia());
        dto.setFechaIngreso(l.getFechaIngreso());
        dto.setFechaCierre(l.getFechaCierre());
        dto.setCabezasInicial(l.getCabezasInicial());
        dto.setCabezasActuales(l.getCabezasActuales());
        dto.setPesoPromedioIngresoKg(l.getPesoPromedioIngresoKg());
        dto.setPrecioCompraKg(l.getPrecioCompraKg());
        dto.setCostoHoteleriaDia(l.getCostoHoteleriaDia());
        if (l.getDieta() != null) {
            dto.setDietaId(l.getDieta().getId());
            dto.setDietaNombre(l.getDieta().getNombre());
        }
        dto.setEstado(l.getEstado());
        dto.setObservaciones(l.getObservaciones());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setUpdatedAt(l.getUpdatedAt());
        return dto;
    }

    private void asignarDietaSiInformada(FeedlotLote lote, Long dietaId, Long empresaId) {
        if (dietaId == null) {
            lote.setDieta(null);
            return;
        }
        FeedlotDieta dieta = dietaRepository.buscarPorIdYEmpresaId(dietaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Dieta no encontrada"));
        lote.setDieta(dieta);
    }

    private FeedlotAjustePlantelRespuesta aAjusteRespuesta(FeedlotAjustePlantel a) {
        FeedlotAjustePlantelRespuesta dto = new FeedlotAjustePlantelRespuesta();
        dto.setId(a.getId());
        dto.setLoteId(a.getLote().getId());
        dto.setEmpresaId(a.getEmpresaId());
        if (a.getUsuario() != null) {
            dto.setUsuarioId(a.getUsuario().getId());
            dto.setUsuarioNombre(a.getUsuario().getEmail());
        }
        dto.setFecha(a.getFecha());
        dto.setCabezasAntes(a.getCabezasAntes());
        dto.setCabezasDespues(a.getCabezasDespues());
        dto.setMotivo(a.getMotivo());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
