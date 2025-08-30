package com.agrocloud.controller;

import com.agrocloud.model.entity.Insumo;
import com.agrocloud.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador público para la gestión de insumos (sin autenticación).
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/public/insumos")
@CrossOrigin(origins = "*")
public class PublicInsumoController {

    @Autowired
    private InsumoRepository insumoRepository;

    /**
     * Obtiene todos los insumos (público).
     */
    @GetMapping
    public ResponseEntity<List<Insumo>> obtenerInsumos() {
        try {
            List<Insumo> insumos = insumoRepository.findAll();
            return ResponseEntity.ok(insumos);
        } catch (Exception e) {
            System.err.println("Error en obtenerInsumos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Obtiene un insumo por ID (público).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Insumo> obtenerInsumoPorId(@PathVariable Long id) {
        try {
            return insumoRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en obtenerInsumoPorId: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea un nuevo insumo (público).
     */
    @PostMapping
    public ResponseEntity<Insumo> crearInsumo(@RequestBody Insumo insumo) {
        try {
            Insumo insumoGuardado = insumoRepository.save(insumo);
            return ResponseEntity.ok(insumoGuardado);
        } catch (Exception e) {
            System.err.println("Error en crearInsumo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualiza un insumo existente (público).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Insumo> actualizarInsumo(@PathVariable Long id, @RequestBody Insumo insumo) {
        try {
            return insumoRepository.findById(id)
                    .map(insumoExistente -> {
                        insumoExistente.setNombre(insumo.getNombre());
                        insumoExistente.setDescripcion(insumo.getDescripcion());
                        insumoExistente.setStockActual(insumo.getStockActual());
                        insumoExistente.setStockMinimo(insumo.getStockMinimo());
                        insumoExistente.setUnidad(insumo.getUnidad());
                        insumoExistente.setPrecioUnitario(insumo.getPrecioUnitario());
                        insumoExistente.setTipo(insumo.getTipo());
                        insumoExistente.setProveedor(insumo.getProveedor());
                        insumoExistente.setActivo(insumo.getActivo());
                        
                        return ResponseEntity.ok(insumoRepository.save(insumoExistente));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en actualizarInsumo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina un insumo (público).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInsumo(@PathVariable Long id) {
        try {
            if (insumoRepository.existsById(id)) {
                insumoRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error en eliminarInsumo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint de prueba.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("PublicInsumoController funcionando correctamente");
    }
}
