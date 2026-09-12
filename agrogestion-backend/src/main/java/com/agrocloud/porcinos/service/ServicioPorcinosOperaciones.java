package com.agrocloud.porcinos.service;

import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.porcinos.model.dto.*;
import com.agrocloud.porcinos.model.entity.*;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicioPorcinosOperaciones {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioPorcinosLotes servicioLotes;
    private final PorcinosPesadaRepository pesadaRepository;
    private final PorcinosConsumoRepository consumoRepository;
    private final PorcinosMuerteRepository muerteRepository;
    private final PorcinosVentaRepository ventaRepository;
    private final PorcinosEventoSanitarioRepository eventoSanitarioRepository;
    private final PorcinosCausaMortalidadRepository causaMortalidadRepository;
    private final InventoryService inventoryService;

    public ServicioPorcinosOperaciones(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioPorcinosLotes servicioLotes,
            PorcinosPesadaRepository pesadaRepository,
            PorcinosConsumoRepository consumoRepository,
            PorcinosMuerteRepository muerteRepository,
            PorcinosVentaRepository ventaRepository,
            PorcinosEventoSanitarioRepository eventoSanitarioRepository,
            PorcinosCausaMortalidadRepository causaMortalidadRepository,
            InventoryService inventoryService) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.pesadaRepository = pesadaRepository;
        this.consumoRepository = consumoRepository;
        this.muerteRepository = muerteRepository;
        this.ventaRepository = ventaRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.causaMortalidadRepository = causaMortalidadRepository;
        this.inventoryService = inventoryService;
    }

    private PorcinosLote loteActivo(Long empresaId, Long loteId) {
        PorcinosLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == PorcinosLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        return lote;
    }

    @Transactional(readOnly = true)
    public List<PorcinosPesadaRespuesta> listarPesadas(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return pesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aPesadaRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PorcinosConsumoRespuesta> listarConsumos(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return consumoRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aConsumoRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PorcinosMuerteRespuesta> listarMuertes(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return muerteRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aMuerteRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PorcinosEventoSanitarioRespuesta> listarEventosSanitariosEmpresa() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return eventoSanitarioRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aEventoRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PorcinosEventoSanitarioRespuesta> listarEventosSanitarios(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return eventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aEventoRespuesta)
                .toList();
    }

    @Transactional
    public PorcinosPesadaRespuesta registrarPesada(Long loteId, PorcinosPesadaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        PorcinosLote lote = loteActivo(empresaId, loteId);
        validarPesada(solicitud);
        PorcinosPesada p = new PorcinosPesada();
        p.setLote(lote);
        p.setEmpresaId(empresaId);
        p.setFecha(solicitud.getFecha());
        p.setPesoPromedioKg(solicitud.getPesoPromedioKg());
        p.setCabezasMuestreadas(solicitud.getCabezasMuestreadas());
        p.setObservaciones(solicitud.getObservaciones());
        return aPesadaRespuesta(pesadaRepository.save(p));
    }

    @Transactional
    public PorcinosConsumoRespuesta registrarConsumo(Long loteId, PorcinosConsumoSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        PorcinosLote lote = loteActivo(empresaId, loteId);
        validarConsumo(solicitud);

        PorcinosConsumo c = new PorcinosConsumo();
        c.setLote(lote);
        c.setEmpresaId(empresaId);
        c.setCampanaId(lote.getCampanaId());
        c.setInsumoId(solicitud.getInsumoId());
        c.setFecha(solicitud.getFecha());
        c.setCantidadKg(solicitud.getCantidadKg());
        c.setObservaciones(solicitud.getObservaciones());
        c = consumoRepository.saveAndFlush(c);

        InventoryResult resultado = inventoryService.consumir(
                empresaId,
                solicitud.getInsumoId(),
                solicitud.getCantidadKg(),
                InventoryOrigin.PORCINOS,
                c.getId(),
                usuarioId);
        if (!resultado.exito()) {
            throw new IllegalStateException(
                    resultado.mensaje() != null ? resultado.mensaje() : "No se pudo registrar el egreso de inventario");
        }
        return aConsumoRespuesta(c);
    }

    @Transactional
    public PorcinosMuerteRespuesta registrarMuerte(Long loteId, PorcinosMuerteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        PorcinosLote lote = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la muerte es obligatoria");
        }
        if (solicitud.getCabezas() == null || solicitud.getCabezas() <= 0) {
            throw new IllegalStateException("Las cabezas deben ser mayores a cero");
        }
        int disponible = servicioLotes.calcularCabezasDisponibles(lote);
        if (solicitud.getCabezas() > disponible) {
            throw new IllegalStateException("Las cabezas superan el plantel disponible (" + disponible + ")");
        }

        PorcinosMuerte m = new PorcinosMuerte();
        m.setLote(lote);
        m.setEmpresaId(empresaId);
        m.setFecha(solicitud.getFecha());
        m.setCabezas(solicitud.getCabezas());
        if (solicitud.getCausaMortalidadId() != null) {
            m.setCausaMortalidad(causaMortalidadRepository.buscarPorIdYEmpresaId(
                            solicitud.getCausaMortalidadId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Causa de mortalidad no encontrada")));
        }
        m.setObservaciones(solicitud.getObservaciones());

        int nuevo = disponible - solicitud.getCabezas();
        lote.setCabezasActuales(nuevo);
        if (nuevo <= 0) {
            lote.setCabezasActuales(0);
            servicioLotes.cerrarLoteYLiberarGalpon(lote);
        } else {
            servicioLotes.guardarLote(lote);
        }
        return aMuerteRespuesta(muerteRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<PorcinosVentaRespuesta> listarVentas() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return ventaRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aVentaRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PorcinosVentaRespuesta> listarVentasPorLote(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return ventaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aVentaRespuesta)
                .toList();
    }

    @Transactional
    public PorcinosVentaRespuesta registrarVenta(Long loteId, PorcinosVentaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        PorcinosLote lote = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la venta es obligatoria");
        }
        if (solicitud.getTipo() == null) {
            throw new IllegalStateException("El tipo de venta es obligatorio");
        }
        if (solicitud.getCabezas() == null || solicitud.getCabezas() <= 0) {
            throw new IllegalStateException("Las cabezas vendidas deben ser mayores a cero");
        }
        int disponible = servicioLotes.calcularCabezasDisponibles(lote);
        if (solicitud.getCabezas() > disponible) {
            throw new IllegalStateException("Las cabezas superan el plantel disponible (" + disponible + ")");
        }

        int nuevo = disponible - solicitud.getCabezas();
        lote.setCabezasActuales(nuevo);
        if (nuevo <= 0) {
            lote.setCabezasActuales(0);
            servicioLotes.cerrarLoteYLiberarGalpon(lote);
        } else {
            servicioLotes.guardarLote(lote);
        }

        PorcinosVenta v = new PorcinosVenta();
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
        v = ventaRepository.save(v);
        return aVentaRespuesta(v);
    }

    @Transactional
    public PorcinosEventoSanitarioRespuesta registrarEventoSanitario(
            Long loteId, PorcinosEventoSanitarioSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        PorcinosLote lote = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null || solicitud.getTipo() == null) {
            throw new IllegalArgumentException("Fecha y tipo son obligatorios");
        }

        PorcinosEventoSanitario e = new PorcinosEventoSanitario();
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
                    InventoryOrigin.PORCINOS,
                    e.getId(),
                    usuarioId);
            if (!res.exito()) {
                throw new IllegalStateException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
        }
        return aEventoRespuesta(e);
    }

    private static void validarPesada(PorcinosPesadaSolicitud solicitud) {
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (solicitud.getPesoPromedioKg() == null || solicitud.getPesoPromedioKg().signum() <= 0) {
            throw new IllegalArgumentException("El peso promedio debe ser mayor a cero");
        }
    }

    private static void validarConsumo(PorcinosConsumoSolicitud solicitud) {
        if (solicitud.getInsumoId() == null || solicitud.getFecha() == null
                || solicitud.getCantidadKg() == null || solicitud.getCantidadKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Insumo, fecha y cantidad válida son obligatorios");
        }
    }

    private static BigDecimal calcularTotalVenta(PorcinosVentaSolicitud solicitud) {
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

    private PorcinosPesadaRespuesta aPesadaRespuesta(PorcinosPesada p) {
        PorcinosPesadaRespuesta dto = new PorcinosPesadaRespuesta();
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

    private PorcinosConsumoRespuesta aConsumoRespuesta(PorcinosConsumo c) {
        PorcinosConsumoRespuesta dto = new PorcinosConsumoRespuesta();
        dto.setId(c.getId());
        dto.setLoteId(c.getLote().getId());
        dto.setEmpresaId(c.getEmpresaId());
        dto.setCampanaId(c.getCampanaId());
        dto.setInsumoId(c.getInsumoId());
        dto.setFecha(c.getFecha());
        dto.setCantidadKg(c.getCantidadKg());
        dto.setObservaciones(c.getObservaciones());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    private PorcinosMuerteRespuesta aMuerteRespuesta(PorcinosMuerte m) {
        PorcinosMuerteRespuesta dto = new PorcinosMuerteRespuesta();
        dto.setId(m.getId());
        dto.setLoteId(m.getLote().getId());
        dto.setEmpresaId(m.getEmpresaId());
        dto.setFecha(m.getFecha());
        dto.setCabezas(m.getCabezas());
        if (m.getCausaMortalidad() != null) {
            dto.setCausaMortalidadId(m.getCausaMortalidad().getId());
            dto.setCausaMortalidadNombre(m.getCausaMortalidad().getNombre());
        }
        dto.setObservaciones(m.getObservaciones());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    private PorcinosVentaRespuesta aVentaRespuesta(PorcinosVenta v) {
        PorcinosVentaRespuesta dto = new PorcinosVentaRespuesta();
        dto.setId(v.getId());
        dto.setLoteId(v.getLote().getId());
        dto.setLoteNombre(v.getLote().getNombre());
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

    private PorcinosEventoSanitarioRespuesta aEventoRespuesta(PorcinosEventoSanitario e) {
        PorcinosEventoSanitarioRespuesta dto = new PorcinosEventoSanitarioRespuesta();
        dto.setId(e.getId());
        dto.setLoteId(e.getLote().getId());
        dto.setLoteNombre(e.getLote().getNombre());
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
