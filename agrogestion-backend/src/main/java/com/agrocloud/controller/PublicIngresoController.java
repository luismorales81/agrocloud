package com.agrocloud.controller;

import com.agrocloud.model.entity.Ingreso;
import com.agrocloud.repository.IngresoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador público para la gestión de ingresos (sin autenticación).
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/public/ingresos")
@CrossOrigin(origins = "*")
public class PublicIngresoController {

    @Autowired
    private IngresoRepository ingresoRepository;

    /**
     * Obtiene todos los ingresos (público).
     */
    @GetMapping
    public ResponseEntity<List<Ingreso>> obtenerIngresos() {
        try {
            List<Ingreso> ingresos = ingresoRepository.findAll();
            return ResponseEntity.ok(ingresos);
        } catch (Exception e) {
            System.err.println("Error en obtenerIngresos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtiene un ingreso por ID (público).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ingreso> obtenerIngresoPorId(@PathVariable Long id) {
        try {
            return ingresoRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en obtenerIngresoPorId: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea un nuevo ingreso (público).
     */
    @PostMapping
    public ResponseEntity<Ingreso> crearIngreso(@RequestBody Ingreso ingreso) {
        try {
            // TODO: Implementar autenticación real - por ahora usar usuario admin
            if (ingreso.getUser() == null) {
                com.agrocloud.model.entity.User usuario = new com.agrocloud.model.entity.User();
                usuario.setId(1L); // Admin por defecto hasta implementar autenticación
                ingreso.setUser(usuario);
            }
            
            Ingreso ingresoGuardado = ingresoRepository.save(ingreso);
            return ResponseEntity.ok(ingresoGuardado);
        } catch (Exception e) {
            System.err.println("Error en crearIngreso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualiza un ingreso existente (público).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Ingreso> actualizarIngreso(@PathVariable Long id, @RequestBody Ingreso ingreso) {
        try {
            return ingresoRepository.findById(id)
                    .map(ingresoExistente -> {
                        ingresoExistente.setConcepto(ingreso.getConcepto());
                        ingresoExistente.setDescripcion(ingreso.getDescripcion());
                        ingresoExistente.setTipoIngreso(ingreso.getTipoIngreso());
                        ingresoExistente.setFecha(ingreso.getFecha());
                        ingresoExistente.setMonto(ingreso.getMonto());
                        ingresoExistente.setUnidadMedida(ingreso.getUnidadMedida());
                        ingresoExistente.setCantidad(ingreso.getCantidad());
                        ingresoExistente.setClienteComprador(ingreso.getClienteComprador());

                        ingresoExistente.setObservaciones(ingreso.getObservaciones());
                        ingresoExistente.setLote(ingreso.getLote());
                        
                        return ResponseEntity.ok(ingresoRepository.save(ingresoExistente));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en actualizarIngreso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina un ingreso (público).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarIngreso(@PathVariable Long id) {
        try {
            if (ingresoRepository.existsById(id)) {
                ingresoRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error en eliminarIngreso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint de prueba.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("PublicIngresoController funcionando correctamente");
    }
}
