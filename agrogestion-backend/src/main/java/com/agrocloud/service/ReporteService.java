package com.agrocloud.service;

import com.agrocloud.dto.ReporteRendimientoDTO;
import com.agrocloud.dto.ReporteCosechasDTO;
import com.agrocloud.model.entity.HistorialCosecha;
import com.agrocloud.repository.HistorialCosechaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
@Transactional(readOnly = true)
public class ReporteService {

    @Autowired
    private HistorialCosechaRepository historialCosechaRepository;


    /**
     * Genera reporte de rendimiento por cultivo y lote.
     */
    public List<ReporteRendimientoDTO> obtenerReporteRendimiento(Long usuarioId, LocalDate fechaInicio, 
                                                                LocalDate fechaFin, Long cultivoId, Long loteId) {
        try {
            System.out.println("[REPORTE_SERVICE] Iniciando obtenerReporteRendimiento para usuarioId: " + usuarioId);
            System.out.println("[REPORTE_SERVICE] Parámetros: fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin + ", cultivoId=" + cultivoId + ", loteId=" + loteId);
            
            List<HistorialCosecha> historiales = historialCosechaRepository.findByUsuarioIdOrderByFechaCosechaDesc(usuarioId);
            System.out.println("[REPORTE_SERVICE] Historiales encontrados: " + historiales.size());
        
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

            List<ReporteRendimientoDTO> resultado = historiales.stream()
                .map(this::mapearARendimientoDTO)
                .collect(Collectors.toList());
            
            System.out.println("[REPORTE_SERVICE] Reporte generado con " + resultado.size() + " registros");
            return resultado;
            
        } catch (Exception e) {
            System.err.println("[REPORTE_SERVICE] ERROR en obtenerReporteRendimiento: " + e.getMessage());
            System.err.println("[REPORTE_SERVICE] Stack trace completo:");
            e.printStackTrace();
            throw new RuntimeException("Error al generar reporte de rendimiento: " + e.getMessage(), e);
        }
    }

    /**
     * Genera reporte de cosechas usando historial_cosechas (tabla unificada).
     */
    public List<ReporteCosechasDTO> obtenerReporteCosechas(Long usuarioId, LocalDate fechaInicio, 
                                                          LocalDate fechaFin, Long cultivoId, Long loteId) {
        List<HistorialCosecha> cosechas = historialCosechaRepository.findByUsuarioIdOrderByFechaCosechaDesc(usuarioId);
        
        // Aplicar filtros
        if (fechaInicio != null) {
            cosechas = cosechas.stream()
                .filter(c -> !c.getFechaCosecha().isBefore(fechaInicio))
                .collect(Collectors.toList());
        }
        if (fechaFin != null) {
            cosechas = cosechas.stream()
                .filter(c -> !c.getFechaCosecha().isAfter(fechaFin))
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
            .map(this::mapearHistorialACosechasDTO)
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
            .filter(s -> s != null)
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
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            
            // Calcular rendimiento promedio filtrando nulls
            List<BigDecimal> rendimientos = cosechas.stream()
                .map(HistorialCosecha::getRendimientoReal)
                .filter(r -> r != null)
                .collect(Collectors.toList());
            
            BigDecimal rendimientoPromedio = rendimientos.isEmpty() 
                ? BigDecimal.ZERO 
                : rendimientos.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(rendimientos.size()), 2, RoundingMode.HALF_UP);
            
            stats.put("rendimientoPromedio", rendimientoPromedio);
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
        // Manejar valores null en rendimientos
        BigDecimal rendimientoReal = historial.getRendimientoReal() != null 
            ? historial.getRendimientoReal() 
            : BigDecimal.ZERO;
        BigDecimal rendimientoEsperado = historial.getRendimientoEsperado() != null 
            ? historial.getRendimientoEsperado() 
            : BigDecimal.ZERO;
        
        // Calcular diferencia solo si ambos valores existen
        BigDecimal diferencia = rendimientoReal.subtract(rendimientoEsperado);
        
        return new ReporteRendimientoDTO(
            historial.getLote().getId(),
            historial.getLote().getNombre(),
            historial.getCultivo().getId(),
            historial.getCultivo().getNombre(),
            historial.getVariedadSemilla(),
            historial.getSuperficieHectareas(),
            historial.getFechaSiembra(),
            historial.getFechaCosecha(),
            rendimientoEsperado,
            rendimientoReal,
            rendimientoReal, // Usar rendimiento real como corregido
            historial.getPorcentajeCumplimiento(),
            diferencia,
            historial.getCultivo().getUnidadRendimiento(),
            historial.getObservaciones()
        );
    }

    /**
     * Mapea HistorialCosecha a ReporteCosechasDTO.
     * Usa la nueva tabla unificada historial_cosechas.
     */
    private ReporteCosechasDTO mapearHistorialACosechasDTO(HistorialCosecha historial) {
        // Manejar valores null
        BigDecimal cantidadCosechada = historial.getCantidadCosechada() != null 
            ? historial.getCantidadCosechada() 
            : BigDecimal.ZERO;
        BigDecimal rendimientoReal = historial.getRendimientoReal() != null 
            ? historial.getRendimientoReal() 
            : BigDecimal.ZERO;
        BigDecimal rendimientoEsperado = historial.getRendimientoEsperado() != null 
            ? historial.getRendimientoEsperado() 
            : BigDecimal.ZERO;
        BigDecimal superficieHectareas = historial.getSuperficieHectareas() != null 
            ? historial.getSuperficieHectareas() 
            : BigDecimal.ZERO;
        BigDecimal porcentajeCumplimiento = historial.getPorcentajeCumplimiento() != null 
            ? historial.getPorcentajeCumplimiento() 
            : BigDecimal.ZERO;
        
        // Usar los nuevos campos de rentabilidad
        BigDecimal precioUnitario = historial.getPrecioVentaUnitario() != null 
            ? historial.getPrecioVentaUnitario() 
            : BigDecimal.ZERO;
        BigDecimal ingresoTotal = historial.getIngresoTotal() != null 
            ? historial.getIngresoTotal() 
            : BigDecimal.ZERO;
        BigDecimal costoTotal = historial.getCostoTotalProduccion() != null 
            ? historial.getCostoTotalProduccion() 
            : BigDecimal.ZERO;
        
        // Calcular rentabilidad
        BigDecimal rentabilidad = costoTotal.compareTo(BigDecimal.ZERO) > 0
            ? ingresoTotal.subtract(costoTotal).divide(costoTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;
        
        return new ReporteCosechasDTO(
            historial.getId(),
            historial.getLote().getId(),
            historial.getLote().getNombre(),
            historial.getCultivo().getId(),
            historial.getCultivo().getNombre(),
            historial.getVariedadSemilla(),
            superficieHectareas,
            historial.getFechaSiembra(),
            historial.getFechaCosecha(),
            cantidadCosechada,
            historial.getUnidadCosecha(),
            rendimientoReal,
            rendimientoEsperado,
            porcentajeCumplimiento,
            precioUnitario,
            costoTotal,
            ingresoTotal,
            rentabilidad,
            historial.getLote().getCampo() != null ? historial.getLote().getCampo().getUbicacion() : null,
            historial.getObservaciones()
        );
    }
    
    // MÉTODOS ELIMINADOS - La entidad Cosecha y tabla cosechas fueron eliminadas en V1_12
    // Todos los reportes ahora usan historial_cosechas únicamente

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
