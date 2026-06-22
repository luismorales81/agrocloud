package com.agrocloud.avicola.ponedoras.service;

import com.agrocloud.avicola.ponedoras.model.dto.*;
import com.agrocloud.avicola.ponedoras.model.entity.*;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasConsumoTipo;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasGalponEstado;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasHuevoCategoria;
import com.agrocloud.avicola.ponedoras.repository.*;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Operaciones de negocio del módulo ponedoras (postura, muerte, descarte, consumo con inventario CORE, sanidad, ventas de huevos).
 */
@Service
public class ServicioAvicolaPonedorasOperaciones {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final AvicolaPonedorasGalponRepository galponRepository;
    private final AvicolaPonedorasPosturaRepository posturaRepository;
    private final AvicolaPonedorasMuerteRepository muerteRepository;
    private final AvicolaPonedorasConsumoRepository consumoRepository;
    private final AvicolaPonedorasDescarteAvesRepository descarteAvesRepository;
    private final AvicolaPonedorasEventoSanitarioRepository eventoSanitarioRepository;
    private final AvicolaPonedorasVentaHuevosRepository ventaHuevosRepository;
    private final InventoryService inventoryService;

    public ServicioAvicolaPonedorasOperaciones(
            ServicioSeguridadContexto servicioSeguridadContexto,
            AvicolaPonedorasGalponRepository galponRepository,
            AvicolaPonedorasPosturaRepository posturaRepository,
            AvicolaPonedorasMuerteRepository muerteRepository,
            AvicolaPonedorasConsumoRepository consumoRepository,
            AvicolaPonedorasDescarteAvesRepository descarteAvesRepository,
            AvicolaPonedorasEventoSanitarioRepository eventoSanitarioRepository,
            AvicolaPonedorasVentaHuevosRepository ventaHuevosRepository,
            InventoryService inventoryService) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.galponRepository = galponRepository;
        this.posturaRepository = posturaRepository;
        this.muerteRepository = muerteRepository;
        this.consumoRepository = consumoRepository;
        this.descarteAvesRepository = descarteAvesRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.ventaHuevosRepository = ventaHuevosRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasPosturaRespuesta> listarPosturas(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirGalpon(empresaId, galponId);
        return posturaRepository.buscarPorGalponIdYEmpresaId(galponId, empresaId).stream()
                .map(ServicioAvicolaPonedorasOperaciones::aPosturaRespuesta)
                .collect(Collectors.toList());
    }

