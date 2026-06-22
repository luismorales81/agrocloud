package com.agrocloud.avicola.carne.service;

import com.agrocloud.avicola.carne.model.dto.AvicolaCarneResumenRespuesta;
import com.agrocloud.avicola.carne.repository.AvicolaCarneConsumoRepository;
import com.agrocloud.avicola.carne.repository.AvicolaCarneMuerteRepository;
import com.agrocloud.avicola.carne.repository.AvicolaCarnePesadaRepository;
import com.agrocloud.avicola.carne.repository.AvicolaCarneVentaRepository;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaLote;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaOperaciones;
import com.agrocloud.avicola.crianza.model.dto.AvicolaConsumoRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaConsumoSolicitud;
import com.agrocloud.avicola.crianza.model.dto.AvicolaEventoSanitarioRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaEventoSanitarioSolicitud;
import com.agrocloud.avicola.crianza.model.dto.AvicolaMuerteRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaMuerteSolicitud;
import com.agrocloud.avicola.crianza.model.dto.AvicolaPesadaRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaPesadaSolicitud;
import com.agrocloud.avicola.crianza.model.dto.AvicolaVentaRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaVentaSolicitud;
import com.agrocloud.avicola.crianza.model.entity.AvicolaConsumo;
import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.entity.AvicolaMuerte;
import com.agrocloud.avicola.crianza.model.entity.AvicolaPesada;
import com.agrocloud.avicola.crianza.model.entity.AvicolaVenta;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Operaciones de negocio avícola-carne (mortalidad sin descuento de stock de aves, ventas con cierre de lote, consumo con inventario CORE).
 */
@Service
public class ServicioAvicolaCarneOperaciones {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioAvicolaCrianzaLote servicioLote;
    private final AvicolaCarneMuerteRepository muerteRepository;
    private final AvicolaCarneVentaRepository ventaRepository;
    private final AvicolaCarneConsumoRepository consumoRepository;
    private final AvicolaCarnePesadaRepository pesadaRepository;
    private final InventoryService inventoryService;
    private final ServicioAvicolaCrianzaOperaciones servicioCrianzaOperaciones;

