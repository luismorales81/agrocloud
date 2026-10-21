package com.agrocloud.controller;

import com.agrocloud.core.application.DashboardService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para el dashboard del sistema
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para estadísticas del dashboard")
public class DashboardController {

    @Autowired
    @Qualifier("dashboardServiceCore")
    private DashboardService dashboardService;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;

    /**
     * Obtener estadísticas del dashboard para el usuario actual
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas del dashboard", description = "Obtener estadísticas del dashboard para el usuario logueado")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDashboard(
            @RequestParam(required = false) String username,
            Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).build();
            }
            String email = (username != null && !username.isEmpty()) ? username : authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email);

            Map<String, Object> estadisticas = dashboardService.obtenerEstadisticasDashboard(usuario);
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener estadísticas del dashboard para el usuario autenticado (JWT)
     */
    @GetMapping("/estadisticas-auth")
    @Operation(summary = "Estadísticas del dashboard autenticado", description = "Obtener estadísticas del dashboard para el usuario logueado usando JWT")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDashboardAutenticado(
            org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay autenticación"));
            }

            String username = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(username);
            
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> estadisticas = dashboardService.obtenerEstadisticasDashboard(usuario);
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener estadísticas del dashboard para un usuario específico por ID
     */
    @GetMapping("/estadisticas/{userId}")
    @Operation(summary = "Estadísticas por usuario ID", description = "Obtener estadísticas del dashboard para un usuario específico")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDashboardPorUsuarioId(
            @PathVariable Long userId) {
        try {
            Map<String, Object> estadisticas = dashboardService.obtenerEstadisticasDashboardPorUsuarioId(userId);
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener estadísticas comparativas entre usuarios
     */
    @GetMapping("/estadisticas-comparativas")
    @Operation(summary = "Estadísticas comparativas", description = "Obtener estadísticas comparativas entre usuarios")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasComparativas() {
        try {
            Map<String, Object> comparativas = dashboardService.obtenerEstadisticasComparativas();
            return ResponseEntity.ok(comparativas);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
