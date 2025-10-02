package com.agrocloud.controller;

import com.agrocloud.dto.BalanceDTO;
import com.agrocloud.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador público para el balance de costos y beneficios.
 * No requiere autenticación para facilitar las pruebas.
 */
@RestController
@RequestMapping("/api/public/balance")
@CrossOrigin(origins = "*")
public class PublicBalanceController {

    @Autowired
    private BalanceService balanceService;

    /**
     * Obtiene el balance general para un rango de fechas (público).
     */
    @GetMapping("/general")
    public ResponseEntity<BalanceDTO> obtenerBalanceGeneral(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            // Usar un usuario por defecto (ID 1) para las pruebas
            Long usuarioId = 1L;
            BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el balance para un lote específico (público).
     */
    @GetMapping("/lote/{loteId}")
    public ResponseEntity<BalanceDTO> obtenerBalancePorLote(
            @PathVariable Long loteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            // Usar un usuario por defecto (ID 1) para las pruebas
            Long usuarioId = 1L;
            BalanceDTO balance = balanceService.calcularBalancePorLote(loteId, usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el balance del mes actual (público).
     */
    @GetMapping("/mes-actual")
    public ResponseEntity<BalanceDTO> obtenerBalanceMesActual() {
        try {
            Long usuarioId = 1L;
            LocalDate fechaInicio = LocalDate.now().withDayOfMonth(1);
            LocalDate fechaFin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el balance del año actual (público).
     */
    @GetMapping("/año-actual")
    public ResponseEntity<BalanceDTO> obtenerBalanceAñoActual() {
        try {
            Long usuarioId = 1L;
            LocalDate fechaInicio = LocalDate.now().withDayOfYear(1);
            LocalDate fechaFin = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
            
            BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene un resumen rápido del balance (público).
     */
    @GetMapping("/resumen")
    public ResponseEntity<BalanceDTO> obtenerResumenBalance() {
        try {
            Long usuarioId = 1L;
            // Últimos 30 días
            LocalDate fechaInicio = LocalDate.now().minusDays(30);
            LocalDate fechaFin = LocalDate.now();
            
            BalanceDTO balance = balanceService.calcularBalance(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
