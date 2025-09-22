package com.agrocloud.controller;

import com.agrocloud.model.dto.CosechaDTO;
import com.agrocloud.model.dto.ComparacionRendimientoDTO;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.CosechaService;
import com.agrocloud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de cosechas
 */
@RestController
@RequestMapping("/api/cosechas")
@Tag(name = "Cosechas", description = "Endpoints para gestión de cosechas y comparación de rendimientos")
@CrossOrigin(origins = "*")
public class CosechaController {

    @Autowired
    private CosechaService cosechaService;

    @Autowired
    private UserService userService;

    /**
     * Crear una nueva cosecha
     */
    @PostMapping
    @Operation(summary = "Crear cosecha", description = "Registrar una nueva cosecha para un cultivo")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<CosechaDTO> crearCosecha(
            @Valid @RequestBody CosechaDTO cosechaDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            CosechaDTO cosechaCreada = cosechaService.crearCosecha(cosechaDTO, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(cosechaCreada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener todas las cosechas del usuario autenticado
     */
    @GetMapping
    @Operation(summary = "Listar cosechas", description = "Obtener todas las cosechas del usuario")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<List<CosechaDTO>> obtenerCosechas(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            List<CosechaDTO> cosechas = cosechaService.obtenerCosechasPorUsuario(usuario);
            return ResponseEntity.ok(cosechas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener cosechas por cultivo
     */
    @GetMapping("/cultivo/{cultivoId}")
    @Operation(summary = "Cosechas por cultivo", description = "Obtener todas las cosechas de un cultivo específico")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<List<CosechaDTO>> obtenerCosechasPorCultivo(
            @PathVariable Long cultivoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            List<CosechaDTO> cosechas = cosechaService.obtenerCosechasPorCultivo(cultivoId, usuario);
            return ResponseEntity.ok(cosechas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener cosecha por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cosecha", description = "Obtener una cosecha específica por su ID")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<CosechaDTO> obtenerCosecha(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            Optional<CosechaDTO> cosecha = cosechaService.obtenerCosechaPorId(id, usuario);
            return cosecha.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar una cosecha existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cosecha", description = "Modificar una cosecha existente")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<CosechaDTO> actualizarCosecha(
            @PathVariable Long id,
            @Valid @RequestBody CosechaDTO cosechaDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            CosechaDTO cosechaActualizada = cosechaService.actualizarCosecha(id, cosechaDTO, usuario);
            return ResponseEntity.ok(cosechaActualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar una cosecha
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cosecha", description = "Eliminar una cosecha específica")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<Void> eliminarCosecha(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            cosechaService.eliminarCosecha(id, usuario);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener comparación de rendimientos para un cultivo
     */
    @GetMapping("/cultivo/{cultivoId}/comparacion")
    @Operation(summary = "Comparación de rendimientos", description = "Obtener comparación entre rendimiento proyectado y real de un cultivo")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<ComparacionRendimientoDTO> obtenerComparacionRendimiento(
            @PathVariable Long cultivoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            ComparacionRendimientoDTO comparacion = cosechaService.obtenerComparacionRendimiento(cultivoId, usuario);
            return ResponseEntity.ok(comparacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener estadísticas de cosechas del usuario
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de cosechas", description = "Obtener estadísticas generales de cosechas del usuario")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<Object[]> obtenerEstadisticas(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            Object[] estadisticas = cosechaService.obtenerEstadisticasPorUsuario(usuario);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener cosechas por rango de fechas
     */
    @GetMapping("/fechas")
    @Operation(summary = "Cosechas por fechas", description = "Obtener cosechas en un rango de fechas específico")
    @PreAuthorize("hasAuthority('GESTION_COSECHAS')")
    public ResponseEntity<List<CosechaDTO>> obtenerCosechasPorFechas(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = userService.findByEmail(userDetails.getUsername());
            List<CosechaDTO> cosechas = cosechaService.obtenerCosechasPorRangoFechas(fechaInicio, fechaFin, usuario);
            return ResponseEntity.ok(cosechas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
