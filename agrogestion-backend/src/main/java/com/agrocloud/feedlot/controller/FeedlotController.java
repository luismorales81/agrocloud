package com.agrocloud.feedlot.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import com.agrocloud.feedlot.model.dto.*;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/feedlot")
@RequiresModule("FEEDLOT")
public class FeedlotController {

    private final ServicioFeedlotCatalogos servicioCatalogos;
    private final ServicioFeedlotLotes servicioLotes;
    private final ServicioFeedlotOperaciones servicioOperaciones;
    private final ServicioFeedlotCloseout servicioCloseout;
    private final ServicioFeedlotPanel servicioPanel;
    private final ServicioFeedlotDietas servicioDietas;
    private final ServicioFeedlotBunk servicioBunk;
    private final ServicioFeedlotReportes servicioReportes;
    private final ServicioFeedlotConfiguracion servicioConfiguracion;
    private final GeneradorPdfFeedlotCloseout generadorPdfCloseout;
    private final UserService userService;

    public FeedlotController(
            ServicioFeedlotCatalogos servicioCatalogos,
            ServicioFeedlotLotes servicioLotes,
            ServicioFeedlotOperaciones servicioOperaciones,
            ServicioFeedlotCloseout servicioCloseout,
            ServicioFeedlotPanel servicioPanel,
            ServicioFeedlotDietas servicioDietas,
            ServicioFeedlotBunk servicioBunk,
            ServicioFeedlotReportes servicioReportes,
            ServicioFeedlotConfiguracion servicioConfiguracion,
            GeneradorPdfFeedlotCloseout generadorPdfCloseout,
            UserService userService) {
        this.servicioCatalogos = servicioCatalogos;
        this.servicioLotes = servicioLotes;
        this.servicioOperaciones = servicioOperaciones;
        this.servicioCloseout = servicioCloseout;
        this.servicioPanel = servicioPanel;
        this.servicioDietas = servicioDietas;
        this.servicioBunk = servicioBunk;
        this.servicioReportes = servicioReportes;
        this.servicioConfiguracion = servicioConfiguracion;
        this.generadorPdfCloseout = generadorPdfCloseout;
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
    public ResponseEntity<List<FeedlotEstablecimientoRespuesta>> listarEstablecimientos(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarEstablecimientos());
    }

