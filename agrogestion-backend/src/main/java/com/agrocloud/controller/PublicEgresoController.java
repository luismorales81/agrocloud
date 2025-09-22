package com.agrocloud.controller;

import com.agrocloud.model.entity.Egreso;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.repository.EgresoRepository;
import com.agrocloud.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

/**
 * Controlador público para la gestión de egresos (sin autenticación).
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/public/egresos")
@CrossOrigin(origins = "*")
public class PublicEgresoController {

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    /**
     * Obtiene todos los egresos (público).
     */
    @GetMapping
    public ResponseEntity<List<Egreso>> obtenerEgresos() {
        try {
            List<Egreso> egresos = egresoRepository.findAll();
            return ResponseEntity.ok(egresos);
        } catch (Exception e) {
            System.err.println("Error en obtenerEgresos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtiene un egreso por ID (público).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Egreso> obtenerEgresoPorId(@PathVariable Long id) {
        try {
            return egresoRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en obtenerEgresoPorId: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea un nuevo egreso (público).
     */
    @PostMapping
    public ResponseEntity<Egreso> crearEgreso(@RequestBody Egreso egreso) {
        try {
            // TODO: Implementar autenticación real - por ahora usar usuario admin
            if (egreso.getUser() == null) {
                com.agrocloud.model.entity.User usuario = new com.agrocloud.model.entity.User();
                usuario.setId(1L); // Admin por defecto hasta implementar autenticación
                egreso.setUser(usuario);
            }
            
            // Si es un egreso de insumos, actualizar el stock
            if (egreso.getTipo() == Egreso.TipoEgreso.INSUMO && 
                egreso.getReferenciaId() != null) {
                
                insumoRepository.findById(egreso.getReferenciaId()).ifPresent(insumo -> {
                    BigDecimal nuevoStock = insumo.getStockActual().subtract(egreso.getCantidad());
                    if (nuevoStock.compareTo(BigDecimal.ZERO) >= 0) {
                        insumo.setStockActual(nuevoStock);
                        insumoRepository.save(insumo);
                    }
                });
            }
            
            Egreso egresoGuardado = egresoRepository.save(egreso);
            return ResponseEntity.ok(egresoGuardado);
        } catch (Exception e) {
            System.err.println("Error en crearEgreso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualiza un egreso existente (público).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Egreso> actualizarEgreso(@PathVariable Long id, @RequestBody Egreso egreso) {
        try {
            return egresoRepository.findById(id)
                    .map(egresoExistente -> {
                        egresoExistente.setTipo(egreso.getTipo());
                        egresoExistente.setReferenciaId(egreso.getReferenciaId());

                        egresoExistente.setFecha(egreso.getFecha());
                        egresoExistente.setCostoTotal(egreso.getCostoTotal());

                        egresoExistente.setCantidad(egreso.getCantidad());


                        egresoExistente.setObservaciones(egreso.getObservaciones());
                        egresoExistente.setLote(egreso.getLote());
                        egresoExistente.setReferenciaId(egreso.getReferenciaId());
                        
                        return ResponseEntity.ok(egresoRepository.save(egresoExistente));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en actualizarEgreso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina un egreso (público).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEgreso(@PathVariable Long id) {
        try {
            if (egresoRepository.existsById(id)) {
                egresoRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error en eliminarEgreso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint de prueba.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("PublicEgresoController funcionando correctamente");
    }
}
