package com.agrocloud.controller;

import com.agrocloud.service.DashboardService;
import com.agrocloud.service.UserService;
import com.agrocloud.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para el dashboard del sistema
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para estadísticas del dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000"})
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    /**
     * Obtener estadísticas del dashboard para el usuario actual
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas del dashboard", description = "Obtener estadísticas del dashboard para el usuario logueado")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDashboard(
            @RequestParam(required = false) String username) {
        try {
            User usuario;
            
            if (username != null && !username.isEmpty()) {
                // Si se especifica username, buscar ese usuario por email (ya que usamos email para login)
                usuario = userService.findByEmail(username);
                if (usuario == null) {
                    return ResponseEntity.notFound().build();
                }
            } else {
                // Por defecto, usar admin (en producción esto vendría del JWT)
                usuario = userService.findByEmail("admin@agrocloud.com");
            }

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

    /**
     * Endpoint de prueba para verificar que el controlador funcione
     */
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Endpoint de prueba para verificar que el controlador funcione")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("DashboardController funcionando correctamente");
    }
}
