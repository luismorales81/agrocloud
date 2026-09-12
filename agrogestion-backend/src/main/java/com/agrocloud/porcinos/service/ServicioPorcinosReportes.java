package com.agrocloud.porcinos.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.porcinos.model.entity.PorcinosLote;
import com.agrocloud.porcinos.model.enums.PorcinosGestacionEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.model.enums.PorcinosMadreEstado;
import com.agrocloud.porcinos.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServicioPorcinosReportes {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final PorcinosLoteRepository loteRepository;
    private final PorcinosMuerteRepository muerteRepository;
    private final PorcinosConsumoRepository consumoRepository;
    private final PorcinosVentaRepository ventaRepository;
    private final PorcinosMadreRepository madreRepository;
    private final PorcinosGestacionRepository gestacionRepository;
    private final PorcinosPartoRepository partoRepository;
    private final PorcinosDesteteRepository desteteRepository;

    public ServicioPorcinosReportes(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            PorcinosLoteRepository loteRepository,
            PorcinosMuerteRepository muerteRepository,
            PorcinosConsumoRepository consumoRepository,
            PorcinosVentaRepository ventaRepository,
            PorcinosMadreRepository madreRepository,
            PorcinosGestacionRepository gestacionRepository,
            PorcinosPartoRepository partoRepository,
            PorcinosDesteteRepository desteteRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.loteRepository = loteRepository;
        this.muerteRepository = muerteRepository;
        this.consumoRepository = consumoRepository;
        this.ventaRepository = ventaRepository;
        this.madreRepository = madreRepository;
        this.gestacionRepository = gestacionRepository;
        this.partoRepository = partoRepository;
        this.desteteRepository = desteteRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerReporte(String tipo) {
        return switch (tipo != null ? tipo.toLowerCase() : "") {
            case "reproductivo" -> reporteReproductivo();
            case "alimentacion" -> reporteAlimentacion();
            case "economico" -> reporteEconomico();
            default -> reporteProductivo();
        };
    }

    private Map<String, Object> reporteProductivo() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);

        List<PorcinosLote> lotes = loteRepository.listarPorEmpresaIdYCampanaId(empresaId, campanaId);
        List<Map<String, Object>> filas = new ArrayList<>();
        int totalCabezas = 0;
        int totalMuertes = 0;
        BigDecimal consumoTotal = BigDecimal.ZERO;

        for (PorcinosLote lote : lotes) {
            Integer muertes = muerteRepository.sumarCabezasPorLoteId(lote.getId(), empresaId);
            int muertesVal = muertes != null ? muertes : 0;
            totalMuertes += muertesVal;
            int cabezas = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
            totalCabezas += cabezas;

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("lote", lote.getNombre());
            fila.put("etapa", lote.getEtapa() != null ? lote.getEtapa().name() : "—");
            fila.put("estado", lote.getEstado() != null ? lote.getEstado().name() : "—");
            fila.put("cabezas", cabezas);
            fila.put("muertes", muertesVal);
            filas.add(fila);
        }

        BigDecimal consumo = consumoRepository.sumarCantidadKgPorEmpresaIdYCampanaId(empresaId, campanaId);
        if (consumo != null) {
            consumoTotal = consumo;
        }

        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("Lotes en período", lotes.size());
        metricas.put("Cabezas actuales", totalCabezas);
        metricas.put("Muertes registradas", totalMuertes);
        metricas.put("Consumo total (kg)", consumoTotal.setScale(2, RoundingMode.HALF_UP));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("tipo", "productivo");
        resultado.put("titulo", "Reporte productivo — período activo");
        resultado.put("metricas", metricas);
        resultado.put("filas", filas);
        return resultado;
    }

    private Map<String, Object> reporteReproductivo() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();

        long madresActivas = madreRepository.listarPorEmpresaId(empresaId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo())).count();
        long gestaciones = gestacionRepository.listarPorEmpresaId(empresaId).stream()
                .filter(g -> g.getEstado() == PorcinosGestacionEstado.EN_CURSO && Boolean.TRUE.equals(g.getActivo()))
                .count();
        long lactancia = madreRepository.listarPorEmpresaIdYEstado(empresaId, PorcinosMadreEstado.LACTANCIA).size();
        long partos = partoRepository.listarPorEmpresaId(empresaId).size();
        long destetes = desteteRepository.count();

        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("Madres activas", madresActivas);
        metricas.put("Gestaciones en curso", gestaciones);
        metricas.put("Madres en lactancia", lactancia);
        metricas.put("Partos registrados", partos);
        metricas.put("Destetes registrados", destetes);

        List<Map<String, Object>> filas = madreRepository.listarPorEmpresaId(empresaId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .map(m -> {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("caravana", m.getCaravana());
                    fila.put("estado", m.getEstado() != null ? m.getEstado().name() : "—");
                    fila.put("raza", m.getRaza() != null ? m.getRaza().getNombre() : "—");
                    return fila;
                })
                .toList();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("tipo", "reproductivo");
        resultado.put("titulo", "Reporte reproductivo");
        resultado.put("metricas", metricas);
        resultado.put("filas", filas);
        return resultado;
    }

    private Map<String, Object> reporteAlimentacion() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);

        BigDecimal consumoTotal = consumoRepository.sumarCantidadKgPorEmpresaIdYCampanaId(empresaId, campanaId);
        if (consumoTotal == null) {
            consumoTotal = BigDecimal.ZERO;
        }

        List<Map<String, Object>> filas = new ArrayList<>();
        for (PorcinosLote lote : loteRepository.listarPorEmpresaIdYCampanaId(empresaId, campanaId)) {
            if (lote.getEstado() != PorcinosLoteEstado.ACTIVO) {
                continue;
            }
            List<com.agrocloud.porcinos.model.entity.PorcinosConsumo> consumos =
                    consumoRepository.listarPorLoteIdYEmpresaId(lote.getId(), empresaId);
            BigDecimal kgLote = consumos.stream()
                    .map(com.agrocloud.porcinos.model.entity.PorcinosConsumo::getCantidadKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("lote", lote.getNombre());
            fila.put("cabezas", lote.getCabezasActuales());
            fila.put("consumoKg", kgLote.setScale(2, RoundingMode.HALF_UP));
            filas.add(fila);
        }

        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("Consumo total período (kg)", consumoTotal.setScale(2, RoundingMode.HALF_UP));
        metricas.put("Lotes con consumo", filas.size());

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("tipo", "alimentacion");
        resultado.put("titulo", "Reporte de alimentación");
        resultado.put("metricas", metricas);
        resultado.put("filas", filas);
        return resultado;
    }

    private Map<String, Object> reporteEconomico() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);

        var ventas = ventaRepository.listarPorEmpresaId(empresaId).stream()
                .filter(v -> campanaId.equals(v.getCampanaId()))
                .toList();

        BigDecimal ingresos = ventas.stream()
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int cabezasVendidas = ventas.stream().mapToInt(v -> v.getCabezas() != null ? v.getCabezas() : 0).sum();

        List<Map<String, Object>> filas = ventas.stream().map(v -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", v.getFecha() != null ? v.getFecha().toString() : "—");
            fila.put("tipo", v.getTipo() != null ? v.getTipo().name() : "—");
            fila.put("cabezas", v.getCabezas());
            fila.put("total", v.getTotal());
            fila.put("comprador", v.getComprador() != null ? v.getComprador() : "—");
            return fila;
        }).toList();

        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("Ventas registradas", ventas.size());
        metricas.put("Cabezas vendidas", cabezasVendidas);
        metricas.put("Ingresos totales", ingresos.setScale(2, RoundingMode.HALF_UP));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("tipo", "economico");
        resultado.put("titulo", "Reporte económico — período activo");
        resultado.put("metricas", metricas);
        resultado.put("filas", filas);
        return resultado;
    }
}