    public ServicioAvicolaCarneOperaciones(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioAvicolaCrianzaLote servicioLote,
            AvicolaCarneMuerteRepository muerteRepository,
            AvicolaCarneVentaRepository ventaRepository,
            AvicolaCarneConsumoRepository consumoRepository,
            AvicolaCarnePesadaRepository pesadaRepository,
            InventoryService inventoryService,
            ServicioAvicolaCrianzaOperaciones servicioCrianzaOperaciones) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLote = servicioLote;
        this.muerteRepository = muerteRepository;
        this.ventaRepository = ventaRepository;
        this.consumoRepository = consumoRepository;
        this.pesadaRepository = pesadaRepository;
        this.inventoryService = inventoryService;
        this.servicioCrianzaOperaciones = servicioCrianzaOperaciones;
    }

    @Transactional(readOnly = true)
    public List<AvicolaPesadaRespuesta> listarPesadas(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioCrianzaOperaciones.listarPesadas(empresaId, loteId);
    }

    @Transactional
    public AvicolaPesadaRespuesta registrarPesada(Long loteId, AvicolaPesadaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioCrianzaOperaciones.registrarPesada(empresaId, loteId, solicitud);
    }

    @Transactional(readOnly = true)
    public List<AvicolaMuerteRespuesta> listarMuertes(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return muerteRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aMuerteRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvicolaVentaRespuesta> listarVentas(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioCrianzaOperaciones.listarVentas(empresaId, loteId);
    }

    @Transactional(readOnly = true)
    public List<AvicolaConsumoRespuesta> listarConsumos(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioCrianzaOperaciones.listarConsumos(empresaId, loteId);
    }

    @Transactional(readOnly = true)
    public List<AvicolaEventoSanitarioRespuesta> listarEventosSanitarios(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioCrianzaOperaciones.listarEventosSanitarios(empresaId, loteId);
    }

    @Transactional
    public AvicolaEventoSanitarioRespuesta registrarEventoSanitario(Long loteId, AvicolaEventoSanitarioSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return servicioCrianzaOperaciones.registrarEventoSanitario(empresaId, loteId, solicitud);
    }

    /**
     * Registra mortalidad: persiste en {@code avicola_muerte} y no modifica {@code cantidadAnimales} del lote.
     */
    @Transactional
    public AvicolaMuerteRespuesta registrarMuerte(Long loteId, AvicolaMuerteSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaLote lote = servicioLote.obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == AvicolaLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        int cantidad = solicitud.getCantidad() != null ? solicitud.getCantidad() : 0;
        if (cantidad <= 0) {
            throw new IllegalStateException("La cantidad de muertes debe ser mayor a cero");
        }
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la muerte es obligatoria");
        }

        AvicolaMuerte m = new AvicolaMuerte();
        m.setLote(lote);
        m.setEmpresaId(empresaId);
        m.setFecha(solicitud.getFecha());
        m.setCantidad(cantidad);
        m.setCausa(solicitud.getCausa());
        m.setObservaciones(solicitud.getObservaciones());
        return aMuerteRespuesta(muerteRepository.save(m));
    }

    /**
     * Registra venta o faena: descuenta {@code cantidadAnimales}; si llega a 0, cierra el lote con fecha de salida hoy.
     */
    @Transactional
    public AvicolaVentaRespuesta registrarVenta(Long loteId, AvicolaVentaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaLote lote = servicioLote.obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == AvicolaLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
            throw new IllegalStateException("La cantidad vendida debe ser mayor a cero");
        }
        if (solicitud.getFecha() == null) {
            throw new IllegalStateException("La fecha de la venta es obligatoria");
        }
        if (solicitud.getTipo() == null) {
            throw new IllegalStateException("El tipo de venta es obligatorio");
        }
        int disponible = lote.getCantidadAnimales() != null ? lote.getCantidadAnimales() : 0;
        if (solicitud.getCantidad() > disponible) {
            throw new IllegalStateException("La cantidad supera las aves disponibles en el lote (" + disponible + ")");
        }

        int nuevo = disponible - solicitud.getCantidad();
        lote.setCantidadAnimales(nuevo);
        if (nuevo <= 0) {
            lote.setCantidadAnimales(0);
            lote.setEstado(AvicolaLoteEstado.CERRADO);
            lote.setFechaSalida(LocalDate.now());
        }
        servicioLote.guardarLote(lote);

        AvicolaVenta v = new AvicolaVenta();
        v.setLote(lote);
        v.setEmpresaId(empresaId);
        v.setFecha(solicitud.getFecha());
        v.setTipo(solicitud.getTipo());
        v.setCantidad(solicitud.getCantidad());
        v.setPesoPromedio(solicitud.getPesoPromedio());
        v.setPrecioUnitario(solicitud.getPrecioUnitario());
        v.setTotal(solicitud.getTotal());
        v.setComprador(solicitud.getComprador());
        v.setObservaciones(solicitud.getObservaciones());
        v.setCampanaId(lote.getCampanaId());
        return aVentaRespuesta(ventaRepository.save(v));
    }

    /**
     * Registra consumo en {@code avicola_consumo} y egreso de inventario CORE ({@link InventoryOrigin#AVICOLA_CARNE}).
     */
    @Transactional
    public AvicolaConsumoRespuesta registrarConsumo(Long loteId, AvicolaConsumoSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        AvicolaLote lote = servicioLote.obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == AvicolaLoteEstado.CERRADO) {
            throw new IllegalStateException("El lote ya está cerrado");
        }
        if (solicitud.getInsumoId() == null || solicitud.getFecha() == null
                || solicitud.getCantidad() == null || solicitud.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Insumo, fecha y cantidad válida son obligatorios");
        }

        AvicolaConsumo c = new AvicolaConsumo();
        c.setLote(lote);
        c.setEmpresaId(empresaId);
        c.setInsumoId(solicitud.getInsumoId());
        c.setFecha(solicitud.getFecha());
        c.setCantidad(solicitud.getCantidad());
        c.setTipo(solicitud.getTipo() != null ? solicitud.getTipo() : "MANUAL");
        c.setObservaciones(solicitud.getObservaciones());
        c.setCampanaId(lote.getCampanaId());
        c = consumoRepository.saveAndFlush(c);

        InventoryResult resultado = inventoryService.consumir(
                empresaId,
                solicitud.getInsumoId(),
                solicitud.getCantidad(),
                InventoryOrigin.AVICOLA_CARNE,
                c.getId(),
                usuarioId
        );
        if (!resultado.exito()) {
            throw new IllegalStateException(
                    resultado.mensaje() != null ? resultado.mensaje() : "No se pudo registrar el egreso de inventario");
        }
        return aConsumoRespuesta(c);
    }

    /**
     * Resumen de KPIs según reglas del módulo carne (disponible, mortalidad %, conversión, días).
     */
    @Transactional(readOnly = true)
    public AvicolaCarneResumenRespuesta calcularResumen(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaLote lote = servicioLote.obtenerEntidadLote(empresaId, loteId);

        BigDecimal sumaMuertesBd = muerteRepository.sumarCantidadMuertesTotalPorLoteIdYEmpresaId(loteId, empresaId);
        int sumaMuertes = sumaMuertesBd != null ? sumaMuertesBd.intValue() : 0;
        int cantidadAnimales = lote.getCantidadAnimales() != null ? lote.getCantidadAnimales() : 0;
        int cantidadDisponible = cantidadAnimales - sumaMuertes;

        BigDecimal mortalidadPct = BigDecimal.ZERO;
        Integer cantidadInicial = lote.getCantidadInicial();
        if (cantidadInicial != null && cantidadInicial > 0) {
            mortalidadPct = BigDecimal.valueOf(sumaMuertes * 100.0 / cantidadInicial)
                    .setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal totalConsumoKg = consumoRepository.sumarCantidadConsumidaTotalPorLoteIdYEmpresaId(loteId, empresaId);
        if (totalConsumoKg == null) {
            totalConsumoKg = BigDecimal.ZERO;
        }

        BigDecimal pesoPromedioActual = lote.getPesoPromedioIngreso();
        List<AvicolaPesada> pesadas = pesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId);
        if (!pesadas.isEmpty() && pesadas.get(0).getPesoPromedio() != null) {
            pesoPromedioActual = pesadas.get(0).getPesoPromedio();
        }

        BigDecimal conversionAlimenticia = null;
        if (pesoPromedioActual != null
                && pesoPromedioActual.compareTo(BigDecimal.ZERO) > 0
                && cantidadDisponible > 0) {
            BigDecimal denominador = pesoPromedioActual.multiply(BigDecimal.valueOf(cantidadDisponible));
            conversionAlimenticia = totalConsumoKg.divide(denominador, 4, RoundingMode.HALF_UP);
        }

        long diasEnProduccion = ChronoUnit.DAYS.between(lote.getFechaIngreso(), LocalDate.now());

        AvicolaCarneResumenRespuesta r = new AvicolaCarneResumenRespuesta();
        r.setLoteId(loteId);
        r.setCantidadDisponible(cantidadDisponible);
        r.setMortalidadPct(mortalidadPct);
        r.setConversionAlimenticia(conversionAlimenticia);
        r.setDiasEnProduccion(diasEnProduccion);
        return r;
    }

    private AvicolaMuerteRespuesta aMuerteRespuesta(AvicolaMuerte m) {
        AvicolaMuerteRespuesta dto = new AvicolaMuerteRespuesta();
        dto.setId(m.getId());
        dto.setLoteId(m.getLote() != null ? m.getLote().getId() : null);
        dto.setEmpresaId(m.getEmpresaId());
        dto.setFecha(m.getFecha());
        dto.setCantidad(m.getCantidad());
        dto.setCausa(m.getCausa());
        dto.setObservaciones(m.getObservaciones());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    private AvicolaVentaRespuesta aVentaRespuesta(AvicolaVenta v) {
        AvicolaVentaRespuesta dto = new AvicolaVentaRespuesta();
        dto.setId(v.getId());
        dto.setLoteId(v.getLote() != null ? v.getLote().getId() : null);
        dto.setEmpresaId(v.getEmpresaId());
        dto.setFecha(v.getFecha());
        dto.setTipo(v.getTipo());
        dto.setCantidad(v.getCantidad());
        dto.setPesoPromedio(v.getPesoPromedio());
        dto.setPrecioUnitario(v.getPrecioUnitario());
        dto.setTotal(v.getTotal());
        dto.setComprador(v.getComprador());
        dto.setObservaciones(v.getObservaciones());
        dto.setIngresoId(v.getIngresoId());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }

    private AvicolaConsumoRespuesta aConsumoRespuesta(AvicolaConsumo c) {
        AvicolaConsumoRespuesta dto = new AvicolaConsumoRespuesta();
        dto.setId(c.getId());
        dto.setLoteId(c.getLote() != null ? c.getLote().getId() : null);
        dto.setEmpresaId(c.getEmpresaId());
        dto.setInsumoId(c.getInsumoId());
        dto.setFecha(c.getFecha());
        dto.setCantidad(c.getCantidad());
        dto.setTipo(c.getTipo());
        dto.setObservaciones(c.getObservaciones());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
