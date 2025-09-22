package com.agrocloud.controller;

import com.agrocloud.dto.ConfirmacionCambioEstado;
import com.agrocloud.dto.ProponerCambioEstadoRequest;
import com.agrocloud.dto.RespuestaCambioEstado;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.service.EstadoLoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión de estados de lotes.
 * Maneja las operaciones de cambio de estado con confirmaciones.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/estados-lotes")
@CrossOrigin(origins = "*")
public class EstadoLoteController {
    
    @Autowired
    private EstadoLoteService estadoLoteService;
    
    /**
     * Proponer cambio de estado de un lote
     */
    @PostMapping("/proponer-cambio")
    public ResponseEntity<RespuestaCambioEstado> proponerCambioEstado(
            @RequestBody ProponerCambioEstadoRequest request,
            Authentication authentication) {
        
        try {
            User usuario = (User) authentication.getPrincipal();
            
            RespuestaCambioEstado respuesta = estadoLoteService.proponerCambioEstado(
                request.getLoteId(), 
                request.getNuevoEstado(), 
                request.getMotivo(),
                usuario
            );
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            RespuestaCambioEstado error = new RespuestaCambioEstado(false, "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Confirmar cambio de estado de un lote
     */
    @PostMapping("/confirmar-cambio")
    public ResponseEntity<Map<String, Object>> confirmarCambioEstado(
            @RequestBody ConfirmacionCambioEstado confirmacion,
            Authentication authentication) {
        
        try {
            User usuario = (User) authentication.getPrincipal();
            
            estadoLoteService.confirmarCambioEstado(confirmacion, usuario);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "✅ Estado del lote actualizado correctamente");
            respuesta.put("loteId", confirmacion.getLoteId());
            respuesta.put("nuevoEstado", confirmacion.getEstadoPropuesto());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "❌ Error al actualizar estado: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Cancelar cambio de estado de un lote
     */
    @PostMapping("/cancelar-cambio")
    public ResponseEntity<Map<String, Object>> cancelarCambioEstado(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("mensaje", "❌ Cambio de estado cancelado por el usuario");
        respuesta.put("loteId", request.get("loteId"));
        respuesta.put("estadoActual", request.get("estadoActual"));
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Obtener resumen de estados de lotes
     */
    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> getResumenEstados(Authentication authentication) {
        try {
            User usuario = (User) authentication.getPrincipal();
            
            Map<String, Object> resumen = new HashMap<>();
            
            // Contar lotes por estado
            for (EstadoLote estado : EstadoLote.values()) {
                List<Plot> lotes = estadoLoteService.getLotesPorEstado(estado);
                resumen.put(estado.name(), lotes.size());
            }
            
            // Lotes que requieren atención
            List<Plot> requierenAtencion = getLotesQueRequierenAtencion();
            resumen.put("requierenAtencion", requierenAtencion.size());
            resumen.put("lotesRequierenAtencion", requierenAtencion);
            
            return ResponseEntity.ok(resumen);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al obtener resumen: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Obtener lotes que requieren atención
     */
    @GetMapping("/requieren-atencion")
    public ResponseEntity<List<Plot>> getLotesQueRequierenAtencion(Authentication authentication) {
        try {
            List<Plot> lotes = getLotesQueRequierenAtencion();
            return ResponseEntity.ok(lotes);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Obtener lotes listos para siembra
     */
    @GetMapping("/listos-para-siembra")
    public ResponseEntity<List<Plot>> getLotesListosParaSiembra(Authentication authentication) {
        try {
            List<Plot> lotes = estadoLoteService.getLotesListosParaSiembra();
            return ResponseEntity.ok(lotes);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Obtener lotes listos para cosecha
     */
    @GetMapping("/listos-para-cosecha")
    public ResponseEntity<List<Plot>> getLotesListosParaCosecha(Authentication authentication) {
        try {
            List<Plot> lotes = estadoLoteService.getLotesListosParaCosecha();
            return ResponseEntity.ok(lotes);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Obtener lotes por estado específico
     */
    @GetMapping("/por-estado/{estado}")
    public ResponseEntity<List<Plot>> getLotesPorEstado(
            @PathVariable EstadoLote estado,
            Authentication authentication) {
        try {
            List<Plot> lotes = estadoLoteService.getLotesPorEstado(estado);
            return ResponseEntity.ok(lotes);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Método auxiliar para obtener lotes que requieren atención
     */
    private List<Plot> getLotesQueRequierenAtencion() {
        List<Plot> lotes = estadoLoteService.getLotesListosParaCosecha();
        
        // Agregar lotes con cultivo enfermo
        lotes.addAll(estadoLoteService.getLotesPorEstado(EstadoLote.ENFERMO));
        
        // Agregar lotes abandonados
        lotes.addAll(estadoLoteService.getLotesPorEstado(EstadoLote.ABANDONADO));
        
        return lotes;
    }
}
