package com.agrocloud.controller;

import com.agrocloud.service.RendimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller para cálculos de rendimiento de cultivos
 */
@RestController
@RequestMapping("/api/v1/rendimiento")
@CrossOrigin(origins = "*")
public class RendimientoController {

    @Autowired
    private RendimientoService rendimientoService;

    /**
     * Calcula el rendimiento real de una cosecha
     * 
     * POST /api/v1/rendimiento/calcular
     * Body: {
     *   "cantidad": 5000,
     *   "unidadCantidad": "kg",
     *   "superficie": 10.5,
     *   "unidadCultivo": "qq/ha"
     * }
     */
    @PostMapping("/calcular")
    public ResponseEntity<Map<String, Object>> calcularRendimiento(
            @RequestBody Map<String, Object> request) {
        
        try {
            BigDecimal cantidad = new BigDecimal(request.get("cantidad").toString());
            String unidadCantidad = request.get("unidadCantidad").toString();
            BigDecimal superficie = new BigDecimal(request.get("superficie").toString());
            String unidadCultivo = request.get("unidadCultivo").toString();
            
            BigDecimal rendimiento = rendimientoService.calcularRendimientoReal(
                    cantidad, unidadCantidad, superficie, unidadCultivo);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("rendimiento", rendimiento);
            response.put("unidad", unidadCultivo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al calcular rendimiento: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Calcula la diferencia porcentual entre rendimiento esperado y real
     * 
     * POST /api/v1/rendimiento/diferencia
     * Body: {
     *   "rendimientoEsperado": 45.5,
     *   "rendimientoReal": 50.2
     * }
     */
    @PostMapping("/diferencia")
    public ResponseEntity<Map<String, Object>> calcularDiferenciaPorcentual(
            @RequestBody Map<String, Object> request) {
        
        try {
            BigDecimal esperado = new BigDecimal(request.get("rendimientoEsperado").toString());
            BigDecimal real = new BigDecimal(request.get("rendimientoReal").toString());
            
            BigDecimal diferencia = rendimientoService.calcularDiferenciaPorcentual(esperado, real);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("diferenciaPorcentual", diferencia);
            response.put("superaExpectativa", diferencia.compareTo(BigDecimal.ZERO) > 0);
            response.put("cumpleExpectativa", diferencia.compareTo(new BigDecimal("-10")) >= 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al calcular diferencia: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}


