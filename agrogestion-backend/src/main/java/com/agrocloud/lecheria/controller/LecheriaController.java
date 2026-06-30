package com.agrocloud.lecheria.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import com.agrocloud.lecheria.model.dto.*;
import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import com.agrocloud.lecheria.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lecheria")
@RequiresModule("LECHERIA")
public class LecheriaController {

    private final ServicioLecheriaCatalogos servicioCatalogos;
    private final ServicioLecheriaAnimales servicioAnimales;
    private final ServicioLecheriaOrdene servicioOrdene;
    private final ServicioLecheriaReproduccion servicioReproduccion;
    private final ServicioLecheriaOperaciones servicioOperaciones;
    private final ServicioLecheriaPanel servicioPanel;
    private final ServicioLecheriaReportes servicioReportes;
    private final ServicioLecheriaImportacion servicioImportacion;
    private final ServicioLecheriaCloseout servicioCloseout;
    private final ServicioLecheriaSenasa servicioSenasa;
    private final UserService userService;

    public LecheriaController(
            ServicioLecheriaCatalogos servicioCatalogos,
            ServicioLecheriaAnimales servicioAnimales,
            ServicioLecheriaOrdene servicioOrdene,
            ServicioLecheriaReproduccion servicioReproduccion,
            ServicioLecheriaOperaciones servicioOperaciones,
            ServicioLecheriaPanel servicioPanel,
            ServicioLecheriaReportes servicioReportes,
            ServicioLecheriaImportacion servicioImportacion,
            ServicioLecheriaCloseout servicioCloseout,
            ServicioLecheriaSenasa servicioSenasa,
            UserService userService) {
        this.servicioCatalogos = servicioCatalogos;
        this.servicioAnimales = servicioAnimales;
        this.servicioOrdene = servicioOrdene;
        this.servicioReproduccion = servicioReproduccion;
        this.servicioOperaciones = servicioOperaciones;
        this.servicioPanel = servicioPanel;
        this.servicioReportes = servicioReportes;
        this.servicioImportacion = servicioImportacion;
        this.servicioCloseout = servicioCloseout;
        this.servicioSenasa = servicioSenasa;
        this.userService = userService;
    }

    private User requerirUsuario(UserDetails detalles) {
        if (detalles == null) throw new IllegalArgumentException("Usuario no autenticado");
        User u = userService.findByEmailWithAllRelations(detalles.getUsername());
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");
        return u;
    }

