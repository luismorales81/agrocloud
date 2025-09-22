package com.agrocloud.controller;

import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.dto.InsumoDTO;
import com.agrocloud.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<InsumoDTO>> obtenerInsumos() {
        try {
            System.out.println("🔍 [INSUMOS] Iniciando obtenerInsumos...");
            System.out.println("🔍 [INSUMOS] Verificando repositorio...");
            
            // Verificar si el repositorio está disponible
            if (insumoRepository == null) {
                System.err.println("❌ [INSUMOS] InsumoRepository es null");
                return ResponseEntity.status(500).body(null);
            }
            
            System.out.println("✅ [INSUMOS] Repositorio disponible");
            System.out.println("🔍 [INSUMOS] Verificando conexión a base de datos...");
            
            try {
                // Verificar conexión a base de datos
                long count = insumoRepository.count();
                System.out.println("✅ [INSUMOS] Conexión a BD OK. Total insumos en BD: " + count);
            } catch (Exception dbError) {
                System.err.println("❌ [INSUMOS] Error conectando a BD: " + dbError.getMessage());
                dbError.printStackTrace();
                return ResponseEntity.status(500).body(null);
            }
            
            System.out.println("📊 [INSUMOS] Ejecutando findAll()...");
            List<Insumo> insumos = insumoRepository.findAll();
            System.out.println("✅ [INSUMOS] findAll() ejecutado exitosamente");
            System.out.println("📊 [INSUMOS] Insumos obtenidos: " + insumos.size());
            
            if (insumos.isEmpty()) {
                System.out.println("ℹ️ [INSUMOS] Lista de insumos está vacía");
            } else {
                System.out.println("📋 [INSUMOS] Primer insumo: " + insumos.get(0).getNombre());
            }
            
            // Convertir entidades a DTOs para evitar referencias circulares
            List<InsumoDTO> insumosDTO = insumos.stream()
                .map(InsumoDTO::new)
                .collect(Collectors.toList());
            
            System.out.println("✅ [INSUMOS] Conversión a DTOs completada: " + insumosDTO.size());
            
            return ResponseEntity.ok(insumosDTO);
        } catch (Exception e) {
            System.err.println("❌ [INSUMOS] Error en obtenerInsumos: " + e.getMessage());
            System.err.println("❌ [INSUMOS] Tipo de error: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtiene un insumo por ID (público).
     */
    @GetMapping("/{id}")
    public ResponseEntity<InsumoDTO> obtenerInsumoPorId(@PathVariable Long id) {
        try {
            System.out.println("🔍 [INSUMOS] Obteniendo insumo con ID: " + id);
            Insumo insumo = insumoRepository.findById(id).orElse(null);
            
            if (insumo == null) {
                System.out.println("❌ [INSUMOS] Insumo no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("✅ [INSUMOS] Insumo encontrado: " + insumo.getNombre());
            InsumoDTO insumoDTO = new InsumoDTO(insumo);
            return ResponseEntity.ok(insumoDTO);
        } catch (Exception e) {
            System.err.println("❌ [INSUMOS] Error obteniendo insumo por ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Crea un nuevo insumo (público).
     */
    @PostMapping
    public ResponseEntity<InsumoDTO> crearInsumo(@RequestBody Insumo insumo) {
        try {
            System.out.println("🔍 [INSUMOS] Creando nuevo insumo: " + insumo.getNombre());
            Insumo insumoGuardado = insumoRepository.save(insumo);
            System.out.println("✅ [INSUMOS] Insumo creado con ID: " + insumoGuardado.getId());
            
            InsumoDTO insumoDTO = new InsumoDTO(insumoGuardado);
            return ResponseEntity.ok(insumoDTO);
        } catch (Exception e) {
            System.err.println("❌ [INSUMOS] Error creando insumo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Actualiza un insumo existente (público).
     */
    @PutMapping("/{id}")
    public ResponseEntity<InsumoDTO> actualizarInsumo(@PathVariable Long id, @RequestBody Insumo insumo) {
        try {
            System.out.println("🔍 [INSUMOS] Actualizando insumo con ID: " + id);
            
            if (!insumoRepository.existsById(id)) {
                System.out.println("❌ [INSUMOS] Insumo no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            insumo.setId(id);
            Insumo insumoActualizado = insumoRepository.save(insumo);
            System.out.println("✅ [INSUMOS] Insumo actualizado: " + insumoActualizado.getNombre());
            
            InsumoDTO insumoDTO = new InsumoDTO(insumoActualizado);
            return ResponseEntity.ok(insumoDTO);
        } catch (Exception e) {
            System.err.println("❌ [INSUMOS] Error actualizando insumo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Elimina un insumo (público).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInsumo(@PathVariable Long id) {
        try {
            System.out.println("🔍 [INSUMOS] Eliminando insumo con ID: " + id);
            
            if (!insumoRepository.existsById(id)) {
                System.out.println("❌ [INSUMOS] Insumo no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            insumoRepository.deleteById(id);
            System.out.println("✅ [INSUMOS] Insumo eliminado con ID: " + id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("❌ [INSUMOS] Error eliminando insumo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint de prueba.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        try {
            System.out.println("🧪 Endpoint de prueba ejecutándose...");
            
            // Verificar repositorio
            if (insumoRepository == null) {
                return ResponseEntity.status(500).body("❌ InsumoRepository es null");
            }
            
            // Verificar conexión a base de datos
            try {
                long count = insumoRepository.count();
                return ResponseEntity.ok("✅ PublicInsumoController funcionando correctamente. Total insumos: " + count);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("❌ Error conectando a base de datos: " + e.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error general: " + e.getMessage());
        }
    }
}
