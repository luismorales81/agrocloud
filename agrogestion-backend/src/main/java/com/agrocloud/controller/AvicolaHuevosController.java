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
import com.agrocloud.core.security.ServicioSeguridadContexto;
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
    private final ServicioSeguridadContexto servicioSeguridadContexto;

    public AvicolaHuevosController(
            ServicioAvicolaHuevosCatalogo servicioCatalogo,
            ServicioAvicolaHuevosLote servicioLote,
            ServicioAvicolaHuevosOperaciones servicioOperaciones,
            ServicioAvicolaHuevosReportes servicioReportes,
            UserService userService,
            ServicioSeguridadContexto servicioSeguridadContexto) {
        this.servicioCatalogo = servicioCatalogo;
        this.servicioLote = servicioLote;
        this.servicioOperaciones = servicioOperaciones;
        this.servicioReportes = servicioReportes;
        this.userService = userService;
        this.servicioSeguridadContexto = servicioSeguridadContexto;
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
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarEstablecimientos(servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @PostMapping("/establecimientos")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoEstablecimientoRespuesta> crearEstablecimiento(
            @RequestBody AvicolaHuevoEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearEstablecimiento(servicioSeguridadContexto.obtenerEmpresaIdActual(), solicitud));
    }

    @PutMapping("/establecimientos/{id}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoEstablecimientoRespuesta> actualizarEstablecimiento(
            @PathVariable Long id,
            @RequestBody AvicolaHuevoEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarEstablecimiento(servicioSeguridadContexto.obtenerEmpresaIdActual(), id, solicitud));
    }

    @GetMapping("/razas")
    public ResponseEntity<List<AvicolaHuevoRazaRespuesta>> listarRazas(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarRazas(servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @PostMapping("/razas")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoRazaRespuesta> crearRaza(
            @RequestBody AvicolaHuevoRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearRaza(servicioSeguridadContexto.obtenerEmpresaIdActual(), solicitud));
    }

    @PutMapping("/razas/{id}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoRazaRespuesta> actualizarRaza(
            @PathVariable Long id,
            @RequestBody AvicolaHuevoRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarRaza(servicioSeguridadContexto.obtenerEmpresaIdActual(), id, solicitud));
    }

    @GetMapping("/lotes")
    public ResponseEntity<List<AvicolaHuevoLoteRespuesta>> listarLotes(
            @RequestParam(required = false) AvicolaHuevoLoteEstado estado,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.listarLotes(servicioSeguridadContexto.obtenerEmpresaIdActual(), estado, delPeriodoActivo));
    }

    @GetMapping("/lotes/{loteId}")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> obtenerLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.obtenerLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> crearLote(
            @RequestBody AvicolaHuevoLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.crearLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), solicitud));
    }

    @PutMapping("/lotes/{loteId}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> actualizarLote(
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.actualizarLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @PostMapping("/lotes/{loteId}/cierre")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> cerrarLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.cerrarLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @GetMapping("/lotes/{loteId}/produccion-diaria")
    public ResponseEntity<List<AvicolaHuevoProduccionDiariaRespuesta>> listarProduccionDiaria(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarProduccionDiaria(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/produccion-diaria")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoProduccionDiariaRespuesta> registrarProduccionDiaria(
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoProduccionDiariaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarProduccionDiaria(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/consumos")
    public ResponseEntity<List<AvicolaHuevoConsumoRespuesta>> listarConsumos(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/consumos")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoConsumoRespuesta> registrarConsumo(
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarConsumo(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud, usuario.getId()));
    }

    @PutMapping("/lotes/{loteId}/consumos/{consumoId}")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoConsumoRespuesta> actualizarConsumo(
            @PathVariable Long loteId,
            @PathVariable Long consumoId,
            @RequestBody AvicolaHuevoConsumoActualizarSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(
                servicioOperaciones.actualizarConsumo(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, consumoId, solicitud, usuario.getId()));
    }

    @PostMapping("/lotes/{loteId}/ajuste-plantel")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoLoteRespuesta> registrarAjustePlantel(
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoAjustePlantelSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.registrarAjustePlantel(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud, usuario));
    }

    @GetMapping("/lotes/{loteId}/ajustes-plantel")
    public ResponseEntity<List<AvicolaHuevoAjustePlantelRespuesta>> listarAjustesPlantel(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.listarAjustesPlantel(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @GetMapping("/reportes/resumen")
    public ResponseEntity<AvicolaHuevosReporteResumenRespuesta> reporteResumenEmpresa(
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        try {
            return ResponseEntity.ok(servicioReportes.resumenEmpresaEnRango(servicioSeguridadContexto.obtenerEmpresaIdActual(), desde, hasta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/reportes/analisis-postura")
    public ResponseEntity<AvicolaHuevosReporteAnalisisRespuesta> reporteAnalisisPostura(
            @RequestParam Long loteId,
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        try {
            return ResponseEntity.ok(servicioReportes.analisisPosturaLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, desde, hasta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/lotes/{loteId}/eventos-sanitarios")
    public ResponseEntity<List<AvicolaHuevoEventoSanitarioRespuesta>> listarEventosSanitarios(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/eventos-sanitarios")
    @RequiresModule(value = "AVICOLA_HUEVOS", permission = "write")
    public ResponseEntity<AvicolaHuevoEventoSanitarioRespuesta> registrarEventoSanitario(
            @PathVariable Long loteId,
            @RequestBody AvicolaHuevoEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarEventoSanitario(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/resumen")
    public ResponseEntity<AvicolaHuevosResumenRespuesta> resumenLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.resumenLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }
}
