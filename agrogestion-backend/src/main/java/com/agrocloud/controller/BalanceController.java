package com.agrocloud.controller;

import com.agrocloud.dto.BalanceDTO;
import com.agrocloud.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para el balance de costos y beneficios.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/balance")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173"})
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    /**
     * Obtiene el balance general para un rango de fechas.
     */
    @GetMapping("/general")
    public ResponseEntity<BalanceDTO> obtenerBalanceGeneral(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        
        Long usuarioId = Long.parseLong(authentication.getName());
        BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(balance);
    }

    /**
     * Obtiene el balance para un lote específico.
     */
    @GetMapping("/lote/{loteId}")
    public ResponseEntity<BalanceDTO> obtenerBalancePorLote(
            @PathVariable Long loteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        
        Long usuarioId = Long.parseLong(authentication.getName());
        BalanceDTO balance = balanceService.calcularBalancePorLote(loteId, usuarioId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(balance);
    }

    /**
     * Obtiene estadísticas de balance por mes para un año específico.
     */
    @GetMapping("/estadisticas/{año}")
    public ResponseEntity<List<BalanceDTO>> obtenerEstadisticasPorMes(
            @PathVariable int año,
            Authentication authentication) {
        
        Long usuarioId = Long.parseLong(authentication.getName());
        List<BalanceDTO> estadisticas = balanceService.obtenerEstadisticasPorMes(usuarioId, año);
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene el balance del mes actual.
     */
    @GetMapping("/mes-actual")
    public ResponseEntity<BalanceDTO> obtenerBalanceMesActual(Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        
        LocalDate fechaInicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fechaFin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(balance);
    }

    /**
     * Obtiene el balance del año actual.
     */
    @GetMapping("/año-actual")
    public ResponseEntity<BalanceDTO> obtenerBalanceAñoActual(Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        
        LocalDate fechaInicio = LocalDate.now().withDayOfYear(1);
        LocalDate fechaFin = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        
        BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(balance);
    }

    /**
     * Obtiene el resumen del balance (endpoint simplificado para el frontend).
     */
    @GetMapping("/resumen")
    public ResponseEntity<BalanceDTO> obtenerResumenBalance(Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            
            // Balance del mes actual
            LocalDate fechaInicio = LocalDate.now().withDayOfMonth(1);
            LocalDate fechaFin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
            
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el balance por período personalizado.
     */
    @GetMapping("/por-periodo")
    public ResponseEntity<BalanceDTO> obtenerBalancePorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el balance general sin parámetros de fecha (último año).
     */
    @GetMapping("/ultimo-año")
    public ResponseEntity<BalanceDTO> obtenerBalanceUltimoAño(Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            
            LocalDate fechaInicio = LocalDate.now().minusYears(1);
            LocalDate fechaFin = LocalDate.now();
            
            BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
