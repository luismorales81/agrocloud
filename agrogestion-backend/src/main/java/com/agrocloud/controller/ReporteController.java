package com.agrocloud.controller;

import com.agrocloud.dto.ReporteRendimientoDTO;
import com.agrocloud.dto.ReporteCosechasDTO;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.ReporteService;
import com.agrocloud.service.UserService;
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

    @Autowired
    private UserService userService;
    
    /**
     * Valida que la fecha de fin no sea anterior a la fecha de inicio.
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @throws IllegalArgumentException si las fechas son inválidas
     */
    private void validarRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

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
        
        try {
            // Validar rango de fechas
            validarRangoFechas(fechaInicio, fechaFin);
            
            System.out.println("[REPORTE_CONTROLLER] Iniciando obtenerReporteRendimiento");
            System.out.println("[REPORTE_CONTROLLER] Parámetros recibidos:");
            System.out.println("[REPORTE_CONTROLLER] - fechaInicio: " + fechaInicio);
            System.out.println("[REPORTE_CONTROLLER] - fechaFin: " + fechaFin);
            System.out.println("[REPORTE_CONTROLLER] - cultivoId: " + cultivoId);
            System.out.println("[REPORTE_CONTROLLER] - loteId: " + loteId);
            System.out.println("[REPORTE_CONTROLLER] - usuarioEmail: " + authentication.getName());
            
            // Obtener el usuario por email y luego su ID
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                System.err.println("[REPORTE_CONTROLLER] ERROR: Usuario no encontrado: " + authentication.getName());
                return ResponseEntity.status(404).build();
            }
            Long usuarioId = user.getId();
            System.out.println("[REPORTE_CONTROLLER] Usuario encontrado - ID: " + usuarioId + ", Email: " + user.getEmail());
            
            List<ReporteRendimientoDTO> reporte = reporteService.obtenerReporteRendimiento(
                usuarioId, fechaInicio, fechaFin, cultivoId, loteId);
            
            System.out.println("[REPORTE_CONTROLLER] Reporte generado con " + reporte.size() + " registros");
            return ResponseEntity.ok(reporte);
            
        } catch (IllegalArgumentException e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR en obtenerReporteRendimiento: " + e.getMessage());
            System.err.println("[REPORTE_CONTROLLER] Stack trace completo:");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
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
        
        try {
            // Validar rango de fechas
            validarRangoFechas(fechaInicio, fechaFin);
            
            System.out.println("[REPORTE_CONTROLLER] Iniciando obtenerReporteCosechas");
            System.out.println("[REPORTE_CONTROLLER] Parámetros recibidos:");
            System.out.println("[REPORTE_CONTROLLER] - fechaInicio: " + fechaInicio);
            System.out.println("[REPORTE_CONTROLLER] - fechaFin: " + fechaFin);
            System.out.println("[REPORTE_CONTROLLER] - cultivoId: " + cultivoId);
            System.out.println("[REPORTE_CONTROLLER] - loteId: " + loteId);
            System.out.println("[REPORTE_CONTROLLER] - usuarioEmail: " + authentication.getName());
            
            // Obtener el usuario por email y luego su ID
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                System.err.println("[REPORTE_CONTROLLER] ERROR: Usuario no encontrado: " + authentication.getName());
                return ResponseEntity.status(404).build();
            }
            Long usuarioId = user.getId();
            System.out.println("[REPORTE_CONTROLLER] Usuario encontrado - ID: " + usuarioId + ", Email: " + user.getEmail());
            
            List<ReporteCosechasDTO> reporte = reporteService.obtenerReporteCosechas(
                usuarioId, fechaInicio, fechaFin, cultivoId, loteId);
            
            System.out.println("[REPORTE_CONTROLLER] Reporte de cosechas generado con " + reporte.size() + " registros");
            if (reporte.size() > 0) {
                System.out.println("[REPORTE_CONTROLLER] Primer registro de cosechas: " + reporte.get(0));
            } else {
                System.out.println("[REPORTE_CONTROLLER] No se encontraron registros de cosechas para el usuario " + usuarioId);
            }
            return ResponseEntity.ok(reporte);
            
        } catch (IllegalArgumentException e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR en obtenerReporteCosechas: " + e.getMessage());
            System.err.println("[REPORTE_CONTROLLER] Stack trace completo:");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }


    /**
     * Obtiene estadísticas generales de producción.
     */
    @GetMapping("/estadisticas-produccion")
    public ResponseEntity<Object> obtenerEstadisticasProduccion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        
        try {
            // Validar rango de fechas
            validarRangoFechas(fechaInicio, fechaFin);
            
            System.out.println("[REPORTE_CONTROLLER] Iniciando obtenerEstadisticasProduccion");
            System.out.println("[REPORTE_CONTROLLER] Parámetros recibidos:");
            System.out.println("[REPORTE_CONTROLLER] - fechaInicio: " + fechaInicio);
            System.out.println("[REPORTE_CONTROLLER] - fechaFin: " + fechaFin);
            System.out.println("[REPORTE_CONTROLLER] - usuarioEmail: " + authentication.getName());
            
            // Obtener el usuario por email y luego su ID
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                System.err.println("[REPORTE_CONTROLLER] ERROR: Usuario no encontrado: " + authentication.getName());
                return ResponseEntity.status(404).build();
            }
            Long usuarioId = user.getId();
            System.out.println("[REPORTE_CONTROLLER] Usuario encontrado - ID: " + usuarioId + ", Email: " + user.getEmail());
            
            Object estadisticas = reporteService.obtenerEstadisticasProduccion(usuarioId, fechaInicio, fechaFin);
            
            System.out.println("[REPORTE_CONTROLLER] Estadísticas de producción generadas exitosamente");
            return ResponseEntity.ok(estadisticas);
            
        } catch (IllegalArgumentException e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR de validación: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR en obtenerEstadisticasProduccion: " + e.getMessage());
            System.err.println("[REPORTE_CONTROLLER] Stack trace completo:");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
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
        
        try {
            // Validar rango de fechas
            validarRangoFechas(fechaInicio, fechaFin);
            
            System.out.println("[REPORTE_CONTROLLER] Iniciando obtenerReporteRentabilidad");
            System.out.println("[REPORTE_CONTROLLER] Parámetros recibidos:");
            System.out.println("[REPORTE_CONTROLLER] - fechaInicio: " + fechaInicio);
            System.out.println("[REPORTE_CONTROLLER] - fechaFin: " + fechaFin);
            System.out.println("[REPORTE_CONTROLLER] - cultivoId: " + cultivoId);
            System.out.println("[REPORTE_CONTROLLER] - usuarioEmail: " + authentication.getName());
            
            // Obtener el usuario por email y luego su ID
            User user = userService.findByEmail(authentication.getName());
            if (user == null) {
                System.err.println("[REPORTE_CONTROLLER] ERROR: Usuario no encontrado: " + authentication.getName());
                return ResponseEntity.status(404).build();
            }
            Long usuarioId = user.getId();
            System.out.println("[REPORTE_CONTROLLER] Usuario encontrado - ID: " + usuarioId + ", Email: " + user.getEmail());
            
            List<Object> reporte = reporteService.obtenerReporteRentabilidad(
                usuarioId, fechaInicio, fechaFin, cultivoId);
            
            System.out.println("[REPORTE_CONTROLLER] Reporte de rentabilidad generado con " + reporte.size() + " registros");
            if (reporte.size() > 0) {
                System.out.println("[REPORTE_CONTROLLER] Primer registro de rentabilidad: " + reporte.get(0));
            } else {
                System.out.println("[REPORTE_CONTROLLER] No se encontraron registros de rentabilidad para el usuario " + usuarioId);
            }
            return ResponseEntity.ok(reporte);
            
        } catch (IllegalArgumentException e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("[REPORTE_CONTROLLER] ERROR en obtenerReporteRentabilidad: " + e.getMessage());
            System.err.println("[REPORTE_CONTROLLER] Stack trace completo:");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