    /**
     * Registra postura de huevos; no modifica {@code cantidadAves} del galpón.
     */
    @Transactional
    public AvicolaPonedorasPosturaRespuesta registrarPostura(Long galponId, AvicolaPonedorasPosturaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalponActivo(empresaId, galponId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de postura es obligatoria");
        }
        if (solicitud.getCategoriaHuevo() == null) {
            throw new IllegalStateException("La categoría de huevo es obligatoria");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
            throw new IllegalStateException("La cantidad de huevos debe ser mayor a cero");
        }

        AvicolaPonedorasPostura p = new AvicolaPonedorasPostura();
        p.setGalpon(galpon);
        p.setEmpresaId(empresaId);
        p.setFecha(solicitud.getFecha());
        p.setCategoriaHuevo(solicitud.getCategoriaHuevo());
        p.setCantidad(solicitud.getCantidad());
        p.setObservaciones(solicitud.getObservaciones());
        p = posturaRepository.save(p);
        return aPosturaRespuesta(p);
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasMuerteRespuesta> listarMuertes(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirGalpon(empresaId, galponId);
        return muerteRepository.buscarPorGalponIdYEmpresaId(galponId, empresaId).stream()
                .map(ServicioAvicolaPonedorasOperaciones::aMuerteRespuesta)
                .collect(Collectors.toList());
    }

    /**
     * Registra mortalidad; no resta {@code cantidadAves} del galpón.
     */
    @Transactional
    public AvicolaPonedorasMuerteRespuesta registrarMuerte(Long galponId, AvicolaPonedorasMuerteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalponActivo(empresaId, galponId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la muerte es obligatoria");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
            throw new IllegalStateException("La cantidad de muertes debe ser mayor a cero");
        }

        AvicolaPonedorasMuerte m = new AvicolaPonedorasMuerte();
        m.setGalpon(galpon);
        m.setEmpresaId(empresaId);
        m.setFecha(solicitud.getFecha());
        m.setCantidad(solicitud.getCantidad());
        m.setCausa(solicitud.getCausa());
        m.setObservaciones(solicitud.getObservaciones());
        m = muerteRepository.save(m);
        return aMuerteRespuesta(m);
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasDescarteAvesRespuesta> listarDescarteAves(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirGalpon(empresaId, galponId);
        return descarteAvesRepository.buscarPorGalponIdYEmpresaId(galponId, empresaId).stream()
                .map(ServicioAvicolaPonedorasOperaciones::aDescarteRespuesta)
                .collect(Collectors.toList());
    }

    /**
     * Registra descarte de aves: descuenta {@code cantidadAves}; si llega a 0, cierra el galpón con fecha de cierre hoy.
     */
    @Transactional
    public AvicolaPonedorasDescarteAvesRespuesta registrarDescarteAves(
            Long galponId,
            AvicolaPonedorasDescarteAvesSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalponActivo(empresaId, galponId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha del descarte es obligatoria");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
            throw new IllegalStateException("La cantidad de aves descartadas debe ser mayor a cero");
        }
        if (solicitud.getMotivo() == null) {
            throw new IllegalStateException("El motivo del descarte es obligatorio");
        }

        int base = avesVivasActuales(galpon);
        if (solicitud.getCantidad() > base) {
            throw new IllegalStateException(
                    "La cantidad de descarte supera las aves disponibles en el galpón (" + base + ")");
        }

        int nuevo = base - solicitud.getCantidad();
        galpon.setCantidadAves(nuevo);
        if (nuevo <= 0) {
            galpon.setCantidadAves(0);
            galpon.setEstado(AvicolaPonedorasGalponEstado.CERRADO);
            galpon.setFechaCierre(LocalDate.now());
        }
        galponRepository.save(galpon);

        AvicolaPonedorasDescarteAves d = new AvicolaPonedorasDescarteAves();
        d.setGalpon(galpon);
        d.setEmpresaId(empresaId);
        d.setFecha(solicitud.getFecha());
        d.setCantidad(solicitud.getCantidad());
        d.setMotivo(solicitud.getMotivo());
        d.setObservaciones(solicitud.getObservaciones());
        d = descarteAvesRepository.save(d);
        return aDescarteRespuesta(d);
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasConsumoRespuesta> listarConsumos(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirGalpon(empresaId, galponId);
        return consumoRepository.buscarPorGalponIdYEmpresaId(galponId, empresaId).stream()
                .map(ServicioAvicolaPonedorasOperaciones::aConsumoRespuesta)
                .collect(Collectors.toList());
    }

    /**
     * Registra consumo y egreso de inventario CORE con origen {@link InventoryOrigin#AVICOLA_PONEDORAS}.
     */
    @Transactional
    public AvicolaPonedorasConsumoRespuesta registrarConsumo(Long galponId, AvicolaPonedorasConsumoSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalponActivo(empresaId, galponId);
        if (solicitud.getInsumoId() == null || solicitud.getFecha() == null
                || solicitud.getCantidad() == null || solicitud.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Insumo, fecha y cantidad válida son obligatorios");
        }
        AvicolaPonedorasConsumoTipo tipo = solicitud.getTipo() != null ? solicitud.getTipo() : AvicolaPonedorasConsumoTipo.MANUAL;

        AvicolaPonedorasConsumo c = new AvicolaPonedorasConsumo();
        c.setGalpon(galpon);
        c.setEmpresaId(empresaId);
        c.setInsumoId(solicitud.getInsumoId());
        c.setFecha(solicitud.getFecha());
        c.setCantidad(solicitud.getCantidad());
        c.setTipo(tipo);
        c.setObservaciones(solicitud.getObservaciones());
        c.setCampanaId(galpon.getCampanaId());
        c = consumoRepository.saveAndFlush(c);

        InventoryResult resultado = inventoryService.consumir(
                empresaId,
                solicitud.getInsumoId(),
                solicitud.getCantidad(),
                InventoryOrigin.AVICOLA_PONEDORAS,
                c.getId(),
                usuarioId);
        if (!resultado.exito()) {
            throw new IllegalStateException(
                    resultado.mensaje() != null ? resultado.mensaje() : "No se pudo registrar el egreso de inventario");
        }
        return aConsumoRespuesta(c);
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasEventoSanitarioRespuesta> listarEventosSanitarios(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirGalpon(empresaId, galponId);
        return eventoSanitarioRepository.buscarPorGalponIdYEmpresaId(galponId, empresaId).stream()
                .map(ServicioAvicolaPonedorasOperaciones::aEventoSanitarioRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaPonedorasEventoSanitarioRespuesta registrarEventoSanitario(
            Long galponId,
            AvicolaPonedorasEventoSanitarioSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalponActivo(empresaId, galponId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha del evento sanitario es obligatoria");
        }
        if (solicitud.getTipo() == null || solicitud.getTipo().isBlank()) {
            throw new IllegalStateException("El tipo de evento sanitario es obligatorio");
        }

        AvicolaPonedorasEventoSanitario e = new AvicolaPonedorasEventoSanitario();
        e.setGalpon(galpon);
        e.setEmpresaId(empresaId);
        e.setFecha(solicitud.getFecha());
        e.setTipo(solicitud.getTipo().trim());
        e.setDescripcion(solicitud.getDescripcion());
        e.setInsumoId(solicitud.getInsumoId());
        e.setDosis(solicitud.getDosis());
        e.setObservaciones(solicitud.getObservaciones());
        e = eventoSanitarioRepository.save(e);
        return aEventoSanitarioRespuesta(e);
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasVentaHuevosRespuesta> listarVentasHuevos(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirGalpon(empresaId, galponId);
        return ventaHuevosRepository.buscarPorGalponIdYEmpresaId(galponId, empresaId).stream()
                .map(ServicioAvicolaPonedorasOperaciones::aVentaHuevosRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaPonedorasVentaHuevosRespuesta registrarVentaHuevos(
            Long galponId,
            AvicolaPonedorasVentaHuevosSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalponActivo(empresaId, galponId);
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la venta es obligatoria");
        }
        if (solicitud.getCategoriaHuevo() == null) {
            throw new IllegalStateException("La categoría de huevo es obligatoria");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
            throw new IllegalStateException("La cantidad de huevos vendidos debe ser mayor a cero");
        }

        AvicolaPonedorasVentaHuevos v = new AvicolaPonedorasVentaHuevos();
        v.setGalpon(galpon);
        v.setEmpresaId(empresaId);
        v.setFecha(solicitud.getFecha());
        v.setCategoriaHuevo(solicitud.getCategoriaHuevo());
        v.setCantidad(solicitud.getCantidad());
        v.setPrecioUnitario(solicitud.getPrecioUnitario());
        v.setTotal(solicitud.getTotal());
        v.setComprador(solicitud.getComprador());
        v.setObservaciones(solicitud.getObservaciones());
        v.setCampanaId(galpon.getCampanaId());
        v = ventaHuevosRepository.save(v);
        return aVentaHuevosRespuesta(v);
    }

    @Transactional(readOnly = true)
    public AvicolaPonedorasResumenRespuesta calcularResumen(Long galponId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalpon(empresaId, galponId);

        long sumaMuertes = muerteRepository.sumarCantidadMuertesPorGalponYEmpresaId(galponId, empresaId);
        int avesRegistradas = avesVivasActuales(galpon);
        int cantidadDisponible = Math.max(0, avesRegistradas - (int) sumaMuertes);

        BigDecimal mortalidadPct = BigDecimal.ZERO;
        if (galpon.getCantidadInicial() != null && galpon.getCantidadInicial() > 0) {
            mortalidadPct = BigDecimal.valueOf(sumaMuertes * 100.0 / galpon.getCantidadInicial())
                    .setScale(4, RoundingMode.HALF_UP);
        }

        long totalHuevos = posturaRepository.sumarTotalHuevosPorGalponYEmpresaId(galponId, empresaId);

        long diasEnProduccion = ChronoUnit.DAYS.between(galpon.getFechaIngreso(), LocalDate.now());
        if (diasEnProduccion < 1) {
            diasEnProduccion = 1;
        }

        BigDecimal porcentajePostura = null;
        BigDecimal huevosPorAvePorDia = null;
        if (cantidadDisponible > 0) {
            BigDecimal denominador = BigDecimal.valueOf((long) cantidadDisponible * diasEnProduccion);
            porcentajePostura = BigDecimal.valueOf(totalHuevos)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(denominador, 4, RoundingMode.HALF_UP);
            huevosPorAvePorDia = BigDecimal.valueOf(totalHuevos)
                    .divide(BigDecimal.valueOf(cantidadDisponible), 6, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(diasEnProduccion), 6, RoundingMode.HALF_UP);
        }

        Map<String, Long> distribucion = new LinkedHashMap<>();
        for (AvicolaPonedorasHuevoCategoria cat : AvicolaPonedorasHuevoCategoria.values()) {
            distribucion.put(cat.name(), 0L);
        }
        List<Object[]> filas = posturaRepository.sumarHuevosAgrupadosPorCategoria(galponId, empresaId);
        for (Object[] fila : filas) {
            if (fila[0] instanceof AvicolaPonedorasHuevoCategoria categoria && fila[1] instanceof Number n) {
                distribucion.put(categoria.name(), n.longValue());
            }
        }

        AvicolaPonedorasResumenRespuesta r = new AvicolaPonedorasResumenRespuesta();
        r.setGalponId(galponId);
        r.setCantidadDisponible(cantidadDisponible);
        r.setMortalidadPct(mortalidadPct);
        r.setTotalHuevos(totalHuevos);
        r.setDiasEnProduccion(diasEnProduccion);
        r.setPorcentajePostura(porcentajePostura);
        r.setHuevosPorAvePorDia(huevosPorAvePorDia);
        r.setDistribucionCategorias(distribucion);
        return r;
    }

    private AvicolaPonedorasGalpon requerirGalpon(Long empresaId, Long galponId) {
        return galponRepository
                .buscarPorIdYEmpresaId(galponId, empresaId)
                .orElseThrow(() -> new IllegalStateException("Galpón no encontrado o no pertenece a la empresa"));
    }

    private AvicolaPonedorasGalpon requerirGalponActivo(Long empresaId, Long galponId) {
        AvicolaPonedorasGalpon galpon = requerirGalpon(empresaId, galponId);
        if (galpon.getEstado() == AvicolaPonedorasGalponEstado.CERRADO) {
            throw new IllegalStateException("El galpón ya está cerrado");
        }
        return galpon;
    }

    private static int avesVivasActuales(AvicolaPonedorasGalpon galpon) {
        if (galpon.getCantidadAves() != null) {
            return galpon.getCantidadAves();
        }
        return galpon.getCantidadInicial() != null ? galpon.getCantidadInicial() : 0;
    }

    private static AvicolaPonedorasPosturaRespuesta aPosturaRespuesta(AvicolaPonedorasPostura p) {
        AvicolaPonedorasPosturaRespuesta dto = new AvicolaPonedorasPosturaRespuesta();
        dto.setId(p.getId());
        dto.setGalponId(p.getGalpon() != null ? p.getGalpon().getId() : null);
        dto.setEmpresaId(p.getEmpresaId());
        dto.setFecha(p.getFecha());
        dto.setCategoriaHuevo(p.getCategoriaHuevo());
        dto.setCantidad(p.getCantidad());
        dto.setObservaciones(p.getObservaciones());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    private static AvicolaPonedorasMuerteRespuesta aMuerteRespuesta(AvicolaPonedorasMuerte m) {
        AvicolaPonedorasMuerteRespuesta dto = new AvicolaPonedorasMuerteRespuesta();
        dto.setId(m.getId());
        dto.setGalponId(m.getGalpon() != null ? m.getGalpon().getId() : null);
        dto.setEmpresaId(m.getEmpresaId());
        dto.setFecha(m.getFecha());
        dto.setCantidad(m.getCantidad());
        dto.setCausa(m.getCausa());
        dto.setObservaciones(m.getObservaciones());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    private static AvicolaPonedorasDescarteAvesRespuesta aDescarteRespuesta(AvicolaPonedorasDescarteAves d) {
        AvicolaPonedorasDescarteAvesRespuesta dto = new AvicolaPonedorasDescarteAvesRespuesta();
        dto.setId(d.getId());
        dto.setGalponId(d.getGalpon() != null ? d.getGalpon().getId() : null);
        dto.setEmpresaId(d.getEmpresaId());
        dto.setFecha(d.getFecha());
        dto.setCantidad(d.getCantidad());
        dto.setMotivo(d.getMotivo());
        dto.setObservaciones(d.getObservaciones());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }

    private static AvicolaPonedorasConsumoRespuesta aConsumoRespuesta(AvicolaPonedorasConsumo c) {
        AvicolaPonedorasConsumoRespuesta dto = new AvicolaPonedorasConsumoRespuesta();
        dto.setId(c.getId());
        dto.setGalponId(c.getGalpon() != null ? c.getGalpon().getId() : null);
        dto.setEmpresaId(c.getEmpresaId());
        dto.setInsumoId(c.getInsumoId());
        dto.setFecha(c.getFecha());
        dto.setCantidad(c.getCantidad());
        dto.setTipo(c.getTipo());
        dto.setObservaciones(c.getObservaciones());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    private static AvicolaPonedorasEventoSanitarioRespuesta aEventoSanitarioRespuesta(AvicolaPonedorasEventoSanitario e) {
        AvicolaPonedorasEventoSanitarioRespuesta dto = new AvicolaPonedorasEventoSanitarioRespuesta();
        dto.setId(e.getId());
        dto.setGalponId(e.getGalpon() != null ? e.getGalpon().getId() : null);
        dto.setEmpresaId(e.getEmpresaId());
        dto.setFecha(e.getFecha());
        dto.setTipo(e.getTipo());
        dto.setDescripcion(e.getDescripcion());
        dto.setInsumoId(e.getInsumoId());
        dto.setDosis(e.getDosis());
        dto.setObservaciones(e.getObservaciones());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private static AvicolaPonedorasVentaHuevosRespuesta aVentaHuevosRespuesta(AvicolaPonedorasVentaHuevos v) {
        AvicolaPonedorasVentaHuevosRespuesta dto = new AvicolaPonedorasVentaHuevosRespuesta();
        dto.setId(v.getId());
        dto.setGalponId(v.getGalpon() != null ? v.getGalpon().getId() : null);
        dto.setEmpresaId(v.getEmpresaId());
        dto.setFecha(v.getFecha());
        dto.setCategoriaHuevo(v.getCategoriaHuevo());
        dto.setCantidad(v.getCantidad());
        dto.setPrecioUnitario(v.getPrecioUnitario());
        dto.setTotal(v.getTotal());
        dto.setComprador(v.getComprador());
        dto.setObservaciones(v.getObservaciones());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }
}
