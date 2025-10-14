package com.agrocloud.controller;

import com.agrocloud.service.AdminDashboardService;
import com.agrocloud.service.UserService;
import com.agrocloud.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para el dashboard del administrador
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Endpoints para el dashboard del administrador")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000"})
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private UserService userService;

    /**
     * Resumen general del sistema para el administrador
     */
    @GetMapping("/resumen")
    @Operation(summary = "Resumen del sistema", description = "Obtener resumen general del sistema para el administrador")
    public ResponseEntity<Map<String, Object>> obtenerResumenSistema(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay autenticación"));
            }

            String email = authentication.getName();
            User usuario = userService.findByEmail(email);
            
            if (usuario == null || !esAdmin(usuario)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado - Solo administradores"));
            }

            Map<String, Object> resumen = adminDashboardService.obtenerResumenSistema();
            return ResponseEntity.ok(resumen);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resumen de usuarios del sistema
     */
    @GetMapping("/usuarios")
    @Operation(summary = "Resumen de usuarios", description = "Obtener resumen de usuarios del sistema")
    public ResponseEntity<Map<String, Object>> obtenerResumenUsuarios(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay autenticación"));
            }

            String email = authentication.getName();
            User usuario = userService.findByEmail(email);
            
            if (usuario == null || !esAdmin(usuario)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado - Solo administradores"));
            }

            Map<String, Object> resumen = adminDashboardService.obtenerResumenUsuarios();
            return ResponseEntity.ok(resumen);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Listado de usuarios con filtros
     */
    @GetMapping("/usuarios/lista")
    @Operation(summary = "Lista de usuarios", description = "Obtener lista de usuarios con filtros")
    public ResponseEntity<Map<String, Object>> obtenerListaUsuarios(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay autenticación"));
            }

            String email = authentication.getName();
            User usuario = userService.findByEmail(email);
            
            if (usuario == null || !esAdmin(usuario)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado - Solo administradores"));
            }

            Map<String, Object> usuarios = adminDashboardService.obtenerListaUsuarios(page, size, estado, rol);
            return ResponseEntity.ok(usuarios);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Estadísticas de uso del sistema
     */
    @GetMapping("/uso-sistema")
    @Operation(summary = "Uso del sistema", description = "Obtener estadísticas de uso del sistema")
    public ResponseEntity<Map<String, Object>> obtenerUsoSistema(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay autenticación"));
            }

            String email = authentication.getName();
            User usuario = userService.findByEmail(email);
            
            if (usuario == null || !esAdmin(usuario)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado - Solo administradores"));
            }

            Map<String, Object> uso = adminDashboardService.obtenerUsoSistema();
            return ResponseEntity.ok(uso);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Auditoría y seguridad
     */
    @GetMapping("/auditoria")
    @Operation(summary = "Auditoría del sistema", description = "Obtener información de auditoría y seguridad")
    public ResponseEntity<Map<String, Object>> obtenerAuditoria(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay autenticación"));
            }

            String email = authentication.getName();
            User usuario = userService.findByEmail(email);
            
            if (usuario == null || !esAdmin(usuario)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado - Solo administradores"));
            }

            Map<String, Object> auditoria = adminDashboardService.obtenerAuditoria();
            return ResponseEntity.ok(auditoria);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reportes globales
     */
    @GetMapping("/reportes")
    @Operation(summary = "Reportes globales", description = "Obtener reportes globales del sistema")
    public ResponseEntity<Map<String, Object>> obtenerReportes(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No hay autenticación"));
            }

            String email = authentication.getName();
            User usuario = userService.findByEmail(email);
            
            if (usuario == null || !esAdmin(usuario)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado - Solo administradores"));
            }

            Map<String, Object> reportes = adminDashboardService.obtenerReportes();
            return ResponseEntity.ok(reportes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verificar si un usuario es administrador
     */
    private boolean esAdmin(User usuario) {
        if (usuario == null || usuario.getRoles() == null) {
            return false;
        }
        
        return usuario.getRoles().stream()
                .anyMatch(role -> "ADMINISTRADOR".equals(role.getNombre()) || "SUPERADMIN".equals(role.getNombre()));
    }
}
