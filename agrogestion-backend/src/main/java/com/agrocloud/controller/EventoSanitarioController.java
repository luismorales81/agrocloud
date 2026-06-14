package com.agrocloud.controller;
import com.agrocloud.core.domain.User;

import com.agrocloud.porcinos.domain.EventoSanitario;
import com.agrocloud.porcinos.application.EventoSanitarioService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/porcinos/eventos-sanitarios")
public class EventoSanitarioController {

    @Autowired
    private EventoSanitarioService eventoSanitarioService;

    @Autowired
    private UserService userService;

    /**
     * Obtener todos los eventos sanitarios
     */
    @GetMapping
    public ResponseEntity<List<EventoSanitario>> obtenerEventosSanitarios(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            List<EventoSanitario> eventos = eventoSanitarioService.obtenerEventosSanitarios(user);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener eventos sanitarios por entidad
     */
    @GetMapping("/entidad/{tipoEntidad}/{entidadId}")
    public ResponseEntity<List<EventoSanitario>> obtenerEventosPorEntidad(
            @PathVariable String tipoEntidad,
            @PathVariable Long entidadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            EventoSanitario.TipoEntidad tipo = EventoSanitario.TipoEntidad.valueOf(tipoEntidad.toUpperCase());
            List<EventoSanitario> eventos = eventoSanitarioService.obtenerEventosPorEntidad(tipo, entidadId, user);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener eventos sanitarios por rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<List<EventoSanitario>> obtenerEventosPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            List<EventoSanitario> eventos = eventoSanitarioService.obtenerEventosPorRangoFechas(fechaInicio, fechaFin, user);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener un evento sanitario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoSanitario> obtenerEventoSanitarioPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Optional<EventoSanitario> evento = eventoSanitarioService.obtenerEventoSanitarioPorId(id, user);
            return evento.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear o actualizar evento sanitario
     */
    @PostMapping
    public ResponseEntity<?> guardarEventoSanitario(
            @RequestBody EventoSanitario evento,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            EventoSanitario guardado = eventoSanitarioService.guardarEventoSanitario(evento, user);
            return ResponseEntity.ok(guardado);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al guardar el evento sanitario: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Eliminar (desactivar) evento sanitario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEventoSanitario(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            eventoSanitarioService.eliminarEventoSanitario(id, user);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marcar retiro como cumplido
     */
    @PostMapping("/{id}/marcar-retiro-cumplido")
    public ResponseEntity<?> marcarRetiroCumplido(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            eventoSanitarioService.marcarRetiroCumplido(id, user);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener retiros vencidos
     */
    @GetMapping("/retiros/vencidos")
    public ResponseEntity<List<EventoSanitario>> obtenerRetirosVencidos(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            List<EventoSanitario> eventos = eventoSanitarioService.obtenerRetirosVencidos(user);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener retiros prÃ³ximos a vencer
     */
    @GetMapping("/retiros/proximos")
    public ResponseEntity<List<EventoSanitario>> obtenerRetirosProximos(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.core.domain.User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            List<EventoSanitario> eventos = eventoSanitarioService.obtenerRetirosProximosAVencer(user);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}


















