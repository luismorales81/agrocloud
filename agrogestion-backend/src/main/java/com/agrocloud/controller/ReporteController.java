package com.agrocloud.controller;

import com.agrocloud.dto.ReporteRendimientoDTO;
import com.agrocloud.dto.ReporteCosechasDTO;
import com.agrocloud.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para reportes avanzados del sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/reportes")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5173"})
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    /**
     * Obtiene reporte de rendimiento por cultivo y lote.
     */
    @GetMapping("/rendimiento")
    public ResponseEntity<List<ReporteRendimientoDTO>> obtenerReporteRendimiento(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long cultivoId,
            @RequestParam(required = false) Long loteId,
            Authentication authentication) {
        
        Long usuarioId = Long.parseLong(authentication.getName());
        List<ReporteRendimientoDTO> reporte = reporteService.obtenerReporteRendimiento(
            usuarioId, fechaInicio, fechaFin, cultivoId, loteId);
        
        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtiene reporte de cosechas.
     */
    @GetMapping("/cosechas")
    public ResponseEntity<List<ReporteCosechasDTO>> obtenerReporteCosechas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long cultivoId,
            @RequestParam(required = false) Long loteId,
            Authentication authentication) {
        
        Long usuarioId = Long.parseLong(authentication.getName());
        List<ReporteCosechasDTO> reporte = reporteService.obtenerReporteCosechas(
            usuarioId, fechaInicio, fechaFin, cultivoId, loteId);
        
        return ResponseEntity.ok(reporte);
    }


    /**
     * Obtiene estadísticas generales de producción.
     */
    @GetMapping("/estadisticas-produccion")
    public ResponseEntity<Object> obtenerEstadisticasProduccion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        
        Long usuarioId = Long.parseLong(authentication.getName());
        Object estadisticas = reporteService.obtenerEstadisticasProduccion(usuarioId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene reporte de análisis de rentabilidad por cultivo.
     */
    @GetMapping("/rentabilidad")
    public ResponseEntity<List<Object>> obtenerReporteRentabilidad(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long cultivoId,
            Authentication authentication) {
        
        Long usuarioId = Long.parseLong(authentication.getName());
        List<Object> reporte = reporteService.obtenerReporteRentabilidad(
            usuarioId, fechaInicio, fechaFin, cultivoId);
        
        return ResponseEntity.ok(reporte);
    }
}
