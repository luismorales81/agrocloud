package com.agrocloud.avicola.crianza.application;

import com.agrocloud.avicola.crianza.model.dto.*;
import com.agrocloud.avicola.crianza.model.entity.*;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.avicola.crianza.repository.*;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaCrianzaOperaciones {

    private final ServicioAvicolaCrianzaLote servicioLote;
    private final AvicolaPesadaRepository pesadaRepository;
    private final AvicolaMuerteRepository muerteRepository;
    private final AvicolaVentaRepository ventaRepository;
    private final AvicolaConsumoRepository consumoRepository;
    private final AvicolaEventoSanitarioRepository eventoSanitarioRepository;
    private final InventoryService inventoryService;

    public ServicioAvicolaCrianzaOperaciones(
            ServicioAvicolaCrianzaLote servicioLote,
            AvicolaPesadaRepository pesadaRepository,
            AvicolaMuerteRepository muerteRepository,
            AvicolaVentaRepository ventaRepository,
            AvicolaConsumoRepository consumoRepository,
            AvicolaEventoSanitarioRepository eventoSanitarioRepository,
            InventoryService inventoryService) {
        this.servicioLote = servicioLote;
        this.pesadaRepository = pesadaRepository;
        this.muerteRepository = muerteRepository;
        this.ventaRepository = ventaRepository;
        this.consumoRepository = consumoRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.inventoryService = inventoryService;
    }

    private AvicolaLote loteActivo(Long empresaId, Long loteId) {
        AvicolaLote l = servicioLote.obtenerEntidadLote(empresaId, loteId);
        if (l.getEstado() == AvicolaLoteEstado.CERRADO) {
            throw new IllegalArgumentException("El lote está cerrado");
        }
        return l;
    }

    @Transactional(readOnly = true)
    public List<AvicolaPesadaRespuesta> listarPesadas(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return pesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aPesadaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaPesadaRespuesta registrarPesada(Long empresaId, Long loteId, AvicolaPesadaSolicitud solicitud) {
        AvicolaLote l = loteActivo(empresaId, loteId);
        AvicolaPesada p = new AvicolaPesada();
        p.setLote(l);
        p.setEmpresaId(empresaId);
        p.setFecha(solicitud.getFecha());
        p.setPesoPromedio(solicitud.getPesoPromedio());
        p.setCantidadPesada(solicitud.getCantidadPesada());
        p.setObservaciones(solicitud.getObservaciones());
        p.setTemperaturaAmbiente(solicitud.getTemperaturaAmbiente());
        p.setHumedadAmbiente(solicitud.getHumedadAmbiente());
        return aPesadaRespuesta(pesadaRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<AvicolaMuerteRespuesta> listarMuertes(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return muerteRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aMuerteRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaMuerteRespuesta registrarMuerte(Long empresaId, Long loteId, AvicolaMuerteSolicitud solicitud) {
        AvicolaLote l = loteActivo(empresaId, loteId);
        int q = solicitud.getCantidad() != null ? solicitud.getCantidad() : 0;
        if (q <= 0) {
            throw new IllegalArgumentException("La cantidad de muertes debe ser mayor a cero");
        }
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de la muerte es obligatoria");
        }

        AvicolaMuerte m = new AvicolaMuerte();
        m.setLote(l);
        m.setEmpresaId(empresaId);
        m.setFecha(solicitud.getFecha());
        m.setCantidad(q);
        m.setCausa(solicitud.getCausa());
        m.setObservaciones(solicitud.getObservaciones());
        return aMuerteRespuesta(muerteRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<AvicolaVentaRespuesta> listarVentas(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return ventaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aVentaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaVentaRespuesta registrarVenta(Long empresaId, Long loteId, AvicolaVentaSolicitud solicitud) {
        AvicolaLote l = loteActivo(empresaId, loteId);
        if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad vendida debe ser mayor a cero");
        }
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de la venta es obligatoria");
        }
        if (solicitud.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de venta es obligatorio");
        }
        int disponible = l.getCantidadAnimales() != null ? l.getCantidadAnimales() : 0;
        if (solicitud.getCantidad() > disponible) {
            throw new IllegalArgumentException("Cantidad de venta supera la disponible (" + disponible + ")");
        }
        int nuevo = disponible - solicitud.getCantidad();
        l.setCantidadAnimales(nuevo);
        if (nuevo <= 0) {
            l.setCantidadAnimales(0);
            l.setEstado(AvicolaLoteEstado.CERRADO);
            l.setFechaSalida(LocalDate.now());
        }
        servicioLote.guardarLote(l);

        AvicolaVenta v = new AvicolaVenta();
        v.setLote(l);
        v.setEmpresaId(empresaId);
        v.setFecha(solicitud.getFecha());
        v.setTipo(solicitud.getTipo());
        v.setCantidad(solicitud.getCantidad());
        v.setPesoPromedio(solicitud.getPesoPromedio());
        v.setPrecioUnitario(solicitud.getPrecioUnitario());
        v.setTotal(solicitud.getTotal());
        v.setComprador(solicitud.getComprador());
        v.setObservaciones(solicitud.getObservaciones());
        v.setCampanaId(l.getCampanaId());
        return aVentaRespuesta(ventaRepository.save(v));
    }

    @Transactional(readOnly = true)
    public List<AvicolaConsumoRespuesta> listarConsumos(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return consumoRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aConsumoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaConsumoRespuesta registrarConsumo(Long empresaId, Long loteId, AvicolaConsumoSolicitud solicitud, Long usuarioId) {
        AvicolaLote l = loteActivo(empresaId, loteId);
        if (solicitud.getInsumoId() == null || solicitud.getFecha() == null
                || solicitud.getCantidad() == null || solicitud.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Insumo, fecha y cantidad válida son obligatorios");
        }

        AvicolaConsumo c = new AvicolaConsumo();
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
                InventoryOrigin.AVICOLA_CRIANZA,
                c.getId(),
                usuarioId
        );
        if (!res.exito()) {
            throw new IllegalArgumentException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
        }
        return aConsumoRespuesta(c);
    }

    @Transactional
    public AvicolaConsumoRespuesta actualizarConsumo(
            Long empresaId,
            Long loteId,
            Long consumoId,
            AvicolaCrianzaConsumoActualizarSolicitud solicitud,
            Long usuarioId) {
        loteActivo(empresaId, loteId);
        AvicolaConsumo c = consumoRepository.buscarPorIdYEmpresaId(consumoId, empresaId)
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
                    empresaId, insumoId, delta, InventoryOrigin.AVICOLA_CRIANZA, c.getId(), usuarioId);
            if (!res.exito()) {
                throw new IllegalArgumentException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
        } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
            InventoryResult res = inventoryService.reponer(
                    empresaId, insumoId, delta.negate(), InventoryOrigin.AVICOLA_CRIANZA, c.getId(), usuarioId);
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
    public List<AvicolaEventoSanitarioRespuesta> listarEventosSanitarios(Long empresaId, Long loteId) {
        servicioLote.obtenerEntidadLote(empresaId, loteId);
        return eventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(this::aEventoRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaEventoSanitarioRespuesta registrarEventoSanitario(Long empresaId, Long loteId, AvicolaEventoSanitarioSolicitud solicitud) {
        AvicolaLote l = loteActivo(empresaId, loteId);
        AvicolaEventoSanitario e = new AvicolaEventoSanitario();
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
    public AvicolaCrianzaResumenRespuesta resumenLote(Long empresaId, Long loteId) {
        AvicolaLote l = servicioLote.obtenerEntidadLote(empresaId, loteId);
        long sumaMuertes = muerteRepository.sumarCantidadMuertesPorLoteYEmpresa(loteId, empresaId);
        BigDecimal sumaConsumos = consumoRepository.sumarCantidadConsumidaPorLoteYEmpresa(loteId, empresaId);
        if (sumaConsumos == null) {
            sumaConsumos = BigDecimal.ZERO;
        }
        int cantidadAnimales = l.getCantidadAnimales() != null ? l.getCantidadAnimales() : 0;
        int cantidadDisponible = cantidadAnimales - (int) sumaMuertes;

        AvicolaCrianzaResumenRespuesta r = new AvicolaCrianzaResumenRespuesta();
        r.setLoteId(loteId);
        r.setCantidadAnimalesRegistrada(cantidadAnimales);
        r.setSumaMuertes(sumaMuertes);
        r.setCantidadDisponible(cantidadDisponible);
        if (l.getCantidadInicial() != null && l.getCantidadInicial() > 0) {
            r.setMortalidadPorcentaje(
                    BigDecimal.valueOf(sumaMuertes * 100.0 / l.getCantidadInicial()).setScale(4, RoundingMode.HALF_UP));
        } else {
            r.setMortalidadPorcentaje(BigDecimal.ZERO);
        }
        r.setSumaConsumos(sumaConsumos);
        BigDecimal pesoRef = l.getPesoPromedioIngreso();
        List<AvicolaPesada> pesadas = pesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId);
        if (!pesadas.isEmpty() && pesadas.get(0).getPesoPromedio() != null) {
            pesoRef = pesadas.get(0).getPesoPromedio();
        }
        r.setPesoPromedioReferencia(pesoRef);
        if (pesoRef != null && pesoRef.compareTo(BigDecimal.ZERO) > 0 && cantidadDisponible > 0) {
            BigDecimal denom = pesoRef.multiply(BigDecimal.valueOf(cantidadDisponible));
            r.setConversionAlimenticia(sumaConsumos.divide(denom, 4, RoundingMode.HALF_UP));
        }
        r.setDiasEnProduccion(ChronoUnit.DAYS.between(l.getFechaIngreso(), LocalDate.now()));
        return r;
    }

    private AvicolaPesadaRespuesta aPesadaRespuesta(AvicolaPesada p) {
        AvicolaPesadaRespuesta dto = new AvicolaPesadaRespuesta();
        dto.setId(p.getId());
        dto.setLoteId(p.getLote() != null ? p.getLote().getId() : null);
        dto.setEmpresaId(p.getEmpresaId());
        dto.setFecha(p.getFecha());
        dto.setPesoPromedio(p.getPesoPromedio());
        dto.setCantidadPesada(p.getCantidadPesada());
        dto.setObservaciones(p.getObservaciones());
        dto.setTemperaturaAmbiente(p.getTemperaturaAmbiente());
        dto.setHumedadAmbiente(p.getHumedadAmbiente());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
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

    private AvicolaEventoSanitarioRespuesta aEventoRespuesta(AvicolaEventoSanitario e) {
        AvicolaEventoSanitarioRespuesta dto = new AvicolaEventoSanitarioRespuesta();
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
        return dto;
    }
}
