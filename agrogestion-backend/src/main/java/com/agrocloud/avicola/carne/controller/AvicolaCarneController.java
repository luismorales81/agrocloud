package com.agrocloud.avicola.carne.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.avicola.carne.model.dto.AvicolaCarneResumenRespuesta;
import com.agrocloud.avicola.carne.service.ServicioAvicolaCarneLotes;
import com.agrocloud.avicola.carne.service.ServicioAvicolaCarneOperaciones;
import com.agrocloud.avicola.crianza.model.dto.*;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST avÃ­cola-carne. La empresa se resuelve en servicio vÃ­a {@link com.agrocloud.core.security.ServicioSeguridadContexto}.
 * <p>
 * Cobertura frente a SPEC de rutas (equivalente a crianza, mÃ³dulo {@code AVICOLA_CARNE}):
 * </p>
 * <ul>
 *   <li>{@code GET/POST /lotes}, {@code GET/PUT /lotes/{id}}</li>
 *   <li>{@code POST/GET /lotes/{id}/pesadas|muertes|ventas|consumos|eventos-sanitarios}</li>
 *   <li>{@code GET /lotes/{id}/resumen}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/avicola-carne")
@RequiresModule("AVICOLA_CARNE")
public class AvicolaCarneController {

    private final ServicioAvicolaCarneLotes servicioLotes;
    private final ServicioAvicolaCarneOperaciones servicioOperaciones;
    private final UserService userService;

    public AvicolaCarneController(
            ServicioAvicolaCarneLotes servicioLotes,
            ServicioAvicolaCarneOperaciones servicioOperaciones,
            UserService userService) {
        this.servicioLotes = servicioLotes;
        this.servicioOperaciones = servicioOperaciones;
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

    @GetMapping("/lotes")
    public ResponseEntity<List<AvicolaLoteRespuesta>> listarLotes(
            @RequestParam(required = false) AvicolaLoteEstado estado,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.listarLotes(estado));
    }

    @PostMapping("/lotes")
    @RequiresModule(value = "AVICOLA_CARNE", permission = "write")
    public ResponseEntity<AvicolaLoteRespuesta> crearLote(
            @Valid @RequestBody AvicolaLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioLotes.crearLote(solicitud));
    }

    @GetMapping("/lotes/{id}")
    public ResponseEntity<AvicolaLoteRespuesta> obtenerLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.obtenerLote(id));
    }

    @PutMapping("/lotes/{id}")
    @RequiresModule(value = "AVICOLA_CARNE", permission = "write")
    public ResponseEntity<AvicolaLoteRespuesta> actualizarLote(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.actualizarLote(id, solicitud));
    }

    @GetMapping("/lotes/{id}/pesadas")
    public ResponseEntity<List<AvicolaPesadaRespuesta>> listarPesadas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarPesadas(id));
    }

    @PostMapping("/lotes/{id}/pesadas")
    @RequiresModule(value = "AVICOLA_CARNE", permission = "write")
    public ResponseEntity<AvicolaPesadaRespuesta> registrarPesada(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaPesadaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarPesada(id, solicitud));
    }

    @GetMapping("/lotes/{id}/muertes")
    public ResponseEntity<List<AvicolaMuerteRespuesta>> listarMuertes(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarMuertes(id));
    }

    @PostMapping("/lotes/{id}/muertes")
    @RequiresModule(value = "AVICOLA_CARNE", permission = "write")
    public ResponseEntity<AvicolaMuerteRespuesta> registrarMuerte(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaMuerteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarMuerte(id, solicitud));
    }

    @GetMapping("/lotes/{id}/ventas")
    public ResponseEntity<List<AvicolaVentaRespuesta>> listarVentas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentas(id));
    }

    @PostMapping("/lotes/{id}/ventas")
    @RequiresModule(value = "AVICOLA_CARNE", permission = "write")
    public ResponseEntity<AvicolaVentaRespuesta> registrarVenta(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaVentaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarVenta(id, solicitud));
    }

    @GetMapping("/lotes/{id}/consumos")
    public ResponseEntity<List<AvicolaConsumoRespuesta>> listarConsumos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(id));
    }

    @PostMapping("/lotes/{id}/consumos")
    @RequiresModule(value = "AVICOLA_CARNE", permission = "write")
    public ResponseEntity<AvicolaConsumoRespuesta> registrarConsumo(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarConsumo(id, solicitud));
    }

    @GetMapping("/lotes/{id}/eventos-sanitarios")
    public ResponseEntity<List<AvicolaEventoSanitarioRespuesta>> listarEventosSanitarios(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(id));
    }

    @PostMapping("/lotes/{id}/eventos-sanitarios")
    @RequiresModule(value = "AVICOLA_CARNE", permission = "write")
    public ResponseEntity<AvicolaEventoSanitarioRespuesta> registrarEventoSanitario(
            @PathVariable Long id,
            @Valid @RequestBody AvicolaEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarEventoSanitario(id, solicitud));
    }

    @GetMapping("/lotes/{id}/resumen")
    public ResponseEntity<AvicolaCarneResumenRespuesta> resumenLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.calcularResumen(id));
    }
}
