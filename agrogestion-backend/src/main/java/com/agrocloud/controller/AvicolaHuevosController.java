package com.agrocloud.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.avicola.huevos.application.ServicioAvicolaHuevosCatalogo;
import com.agrocloud.avicola.huevos.application.ServicioAvicolaHuevosLote;
import com.agrocloud.avicola.huevos.application.ServicioAvicolaHuevosOperaciones;
import com.agrocloud.avicola.huevos.application.ServicioAvicolaHuevosReportes;
import com.agrocloud.avicola.huevos.model.dto.*;
import com.agrocloud.avicola.huevos.model.enums.AvicolaHuevoLoteEstado;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * API REST del mÃ³dulo avÃ­cola producciÃ³n de huevos.
 */
@RestController
@RequestMapping("/api/avicola-huevos")
@RequiresModule("AVICOLA_HUEVOS")
public class AvicolaHuevosController {

    private final ServicioAvicolaHuevosCatalogo servicioCatalogo;
    private final ServicioAvicolaHuevosLote servicioLote;
    private final ServicioAvicolaHuevosOperaciones servicioOperaciones;
    private final ServicioAvicolaHuevosReportes servicioReportes;
    private final UserService userService;

    public AvicolaHuevosController(
            ServicioAvicolaHuevosCatalogo servicioCatalogo,
            ServicioAvicolaHuevosLote servicioLote,
            ServicioAvicolaHuevosOperaciones servicioOperaciones,
            ServicioAvicolaHuevosReportes servicioReportes,
            UserService userService) {
        this.servicioCatalogo = servicioCatalogo;
        this.servicioLote = servicioLote;
        this.servicioOperaciones = servicioOperaciones;
        this.servicioReportes = servicioReportes;
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

    @GetMapping("/establecimientos")
    public ResponseEntity<List<AvicolaHuevoEstablecimientoRespuesta>> listarEstablecimientos(
            @RequestHeader("X-Company-Id") Long empresaId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarEstablecimientos(empresaId));
    }

    @PostMapping("/establecimientos")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoEstablecimientoRespuesta> crearEstablecimiento(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestBody AvicolaHuevoEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearEstablecimiento(empresaId, solicitud));
    }

    @PutMapping("/establecimientos/{id}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoEstablecimientoRespuesta> actualizarEstablecimiento(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long id,
            @RequestBody AvicolaHuevoEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarEstablecimiento(empresaId, id, solicitud));
    }

    @GetMapping("/razas")
    public ResponseEntity<List<AvicolaHuevoRazaRespuesta>> listarRazas(
            @RequestHeader("X-Company-Id") Long empresaId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarRazas(empresaId));
    }

    @PostMapping("/razas")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoRazaRespuesta> crearRaza(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestBody AvicolaHuevoRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearRaza(empresaId, solicitud));
    }

    @PutMapping("/razas/{id}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoRazaRespuesta> actualizarRaza(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long id,
            @RequestBody AvicolaHuevoRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarRaza(empresaId, id, solicitud));
    }

    @GetMapping("/lotes")
    public ResponseEntity<List<AvicolaHuevoLoteRespuesta>> listarLotes(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestParam(required = false) AvicolaHuevoLoteEstado estado,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.listarLotes(empresaId, estado, delPeriodoActivo));
    }

    @GetMapping("/lotes/{loteId}")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> obtenerLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.obtenerLote(empresaId, loteId));
    }

    @PostMapping("/lotes")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> crearLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestBody AvicolaHuevoLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.crearLote(empresaId, solicitud));
    }

    @PutMapping("/lotes/{loteId}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> actualizarLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.actualizarLote(empresaId, loteId, solicitud));
    }

    @PostMapping("/lotes/{loteId}/cierre")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> cerrarLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.cerrarLote(empresaId, loteId));
    }

    @GetMapping("/lotes/{loteId}/produccion-diaria")
    public ResponseEntity<List<AvicolaHuevoProduccionDiariaRespuesta>> listarProduccionDiaria(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarProduccionDiaria(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/produccion-diaria")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoProduccionDiariaRespuesta> registrarProduccionDiaria(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoProduccionDiariaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarProduccionDiaria(empresaId, loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/consumos")
    public ResponseEntity<List<AvicolaHuevoConsumoRespuesta>> listarConsumos(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/consumos")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoConsumoRespuesta> registrarConsumo(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarConsumo(empresaId, loteId, solicitud, usuario.getId()));
    }

    @PutMapping("/lotes/{loteId}/consumos/{consumoId}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoConsumoRespuesta> actualizarConsumo(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @PathVariable Long consumoId,
            @RequestBody AvicolaHuevoConsumoActualizarSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(
                servicioOperaciones.actualizarConsumo(empresaId, loteId, consumoId, solicitud, usuario.getId()));
    }

    @PostMapping("/lotes/{loteId}/ajuste-plantel")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> registrarAjustePlantel(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoAjustePlantelSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.registrarAjustePlantel(empresaId, loteId, solicitud, usuario));
    }

    @GetMapping("/lotes/{loteId}/ajustes-plantel")
    public ResponseEntity<List<AvicolaHuevoAjustePlantelRespuesta>> listarAjustesPlantel(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.listarAjustesPlantel(empresaId, loteId));
    }

    @GetMapping("/reportes/resumen")
    public ResponseEntity<AvicolaHuevosReporteResumenRespuesta> reporteResumenEmpresa(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        try {
            return ResponseEntity.ok(servicioReportes.resumenEmpresaEnRango(empresaId, desde, hasta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/reportes/analisis-postura")
    public ResponseEntity<AvicolaHuevosReporteAnalisisRespuesta> reporteAnalisisPostura(
            @RequestHeader("X-Company-Id") Long empresaId,
            @RequestParam Long loteId,
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        try {
            return ResponseEntity.ok(servicioReportes.analisisPosturaLote(empresaId, loteId, desde, hasta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/lotes/{loteId}/eventos-sanitarios")
    public ResponseEntity<List<AvicolaHuevoEventoSanitarioRespuesta>> listarEventosSanitarios(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(empresaId, loteId));
    }

    @PostMapping("/lotes/{loteId}/eventos-sanitarios")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoEventoSanitarioRespuesta> registrarEventoSanitario(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarEventoSanitario(empresaId, loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/resumen")
    public ResponseEntity<AvicolaHuevosResumenRespuesta> resumenLote(
            @RequestHeader("X-Company-Id") Long empresaId,
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.resumenLote(empresaId, loteId));
    }
}
