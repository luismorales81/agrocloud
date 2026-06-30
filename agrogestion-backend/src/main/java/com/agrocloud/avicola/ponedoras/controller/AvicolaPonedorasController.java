package com.agrocloud.avicola.ponedoras.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.avicola.ponedoras.model.dto.*;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasGalponEstado;
import com.agrocloud.avicola.ponedoras.service.ServicioAvicolaPonedorasAmbiente;
import com.agrocloud.avicola.ponedoras.service.ServicioAvicolaPonedorasGalpon;
import com.agrocloud.avicola.ponedoras.service.ServicioAvicolaPonedorasOperaciones;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * API REST avÃ­cola ponedoras. La empresa se resuelve en servicio vÃ­a {@link com.agrocloud.core.security.ServicioSeguridadContexto}.
 * <p>
 * Rutas (entidad central galpÃ³n, mÃ³dulo {@code AVICOLA_PONEDORAS}):
 * </p>
 * <ul>
 *   <li>{@code GET/POST /galpones}, {@code GET/PUT /galpones/{id}}</li>
 *   <li>{@code POST/GET /galpones/{id}/posturas|muertes|consumos|eventos-sanitarios|ventas-huevos|descarte-aves}</li>
 *   <li>{@code GET /galpones/{id}/resumen}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/avicola-ponedoras")
@RequiresModule("AVICOLA_PONEDORAS")
public class AvicolaPonedorasController {

    private final ServicioAvicolaPonedorasGalpon servicioGalpon;
    private final ServicioAvicolaPonedorasOperaciones servicioOperaciones;
    private final ServicioAvicolaPonedorasAmbiente servicioAmbiente;
    private final UserService userService;

    public AvicolaPonedorasController(
            ServicioAvicolaPonedorasGalpon servicioGalpon,
            ServicioAvicolaPonedorasOperaciones servicioOperaciones,
            ServicioAvicolaPonedorasAmbiente servicioAmbiente,
            UserService userService) {
        this.servicioGalpon = servicioGalpon;
        this.servicioOperaciones = servicioOperaciones;
        this.servicioAmbiente = servicioAmbiente;
        this.userService = userService;
    }

    private User requerirUsuario(UserDetails detalles) {
        if (detalles == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        User u = userService.findByEmailWithAllRelations(detalles.getUsername());
        if (u == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        return u;
    }

    @GetMapping("/galpones")
    public ResponseEntity<List<AvicolaPonedorasGalponRespuesta>> listarGalpones(
            @RequestParam(required = false) AvicolaPonedorasGalponEstado estado,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioGalpon.listarGalpones(estado, delPeriodoActivo));
    }

    @PostMapping("/galpones")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasGalponRespuesta> crearGalpon(
            @Valid @RequestBody AvicolaPonedorasGalponSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioGalpon.crearGalpon(solicitud));
    }

    @GetMapping("/galpones/{id}")
    public ResponseEntity<AvicolaPonedorasGalponRespuesta> obtenerGalpon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioGalpon.obtenerGalpon(id));
    }

    @PutMapping("/galpones/{id}")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasGalponRespuesta> actualizarGalpon(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasGalponSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioGalpon.actualizarGalpon(id, solicitud));
    }

    @GetMapping("/galpones/{id}/posturas")
    public ResponseEntity<List<AvicolaPonedorasPosturaRespuesta>> listarPosturas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarPosturas(id));
    }

    @PostMapping("/galpones/{id}/posturas")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasPosturaRespuesta> registrarPostura(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasPosturaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarPostura(id, solicitud));
    }

    @GetMapping("/galpones/{id}/muertes")
    public ResponseEntity<List<AvicolaPonedorasMuerteRespuesta>> listarMuertes(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarMuertes(id));
    }

    @PostMapping("/galpones/{id}/muertes")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasMuerteRespuesta> registrarMuerte(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasMuerteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarMuerte(id, solicitud));
    }

    @GetMapping("/galpones/{id}/consumos")
    public ResponseEntity<List<AvicolaPonedorasConsumoRespuesta>> listarConsumos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(id));
    }

    @PostMapping("/galpones/{id}/consumos")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasConsumoRespuesta> registrarConsumo(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarConsumo(id, solicitud));
    }

    @GetMapping("/galpones/{id}/eventos-sanitarios")
    public ResponseEntity<List<AvicolaPonedorasEventoSanitarioRespuesta>> listarEventosSanitarios(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(id));
    }

    @PostMapping("/galpones/{id}/eventos-sanitarios")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasEventoSanitarioRespuesta> registrarEventoSanitario(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarEventoSanitario(id, solicitud));
    }

    @GetMapping("/galpones/{id}/ventas-huevos")
    public ResponseEntity<List<AvicolaPonedorasVentaHuevosRespuesta>> listarVentasHuevos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentasHuevos(id));
    }

    @PostMapping("/galpones/{id}/ventas-huevos")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasVentaHuevosRespuesta> registrarVentaHuevos(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasVentaHuevosSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarVentaHuevos(id, solicitud));
    }

    @GetMapping("/galpones/{id}/descarte-aves")
    public ResponseEntity<List<AvicolaPonedorasDescarteAvesRespuesta>> listarDescarteAves(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarDescarteAves(id));
    }

    @PostMapping("/galpones/{id}/descarte-aves")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasDescarteAvesRespuesta> registrarDescarteAves(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasDescarteAvesSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarDescarteAves(id, solicitud));
    }

    @GetMapping("/galpones/{id}/resumen")
    public ResponseEntity<AvicolaPonedorasResumenRespuesta> resumenGalpon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.calcularResumen(id));
    }

    @GetMapping("/galpones/{id}/ambiente-diario")
    public ResponseEntity<List<AvicolaPonedorasAmbienteDiarioRespuesta>> listarAmbienteDiario(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioAmbiente.listarAmbienteDiario(id, desde, hasta));
    }

    @PutMapping("/galpones/{id}/ambiente-diario")
    @RequiresModule(value = "AVICOLA_PONEDORAS", permission = "write")
    public ResponseEntity<AvicolaPonedorasAmbienteDiarioRespuesta> guardarAmbienteDiario(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPonedorasAmbienteDiarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioAmbiente.guardarAmbienteDiario(id, solicitud));
    }
}
