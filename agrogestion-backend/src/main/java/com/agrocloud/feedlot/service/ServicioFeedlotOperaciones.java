package com.agrocloud.feedlot.service;

import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.*;
import com.agrocloud.feedlot.model.entity.*;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioFeedlotOperaciones {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioFeedlotLotes servicioLotes;
    private final FeedlotPesadaRepository pesadaRepository;
    private final FeedlotConsumoRepository consumoRepository;
    private final FeedlotMuerteRepository muerteRepository;
    private final FeedlotVentaRepository ventaRepository;
    private final FeedlotEventoSanitarioRepository eventoSanitarioRepository;
    private final FeedlotMotivoMuerteRepository motivoMuerteRepository;
    private final InventoryService inventoryService;

    public ServicioFeedlotOperaciones(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioFeedlotLotes servicioLotes,
            FeedlotPesadaRepository pesadaRepository,
            FeedlotConsumoRepository consumoRepository,
            FeedlotMuerteRepository muerteRepository,
            FeedlotVentaRepository ventaRepository,
            FeedlotEventoSanitarioRepository eventoSanitarioRepository,
            FeedlotMotivoMuerteRepository motivoMuerteRepository,
            InventoryService inventoryService) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.pesadaRepository = pesadaRepository;
        this.consumoRepository = consumoRepository;
        this.muerteRepository = muerteRepository;
        this.ventaRepository = ventaRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.motivoMuerteRepository = motivoMuerteRepository;
        this.inventoryService = inventoryService;
    }

    private FeedlotLote loteActivo(Long empresaId, Long loteId) {
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == FeedlotLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        return lote;
    }

    @Transactional(readOnly = true)
    public List<FeedlotPesadaRespuesta> listarPesadas(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return pesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aPesadaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotPesadaRespuesta registrarPesada(Long loteId, FeedlotPesadaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = loteActivo(empresaId, loteId);
        validarPesada(solicitud);
        FeedlotPesada p = new FeedlotPesada();
        p.setLote(lote);
        p.setEmpresaId(empresaId);
        p.setFecha(solicitud.getFecha());
        p.setPesoPromedioKg(solicitud.getPesoPromedioKg());
        p.setCabezasMuestreadas(solicitud.getCabezasMuestreadas());
        p.setObservaciones(solicitud.getObservaciones());
        return aPesadaRespuesta(pesadaRepository.save(p));
    }

    @Transactional
    public FeedlotPesadaRespuesta actualizarPesada(Long loteId, Long pesadaId, FeedlotPesadaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        loteActivo(empresaId, loteId);
        FeedlotPesada p = pesadaRepository.buscarPorIdYEmpresaId(pesadaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesada no encontrada"));
        if (!p.getLote().getId().equals(loteId)) {
            throw new IllegalArgumentException("La pesada no pertenece a este lote");
        }
        validarPesada(solicitud);
        p.setFecha(solicitud.getFecha());
        p.setPesoPromedioKg(solicitud.getPesoPromedioKg());
        p.setCabezasMuestreadas(solicitud.getCabezasMuestreadas());
        if (solicitud.getObservaciones() != null) {
            p.setObservaciones(solicitud.getObservaciones());
        }
        return aPesadaRespuesta(pesadaRepository.save(p));
    }

    @Transactional
    public void eliminarPesada(Long loteId, Long pesadaId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        loteActivo(empresaId, loteId);
        FeedlotPesada p = pesadaRepository.buscarPorIdYEmpresaId(pesadaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesada no encontrada"));
        if (!p.getLote().getId().equals(loteId)) {
            throw new IllegalArgumentException("La pesada no pertenece a este lote");
        }
        pesadaRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public List<FeedlotConsumoRespuesta> listarConsumos(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return consumoRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aConsumoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotConsumoRespuesta registrarConsumo(Long loteId, FeedlotConsumoSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        FeedlotLote lote = loteActivo(empresaId, loteId);
        validarConsumo(solicitud);

        FeedlotConsumo c = new FeedlotConsumo();
        c.setLote(lote);
        c.setEmpresaId(empresaId);
        c.setCampanaId(lote.getCampanaId());
        c.setInsumoId(solicitud.getInsumoId());
        c.setFecha(solicitud.getFecha());
        c.setCantidadKg(solicitud.getCantidadKg());
        c.setMateriaSecaPct(solicitud.getMateriaSecaPct());
        c.setObservaciones(solicitud.getObservaciones());
        c = consumoRepository.saveAndFlush(c);

        InventoryResult resultado = inventoryService.consumir(
                empresaId,
                solicitud.getInsumoId(),
                solicitud.getCantidadKg(),
                InventoryOrigin.FEEDLOT,
                c.getId(),
                usuarioId);
        if (!resultado.exito()) {
            throw new IllegalStateException(
                    resultado.mensaje() != null ? resultado.mensaje() : "No se pudo registrar el egreso de inventario");
        }
        return aConsumoRespuesta(c);
    }

    @Transactional
    public FeedlotConsumoRespuesta actualizarConsumo(
            Long loteId, Long consumoId, FeedlotConsumoActualizarSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        loteActivo(empresaId, loteId);
        FeedlotConsumo c = consumoRepository.buscarPorIdYEmpresaId(consumoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumo no encontrado"));
        if (!c.getLote().getId().equals(loteId)) {
            throw new IllegalArgumentException("El consumo no pertenece a este lote");
        }
        if (solicitud.getFecha() == null || solicitud.getCantidadKg() == null
                || solicitud.getCantidadKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fecha y cantidad válida son obligatorias");
        }

        Long insumoId = c.getInsumoId();
        BigDecimal anterior = c.getCantidadKg();
        BigDecimal delta = solicitud.getCantidadKg().subtract(anterior);
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            InventoryResult res = inventoryService.consumir(
                    empresaId, insumoId, delta, InventoryOrigin.FEEDLOT, c.getId(), usuarioId);
            if (!res.exito()) {
                throw new IllegalStateException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
        } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
            InventoryResult res = inventoryService.reponer(
                    empresaId, insumoId, delta.negate(), InventoryOrigin.FEEDLOT, c.getId(), usuarioId);
            if (!res.exito()) {
                throw new IllegalStateException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
        }

        c.setFecha(solicitud.getFecha());
        c.setCantidadKg(solicitud.getCantidadKg());
        if (solicitud.getMateriaSecaPct() != null) {
            c.setMateriaSecaPct(solicitud.getMateriaSecaPct());
        }
        if (solicitud.getObservaciones() != null) {
            c.setObservaciones(solicitud.getObservaciones());
        }
        return aConsumoRespuesta(consumoRepository.save(c));
    }

    @Transactional
    public void eliminarConsumo(Long loteId, Long consumoId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        loteActivo(empresaId, loteId);
        FeedlotConsumo c = consumoRepository.buscarPorIdYEmpresaId(consumoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumo no encontrado"));
        if (!c.getLote().getId().equals(loteId)) {
            throw new IllegalArgumentException("El consumo no pertenece a este lote");
        }
        InventoryResult res = inventoryService.reponer(
                empresaId, c.getInsumoId(), c.getCantidadKg(), InventoryOrigin.FEEDLOT, c.getId(), usuarioId);
        if (!res.exito()) {
            throw new IllegalStateException(res.mensaje() != null ? res.mensaje() : "Error al revertir inventario");
        }
        consumoRepository.delete(c);
    }

    @Transactional(readOnly = true)
    public List<FeedlotMuerteRespuesta> listarMuertes(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return muerteRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aMuerteRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotMuerteRespuesta registrarMuerte(Long loteId, FeedlotMuerteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la muerte es obligatoria");
        }
        if (solicitud.getFecha().isBefore(lote.getFechaIngreso())) {
            throw new IllegalStateException("La fecha de muerte no puede ser anterior al ingreso del lote");
        }
        if (solicitud.getCabezas() == null || solicitud.getCabezas() <= 0) {
            throw new IllegalStateException("Las cabezas deben ser mayores a cero");
        }
        int disponible = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        if (solicitud.getCabezas() > disponible) {
            throw new IllegalStateException("Las cabezas superan el plantel actual (" + disponible + ")");
        }

        FeedlotMuerte m = new FeedlotMuerte();
        m.setLote(lote);
        m.setEmpresaId(empresaId);
        m.setFecha(solicitud.getFecha());
        m.setCabezas(solicitud.getCabezas());
        if (solicitud.getMotivoId() != null) {
            m.setMotivo(motivoMuerteRepository.buscarPorIdYEmpresaId(solicitud.getMotivoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Motivo de muerte no encontrado")));
        }
        m.setObservaciones(solicitud.getObservaciones());

        int nuevo = disponible - solicitud.getCabezas();
        lote.setCabezasActuales(nuevo);
        if (nuevo <= 0) {
            lote.setCabezasActuales(0);
            servicioLotes.cerrarLoteYLiberarCorral(lote);
        } else {
            servicioLotes.guardarLote(lote);
        }
        return aMuerteRespuesta(muerteRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<FeedlotVentaRespuesta> listarVentas(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return ventaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aVentaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotVentaRespuesta registrarVenta(Long loteId, FeedlotVentaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la venta es obligatoria");
        }
        if (solicitud.getTipo() == null) {
            throw new IllegalStateException("El tipo de venta es obligatorio");
        }
        if (solicitud.getCabezas() == null || solicitud.getCabezas() <= 0) {
            throw new IllegalStateException("Las cabezas vendidas deben ser mayores a cero");
        }
        int disponible = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        if (solicitud.getCabezas() > disponible) {
            throw new IllegalStateException("Las cabezas superan el plantel actual (" + disponible + ")");
        }

        int nuevo = disponible - solicitud.getCabezas();
        lote.setCabezasActuales(nuevo);
        if (nuevo <= 0) {
            lote.setCabezasActuales(0);
            servicioLotes.cerrarLoteYLiberarCorral(lote);
        } else {
            servicioLotes.guardarLote(lote);
        }

        FeedlotVenta v = new FeedlotVenta();
        v.setLote(lote);
        v.setEmpresaId(empresaId);
        v.setCampanaId(lote.getCampanaId());
        v.setFecha(solicitud.getFecha());
        v.setTipo(solicitud.getTipo());
        v.setCabezas(solicitud.getCabezas());
        v.setPesoPromedioKg(solicitud.getPesoPromedioKg());
        v.setPrecioKg(solicitud.getPrecioKg());
        v.setTotal(calcularTotalVenta(solicitud));
        v.setComprador(solicitud.getComprador());
        v.setObservaciones(solicitud.getObservaciones());
        return aVentaRespuesta(ventaRepository.save(v));
    }

    @Transactional(readOnly = true)
    public List<FeedlotEventoSanitarioRespuesta> listarEventosSanitarios(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return eventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aEventoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotEventoSanitarioRespuesta registrarEventoSanitario(
            Long loteId, FeedlotEventoSanitarioSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        FeedlotLote lote = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null || solicitud.getTipo() == null) {
            throw new IllegalArgumentException("Fecha y tipo son obligatorios");
        }

        FeedlotEventoSanitario e = new FeedlotEventoSanitario();
        e.setLote(lote);
        e.setEmpresaId(empresaId);
        e.setFecha(solicitud.getFecha());
        e.setTipo(solicitud.getTipo());
        e.setDescripcion(solicitud.getDescripcion());
        e.setInsumoId(solicitud.getInsumoId());
        e.setDiasRetiro(solicitud.getDiasRetiro());
        e.setObservaciones(solicitud.getObservaciones());
        e = eventoSanitarioRepository.saveAndFlush(e);

        if (solicitud.getInsumoId() != null && solicitud.getCantidadInsumo() != null
                && solicitud.getCantidadInsumo().compareTo(BigDecimal.ZERO) > 0) {
            InventoryResult res = inventoryService.consumir(
                    empresaId,
                    solicitud.getInsumoId(),
                    solicitud.getCantidadInsumo(),
                    InventoryOrigin.FEEDLOT,
                    e.getId(),
                    usuarioId);
            if (!res.exito()) {
                throw new IllegalStateException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
        }
        return aEventoRespuesta(e);
    }

    @Transactional
    public FeedlotEventoSanitarioRespuesta actualizarEventoSanitario(
            Long loteId, Long eventoId, FeedlotEventoSanitarioSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        loteActivo(empresaId, loteId);
        FeedlotEventoSanitario e = eventoSanitarioRepository.buscarPorIdYEmpresaId(eventoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento sanitario no encontrado"));
        if (!e.getLote().getId().equals(loteId)) {
            throw new IllegalArgumentException("El evento no pertenece a este lote");
        }
        if (solicitud.getFecha() != null) {
            e.setFecha(solicitud.getFecha());
        }
        if (solicitud.getTipo() != null) {
            e.setTipo(solicitud.getTipo());
        }
        if (solicitud.getDescripcion() != null) {
            e.setDescripcion(solicitud.getDescripcion());
        }
        if (solicitud.getDiasRetiro() != null) {
            e.setDiasRetiro(solicitud.getDiasRetiro());
        }
        if (solicitud.getObservaciones() != null) {
            e.setObservaciones(solicitud.getObservaciones());
        }
        return aEventoRespuesta(eventoSanitarioRepository.save(e));
    }

    @Transactional
    public void eliminarEventoSanitario(Long loteId, Long eventoId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        loteActivo(empresaId, loteId);
        FeedlotEventoSanitario e = eventoSanitarioRepository.buscarPorIdYEmpresaId(eventoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento sanitario no encontrado"));
        if (!e.getLote().getId().equals(loteId)) {
            throw new IllegalArgumentException("El evento no pertenece a este lote");
        }
        eventoSanitarioRepository.delete(e);
    }

    private static void validarPesada(FeedlotPesadaSolicitud solicitud) {
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (solicitud.getPesoPromedioKg() == null || solicitud.getPesoPromedioKg().signum() <= 0) {
            throw new IllegalArgumentException("El peso promedio debe ser mayor a cero");
        }
    }

    private static void validarConsumo(FeedlotConsumoSolicitud solicitud) {
        if (solicitud.getInsumoId() == null || solicitud.getFecha() == null
                || solicitud.getCantidadKg() == null || solicitud.getCantidadKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Insumo, fecha y cantidad válida son obligatorios");
        }
    }

    private static BigDecimal calcularTotalVenta(FeedlotVentaSolicitud solicitud) {
        if (solicitud.getTotal() != null) {
            return solicitud.getTotal();
        }
        if (solicitud.getPrecioKg() != null && solicitud.getPesoPromedioKg() != null) {
            return solicitud.getPrecioKg()
                    .multiply(solicitud.getPesoPromedioKg())
                    .multiply(BigDecimal.valueOf(solicitud.getCabezas()));
        }
        return null;
    }

    private FeedlotPesadaRespuesta aPesadaRespuesta(FeedlotPesada p) {
        FeedlotPesadaRespuesta dto = new FeedlotPesadaRespuesta();
        dto.setId(p.getId());
        dto.setLoteId(p.getLote().getId());
        dto.setEmpresaId(p.getEmpresaId());
        dto.setFecha(p.getFecha());
        dto.setPesoPromedioKg(p.getPesoPromedioKg());
        dto.setCabezasMuestreadas(p.getCabezasMuestreadas());
        dto.setObservaciones(p.getObservaciones());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    private FeedlotConsumoRespuesta aConsumoRespuesta(FeedlotConsumo c) {
        FeedlotConsumoRespuesta dto = new FeedlotConsumoRespuesta();
        dto.setId(c.getId());
        dto.setLoteId(c.getLote().getId());
        dto.setEmpresaId(c.getEmpresaId());
        dto.setCampanaId(c.getCampanaId());
        dto.setInsumoId(c.getInsumoId());
        dto.setFecha(c.getFecha());
        dto.setCantidadKg(c.getCantidadKg());
        dto.setMateriaSecaPct(c.getMateriaSecaPct());
        dto.setObservaciones(c.getObservaciones());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    private FeedlotMuerteRespuesta aMuerteRespuesta(FeedlotMuerte m) {
        FeedlotMuerteRespuesta dto = new FeedlotMuerteRespuesta();
        dto.setId(m.getId());
        dto.setLoteId(m.getLote().getId());
        dto.setEmpresaId(m.getEmpresaId());
        dto.setFecha(m.getFecha());
        dto.setCabezas(m.getCabezas());
        if (m.getMotivo() != null) {
            dto.setMotivoId(m.getMotivo().getId());
            dto.setMotivoNombre(m.getMotivo().getNombre());
        }
        dto.setObservaciones(m.getObservaciones());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    private FeedlotVentaRespuesta aVentaRespuesta(FeedlotVenta v) {
        FeedlotVentaRespuesta dto = new FeedlotVentaRespuesta();
        dto.setId(v.getId());
        dto.setLoteId(v.getLote().getId());
        dto.setEmpresaId(v.getEmpresaId());
        dto.setCampanaId(v.getCampanaId());
        dto.setFecha(v.getFecha());
        dto.setTipo(v.getTipo());
        dto.setCabezas(v.getCabezas());
        dto.setPesoPromedioKg(v.getPesoPromedioKg());
        dto.setPrecioKg(v.getPrecioKg());
        dto.setTotal(v.getTotal());
        dto.setComprador(v.getComprador());
        dto.setIngresoId(v.getIngresoId());
        dto.setObservaciones(v.getObservaciones());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }

    private FeedlotEventoSanitarioRespuesta aEventoRespuesta(FeedlotEventoSanitario e) {
        FeedlotEventoSanitarioRespuesta dto = new FeedlotEventoSanitarioRespuesta();
        dto.setId(e.getId());
        dto.setLoteId(e.getLote().getId());
        dto.setEmpresaId(e.getEmpresaId());
        dto.setFecha(e.getFecha());
        dto.setTipo(e.getTipo());
        dto.setDescripcion(e.getDescripcion());
        dto.setInsumoId(e.getInsumoId());
        dto.setDiasRetiro(e.getDiasRetiro());
        dto.setObservaciones(e.getObservaciones());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
