package com.agrocloud.controller;

import com.agrocloud.config.CampanaRequestContext;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.Campana;
import com.agrocloud.core.domain.Ingreso;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.IngresoRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class IngresoController {

    @Autowired
    @Qualifier("ingresoRepositoryCore")
        private IngresoRepository ingresoRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;

    @Autowired
    private ServicioSeguridadContexto servicioSeguridadContexto;

    @Autowired
    @Qualifier("campanaContextServiceCore")
    private CampanaContextService campanaContextService;

    /**
     * Obtiene todos los ingresos del usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<List<Ingreso>> obtenerIngresos(Authentication authentication) {
        User user = userService.findByEmailWithAllRelations(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        Long campanaId = CampanaRequestContext.getCampanaId();
        if (campanaId != null) {
            try {
                Campana campana = campanaContextService.resolverCampanaActiva(servicioSeguridadContexto.obtenerEmpresaIdActual());
                return ResponseEntity.ok(ingresoRepository.findByCampanaIdAndFechaBetweenOrderByFechaDesc(
                        campana.getId(), campana.getFechaInicio(), campana.getFechaFin()));
            } catch (Exception e) {
                return ResponseEntity.ok(ingresoRepository.findByUserIdOrderByFechaDesc(user.getId()));
            }
        }
        List<Ingreso> ingresos = ingresoRepository.findByUserIdOrderByFechaDesc(user.getId());
        return ResponseEntity.ok(ingresos);
    }

    /**
     * Obtiene un ingreso por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ingreso> obtenerIngresoPorId(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = Long.parseLong(authentication.getName());
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        
        if (ingreso.isPresent() && ingreso.get().getUser().getId().equals(usuarioId)) {
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
        User user = userService.findByEmailWithAllRelations(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validar que el lote pertenece al usuario si se especifica
        if (ingreso.getLote() != null && ingreso.getLote().getId() != null) {
            Optional<com.agrocloud.cultivos.domain.Plot> lote = plotRepository.findById(ingreso.getLote().getId());
            if (lote.isEmpty() || !lote.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        ingreso.setUser(user);
        try {
            Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
            ingreso.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        } catch (Exception ignored) {
        }
        
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
        
        if (ingresoExistente.isEmpty() || !ingresoExistente.get().getUser().getId().equals(usuarioId)) {
            return ResponseEntity.notFound().build();
        }
        
        Ingreso ingresoActual = ingresoExistente.get();
        ingresoActual.setConcepto(ingreso.getConcepto());
        ingresoActual.setDescripcion(ingreso.getDescripcion());
        ingresoActual.setTipoIngreso(ingreso.getTipoIngreso());
        ingresoActual.setFecha(ingreso.getFecha());
        ingresoActual.setMonto(ingreso.getMonto());
        ingresoActual.setUnidadMedida(ingreso.getUnidadMedida());
        ingresoActual.setCantidad(ingreso.getCantidad());
        ingresoActual.setClienteComprador(ingreso.getClienteComprador());

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
        
        if (ingreso.isPresent() && ingreso.get().getUser().getId().equals(usuarioId)) {
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
        List<Ingreso> ingresos = ingresoRepository.findByTipoIngresoOrderByFechaDesc(tipoIngreso);
        return ResponseEntity.ok(ingresos);
    }

    /**
     * Obtiene ingresos por lote.
     */
    @GetMapping("/lote/{loteId}")
    public ResponseEntity<List<Ingreso>> obtenerIngresosPorLote(@PathVariable Long loteId) {
        List<Ingreso> ingresos = ingresoRepository.findByLoteIdOrderByFechaDesc(loteId);
        return ResponseEntity.ok(ingresos);
    }
}
