package com.agrocloud.controller;

import com.agrocloud.model.entity.Egreso;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.EgresoRepository;
import com.agrocloud.repository.InsumoRepository;
import com.agrocloud.repository.PlotRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.service.EgresoService;
import com.agrocloud.dto.CrearEgresoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de egresos.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/egresos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173"})
public class EgresoController {

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private EgresoService egresoService;

    /**
     * Obtiene todos los egresos del usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<List<Egreso>> obtenerEgresos(Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            List<Egreso> egresos = egresoRepository.findByUserIdAndFechaBetweenOrderByFechaDesc(
                    usuarioId, LocalDate.now().minusYears(1), LocalDate.now());
            return ResponseEntity.ok(egresos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene un egreso por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Egreso> obtenerEgresoPorId(@PathVariable Long id, Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            Optional<Egreso> egreso = egresoRepository.findById(id);
            
            if (egreso.isPresent() && egreso.get().getUser().getId().equals(usuarioId)) {
                return ResponseEntity.ok(egreso.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crea un nuevo egreso.
     */
    @PostMapping
    public ResponseEntity<Egreso> crearEgreso(@Valid @RequestBody Egreso egreso, Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            Optional<User> usuario = userRepository.findById(usuarioId);
            
            if (!usuario.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            egreso.setUser(usuario.get());

            // Si se especifica un lote, validar que existe
            if (egreso.getLote() != null && egreso.getLote().getId() != null) {
                Optional<Plot> lote = plotRepository.findById(egreso.getLote().getId());
                if (!lote.isPresent()) {
                    return ResponseEntity.badRequest().build();
                }
                egreso.setLote(lote.get());
            }

            // Si es un egreso de insumos, actualizar el stock del insumo
            if (egreso.getTipo() == Egreso.TipoEgreso.INSUMO && egreso.getReferenciaId() != null) {
                Optional<Insumo> insumo = insumoRepository.findById(egreso.getReferenciaId());
                if (insumo.isPresent()) {
                    Insumo insumoActual = insumo.get();
                    egreso.setReferenciaId(insumoActual.getId());
                    
                    // Actualizar la cantidad del insumo si se especifica
                    if (egreso.getCantidad() != null && egreso.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal nuevaCantidad = insumoActual.getStockActual().subtract(egreso.getCantidad());
                        // No permitir stock negativo
                        if (nuevaCantidad.compareTo(BigDecimal.ZERO) >= 0) {
                            insumoActual.setStockActual(nuevaCantidad);
                            insumoRepository.save(insumoActual);
                        } else {
                            // Si el stock sería negativo, establecer en 0
                            insumoActual.setStockActual(BigDecimal.ZERO);
                            insumoRepository.save(insumoActual);
                        }
                    }
                }
            }

            Egreso egresoGuardado = egresoRepository.save(egreso);
            return ResponseEntity.status(HttpStatus.CREATED).body(egresoGuardado);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crea un nuevo egreso con integración automática.
     */
    @PostMapping("/integrado")
    public ResponseEntity<Egreso> crearEgresoIntegrado(@Valid @RequestBody CrearEgresoRequest request, Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            request.setUserId(usuarioId);
            
            Egreso egreso = egresoService.crearEgreso(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(egreso);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualiza un egreso existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Egreso> actualizarEgreso(@PathVariable Long id, @Valid @RequestBody Egreso egresoActualizado, Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            Optional<Egreso> egresoExistente = egresoRepository.findById(id);
            
            if (!egresoExistente.isPresent() || !egresoExistente.get().getUser().getId().equals(usuarioId)) {
                return ResponseEntity.notFound().build();
            }

            Egreso egreso = egresoExistente.get();
            
            // Actualizar campos
            egreso.setTipo(egresoActualizado.getTipo());
            egreso.setReferenciaId(egresoActualizado.getReferenciaId());

            egreso.setFecha(egresoActualizado.getFecha());
            egreso.setCostoTotal(egresoActualizado.getCostoTotal());

            egreso.setCantidad(egresoActualizado.getCantidad());


            egreso.setObservaciones(egresoActualizado.getObservaciones());

            // Actualizar lote si se especifica
            if (egresoActualizado.getLote() != null && egresoActualizado.getLote().getId() != null) {
                Optional<Plot> lote = plotRepository.findById(egresoActualizado.getLote().getId());
                if (lote.isPresent()) {
                    egreso.setLote(lote.get());
                }
            }

            // Actualizar referencia si se especifica
            if (egresoActualizado.getReferenciaId() != null) {
                egreso.setReferenciaId(egresoActualizado.getReferenciaId());
            }

            Egreso egresoActualizadoGuardado = egresoRepository.save(egreso);
            return ResponseEntity.ok(egresoActualizadoGuardado);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Elimina un egreso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEgreso(@PathVariable Long id, Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            Optional<Egreso> egreso = egresoRepository.findById(id);
            
            if (!egreso.isPresent() || !egreso.get().getUser().getId().equals(usuarioId)) {
                return ResponseEntity.notFound().build();
            }

            egresoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene egresos por rango de fechas.
     */
    @GetMapping("/por-fechas")
    public ResponseEntity<List<Egreso>> obtenerEgresosPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            List<Egreso> egresos = egresoRepository.findByUserIdAndFechaBetweenOrderByFechaDesc(
                    usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(egresos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene egresos por tipo.
     */
    @GetMapping("/por-tipo/{tipoEgreso}")
    public ResponseEntity<List<Egreso>> obtenerEgresosPorTipo(
            @PathVariable Egreso.TipoEgreso tipoEgreso,
            Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            List<Egreso> egresos = egresoRepository.findByTipoAndUserIdOrderByFechaDesc(
                    tipoEgreso, usuarioId);
            return ResponseEntity.ok(egresos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene egresos por lote.
     */
    @GetMapping("/por-lote/{loteId}")
    public ResponseEntity<List<Egreso>> obtenerEgresosPorLote(
            @PathVariable Long loteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            List<Egreso> egresos = egresoRepository.findByLoteIdAndFechaBetweenOrderByFechaDesc(
                    loteId, fechaInicio, fechaFin);
            return ResponseEntity.ok(egresos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el total de egresos por usuario y rango de fechas.
     */
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> obtenerTotalEgresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        try {
            Long usuarioId = Long.parseLong(authentication.getName());
            BigDecimal total = egresoRepository.calcularTotalEgresosPorUsuarioYFecha(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
