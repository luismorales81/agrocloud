package com.agrocloud.controller;

import com.agrocloud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de prueba para diagnosticar el dashboard
 */
@RestController
@RequestMapping("/api/test/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000"})
public class TestDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MaquinariaRepository maquinariaRepository;

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    /**
     * Prueba básica de conectividad
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Test endpoint funcionando");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Prueba de autenticación
     */
    @GetMapping("/auth-test")
    public ResponseEntity<Map<String, Object>> authTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("status", "ERROR");
            response.put("message", "No hay autenticación");
            return ResponseEntity.status(401).body(response);
        }
        
        response.put("status", "OK");
        response.put("message", "Autenticación funcionando");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    /**
     * Prueba de repositorios uno por uno
     */
    @GetMapping("/repositories-test")
    public ResponseEntity<Map<String, Object>> repositoriesTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("status", "ERROR");
            response.put("message", "No hay autenticación");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // Probar cada repositorio individualmente
            Map<String, Object> repositories = new HashMap<>();
            
            // UserRepository
            try {
                long userCount = userRepository.count();
                repositories.put("userRepository", "OK - " + userCount + " usuarios");
            } catch (Exception e) {
                repositories.put("userRepository", "ERROR: " + e.getMessage());
            }
            
            // FieldRepository
            try {
                long fieldCount = fieldRepository.count();
                repositories.put("fieldRepository", "OK - " + fieldCount + " campos");
            } catch (Exception e) {
                repositories.put("fieldRepository", "ERROR: " + e.getMessage());
            }
            
            // PlotRepository
            try {
                long plotCount = plotRepository.count();
                repositories.put("plotRepository", "OK - " + plotCount + " lotes");
            } catch (Exception e) {
                repositories.put("plotRepository", "ERROR: " + e.getMessage());
            }
            
            // CultivoRepository
            try {
                long cultivoCount = cultivoRepository.count();
                repositories.put("cultivoRepository", "OK - " + cultivoCount + " cultivos");
            } catch (Exception e) {
                repositories.put("cultivoRepository", "ERROR: " + e.getMessage());
            }
            
            // InsumoRepository
            try {
                long insumoCount = insumoRepository.count();
                repositories.put("insumoRepository", "OK - " + insumoCount + " insumos");
            } catch (Exception e) {
                repositories.put("insumoRepository", "ERROR: " + e.getMessage());
            }
            
            // MaquinariaRepository
            try {
                long maquinariaCount = maquinariaRepository.count();
                repositories.put("maquinariaRepository", "OK - " + maquinariaCount + " maquinaria");
            } catch (Exception e) {
                repositories.put("maquinariaRepository", "ERROR: " + e.getMessage());
            }
            
            // LaborRepository
            try {
                long laborCount = laborRepository.count();
                repositories.put("laborRepository", "OK - " + laborCount + " labores");
            } catch (Exception e) {
                repositories.put("laborRepository", "ERROR: " + e.getMessage());
            }
            
            // IngresoRepository
            try {
                long ingresoCount = ingresoRepository.count();
                repositories.put("ingresoRepository", "OK - " + ingresoCount + " ingresos");
            } catch (Exception e) {
                repositories.put("ingresoRepository", "ERROR: " + e.getMessage());
            }
            
            // EgresoRepository
            try {
                long egresoCount = egresoRepository.count();
                repositories.put("egresoRepository", "OK - " + egresoCount + " egresos");
            } catch (Exception e) {
                repositories.put("egresoRepository", "ERROR: " + e.getMessage());
            }
            
            response.put("status", "OK");
            response.put("message", "Prueba de repositorios completada");
            response.put("repositories", repositories);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error general: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Prueba de métodos específicos que causan problemas
     */
    @GetMapping("/methods-test")
    public ResponseEntity<Map<String, Object>> methodsTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("status", "ERROR");
            response.put("message", "No hay autenticación");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            Map<String, Object> methods = new HashMap<>();
            
            // Probar countByActivoTrue
            try {
                long activos = userRepository.countByActivoTrue();
                methods.put("countByActivoTrue", "OK - " + activos + " usuarios activos");
            } catch (Exception e) {
                methods.put("countByActivoTrue", "ERROR: " + e.getMessage());
            }
            
            // Probar countByEstado
            try {
                long pendientes = userRepository.countByEstado(com.agrocloud.model.entity.EstadoUsuario.PENDIENTE);
                methods.put("countByEstado", "OK - " + pendientes + " usuarios pendientes");
            } catch (Exception e) {
                methods.put("countByEstado", "ERROR: " + e.getMessage());
            }
            
            // Probar sumAllIngresos
            try {
                java.math.BigDecimal totalIngresos = ingresoRepository.sumAllIngresos();
                methods.put("sumAllIngresos", "OK - Total: " + totalIngresos);
            } catch (Exception e) {
                methods.put("sumAllIngresos", "ERROR: " + e.getMessage());
            }
            
            // Probar sumAllEgresos
            try {
                java.math.BigDecimal totalEgresos = egresoRepository.sumAllEgresos();
                methods.put("sumAllEgresos", "OK - Total: " + totalEgresos);
            } catch (Exception e) {
                methods.put("sumAllEgresos", "ERROR: " + e.getMessage());
            }
            
            response.put("status", "OK");
            response.put("message", "Prueba de métodos completada");
            response.put("methods", methods);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error general: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }
}
