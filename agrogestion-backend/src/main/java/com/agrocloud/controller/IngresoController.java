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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de ingresos.
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

    private Optional<User> obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(userService.findByEmailWithAllRelations(userDetails.getUsername()));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    @GetMapping
    public ResponseEntity<List<Ingreso>> obtenerIngresos(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> usuario = obtenerUsuario(userDetails);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = usuario.get();
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
        return ResponseEntity.ok(ingresoRepository.findByUserIdOrderByFechaDesc(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingreso> obtenerIngresoPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> usuario = obtenerUsuario(userDetails);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        if (ingreso.isPresent() && ingreso.get().getUser().getId().equals(usuario.get().getId())) {
            return ResponseEntity.ok(ingreso.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Ingreso> crearIngreso(
            @Valid @RequestBody Ingreso ingreso,
            @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> usuario = obtenerUsuario(userDetails);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = usuario.get();

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

        return ResponseEntity.ok(ingresoRepository.save(ingreso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ingreso> actualizarIngreso(
            @PathVariable Long id,
            @Valid @RequestBody Ingreso ingreso,
            @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> usuario = obtenerUsuario(userDetails);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Ingreso> ingresoExistente = ingresoRepository.findById(id);
        if (ingresoExistente.isEmpty() || !ingresoExistente.get().getUser().getId().equals(usuario.get().getId())) {
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

        return ResponseEntity.ok(ingresoRepository.save(ingresoActual));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarIngreso(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> usuario = obtenerUsuario(userDetails);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        if (ingreso.isPresent() && ingreso.get().getUser().getId().equals(usuario.get().getId())) {
            ingresoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/tipo/{tipoIngreso}")
    public ResponseEntity<List<Ingreso>> obtenerIngresosPorTipo(@PathVariable Ingreso.TipoIngreso tipoIngreso) {
        return ResponseEntity.ok(ingresoRepository.findByTipoIngresoOrderByFechaDesc(tipoIngreso));
    }

    @GetMapping("/lote/{loteId}")
    public ResponseEntity<List<Ingreso>> obtenerIngresosPorLote(@PathVariable Long loteId) {
        return ResponseEntity.ok(ingresoRepository.findByLoteIdOrderByFechaDesc(loteId));
    }
}
