package com.agrocloud.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaCatalogo;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaLote;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaOperaciones;
import com.agrocloud.avicola.crianza.model.dto.*;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST del mÃ³dulo avÃ­cola crianza (catÃ¡logo, lotes y operaciones).
 */
@RestController
@RequestMapping("/api/avicola-crianza")
@RequiresModule("AVICOLA_CRIANZA")
public class AvicolaCrianzaController {

    private final ServicioAvicolaCrianzaCatalogo servicioCatalogo;
    private final ServicioAvicolaCrianzaLote servicioLote;
    private final ServicioAvicolaCrianzaOperaciones servicioOperaciones;
    private final UserService userService;

    public AvicolaCrianzaController(
            ServicioAvicolaCrianzaCatalogo servicioCatalogo,
            ServicioAvicolaCrianzaLote servicioLote,
            ServicioAvicolaCrianzaOperaciones servicioOperaciones,
            UserService userService) {
        this.servicioCatalogo = servicioCatalogo;
        this.servicioLote = servicioLote;
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

    // --- CatÃ¡logo ---

    @GetMapping("/establecimientos")
    public ResponseEntity<List<AvicolaEstablecimientoRespuesta>> listarEstablecimientos(
            @RequestHeader("X-Company-Id") Long empresaId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarEstablecimientos(empresaId));
    }

    @PostMapping("/establecimientos")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaEstablecimientoRespuesta> crearEstablecimiento(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestBody AvicolaEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearEstablecimiento(empresaId, solicitud));
    }

    @PutMapping("/establecimientos/{id}")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaEstablecimientoRespuesta> actualizarEstablecimiento(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long id,
            @RequestBody AvicolaEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarEstablecimiento(empresaId, id, solicitud));
    }

    @GetMapping("/razas")
    public ResponseEntity<List<AvicolaRazaRespuesta>> listarRazas(
            @RequestHeader("X-Company-Id") Long empresaId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarRazas(empresaId));
    }

    @PostMapping("/razas")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaRazaRespuesta> crearRaza(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestBody AvicolaRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearRaza(empresaId, solicitud));
    }

    @PutMapping("/razas/{id}")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaRazaRespuesta> actualizarRaza(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long id,
            @RequestBody AvicolaRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarRaza(empresaId, id, solicitud));
    }

    // --- Lotes ---

    @GetMapping("/lotes")
    public ResponseEntity<List<AvicolaLoteRespuesta>> listarLotes(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestParam(required = false) AvicolaLoteEstado estado,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.listarLotes(empresaId, estado, delPeriodoActivo));
    }

    @GetMapping("/lotes/{loteId}")
    public ResponseEntity<AvicolaLoteRespuesta> obtenerLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.obtenerLote(empresaId, loteId));
    }

    @PostMapping("/lotes")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaLoteRespuesta> crearLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestBody AvicolaLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.crearLote(empresaId, solicitud));
    }

    @PutMapping("/lotes/{loteId}")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaLoteRespuesta> actualizarLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.actualizarLote(empresaId, loteId, solicitud));
    }

    // --- Operaciones por lote ---

    @GetMapping("/lotes/{loteId}/pesadas")
    public ResponseEntity<List<AvicolaPesadaRespuesta>> listarPesadas(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarPesadas(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/pesadas")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaPesadaRespuesta> registrarPesada(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaPesadaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarPesada(empresaId, loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/muertes")
    public ResponseEntity<List<AvicolaMuerteRespuesta>> listarMuertes(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarMuertes(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/muertes")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaMuerteRespuesta> registrarMuerte(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaMuerteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarMuerte(empresaId, loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/ventas")
    public ResponseEntity<List<AvicolaVentaRespuesta>> listarVentas(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentas(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/ventas")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaVentaRespuesta> registrarVenta(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaVentaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarVenta(empresaId, loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/consumos")
    public ResponseEntity<List<AvicolaConsumoRespuesta>> listarConsumos(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/consumos")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaConsumoRespuesta> registrarConsumo(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarConsumo(empresaId, loteId, solicitud, usuario.getId()));
    }

    @GetMapping("/lotes/{loteId}/eventos-sanitarios")
    public ResponseEntity<List<AvicolaEventoSanitarioRespuesta>> listarEventosSanitarios(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/eventos-sanitarios")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaEventoSanitarioRespuesta> registrarEventoSanitario(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarEventoSanitario(empresaId, loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/resumen")
    public ResponseEntity<AvicolaCrianzaResumenRespuesta> resumenLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.resumenLote(empresaId, loteId));
    }
}
