package com.agrocloud.controller;

import com.agrocloud.model.entity.Ingreso;
import com.agrocloud.repository.IngresoRepository;
import com.agrocloud.repository.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de ingresos.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/ingresos")
@CrossOrigin(origins = "*")
public class IngresoController {

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private PlotRepository plotRepository;

    /**
     * Obtiene todos los ingresos del usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<List<Ingreso>> obtenerIngresos(Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        List<Ingreso> ingresos = ingresoRepository.findByUsuarioIdOrderByFechaIngresoDesc(usuarioId);
        return ResponseEntity.ok(ingresos);
    }

    /**
     * Obtiene un ingreso por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ingreso> obtenerIngresoPorId(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        
        if (ingreso.isPresent() && ingreso.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.ok(ingreso.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea un nuevo ingreso.
     */
    @PostMapping
    public ResponseEntity<Ingreso> crearIngreso(@Valid @RequestBody Ingreso ingreso, Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        
        // Validar que el lote pertenece al usuario si se especifica
        if (ingreso.getLote() != null && ingreso.getLote().getId() != null) {
            Optional<com.agrocloud.model.entity.Plot> lote = plotRepository.findById(ingreso.getLote().getId());
            if (lote.isEmpty() || !lote.get().getUser().getId().equals(usuarioId)) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        // Establecer el usuario
        com.agrocloud.model.entity.User usuario = new com.agrocloud.model.entity.User();
        usuario.setId(usuarioId);
        ingreso.setUsuario(usuario);
        
        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);
        return ResponseEntity.ok(ingresoGuardado);
    }

    /**
     * Actualiza un ingreso existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Ingreso> actualizarIngreso(@PathVariable Long id, @Valid @RequestBody Ingreso ingreso, Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        Optional<Ingreso> ingresoExistente = ingresoRepository.findById(id);
        
        if (ingresoExistente.isEmpty() || !ingresoExistente.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.notFound().build();
        }
        
        Ingreso ingresoActual = ingresoExistente.get();
        ingresoActual.setConcepto(ingreso.getConcepto());
        ingresoActual.setDescripcion(ingreso.getDescripcion());
        ingresoActual.setTipoIngreso(ingreso.getTipoIngreso());
        ingresoActual.setFechaIngreso(ingreso.getFechaIngreso());
        ingresoActual.setMonto(ingreso.getMonto());
        ingresoActual.setUnidadMedida(ingreso.getUnidadMedida());
        ingresoActual.setCantidad(ingreso.getCantidad());
        ingresoActual.setClienteComprador(ingreso.getClienteComprador());
        ingresoActual.setEstado(ingreso.getEstado());
        ingresoActual.setObservaciones(ingreso.getObservaciones());
        ingresoActual.setLote(ingreso.getLote());
        
        Ingreso ingresoActualizado = ingresoRepository.save(ingresoActual);
        return ResponseEntity.ok(ingresoActualizado);
    }

    /**
     * Elimina un ingreso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarIngreso(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        
        if (ingreso.isPresent() && ingreso.get().getUsuario().getId().equals(usuarioId)) {
            ingresoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene ingresos por tipo.
     */
    @GetMapping("/tipo/{tipoIngreso}")
    public ResponseEntity<List<Ingreso>> obtenerIngresosPorTipo(@PathVariable Ingreso.TipoIngreso tipoIngreso) {
        List<Ingreso> ingresos = ingresoRepository.findByTipoIngresoOrderByFechaIngresoDesc(tipoIngreso);
        return ResponseEntity.ok(ingresos);
    }

    /**
     * Obtiene ingresos por lote.
     */
    @GetMapping("/lote/{loteId}")
    public ResponseEntity<List<Ingreso>> obtenerIngresosPorLote(@PathVariable Long loteId) {
        List<Ingreso> ingresos = ingresoRepository.findByLoteIdOrderByFechaIngresoDesc(loteId);
        return ResponseEntity.ok(ingresos);
    }
}
