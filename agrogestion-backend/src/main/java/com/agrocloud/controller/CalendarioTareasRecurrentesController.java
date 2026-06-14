package com.agrocloud.controller;

import com.agrocloud.core.application.CalendarioTareasRecurrentesService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.SerieTareaRecurrenteCalendario;
import com.agrocloud.core.domain.User;
import com.agrocloud.dto.calendario.ActualizarSerieTareaRecurrenteSolicitud;
import com.agrocloud.dto.calendario.CrearSerieTareaRecurrenteSolicitud;
import com.agrocloud.dto.calendario.MarcarCumplimientoOcurrenciaSolicitud;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendario/tareas-recurrentes")
public class CalendarioTareasRecurrentesController {

    private final CalendarioTareasRecurrentesService tareasRecurrentesService;
    private final UserService userService;

    public CalendarioTareasRecurrentesController(
            CalendarioTareasRecurrentesService tareasRecurrentesService,
            UserService userService) {
        this.tareasRecurrentesService = tareasRecurrentesService;
        this.userService = userService;
    }

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping
    public ResponseEntity<List<SerieTareaRecurrenteCalendario>> listar(@AuthenticationPrincipal UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tareasRecurrentesService.listarSeriesUsuario(user.getId()));
    }

    @PostMapping
    public ResponseEntity<SerieTareaRecurrenteCalendario> crear(
            @RequestBody CrearSerieTareaRecurrenteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(tareasRecurrentesService.crear(user, solicitud));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SerieTareaRecurrenteCalendario> actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarSerieTareaRecurrenteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(tareasRecurrentesService.actualizar(id, user.getId(), solicitud));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            tareasRecurrentesService.eliminarSerie(id, user.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/cumplimiento")
    public ResponseEntity<Map<String, Object>> marcarCumplimiento(
            @PathVariable Long id,
            @RequestParam LocalDate fecha,
            @RequestBody MarcarCumplimientoOcurrenciaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            tareasRecurrentesService.marcarCumplimiento(id, user.getId(), fecha, solicitud.isCumplida());
            return ResponseEntity.ok(Map.of(
                    "serieId", id,
                    "fecha", fecha.toString(),
                    "cumplida", solicitud.isCumplida()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
