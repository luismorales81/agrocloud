package com.agrocloud.controller;

import com.agrocloud.dto.LaborDetalladoDTO;
import com.agrocloud.dto.RespuestaCambioEstado;
import com.agrocloud.dto.ConfirmacionCambioEstado;
import com.agrocloud.dto.ReporteCosechaDTO;
import com.agrocloud.dto.CrearLaborRequest;
import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.LaborMaquinaria;
import com.agrocloud.model.entity.LaborManoObra;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.LaborService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/labores")
@CrossOrigin(origins = "*")
public class LaborController {

    @Autowired
    private LaborService laborService;

    @Autowired
    private UserService userService;

    /**
     * Obtener todas las labores accesibles por el usuario con costos detallados
     */
    @GetMapping
    public ResponseEntity<List<LaborDetalladoDTO>> getAllLabores(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<LaborDetalladoDTO> labores = laborService.getLaboresDetalladasByUser(user);
            return ResponseEntity.ok(labores);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener labor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Labor> getLaborById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Optional<Labor> labor = laborService.getLaborById(id, user);
            
            return labor.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear nueva labor
     */
    @PostMapping
    public ResponseEntity<Labor> createLabor(@RequestBody CrearLaborRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("=== CONTROLADOR: Creando labor ===");
            System.out.println("Request recibido: " + request);
            System.out.println("Tipo labor: " + request.getTipoLabor());
            System.out.println("Maquinaria asignada: " + request.getMaquinariaAsignada());
            System.out.println("Mano de obra: " + request.getManoObra());
            
            User user = userService.findByEmail(userDetails.getUsername());
            Labor laborCreada = laborService.crearLaborDesdeRequest(request, user);
            return ResponseEntity.ok(laborCreada);
        } catch (Exception e) {
            System.err.println("Error al crear labor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar labor existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Labor> updateLabor(@PathVariable Long id, @RequestBody Labor labor, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Labor laborActualizada = laborService.actualizarLabor(id, labor, user);
            return ResponseEntity.ok(laborActualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar labor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLabor(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            laborService.eliminarLabor(id, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener labores por lote
     */
    @GetMapping("/lote/{loteId}")
    public ResponseEntity<List<Labor>> getLaboresByLote(@PathVariable Long loteId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<Labor> labores = laborService.getLaboresByLote(loteId, user);
            return ResponseEntity.ok(labores);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Agregar maquinaria a una labor
     */
    @PostMapping("/{laborId}/maquinaria")
    public ResponseEntity<LaborMaquinaria> agregarMaquinaria(@PathVariable Long laborId, @RequestBody LaborMaquinaria maquinaria, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            LaborMaquinaria maquinariaCreada = laborService.agregarMaquinaria(laborId, maquinaria, user);
            return ResponseEntity.ok(maquinariaCreada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Agregar mano de obra a una labor
     */
    @PostMapping("/{laborId}/mano-obra")
    public ResponseEntity<LaborManoObra> agregarManoObra(@PathVariable Long laborId, @RequestBody LaborManoObra manoObra, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            LaborManoObra manoObraCreada = laborService.agregarManoObra(laborId, manoObra, user);
            return ResponseEntity.ok(manoObraCreada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Crear labor de siembra con confirmación de cambio de estado
     */
    @PostMapping("/siembra")
    public ResponseEntity<RespuestaCambioEstado> crearLaborSiembra(@RequestBody Labor labor, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            RespuestaCambioEstado respuesta = laborService.crearLaborSiembraConConfirmacion(labor, user);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            RespuestaCambioEstado error = new RespuestaCambioEstado(false, "Error al crear labor de siembra: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Crear labor de cosecha con confirmación de cambio de estado
     */
    @PostMapping("/cosecha")
    public ResponseEntity<RespuestaCambioEstado> crearLaborCosecha(@RequestBody Labor labor, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            RespuestaCambioEstado respuesta = laborService.crearLaborCosechaConConfirmacion(labor, user);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            RespuestaCambioEstado error = new RespuestaCambioEstado(false, "Error al crear labor de cosecha: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Confirmar labor de siembra y actualizar estado del lote
     */
    @PostMapping("/{laborId}/confirmar-siembra")
    public ResponseEntity<Map<String, Object>> confirmarLaborSiembra(@PathVariable Long laborId, @RequestBody ConfirmacionCambioEstado confirmacion, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            laborService.confirmarLaborSiembra(laborId, confirmacion, user);
            
            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "✅ Labor de siembra confirmada y estado del lote actualizado");
            respuesta.put("laborId", laborId);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("mensaje", "❌ Error al confirmar labor de siembra: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Confirmar labor de cosecha y actualizar estado del lote
     */
    @PostMapping("/{laborId}/confirmar-cosecha")
    public ResponseEntity<Map<String, Object>> confirmarLaborCosecha(@PathVariable Long laborId, @RequestBody ConfirmacionCambioEstado confirmacion, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            laborService.confirmarLaborCosecha(laborId, confirmacion, user);
            
            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "✅ Labor de cosecha confirmada y estado del lote actualizado");
            respuesta.put("laborId", laborId);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("mensaje", "❌ Error al confirmar labor de cosecha: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Generar reporte de cosecha para un lote específico
     */
    @GetMapping("/reporte-cosecha/{loteId}")
    public ResponseEntity<ReporteCosechaDTO> generarReporteCosecha(@PathVariable Long loteId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            ReporteCosechaDTO reporte = laborService.generarReporteCosecha(loteId, user);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Generar reportes de cosecha para todos los lotes del usuario
     */
    @GetMapping("/reportes-cosecha")
    public ResponseEntity<List<ReporteCosechaDTO>> generarReportesCosecha(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<ReporteCosechaDTO> reportes = laborService.generarReportesCosechaPorUsuario(user);
            return ResponseEntity.ok(reportes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtener estadísticas de cosecha por cultivo
     */
    @GetMapping("/estadisticas-cosecha")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCosecha(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Map<String, Object> estadisticas = laborService.obtenerEstadisticasCosechaPorCultivo(user);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
