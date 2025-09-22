package com.agrocloud.service;

import com.agrocloud.dto.ReporteRendimientoDTO;
import com.agrocloud.dto.ReporteCosechasDTO;
import com.agrocloud.model.entity.Cosecha;
import com.agrocloud.model.entity.HistorialCosecha;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.Cultivo;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.CosechaRepository;
import com.agrocloud.repository.HistorialCosechaRepository;
import com.agrocloud.repository.PlotRepository;
import com.agrocloud.repository.CultivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para generar reportes avanzados del sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service
public class ReporteService {

    @Autowired
    private CosechaRepository cosechaRepository;

    @Autowired
    private HistorialCosechaRepository historialCosechaRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private UserService userService;

    /**
     * Genera reporte de rendimiento por cultivo y lote.
     */
    public List<ReporteRendimientoDTO> obtenerReporteRendimiento(Long usuarioId, LocalDate fechaInicio, 
                                                                LocalDate fechaFin, Long cultivoId, Long loteId) {
        List<HistorialCosecha> historiales = historialCosechaRepository.findByUsuarioIdOrderByFechaCosechaDesc(usuarioId);
        
        // Aplicar filtros
        if (fechaInicio != null) {
            historiales = historiales.stream()
                .filter(h -> !h.getFechaCosecha().isBefore(fechaInicio))
                .collect(Collectors.toList());
        }
        if (fechaFin != null) {
            historiales = historiales.stream()
                .filter(h -> !h.getFechaCosecha().isAfter(fechaFin))
                .collect(Collectors.toList());
        }
        if (cultivoId != null) {
            historiales = historiales.stream()
                .filter(h -> h.getCultivo().getId().equals(cultivoId))
                .collect(Collectors.toList());
        }
        if (loteId != null) {
            historiales = historiales.stream()
                .filter(h -> h.getLote().getId().equals(loteId))
                .collect(Collectors.toList());
        }

        return historiales.stream()
            .map(this::mapearARendimientoDTO)
            .collect(Collectors.toList());
    }

    /**
     * Genera reporte de cosechas.
     */
    public List<ReporteCosechasDTO> obtenerReporteCosechas(Long usuarioId, LocalDate fechaInicio, 
                                                          LocalDate fechaFin, Long cultivoId, Long loteId) {
        List<Cosecha> cosechas = cosechaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
        
        // Aplicar filtros
        if (fechaInicio != null) {
            cosechas = cosechas.stream()
                .filter(c -> !c.getFecha().isBefore(fechaInicio))
                .collect(Collectors.toList());
        }
        if (fechaFin != null) {
            cosechas = cosechas.stream()
                .filter(c -> !c.getFecha().isAfter(fechaFin))
                .collect(Collectors.toList());
        }
        if (cultivoId != null) {
            cosechas = cosechas.stream()
                .filter(c -> c.getCultivo().getId().equals(cultivoId))
                .collect(Collectors.toList());
        }
        if (loteId != null) {
            cosechas = cosechas.stream()
                .filter(c -> c.getLote().getId().equals(loteId))
                .collect(Collectors.toList());
        }

        return cosechas.stream()
            .map(this::mapearACosechasDTO)
            .collect(Collectors.toList());
    }


