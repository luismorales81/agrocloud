package com.agrocloud.controller;

import com.agrocloud.dto.DosisAgroquimicoRequest;
import com.agrocloud.dto.DosisAgroquimicoResponse;
import com.agrocloud.service.DosisAgroquimicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de dosis de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/dosis-agroquimicos")
@CrossOrigin(origins = "*")
public class DosisAgroquimicoController {

    @Autowired
    private DosisAgroquimicoService dosisAgroquimicoService;

    /**
     * Crear nueva dosis para un agroquímico
     */
    @PostMapping
    public ResponseEntity<?> crearDosis(@RequestBody DosisAgroquimicoRequest request) {
        try {
            System.out.println("[DOSIS_AGROQUIMICO_CONTROLLER] Creando dosis para insumo: " + request.getInsumoId());
            DosisAgroquimicoResponse response = dosisAgroquimicoService.crear(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[DOSIS_AGROQUIMICO_CONTROLLER] ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtener dosis por insumo
     */
    @GetMapping("/insumo/{insumoId}")
    public ResponseEntity<List<DosisAgroquimicoResponse>> obtenerDosisPorInsumo(@PathVariable Long insumoId) {
        try {
            List<DosisAgroquimicoResponse> dosis = dosisAgroquimicoService.obtenerPorInsumo(insumoId);
            return ResponseEntity.ok(dosis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar todas las dosis de un insumo
     */
    @DeleteMapping("/insumo/{insumoId}")
    public ResponseEntity<Void> eliminarDosisPorInsumo(@PathVariable Long insumoId) {
        try {
            dosisAgroquimicoService.eliminarPorInsumo(insumoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar dosis específica
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDosis(@PathVariable Long id) {
        try {
            dosisAgroquimicoService.eliminar(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}