    @PostMapping("/establecimientos")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotEstablecimientoRespuesta> crearEstablecimiento(
            @Valid @RequestBody FeedlotEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearEstablecimiento(solicitud));
    }

    @PutMapping("/establecimientos/{id}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotEstablecimientoRespuesta> actualizarEstablecimiento(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarEstablecimiento(id, solicitud));
    }

    @GetMapping("/establecimientos/{establecimientoId}/corrales")
    public ResponseEntity<List<FeedlotCorralRespuesta>> listarCorrales(
            @PathVariable Long establecimientoId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarCorrales(establecimientoId));
    }

    @PostMapping("/establecimientos/{establecimientoId}/corrales")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCorralRespuesta> crearCorral(
            @PathVariable Long establecimientoId,
            @Valid @RequestBody FeedlotCorralSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioCatalogos.crearCorral(establecimientoId, solicitud));
    }

    @PutMapping("/establecimientos/{establecimientoId}/corrales/{corralId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCorralRespuesta> actualizarCorral(
            @PathVariable Long establecimientoId,
            @PathVariable Long corralId,
            @Valid @RequestBody FeedlotCorralSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarCorral(establecimientoId, corralId, solicitud));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<FeedlotCatalogoRespuesta>> listarCategorias(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarCategorias());
    }

    @PostMapping("/categorias")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCatalogoRespuesta> crearCategoria(
            @Valid @RequestBody FeedlotCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearCategoria(solicitud));
    }

    @PutMapping("/categorias/{id}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCatalogoRespuesta> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarCategoria(id, solicitud));
    }

    @GetMapping("/razas")
    public ResponseEntity<List<FeedlotCatalogoRespuesta>> listarRazas(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarRazas());
    }

    @PostMapping("/razas")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCatalogoRespuesta> crearRaza(
            @Valid @RequestBody FeedlotCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearRaza(solicitud));
    }

    @PutMapping("/razas/{id}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCatalogoRespuesta> actualizarRaza(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarRaza(id, solicitud));
    }

    @GetMapping("/motivos-muerte")
    public ResponseEntity<List<FeedlotCatalogoRespuesta>> listarMotivosMuerte(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarMotivosMuerte());
    }

    @PostMapping("/motivos-muerte")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCatalogoRespuesta> crearMotivoMuerte(
            @Valid @RequestBody FeedlotCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearMotivoMuerte(solicitud));
    }

    @PutMapping("/motivos-muerte/{id}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotCatalogoRespuesta> actualizarMotivoMuerte(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarMotivoMuerte(id, solicitud));
    }

    @GetMapping("/proveedores")
    public ResponseEntity<List<FeedlotProveedorOrigenRespuesta>> listarProveedores(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarProveedores());
    }

    @PostMapping("/proveedores")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotProveedorOrigenRespuesta> crearProveedor(
            @Valid @RequestBody FeedlotProveedorOrigenSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearProveedor(solicitud));
    }

    @PutMapping("/proveedores/{id}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotProveedorOrigenRespuesta> actualizarProveedor(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotProveedorOrigenSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarProveedor(id, solicitud));
    }

    @GetMapping("/lotes")
    public ResponseEntity<List<FeedlotLoteRespuesta>> listarLotes(
            @RequestParam(required = false) FeedlotLoteEstado estado,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @RequestParam(required = false) Long corralId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.listarLotes(estado, delPeriodoActivo, corralId));
    }

    @GetMapping("/lotes/{id}")
    public ResponseEntity<FeedlotLoteRespuesta> obtenerLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.obtenerLote(id));
    }

    @PostMapping("/lotes")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotLoteRespuesta> crearLote(
            @Valid @RequestBody FeedlotLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioLotes.crearLote(solicitud));
    }

    @PutMapping("/lotes/{id}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotLoteRespuesta> actualizarLote(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.actualizarLote(id, solicitud));
    }

    @PostMapping("/lotes/{id}/cierre")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotLoteRespuesta> cerrarLote(
            @PathVariable Long id,
            @RequestBody(required = false) FeedlotCierreLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.cerrarLote(id, solicitud));
    }

    @GetMapping("/lotes/{id}/resumen")
    public ResponseEntity<FeedlotResumenRespuesta> resumenLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCloseout.calcularResumen(id));
    }

    @GetMapping("/lotes/{id}/closeout")
    public ResponseEntity<FeedlotCloseoutRespuesta> closeoutLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCloseout.calcularCloseout(id));
    }

    @GetMapping("/lotes/{id}/pesadas")
    public ResponseEntity<List<FeedlotPesadaRespuesta>> listarPesadas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarPesadas(id));
    }

    @PostMapping("/lotes/{id}/pesadas")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotPesadaRespuesta> registrarPesada(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotPesadaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarPesada(id, solicitud));
    }

    @PutMapping("/lotes/{id}/pesadas/{pesadaId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotPesadaRespuesta> actualizarPesada(
            @PathVariable Long id,
            @PathVariable Long pesadaId,
            @Valid @RequestBody FeedlotPesadaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.actualizarPesada(id, pesadaId, solicitud));
    }

    @DeleteMapping("/lotes/{id}/pesadas/{pesadaId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<Void> eliminarPesada(
            @PathVariable Long id,
            @PathVariable Long pesadaId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        servicioOperaciones.eliminarPesada(id, pesadaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lotes/{id}/consumos")
    public ResponseEntity<List<FeedlotConsumoRespuesta>> listarConsumos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(id));
    }

    @PostMapping("/lotes/{id}/consumos")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotConsumoRespuesta> registrarConsumo(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarConsumo(id, solicitud));
    }

    @PutMapping("/lotes/{id}/consumos/{consumoId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotConsumoRespuesta> actualizarConsumo(
            @PathVariable Long id,
            @PathVariable Long consumoId,
            @Valid @RequestBody FeedlotConsumoActualizarSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.actualizarConsumo(id, consumoId, solicitud));
    }

    @DeleteMapping("/lotes/{id}/consumos/{consumoId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<Void> eliminarConsumo(
            @PathVariable Long id,
            @PathVariable Long consumoId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        servicioOperaciones.eliminarConsumo(id, consumoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lotes/{id}/muertes")
    public ResponseEntity<List<FeedlotMuerteRespuesta>> listarMuertes(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarMuertes(id));
    }

    @PostMapping("/lotes/{id}/muertes")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotMuerteRespuesta> registrarMuerte(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotMuerteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarMuerte(id, solicitud));
    }

    @GetMapping("/lotes/{id}/eventos-sanitarios")
    public ResponseEntity<List<FeedlotEventoSanitarioRespuesta>> listarEventosSanitarios(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(id));
    }

    @PostMapping("/lotes/{id}/eventos-sanitarios")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotEventoSanitarioRespuesta> registrarEventoSanitario(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioOperaciones.registrarEventoSanitario(id, solicitud));
    }

    @PutMapping("/lotes/{id}/eventos-sanitarios/{eventoId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotEventoSanitarioRespuesta> actualizarEventoSanitario(
            @PathVariable Long id,
            @PathVariable Long eventoId,
            @Valid @RequestBody FeedlotEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.actualizarEventoSanitario(id, eventoId, solicitud));
    }

    @DeleteMapping("/lotes/{id}/eventos-sanitarios/{eventoId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<Void> eliminarEventoSanitario(
            @PathVariable Long id,
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        servicioOperaciones.eliminarEventoSanitario(id, eventoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lotes/{id}/ventas")
    public ResponseEntity<List<FeedlotVentaRespuesta>> listarVentas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentas(id));
    }

    @PostMapping("/lotes/{id}/ventas")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotVentaRespuesta> registrarVenta(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotVentaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarVenta(id, solicitud));
    }

    @PostMapping("/lotes/{id}/ajustes-plantel")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotAjustePlantelRespuesta> registrarAjustePlantel(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotAjustePlantelSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User usuario = requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioLotes.registrarAjustePlantel(id, solicitud, usuario));
    }

    @GetMapping("/lotes/{id}/ajustes-plantel")
    public ResponseEntity<List<FeedlotAjustePlantelRespuesta>> listarAjustesPlantel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.listarAjustesPlantel(id));
    }

    @GetMapping("/panel/resumen")
    public ResponseEntity<FeedlotPanelRespuesta> panelResumen(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioPanel.resumenPeriodoActivo());
    }

    @GetMapping("/reportes/analisis-lotes")
    public ResponseEntity<FeedlotAnalisisLotesRespuesta> reporteAnalisisLotes(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioPanel.analisisLotesPeriodoActivo());
    }

    @GetMapping("/dietas")
    public ResponseEntity<List<FeedlotDietaRespuesta>> listarDietas(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.listarDietas());
    }

    @GetMapping("/dietas/{id}")
    public ResponseEntity<FeedlotDietaRespuesta> obtenerDieta(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.obtenerDieta(id));
    }

    @PostMapping("/dietas")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotDietaRespuesta> crearDieta(
            @Valid @RequestBody FeedlotDietaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioDietas.crearDieta(solicitud));
    }

    @PutMapping("/dietas/{id}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotDietaRespuesta> actualizarDieta(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotDietaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.actualizarDieta(id, solicitud));
    }

    @PostMapping("/dietas/{id}/fases")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotDietaFaseRespuesta> crearFaseDieta(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotDietaFaseSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioDietas.crearFase(id, solicitud));
    }

    @PutMapping("/dietas/{id}/fases/{faseId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotDietaFaseRespuesta> actualizarFaseDieta(
            @PathVariable Long id,
            @PathVariable Long faseId,
            @Valid @RequestBody FeedlotDietaFaseSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.actualizarFase(id, faseId, solicitud));
    }

    @GetMapping("/lotes/{id}/lecturas-comedero")
    public ResponseEntity<List<FeedlotLecturaComederoRespuesta>> listarLecturasComedero(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioBunk.listarLecturas(id));
    }

    @PostMapping("/lotes/{id}/lecturas-comedero")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotLecturaComederoRespuesta> registrarLecturaComedero(
            @PathVariable Long id,
            @Valid @RequestBody FeedlotLecturaComederoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioBunk.registrarLectura(id, solicitud));
    }

    @PutMapping("/lotes/{id}/lecturas-comedero/{lecturaId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotLecturaComederoRespuesta> actualizarLecturaComedero(
            @PathVariable Long id,
            @PathVariable Long lecturaId,
            @Valid @RequestBody FeedlotLecturaComederoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioBunk.actualizarLectura(id, lecturaId, solicitud));
    }

    @DeleteMapping("/lotes/{id}/lecturas-comedero/{lecturaId}")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<Void> eliminarLecturaComedero(
            @PathVariable Long id,
            @PathVariable Long lecturaId,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        servicioBunk.eliminarLectura(id, lecturaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lotes/{id}/consumo-teorico")
    public ResponseEntity<FeedlotConsumoTeoricoRespuesta> consumoTeoricoLote(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.calcularConsumoTeorico(id, fechaDesde, fechaHasta));
    }

    @GetMapping("/reportes/lote/{id}/curva-peso")
    public ResponseEntity<FeedlotCurvaPesoRespuesta> curvaPesoLote(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal pesoObjetivoKg,
            @RequestParam(required = false) Integer diasProyeccion,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.curvaPeso(id, pesoObjetivoKg, diasProyeccion));
    }

    @GetMapping("/reportes/exportar")
    public ResponseEntity<byte[]> exportarReportesExcel(
            @AuthenticationPrincipal UserDetails detalles) throws IOException {
        requerirUsuario(detalles);
        byte[] excel = servicioReportes.exportarExcelPeriodoActivo();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "Feedlot_Comparativa_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/lotes/{id}/closeout/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> closeoutPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) throws IOException {
        requerirUsuario(detalles);
        FeedlotCloseoutRespuesta closeout = servicioCloseout.calcularCloseout(id);
        byte[] pdf = generadorPdfCloseout.generar(closeout);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Closeout_Lote_" + id + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/configuracion/closeout")
    public ResponseEntity<FeedlotConfiguracionCloseoutRespuesta> obtenerConfiguracionCloseout(
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioConfiguracion.obtenerMetodoCloseout());
    }

    @PutMapping("/configuracion/closeout")
    @RequiresModule(value = "FEEDLOT", permission = "write")
    public ResponseEntity<FeedlotConfiguracionCloseoutRespuesta> actualizarConfiguracionCloseout(
            @Valid @RequestBody FeedlotConfiguracionCloseoutSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        requerirUsuario(detalles);
        return ResponseEntity.ok(servicioConfiguracion.actualizarMetodoCloseout(solicitud));
    }
}