    /**
     * Obtiene estadísticas generales de producción.
     */
    public Object obtenerEstadisticasProduccion(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<HistorialCosecha> historiales = historialCosechaRepository.findByUsuarioIdOrderByFechaCosechaDesc(usuarioId);
        
        if (fechaInicio != null) {
            historiales = historiales.stream()
                .filter(h -> !h.getFechaCosecha().isBefore(fechaInicio))
                .collect(Collectors.toList());
        }
        if (fechaFin != null) {
            historiales = historiales.stream()
                .filter(h -> !h.getFechaCosecha().isAfter(fechaFin))
                .collect(Collectors.toList());
        }

        Map<String, Object> estadisticas = new java.util.HashMap<>();
        
        // Estadísticas generales
        estadisticas.put("totalCosechas", historiales.size());
        estadisticas.put("superficieTotal", historiales.stream()
            .map(HistorialCosecha::getSuperficieHectareas)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // Por cultivo
        Map<String, List<HistorialCosecha>> porCultivo = historiales.stream()
            .collect(Collectors.groupingBy(h -> h.getCultivo().getNombre()));
        
        Map<String, Object> porCultivoStats = new java.util.HashMap<>();
        porCultivo.forEach((cultivo, cosechas) -> {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("cantidadCosechas", cosechas.size());
            stats.put("superficieTotal", cosechas.stream()
                .map(HistorialCosecha::getSuperficieHectareas)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            stats.put("rendimientoPromedio", cosechas.stream()
                .map(HistorialCosecha::getRendimientoReal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(cosechas.size()), 2, RoundingMode.HALF_UP));
            porCultivoStats.put(cultivo, stats);
        });
        
        estadisticas.put("porCultivo", porCultivoStats);
        
        return estadisticas;
    }

    /**
     * Genera reporte de análisis de rentabilidad por cultivo.
     */
    public List<Object> obtenerReporteRentabilidad(Long usuarioId, LocalDate fechaInicio, 
                                                  LocalDate fechaFin, Long cultivoId) {
        List<ReporteCosechasDTO> cosechas = obtenerReporteCosechas(usuarioId, fechaInicio, fechaFin, cultivoId, null);
        
        return cosechas.stream()
            .map(this::calcularRentabilidad)
            .collect(Collectors.toList());
    }

    // Métodos privados auxiliares

    private ReporteRendimientoDTO mapearARendimientoDTO(HistorialCosecha historial) {
        return new ReporteRendimientoDTO(
            historial.getLote().getId(),
            historial.getLote().getNombre(),
            historial.getCultivo().getId(),
            historial.getCultivo().getNombre(),
            historial.getVariedadSemilla(),
            historial.getSuperficieHectareas(),
            historial.getFechaSiembra(),
            historial.getFechaCosecha(),
            historial.getRendimientoEsperado(),
            historial.getRendimientoReal(),
            historial.getRendimientoReal(), // Usar rendimiento real como corregido
            historial.getPorcentajeCumplimiento(),
            historial.getRendimientoReal().subtract(historial.getRendimientoEsperado()), // Diferencia
            historial.getCultivo().getUnidadRendimiento(),
            historial.getObservaciones()
        );
    }

    private ReporteCosechasDTO mapearACosechasDTO(Cosecha cosecha) {
        BigDecimal ingresoTotal = calcularIngresoTotal(cosecha);
        BigDecimal rentabilidad = BigDecimal.ZERO; // Se calculará en el método de rentabilidad
        
        // Calcular rendimiento real basado en cantidad y superficie
        BigDecimal rendimientoReal = BigDecimal.ZERO;
        if (cosecha.getCantidadToneladas() != null && cosecha.getLote().getAreaHectareas() != null) {
            rendimientoReal = cosecha.getCantidadToneladas().divide(cosecha.getLote().getAreaHectareas(), 2, RoundingMode.HALF_UP);
        }
        
        // Calcular porcentaje de cumplimiento
        BigDecimal porcentajeCumplimiento = BigDecimal.ZERO;
        if (cosecha.getCultivo().getRendimientoEsperado() != null && cosecha.getCultivo().getRendimientoEsperado().compareTo(BigDecimal.ZERO) > 0) {
            porcentajeCumplimiento = rendimientoReal.divide(cosecha.getCultivo().getRendimientoEsperado(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        
        return new ReporteCosechasDTO(
            cosecha.getId(),
            cosecha.getLote().getId(),
            cosecha.getLote().getNombre(),
            cosecha.getCultivo().getId(),
            cosecha.getCultivo().getNombre(),
            cosecha.getCultivo().getVariedad(),
            cosecha.getLote().getAreaHectareas(),
            cosecha.getLote().getFechaSiembra(),
            cosecha.getFecha(),
            cosecha.getCantidadToneladas(),
            "tn", // Unidad por defecto
            rendimientoReal,
            cosecha.getCultivo().getRendimientoEsperado(),
            porcentajeCumplimiento,
            cosecha.getPrecioPorTonelada(),
            cosecha.getCostoTotal(),
            ingresoTotal,
            rentabilidad,
            cosecha.getLote().getCampo() != null ? cosecha.getLote().getCampo().getUbicacion() : null,
            cosecha.getObservaciones()
        );
    }



    private BigDecimal calcularIngresoTotal(Cosecha cosecha) {
        if (cosecha.getPrecioPorTonelada() != null && cosecha.getCantidadToneladas() != null) {
            return cosecha.getPrecioPorTonelada().multiply(cosecha.getCantidadToneladas());
        }
        return BigDecimal.ZERO;
    }

    private Object calcularRentabilidad(ReporteCosechasDTO cosecha) {
        Map<String, Object> rentabilidad = new java.util.HashMap<>();
        
        BigDecimal ingresoTotal = cosecha.getIngresoTotal();
        BigDecimal costoTotal = cosecha.getCostoTotal() != null ? cosecha.getCostoTotal() : BigDecimal.ZERO;
        
        rentabilidad.put("cosechaId", cosecha.getCosechaId());
        rentabilidad.put("lote", cosecha.getNombreLote());
        rentabilidad.put("cultivo", cosecha.getNombreCultivo());
        rentabilidad.put("ingresoTotal", ingresoTotal);
        rentabilidad.put("costoTotal", costoTotal);
        
        if (costoTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margen = ingresoTotal.subtract(costoTotal);
            BigDecimal porcentajeRentabilidad = margen.divide(costoTotal, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            
            rentabilidad.put("margen", margen);
            rentabilidad.put("porcentajeRentabilidad", porcentajeRentabilidad);
        } else {
            rentabilidad.put("margen", ingresoTotal);
            rentabilidad.put("porcentajeRentabilidad", BigDecimal.ZERO);
        }
        
        return rentabilidad;
    }
}
