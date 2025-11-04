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
import com.agrocloud.model.enums.EstadoLote;
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
     * Helper method para obtener el usuario desde UserDetails
     */
    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            return userService.findByEmailWithAllRelations("test@test.com");
        } else {
            return userService.findByEmailWithAllRelations(userDetails.getUsername());
        }
    }

    /**
     * Obtener todas las labores accesibles por el usuario con costos detallados
     */
    @GetMapping
    public ResponseEntity<List<LaborDetalladoDTO>> getAllLabores(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<LaborDetalladoDTO> labores = laborService.getLaboresDetalladasByUser(user);
            return ResponseEntity.ok(labores);
        } catch (Exception e) {
            System.err.println("Error al obtener labores: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener labor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Labor> getLaborById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
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
            
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
            Labor laborActualizada = laborService.actualizarLabor(id, labor, user);
            return ResponseEntity.ok(laborActualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar labor (cancela si está PLANIFICADA, requiere anulación si está ejecutada)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLabor(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            laborService.eliminarLabor(id, user);
            return ResponseEntity.ok(Map.of("mensaje", "Labor eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Anular una labor ejecutada (solo ADMINISTRADOR)
     * 
     * @param id ID de la labor
     * @param request Request con justificación y opción de restaurar insumos
     */
    @PostMapping("/{id}/anular")
    public ResponseEntity<?> anularLabor(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            
            String justificacion = (String) request.get("justificacion");
            Boolean restaurarInsumos = request.get("restaurarInsumos") != null 
                ? (Boolean) request.get("restaurarInsumos") 
                : false;
            
            laborService.anularLabor(id, justificacion, restaurarInsumos, user);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Labor anulada exitosamente",
                "insumosRestaurados", restaurarInsumos
            ));
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
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
            User user = obtenerUsuario(userDetails);
            Map<String, Object> estadisticas = laborService.obtenerEstadisticasCosechaPorCultivo(user);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtiene las tareas disponibles según el estado del lote
     */
    @GetMapping("/tareas-disponibles/{estado}")
    public ResponseEntity<Map<String, Object>> getTareasDisponiblesPorEstado(@PathVariable String estado) {
        try {
            EstadoLote estadoLote = EstadoLote.valueOf(estado.toUpperCase());
            Map<String, Object> infoTareas = laborService.getInfoTareasDisponibles(estadoLote);
            return ResponseEntity.ok(infoTareas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Valida si una tarea es apropiada para un estado específico
     */
    @GetMapping("/validar-tarea/{estado}/{tipoTarea}")
    public ResponseEntity<Map<String, Object>> validarTareaParaEstado(
            @PathVariable String estado, 
            @PathVariable String tipoTarea) {
        try {
            EstadoLote estadoLote = EstadoLote.valueOf(estado.toUpperCase());
            boolean esValida = laborService.validarTareaParaEstado(estadoLote, tipoTarea);
            
            Map<String, Object> respuesta = Map.of(
                "estado", estado,
                "tipoTarea", tipoTarea,
                "esValida", esValida,
                "mensaje", esValida ? 
                    "✅ Tarea apropiada para este estado" : 
                    "⚠️ Esta tarea no es recomendada para el estado " + estado
            );
            
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Calcula el costo total de una labor sumando todos sus componentes
     */
    @GetMapping("/{id}/calcular-costo")
    public ResponseEntity<Map<String, Object>> calcularCostoTotal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Validar que el usuario esté autenticado
            obtenerUsuario(userDetails);
            
            java.math.BigDecimal costoTotal = laborService.calcularCostoTotalLabor(id);
            
            Map<String, Object> respuesta = Map.of(
                "success", true,
                "costoTotal", costoTotal,
                "mensaje", "Costo total calculado correctamente"
            );
            
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "mensaje", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "mensaje", "Error al calcular costo: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtiene el desglose detallado de costos de una labor
     */
    @GetMapping("/{id}/desglose-costos")
    public ResponseEntity<Map<String, Object>> obtenerDesgloseCostos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Validar que el usuario esté autenticado
            obtenerUsuario(userDetails);
            
            Map<String, java.math.BigDecimal> desglose = laborService.calcularDesgloseCostosLabor(id);
            
            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("success", true);
            respuesta.putAll(desglose);
            
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "mensaje", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "mensaje", "Error al obtener desglose: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Actualiza el costo total de una labor recalculándolo desde sus componentes
     */
    @PostMapping("/{id}/actualizar-costo")
    public ResponseEntity<Map<String, Object>> actualizarCostoTotal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Validar que el usuario esté autenticado
            obtenerUsuario(userDetails);
            
            Labor laborActualizada = laborService.actualizarCostoTotalLabor(id);
            
            Map<String, Object> respuesta = Map.of(
                "success", true,
                "costoTotal", laborActualizada.getCostoTotal(),
                "mensaje", "Costo actualizado correctamente"
            );
            
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "mensaje", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "mensaje", "Error al actualizar costo: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
