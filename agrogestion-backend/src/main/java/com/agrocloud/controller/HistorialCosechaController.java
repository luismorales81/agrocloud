package com.agrocloud.controller;

import com.agrocloud.dto.CosechaDTO;
import com.agrocloud.model.entity.HistorialCosecha;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.HistorialCosechaService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar el historial de cosechas
 */
@RestController
@RequestMapping({"/api/historial-cosechas", "/api/v1/cosechas"})
@CrossOrigin(origins = "*")
public class HistorialCosechaController {

    @Autowired
    private HistorialCosechaService historialCosechaService;

    @Autowired
    private UserService userService;

    /**
     * Obtener historial de cosechas por lote
     */
    @GetMapping("/lote/{loteId}")
    public ResponseEntity<List<CosechaDTO>> getHistorialPorLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            List<HistorialCosecha> historial = historialCosechaService.getHistorialPorLote(loteId, user);
            
            // Convertir a DTOs para evitar problemas de lazy loading
            List<CosechaDTO> cosechasDTO = historial.stream()
                    .map(CosechaDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(cosechasDTO);
        } catch (Exception e) {
            System.err.println("Error en getHistorialPorLote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener historial de cosechas del usuario
     */
    @GetMapping
    public ResponseEntity<List<CosechaDTO>> getHistorialPorUsuario(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            List<HistorialCosecha> historial = historialCosechaService.getHistorialPorUsuario(user);
            
            // Convertir a DTOs para evitar problemas de lazy loading
            List<CosechaDTO> cosechasDTO = historial.stream()
                    .map(CosechaDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(cosechasDTO);
        } catch (Exception e) {
            System.err.println("Error en getHistorialPorUsuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener última cosecha de un lote
     */
    @GetMapping("/lote/{loteId}/ultima")
    public ResponseEntity<HistorialCosecha> getUltimaCosechaPorLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            Optional<HistorialCosecha> ultimaCosecha = historialCosechaService.getUltimaCosechaPorLote(loteId);
            
            if (ultimaCosecha.isPresent()) {
                // Verificar acceso
                if (user.isAdmin() || ultimaCosecha.get().getUsuario().equals(user)) {
                    return ResponseEntity.ok(ultimaCosecha.get());
                }
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Liberar lote para nueva siembra
     */
    @PutMapping("/lote/{loteId}/liberar")
    public ResponseEntity<String> liberarLoteParaNuevaSiembra(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            
            // Verificar si puede ser liberado
            if (!historialCosechaService.puedeLiberarLote(loteId)) {
                return ResponseEntity.badRequest()
                    .body("No se puede liberar el lote. Tiene cosechas muy recientes (menos de 7 días).");
            }
            
            boolean liberado = historialCosechaService.liberarLoteParaNuevaSiembra(loteId, user);
            
            if (liberado) {
                return ResponseEntity.ok("Lote liberado exitosamente para nueva siembra");
            } else {
                return ResponseEntity.badRequest().body("No se pudo liberar el lote");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Liberar lote forzadamente (ignorando período de descanso)
     */
    @PutMapping("/lote/{loteId}/liberar-forzado")
    public ResponseEntity<String> liberarLoteForzadamente(
            @PathVariable Long loteId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            String justificacion = request.get("justificacion");
            
            if (justificacion == null || justificacion.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("La justificación es obligatoria para la liberación forzada");
            }
            
            boolean liberado = historialCosechaService.liberarLoteForzadamente(loteId, user, justificacion);
            
            if (liberado) {
                return ResponseEntity.ok("Lote liberado forzadamente para nueva siembra. Justificación registrada.");
            } else {
                return ResponseEntity.badRequest().body("No se pudo liberar el lote forzadamente");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verificar si un lote puede ser liberado
     */
    @GetMapping("/lote/{loteId}/puede-liberar")
    public ResponseEntity<Boolean> puedeLiberarLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            boolean puedeLiberar = historialCosechaService.puedeLiberarLote(loteId);
            return ResponseEntity.ok(puedeLiberar);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener días de descanso recomendados para un lote
     */
    @GetMapping("/lote/{loteId}/dias-descanso")
    public ResponseEntity<Integer> getDiasDescansoRecomendados(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            int diasDescanso = historialCosechaService.getDiasDescansoRecomendados(loteId);
            return ResponseEntity.ok(diasDescanso);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener estadísticas de rendimiento por cultivo
     */
    @GetMapping("/estadisticas/rendimiento")
    public ResponseEntity<List<Object[]>> getEstadisticasRendimientoPorCultivo(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            List<Object[]> estadisticas = historialCosechaService.getEstadisticasRendimientoPorCultivo(user);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener cosechas recientes (últimos 30 días)
     */
    @GetMapping("/recientes")
    public ResponseEntity<List<HistorialCosecha>> getCosechasRecientes(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            List<HistorialCosecha> cosechasRecientes = historialCosechaService.getCosechasRecientes(user);
            return ResponseEntity.ok(cosechasRecientes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener historial por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistorialCosecha> getHistorialById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            Optional<HistorialCosecha> historial = historialCosechaService.getHistorialById(id, user);
            
            return historial.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar historial (solo administradores)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarHistorial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            boolean eliminado = historialCosechaService.eliminarHistorial(id, user);
            
            if (eliminado) {
                return ResponseEntity.ok("Historial eliminado exitosamente");
            } else {
                return ResponseEntity.badRequest().body("No se pudo eliminar el historial");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
