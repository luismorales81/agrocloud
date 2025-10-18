package com.agrocloud.controller;

import com.agrocloud.dto.InventarioGranoDTO;
import com.agrocloud.dto.VentaGranoRequest;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.InventarioGranoService;
import com.agrocloud.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar el inventario de granos cosechados.
 */
@RestController
@RequestMapping("/api/v1/inventario-granos")
public class InventarioGranoController {

    @Autowired
    private InventarioGranoService inventarioService;

    @Autowired
    private UserService userService;

    /**
     * Obtiene todo el inventario del usuario.
     * GET /api/v1/inventario-granos
     */
    @GetMapping
    public ResponseEntity<List<InventarioGranoDTO>> obtenerInventario(Authentication authentication) {
        try {
            User usuario = userService.findByEmailWithRelations(authentication.getName());
            List<InventarioGranoDTO> inventario = inventarioService.obtenerInventarioPorUsuario(usuario.getId());
            return ResponseEntity.ok(inventario);
        } catch (Exception e) {
            System.err.println("[INVENTARIO_CONTROLLER] Error obteniendo inventario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene inventario disponible (con stock > 0).
     * GET /api/v1/inventario-granos/disponible
     */
    @GetMapping("/disponible")
    public ResponseEntity<List<InventarioGranoDTO>> obtenerInventarioDisponible(Authentication authentication) {
        try {
            User usuario = userService.findByEmailWithRelations(authentication.getName());
            List<InventarioGranoDTO> inventario = inventarioService.obtenerInventarioDisponible(usuario.getId());
            return ResponseEntity.ok(inventario);
        } catch (Exception e) {
            System.err.println("[INVENTARIO_CONTROLLER] Error obteniendo inventario disponible: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Procesa una venta de grano desde el inventario.
     * POST /api/v1/inventario-granos/vender
     */
    @PostMapping("/vender")
    public ResponseEntity<?> venderGrano(@Valid @RequestBody VentaGranoRequest request, 
                                        Authentication authentication) {
        try {
            System.out.println("[INVENTARIO_CONTROLLER] Procesando venta de grano");
            User usuario = userService.findByEmailWithRelations(authentication.getName());
            
            Long ingresoId = inventarioService.venderGrano(request, usuario);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Venta procesada exitosamente",
                "ingresoId", ingresoId
            ));
        } catch (RuntimeException e) {
            System.err.println("[INVENTARIO_CONTROLLER] Error en venta: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("[INVENTARIO_CONTROLLER] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }
}