    @GetMapping("/panel/resumen")
    public ResponseEntity<LecheriaPanelRespuesta> panelResumen(@AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioPanel.resumenPeriodoActivo());
    }

    @GetMapping("/establecimientos")
    public ResponseEntity<List<LecheriaEstablecimientoRespuesta>> listarEstablecimientos(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarEstablecimientos());
    }

    @PostMapping("/establecimientos")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaEstablecimientoRespuesta> crearEstablecimiento(
            @Valid @RequestBody LecheriaEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearEstablecimiento(solicitud));
    }

    @PutMapping("/establecimientos/{id}")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaEstablecimientoRespuesta> actualizarEstablecimiento(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarEstablecimiento(id, solicitud));
    }

    @GetMapping("/establecimientos/{establecimientoId}/rodeos")
    public ResponseEntity<List<LecheriaRodeoRespuesta>> listarRodeos(
            @PathVariable Long establecimientoId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarRodeos(establecimientoId));
    }

    @PostMapping("/establecimientos/{establecimientoId}/rodeos")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaRodeoRespuesta> crearRodeo(
            @PathVariable Long establecimientoId,
            @Valid @RequestBody LecheriaRodeoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearRodeo(establecimientoId, solicitud));
    }

    @GetMapping("/catalogos/razas")
    public ResponseEntity<List<LecheriaRazaRespuesta>> listarRazas(
            @RequestParam(required = false) LecheriaEspecie especie,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarRazas(especie));
    }

    @PostMapping("/catalogos/razas")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaRazaRespuesta> crearRaza(
            @Valid @RequestBody LecheriaRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearRaza(solicitud));
    }

    @PutMapping("/catalogos/razas/{id}")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaRazaRespuesta> actualizarRaza(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarRaza(id, solicitud));
    }

    @GetMapping("/catalogos/motivos-baja")
    public ResponseEntity<List<LecheriaMotivoBajaRespuesta>> listarMotivosBaja(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarMotivosBaja());
    }

    @PostMapping("/catalogos/motivos-baja")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaMotivoBajaRespuesta> crearMotivoBaja(
            @Valid @RequestBody LecheriaMotivoBajaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearMotivoBaja(solicitud));
    }

    @PutMapping("/catalogos/motivos-baja/{id}")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaMotivoBajaRespuesta> actualizarMotivoBaja(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaMotivoBajaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarMotivoBaja(id, solicitud));
    }

    @GetMapping("/animales")
    public ResponseEntity<List<LecheriaAnimalRespuesta>> listarAnimales(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioAnimales.listarAnimales());
    }

    @GetMapping("/animales/{id}")
    public ResponseEntity<LecheriaAnimalRespuesta> obtenerAnimal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioAnimales.obtenerAnimal(id));
    }

    @PostMapping("/animales")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaAnimalRespuesta> crearAnimal(
            @Valid @RequestBody LecheriaAnimalSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioAnimales.crearAnimal(solicitud));
    }

    @PutMapping("/animales/{id}")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaAnimalRespuesta> actualizarAnimal(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaAnimalSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioAnimales.actualizarAnimal(id, solicitud));
    }

    @GetMapping("/animales/{id}/ordenes")
    public ResponseEntity<List<LecheriaRegistroOrdeneRespuesta>> listarOrdenes(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOrdene.listarOrdenes(id));
    }

    @PostMapping("/animales/{id}/ordenes")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaRegistroOrdeneRespuesta> registrarOrdene(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaRegistroOrdeneSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOrdene.registrarOrdene(id, solicitud));
    }

    @GetMapping("/animales/{id}/eventos-reproductivos")
    public ResponseEntity<List<LecheriaEventoReproductivoRespuesta>> listarEventosReproductivos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.listarEventos(id));
    }

    @PostMapping("/animales/{id}/eventos-reproductivos")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaEventoReproductivoRespuesta> registrarEventoReproductivo(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaEventoReproductivoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioReproduccion.registrarEvento(id, solicitud));
    }

    @GetMapping("/animales/{id}/eventos-sanitarios")
    public ResponseEntity<List<LecheriaEventoSanitarioRespuesta>> listarEventosSanitarios(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(id));
    }

    @PostMapping("/animales/{id}/eventos-sanitarios")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaEventoSanitarioRespuesta> registrarEventoSanitario(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarEventoSanitario(id, solicitud));
    }

    @GetMapping("/animales/{id}/scores-corporales")
    public ResponseEntity<List<LecheriaScoreCorporalRespuesta>> listarScoresCorporales(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarScoresCorporales(id));
    }

    @PostMapping("/animales/{id}/scores-corporales")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaScoreCorporalRespuesta> registrarScoreCorporal(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaScoreCorporalSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarScoreCorporal(id, solicitud));
    }

    @PostMapping("/animales/{id}/baja")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaBajaAnimalRespuesta> registrarBaja(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaBajaAnimalSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarBaja(id, solicitud));
    }

    @GetMapping("/rodeos/{id}/consumos")
    public ResponseEntity<List<LecheriaConsumoRespuesta>> listarConsumos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(id));
    }

    @PostMapping("/rodeos/{id}/consumos")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaConsumoRespuesta> registrarConsumo(
            @PathVariable Long id,
            @Valid @RequestBody LecheriaConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarConsumo(id, solicitud));
    }

    @GetMapping("/ventas-leche")
    public ResponseEntity<List<LecheriaVentaLecheRespuesta>> listarVentasLeche(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentasLeche());
    }

    @PostMapping("/ventas-leche")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaVentaLecheRespuesta> registrarVentaLeche(
            @Valid @RequestBody LecheriaVentaLecheSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarVentaLeche(solicitud));
    }

    @GetMapping("/reportes/curvas-lactancia")
    public ResponseEntity<List<LecheriaCurvaLactanciaRespuesta>> curvasLactancia(
            @RequestParam(required = false) Long animalId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.curvasLactancia(animalId));
    }

    @GetMapping("/reportes/ranking-produccion")
    public ResponseEntity<LecheriaRankingProduccionRespuesta> rankingProduccion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.rankingProduccion(desde, hasta));
    }

    @GetMapping("/reportes/clima-produccion")
    public ResponseEntity<LecheriaClimaProduccionRespuesta> climaProduccion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.climaProduccion(desde, hasta));
    }

    @PostMapping(value = "/import/control-lechero", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaImportControlLecheroRespuesta> importarControlLechero(
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioImportacion.importarControlLechero(archivo));
    }

    @GetMapping("/rodeos/{id}/closeout")
    public ResponseEntity<LecheriaCloseoutRodeoRespuesta> closeoutRodeo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCloseout.calcularCloseoutRodeo(id));
    }

    @PostMapping("/movimientos-senasa")
    @RequiresModule(value = "LECHERIA", permission = "write")
    public ResponseEntity<LecheriaMovimientoSenasaRespuesta> registrarMovimientoSenasa(
            @Valid @RequestBody LecheriaMovimientoSenasaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioSenasa.registrarMovimiento(solicitud));
    }

    @GetMapping("/movimientos-senasa/pendientes")
    public ResponseEntity<List<LecheriaMovimientoSenasaRespuesta>> listarMovimientosSenasaPendientes(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioSenasa.listarPendientesExportacion());
    }
}
