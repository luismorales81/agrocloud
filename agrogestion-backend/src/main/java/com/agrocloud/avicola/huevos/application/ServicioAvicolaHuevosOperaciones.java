package com.agrocloud.avicola.huevos.application;

import com.agrocloud.avicola.huevos.model.dto.*;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoConsumo;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoEventoSanitario;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoLote;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoProduccionDiaria;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoVenta;
import com.agrocloud.avicola.huevos.model.enums.AvicolaHuevoLoteEstado;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoConsumoRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoEventoSanitarioRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoProduccionDiariaRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoVentaRepository;
import com.agrocloud.core.application.ServicioLibroEmpresa;
import com.agrocloud.core.domain.Ingreso;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.exception.BadRequestException;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.model.enums.ModuloSistema;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaHuevosOperaciones {

    private final ServicioAvicolaHuevosLote servicioLote;
    private final AvicolaHuevoProduccionDiariaRepository produccionRepository;
    private final AvicolaHuevoConsumoRepository consumoRepository;
    private final AvicolaHuevoEventoSanitarioRepository eventoSanitarioRepository;
    private final InventoryService inventoryService;
    private final InsumoRepository insumoRepository;
    private final AvicolaHuevoVentaRepository ventaRepository;
    private final ServicioLibroEmpresa libroEmpresa;

    public ServicioAvicolaHuevosOperaciones(
            ServicioAvicolaHuevosLote servicioLote,
            AvicolaHuevoProduccionDiariaRepository produccionRepository,
            AvicolaHuevoConsumoRepository consumoRepository,
            AvicolaHuevoEventoSanitarioRepository eventoSanitarioRepository,
            InventoryService inventoryService,
            @Qualifier("insumoRepositoryInventario") InsumoRepository insumoRepository,
            AvicolaHuevoVentaRepository ventaRepository,
            ServicioLibroEmpresa libroEmpresa) {
        this.servicioLote = servicioLote;
        this.produccionRepository = produccionRepository;
        this.consumoRepository = consumoRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.inventoryService = inventoryService;
        this.insumoRepository = insumoRepository;
        this.ventaRepository = ventaRepository;
        this.libroEmpresa = libroEmpresa;
    }

    private static int nz(Integer v) {
        return v != null ? v : 0;
    }

    private AvicolaHuevoLote loteActivo(Long empresaId, Long loteId) {
        AvicolaHuevoLote l = servicioLote.obtenerEntidadLote(empresaId, loteId);
        if (l.getEstado() == AvicolaHuevoLoteEstado.CERRADO) {
            throw new IllegalArgumentException("El lote está cerrado");
        }
        return l;
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoProduccionDiariaRespuesta> listarProduccionDiaria(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return produccionRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aProduccionRespuesta)
                .collect(Collectors.toList());
    }

    /**
     * Registra o actualiza la producción del día (restricción única lote + fecha en BD).
     * Total del día = suma de tamaños 1–4 más huevos rotos.
     */
    @Transactional
    public AvicolaHuevoProduccionDiariaRespuesta registrarProduccionDiaria(
            Long empresaId, Long loteId, AvicolaHuevoProduccionDiariaSolicitud solicitud) {
        AvicolaHuevoLote l = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        boolean detallado = solicitud.getHuevosTam1() != null || solicitud.getHuevosTam2() != null
                || solicitud.getHuevosTam3() != null || solicitud.getHuevosTam4() != null
                || solicitud.getHuevosRotos() != null;
        int t1;
        int t2;
        int t3;
        int t4;
        int rotos;
        if (detallado) {
            t1 = nz(solicitud.getHuevosTam1());
            t2 = nz(solicitud.getHuevosTam2());
            t3 = nz(solicitud.getHuevosTam3());
            t4 = nz(solicitud.getHuevosTam4());
            rotos = nz(solicitud.getHuevosRotos());
        } else if (solicitud.getCantidadHuevos() != null && solicitud.getCantidadHuevos() >= 0) {
            t1 = solicitud.getCantidadHuevos();
            t2 = 0;
            t3 = 0;
            t4 = 0;
            rotos = 0;
        } else {
            throw new IllegalArgumentException("Indique la cantidad total de huevos o el desglose por tamaño y rotos");
        }
        if (t1 < 0 || t2 < 0 || t3 < 0 || t4 < 0 || rotos < 0) {
            throw new IllegalArgumentException("Las cantidades no pueden ser negativas");
        }
        int total = t1 + t2 + t3 + t4 + rotos;

        AvicolaHuevoProduccionDiaria p = produccionRepository
                .buscarPorLoteEmpresaYFecha(loteId, empresaId, solicitud.getFecha())
                .orElse(null);
        if (p == null) {
            p = new AvicolaHuevoProduccionDiaria();
            p.setLote(l);
            p.setEmpresaId(empresaId);
            p.setFecha(solicitud.getFecha());
        }
        p.setHuevosTam1(t1);
        p.setHuevosTam2(t2);
        p.setHuevosTam3(t3);
        p.setHuevosTam4(t4);
        p.setHuevosRotos(rotos);
        p.setTotalHuevosDia(total);
        p.setCantidadHuevos(total);
        p.setTemperaturaDia(solicitud.getTemperaturaDia());
        p.setHumedadDia(solicitud.getHumedadDia());
        if (solicitud.getObservaciones() != null) {
            p.setObservaciones(solicitud.getObservaciones());
        }
        return aProduccionRespuesta(produccionRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoConsumoRespuesta> listarConsumos(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return consumoRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aConsumoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaHuevoConsumoRespuesta registrarConsumo(
            Long empresaId, Long loteId, AvicolaHuevoConsumoSolicitud solicitud, Long usuarioId) {
        AvicolaHuevoLote l = loteActivo(empresaId, loteId);
        if (solicitud.getInsumoId() == null || solicitud.getFecha() == null
                || solicitud.getCantidad() == null || solicitud.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Insumo, fecha y cantidad válida son obligatorios");
        }

        AvicolaHuevoConsumo c = new AvicolaHuevoConsumo();
        c.setLote(l);
        c.setEmpresaId(empresaId);
        c.setInsumoId(solicitud.getInsumoId());
        c.setFecha(solicitud.getFecha());
        c.setCantidad(solicitud.getCantidad());
        c.setTipo(solicitud.getTipo() != null ? solicitud.getTipo() : "MANUAL");
        c.setObservaciones(solicitud.getObservaciones());
        c.setCampanaId(l.getCampanaId());
        c = consumoRepository.saveAndFlush(c);

        InventoryResult res = inventoryService.consumir(
                empresaId,
                solicitud.getInsumoId(),
                solicitud.getCantidad(),
                InventoryOrigin.AVICOLA_HUEVOS,
                c.getId(),
                usuarioId
        );
        if (!res.exito()) {
            throw new IllegalArgumentException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
        }
        return aConsumoRespuesta(c);
    }

    /**
     * Corrige cantidad o fecha de un consumo ya registrado y ajusta inventario por la diferencia.
     */
    @Transactional
    public AvicolaHuevoConsumoRespuesta actualizarConsumo(
            Long empresaId,
            Long loteId,
            Long consumoId,
            AvicolaHuevoConsumoActualizarSolicitud solicitud,
            Long usuarioId) {
        loteActivo(empresaId, loteId);
        AvicolaHuevoConsumo c = consumoRepository.buscarPorIdYEmpresaId(consumoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Consumo no encontrado"));
        if (!c.getLote().getId().equals(loteId)) {
            throw new IllegalArgumentException("El consumo no pertenece a este lote");
        }
        if (solicitud.getFecha() == null || solicitud.getCantidad() == null
                || solicitud.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fecha y cantidad válida son obligatorias");
        }
        Long insumoId = c.getInsumoId();
        BigDecimal anterior = c.getCantidad();
        BigDecimal delta = solicitud.getCantidad().subtract(anterior);
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            InventoryResult res = inventoryService.consumir(
                    empresaId, insumoId, delta, InventoryOrigin.AVICOLA_HUEVOS, c.getId(), usuarioId);
            if (!res.exito()) {
                throw new IllegalArgumentException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
        } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
            InventoryResult res = inventoryService.reponer(
                    empresaId, insumoId, delta.negate(), InventoryOrigin.AVICOLA_HUEVOS, c.getId(), usuarioId);
            if (!res.exito()) {
                throw new IllegalArgumentException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
        }
        c.setFecha(solicitud.getFecha());
        c.setCantidad(solicitud.getCantidad());
        if (solicitud.getObservaciones() != null) {
            c.setObservaciones(solicitud.getObservaciones());
        }
        return aConsumoRespuesta(consumoRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoEventoSanitarioRespuesta> listarEventosSanitarios(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return eventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aEventoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaHuevoEventoSanitarioRespuesta registrarEventoSanitario(
            Long empresaId, Long loteId, AvicolaHuevoEventoSanitarioSolicitud solicitud) {
        AvicolaHuevoLote l = loteActivo(empresaId, loteId);
        AvicolaHuevoEventoSanitario e = new AvicolaHuevoEventoSanitario();
        e.setLote(l);
        e.setEmpresaId(empresaId);
        e.setFecha(solicitud.getFecha());
        e.setTipo(solicitud.getTipo());
        e.setDescripcion(solicitud.getDescripcion());
        e.setInsumoId(solicitud.getInsumoId());
        e.setDosis(solicitud.getDosis());
        e.setObservaciones(solicitud.getObservaciones());
        return aEventoRespuesta(eventoSanitarioRepository.save(e));
    }

    @Transactional(readOnly = true)
    public AvicolaHuevosResumenRespuesta resumenLote(Long empresaId, Long loteId) {
        AvicolaHuevoLote l = servicioLote.obtenerEntidadLote(empresaId, loteId);
        Long totalHuevos = produccionRepository.sumarCantidadHuevosPorLoteYEmpresa(loteId, empresaId);
        BigDecimal sumaConsumos = consumoRepository.sumarCantidadConsumidaPorLoteYEmpresa(loteId, empresaId);
        long dias = ChronoUnit.DAYS.between(l.getFechaInicio(), LocalDate.now());
        if (dias < 1) {
            dias = 1;
        }

        AvicolaHuevosResumenRespuesta r = new AvicolaHuevosResumenRespuesta();
        r.setLoteId(loteId);
        r.setCantidadAvesActual(l.getCantidadAvesActual());
        r.setTotalHuevosProducidos(totalHuevos != null ? totalHuevos : 0L);
        r.setSumaConsumosInsumo(sumaConsumos);
        r.setDiasEnPostura(dias);
        int aves = l.getCantidadAvesActual() != null ? l.getCantidadAvesActual() : 0;
        if (aves > 0 && totalHuevos != null && totalHuevos > 0) {
            BigDecimal denom = BigDecimal.valueOf(aves).multiply(BigDecimal.valueOf(dias));
            r.setHuevosPromedioPorAveYdia(
                    BigDecimal.valueOf(totalHuevos).divide(denom, 4, RoundingMode.HALF_UP));
        }
        return r;
    }

    private AvicolaHuevoProduccionDiariaRespuesta aProduccionRespuesta(AvicolaHuevoProduccionDiaria p) {
        AvicolaHuevoProduccionDiariaRespuesta dto = new AvicolaHuevoProduccionDiariaRespuesta();
        dto.setId(p.getId());
        dto.setLoteId(p.getLote() != null ? p.getLote().getId() : null);
        dto.setEmpresaId(p.getEmpresaId());
        dto.setFecha(p.getFecha());
        dto.setCantidadHuevos(p.getCantidadHuevos());
        dto.setHuevosTam1(p.getHuevosTam1());
        dto.setHuevosTam2(p.getHuevosTam2());
        dto.setHuevosTam3(p.getHuevosTam3());
        dto.setHuevosTam4(p.getHuevosTam4());
        dto.setHuevosRotos(p.getHuevosRotos());
        dto.setTotalHuevosDia(p.getTotalHuevosDia());
        dto.setTemperaturaDia(p.getTemperaturaDia());
        dto.setHumedadDia(p.getHumedadDia());
        dto.setObservaciones(p.getObservaciones());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    private AvicolaHuevoConsumoRespuesta aConsumoRespuesta(AvicolaHuevoConsumo c) {
        AvicolaHuevoConsumoRespuesta dto = new AvicolaHuevoConsumoRespuesta();
        dto.setId(c.getId());
        dto.setLoteId(c.getLote() != null ? c.getLote().getId() : null);
        dto.setEmpresaId(c.getEmpresaId());
        dto.setInsumoId(c.getInsumoId());
        dto.setFecha(c.getFecha());
        dto.setCantidad(c.getCantidad());
        dto.setTipo(c.getTipo());
        dto.setObservaciones(c.getObservaciones());
        dto.setCreatedAt(c.getCreatedAt());
        if (c.getInsumoId() != null) {
            insumoRepository.findById(c.getInsumoId()).map(Insumo::getNombre).ifPresent(dto::setInsumoNombre);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AvicolaHuevoVentaRespuesta> listarVentas(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return ventaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aVentaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaHuevoVentaRespuesta registrarVenta(Long empresaId, Long loteId, AvicolaHuevoVentaSolicitud solicitud) {
        AvicolaHuevoLote lote = loteActivo(empresaId, loteId);
        if (solicitud.getFecha() == null) {
            throw new BadRequestException("La fecha de la venta es obligatoria");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
            throw new BadRequestException("La cantidad de huevos vendidos debe ser mayor que cero");
        }
        long comercializables = nzLong(produccionRepository.sumarHuevosComercializables(loteId, empresaId));
        long yaVendidos = nzLong(ventaRepository.sumarCantidadVendida(loteId, empresaId));
        long disponible = comercializables - yaVendidos;
        if (solicitud.getCantidad() > disponible) {
            throw new ResourceConflictException(
                    "No hay huevos suficientes para vender. Disponibles: " + Math.max(disponible, 0)
                            + " (producidos sin rotos, menos lo ya vendido).");
        }

        BigDecimal total = solicitud.getTotal();
        if (total == null && solicitud.getPrecioUnitario() != null) {
            total = solicitud.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(solicitud.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        AvicolaHuevoVenta venta = new AvicolaHuevoVenta();
        venta.setLote(lote);
        venta.setEmpresaId(empresaId);
        venta.setFecha(solicitud.getFecha());
        venta.setCantidad(solicitud.getCantidad());
        venta.setPrecioUnitario(solicitud.getPrecioUnitario());
        venta.setTotal(total);
        venta.setComprador(solicitud.getComprador());
        venta.setObservaciones(solicitud.getObservaciones());
        venta.setCampanaId(lote.getCampanaId());
        venta = ventaRepository.save(venta);

        Long ingresoId = libroEmpresa.asegurarIngresoDeVenta(
                venta.getIngresoId(),
                ModuloSistema.AVICOLA_HUEVOS,
                Ingreso.TipoIngreso.VENTA_HUEVOS,
                venta.getTotal(),
                venta.getFecha(),
                "Venta de huevos lote " + lote.getNombre(),
                venta.getComprador(),
                venta.getCampanaId(),
                ServicioLibroEmpresa.REF_VENTA_HUEVOS,
                venta.getId());
        if (ingresoId != null && venta.getIngresoId() == null) {
            venta.setIngresoId(ingresoId);
            venta = ventaRepository.save(venta);
        }
        return aVentaRespuesta(venta);
    }

    private static long nzLong(Long valor) {
        return valor != null ? valor : 0L;
    }

    private AvicolaHuevoVentaRespuesta aVentaRespuesta(AvicolaHuevoVenta venta) {
        AvicolaHuevoVentaRespuesta dto = new AvicolaHuevoVentaRespuesta();
        dto.setId(venta.getId());
        dto.setLoteId(venta.getLote() != null ? venta.getLote().getId() : null);
        dto.setEmpresaId(venta.getEmpresaId());
        dto.setFecha(venta.getFecha());
        dto.setCantidad(venta.getCantidad());
        dto.setPrecioUnitario(venta.getPrecioUnitario());
        dto.setTotal(venta.getTotal());
        dto.setComprador(venta.getComprador());
        dto.setObservaciones(venta.getObservaciones());
        dto.setCampanaId(venta.getCampanaId());
        dto.setIngresoId(venta.getIngresoId());
        dto.setCreatedAt(venta.getCreatedAt());
        return dto;
    }

    private AvicolaHuevoEventoSanitarioRespuesta aEventoRespuesta(AvicolaHuevoEventoSanitario e) {
        AvicolaHuevoEventoSanitarioRespuesta dto = new AvicolaHuevoEventoSanitarioRespuesta();
        dto.setId(e.getId());
        dto.setLoteId(e.getLote() != null ? e.getLote().getId() : null);
        dto.setEmpresaId(e.getEmpresaId());
        dto.setFecha(e.getFecha());
        dto.setTipo(e.getTipo());
        dto.setDescripcion(e.getDescripcion());
        dto.setInsumoId(e.getInsumoId());
        dto.setDosis(e.getDosis());
        dto.setObservaciones(e.getObservaciones());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getInsumoId() != null) {
            insumoRepository.findById(e.getInsumoId()).map(Insumo::getNombre).ifPresent(dto::setInsumoNombre);
        }
        return dto;
    }
}
