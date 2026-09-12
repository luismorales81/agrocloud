package com.agrocloud.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaCatalogo;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaLote;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaOperaciones;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaReportes;
import com.agrocloud.avicola.crianza.model.dto.*;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.security.ServicioSeguridadContexto;
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
    private final ServicioAvicolaCrianzaReportes servicioReportes;
    private final UserService userService;
    private final ServicioSeguridadContexto servicioSeguridadContexto;

    public AvicolaCrianzaController(
            ServicioAvicolaCrianzaCatalogo servicioCatalogo,
            ServicioAvicolaCrianzaLote servicioLote,
            ServicioAvicolaCrianzaOperaciones servicioOperaciones,
            ServicioAvicolaCrianzaReportes servicioReportes,
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

    // --- CatÃ¡logo ---

    @GetMapping("/establecimientos")
    public ResponseEntity<List<AvicolaEstablecimientoRespuesta>> listarEstablecimientos(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarEstablecimientos(servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @PostMapping("/establecimientos")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaEstablecimientoRespuesta> crearEstablecimiento(
            @RequestBody AvicolaEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearEstablecimiento(servicioSeguridadContexto.obtenerEmpresaIdActual(), solicitud));
    }

    @PutMapping("/establecimientos/{id}")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaEstablecimientoRespuesta> actualizarEstablecimiento(
            @PathVariable Long id,
            @RequestBody AvicolaEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarEstablecimiento(servicioSeguridadContexto.obtenerEmpresaIdActual(), id, solicitud));
    }

    @GetMapping("/razas")
    public ResponseEntity<List<AvicolaRazaRespuesta>> listarRazas(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.listarRazas(servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @PostMapping("/razas")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaRazaRespuesta> crearRaza(
            @RequestBody AvicolaRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.crearRaza(servicioSeguridadContexto.obtenerEmpresaIdActual(), solicitud));
    }

    @PutMapping("/razas/{id}")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaRazaRespuesta> actualizarRaza(
            @PathVariable Long id,
            @RequestBody AvicolaRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogo.actualizarRaza(servicioSeguridadContexto.obtenerEmpresaIdActual(), id, solicitud));
    }

    // --- Lotes ---

    @GetMapping("/lotes")
    public ResponseEntity<List<AvicolaLoteRespuesta>> listarLotes(
            @RequestParam(required = false) AvicolaLoteEstado estado,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.listarLotes(servicioSeguridadContexto.obtenerEmpresaIdActual(), estado, delPeriodoActivo));
    }

    @GetMapping("/lotes/{loteId}")
    public ResponseEntity<AvicolaLoteRespuesta> obtenerLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.obtenerLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaLoteRespuesta> crearLote(
            @RequestBody AvicolaLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.crearLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), solicitud));
    }

    @PutMapping("/lotes/{loteId}")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaLoteRespuesta> actualizarLote(
            @PathVariable Long loteId,
            @RequestBody AvicolaLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.actualizarLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @PostMapping("/lotes/{loteId}/cierre")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaLoteRespuesta> cerrarLote(
            @PathVariable Long loteId,
            @RequestBody(required = false) AvicolaCrianzaCierreLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLote.cerrarLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    // --- Operaciones por lote ---

    @GetMapping("/lotes/{loteId}/pesadas")
    public ResponseEntity<List<AvicolaPesadaRespuesta>> listarPesadas(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarPesadas(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/pesadas")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaPesadaRespuesta> registrarPesada(
            @PathVariable Long loteId,
            @RequestBody AvicolaPesadaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarPesada(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/muertes")
    public ResponseEntity<List<AvicolaMuerteRespuesta>> listarMuertes(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarMuertes(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/muertes")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaMuerteRespuesta> registrarMuerte(
            @PathVariable Long loteId,
            @RequestBody AvicolaMuerteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarMuerte(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/ventas")
    public ResponseEntity<List<AvicolaVentaRespuesta>> listarVentas(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentas(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/ventas")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaVentaRespuesta> registrarVenta(
            @PathVariable Long loteId,
            @RequestBody AvicolaVentaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarVenta(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/consumos")
    public ResponseEntity<List<AvicolaConsumoRespuesta>> listarConsumos(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/consumos")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaConsumoRespuesta> registrarConsumo(
            @PathVariable Long loteId,
            @RequestBody AvicolaConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarConsumo(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud, usuario.getId()));
    }

    @PutMapping("/lotes/{loteId}/consumos/{consumoId}")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaConsumoRespuesta> actualizarConsumo(
            @PathVariable Long loteId,
            @PathVariable Long consumoId,
            @RequestBody AvicolaCrianzaConsumoActualizarSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.ok(
                servicioOperaciones.actualizarConsumo(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, consumoId, solicitud, usuario.getId()));
    }

    @GetMapping("/lotes/{loteId}/eventos-sanitarios")
    public ResponseEntity<List<AvicolaEventoSanitarioRespuesta>> listarEventosSanitarios(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    @PostMapping("/lotes/{loteId}/eventos-sanitarios")
    @RequiresModule(value = "AVICOLA_CRIANZA", permission = "write")
    public ResponseEntity<AvicolaEventoSanitarioRespuesta> registrarEventoSanitario(
            @PathVariable Long loteId,
            @RequestBody AvicolaEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.registrarEventoSanitario(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId, solicitud));
    }

    @GetMapping("/lotes/{loteId}/resumen")
    public ResponseEntity<AvicolaCrianzaResumenRespuesta> resumenLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.resumenLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }

    // --- Reportes ---

    @GetMapping("/reportes/resumen")
    public ResponseEntity<AvicolaCrianzaReporteResumenRespuesta> reporteResumen(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.resumenEmpresa(servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @GetMapping("/reportes/analisis-lotes")
    public ResponseEntity<AvicolaCrianzaReporteResumenRespuesta> reporteAnalisisLotes(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.analisisLotes(servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @GetMapping("/reportes/lote/{loteId}/curva-peso")
    public ResponseEntity<AvicolaCrianzaCurvaPesoRespuesta> reporteCurvaPeso(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.curvaPesoLote(servicioSeguridadContexto.obtenerEmpresaIdActual(), loteId));
    }
}
