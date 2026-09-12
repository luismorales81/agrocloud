package com.agrocloud.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.config.ResolvedorUsuarioPeticion;
import com.agrocloud.porcinos.model.dto.*;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/porcinos")
@RequiresModule("PORCINOS")
public class PorcinosController {

    private final ServicioPorcinosCatalogos servicioCatalogos;
    private final ServicioPorcinosReproduccion servicioReproduccion;
    private final ServicioPorcinosLotes servicioLotes;
    private final ServicioPorcinosOperaciones servicioOperaciones;
    private final ServicioPorcinosPanel servicioPanel;
    private final ServicioPorcinosDietas servicioDietas;
    private final ServicioPorcinosReportes servicioReportes;
    private final ServicioPorcinosEconomia servicioEconomia;
    private final ResolvedorUsuarioPeticion resolvedorUsuarioPeticion;

    public PorcinosController(
            ServicioPorcinosCatalogos servicioCatalogos,
            ServicioPorcinosReproduccion servicioReproduccion,
            ServicioPorcinosLotes servicioLotes,
            ServicioPorcinosOperaciones servicioOperaciones,
            ServicioPorcinosPanel servicioPanel,
            ServicioPorcinosDietas servicioDietas,
            ServicioPorcinosReportes servicioReportes,
            ServicioPorcinosEconomia servicioEconomia,
            ResolvedorUsuarioPeticion resolvedorUsuarioPeticion) {
        this.servicioCatalogos = servicioCatalogos;
        this.servicioReproduccion = servicioReproduccion;
        this.servicioLotes = servicioLotes;
        this.servicioOperaciones = servicioOperaciones;
        this.servicioPanel = servicioPanel;
        this.servicioDietas = servicioDietas;
        this.servicioReportes = servicioReportes;
        this.servicioEconomia = servicioEconomia;
        this.resolvedorUsuarioPeticion = resolvedorUsuarioPeticion;
    }

    @GetMapping("/establecimientos")
    public ResponseEntity<List<PorcinosEstablecimientoRespuesta>> listarEstablecimientos(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarEstablecimientos());
    }

    @PostMapping("/establecimientos")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosEstablecimientoRespuesta> crearEstablecimiento(
            @Valid @RequestBody PorcinosEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearEstablecimiento(solicitud));
    }

    @PutMapping("/establecimientos/{id}")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosEstablecimientoRespuesta> actualizarEstablecimiento(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosEstablecimientoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarEstablecimiento(id, solicitud));
    }

    @GetMapping("/establecimientos/{establecimientoId}/galpones")
    public ResponseEntity<List<PorcinosGalponRespuesta>> listarGalpones(
            @PathVariable Long establecimientoId,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarGalpones(establecimientoId));
    }

    @PostMapping("/establecimientos/{establecimientoId}/galpones")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosGalponRespuesta> crearGalpon(
            @PathVariable Long establecimientoId,
            @Valid @RequestBody PorcinosGalponSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioCatalogos.crearGalpon(establecimientoId, solicitud));
    }

    @PutMapping("/establecimientos/{establecimientoId}/galpones/{galponId}")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosGalponRespuesta> actualizarGalpon(
            @PathVariable Long establecimientoId,
            @PathVariable Long galponId,
            @Valid @RequestBody PorcinosGalponSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarGalpon(establecimientoId, galponId, solicitud));
    }

    @GetMapping("/razas")
    public ResponseEntity<List<PorcinosRazaRespuesta>> listarRazas(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarRazas());
    }

    @PostMapping("/razas")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosRazaRespuesta> crearRaza(
            @Valid @RequestBody PorcinosRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearRaza(solicitud));
    }

    @PutMapping("/razas/{id}")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosRazaRespuesta> actualizarRaza(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosRazaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.actualizarRaza(id, solicitud));
    }

    @GetMapping("/motivos-baja")
    public ResponseEntity<List<PorcinosCatalogoRespuesta>> listarMotivosBaja(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarMotivosBaja());
    }

    @PostMapping("/motivos-baja")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosCatalogoRespuesta> crearMotivoBaja(
            @Valid @RequestBody PorcinosCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearMotivoBaja(solicitud));
    }

    @GetMapping("/causas-mortalidad")
    public ResponseEntity<List<PorcinosCatalogoRespuesta>> listarCausasMortalidad(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarCausasMortalidad());
    }

    @PostMapping("/causas-mortalidad")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosCatalogoRespuesta> crearCausaMortalidad(
            @Valid @RequestBody PorcinosCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearCausaMortalidad(solicitud));
    }

    @GetMapping("/tipos-servicio")
    public ResponseEntity<List<PorcinosCatalogoRespuesta>> listarTiposServicio(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioCatalogos.listarTiposServicio());
    }

    @PostMapping("/tipos-servicio")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosCatalogoRespuesta> crearTipoServicio(
            @Valid @RequestBody PorcinosCatalogoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCatalogos.crearTipoServicio(solicitud));
    }

    @GetMapping("/madres")
    public ResponseEntity<List<PorcinosMadreRespuesta>> listarMadres(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.listarMadres());
    }

    @GetMapping("/madres/{id}")
    public ResponseEntity<PorcinosMadreRespuesta> obtenerMadre(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.obtenerMadre(id));
    }

    @PostMapping("/madres")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosMadreRespuesta> crearMadre(
            @Valid @RequestBody PorcinosMadreSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioReproduccion.crearMadre(solicitud));
    }

    @PutMapping("/madres/{id}")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosMadreRespuesta> actualizarMadre(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosMadreSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.actualizarMadre(id, solicitud));
    }

    @GetMapping("/padrillos")
    public ResponseEntity<List<PorcinosPadrilloRespuesta>> listarPadrillos(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.listarPadrillos());
    }

    @PostMapping("/padrillos")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosPadrilloRespuesta> crearPadrillo(
            @Valid @RequestBody PorcinosPadrilloSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioReproduccion.crearPadrillo(solicitud));
    }

    @GetMapping("/madres/{id}/gestacion-activa")
    public ResponseEntity<PorcinosGestacionRespuesta> obtenerGestacionActivaMadre(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.obtenerGestacionActivaMadre(id));
    }

    @GetMapping("/madres/{id}/parto-pendiente-destete")
    public ResponseEntity<PorcinosPartoRespuesta> obtenerPartoPendienteDestete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.obtenerPartoPendienteDestete(id));
    }

    @PostMapping("/madres/{id}/servicios")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosServicioRespuesta> registrarServicio(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosServicioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioReproduccion.registrarServicio(id, solicitud));
    }

    @PostMapping("/gestaciones/{id}/partos")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosPartoRespuesta> registrarParto(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosPartoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioReproduccion.registrarParto(id, solicitud));
    }

    @PostMapping("/partos/{id}/destetes")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosDesteteRespuesta> registrarDestete(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosDesteteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioReproduccion.registrarDestete(id, solicitud));
    }

    @GetMapping("/reproduccion/resumen")
    public ResponseEntity<PorcinosReproduccionResumen> resumenReproduccion(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReproduccion.resumenReproduccion());
    }

    @GetMapping("/lotes")
    public ResponseEntity<List<PorcinosLoteRespuesta>> listarLotes(
            @RequestParam(required = false) PorcinosLoteEstado estado,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @RequestParam(required = false) Long galponId,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.listarLotes(estado, delPeriodoActivo, galponId));
    }

    @GetMapping("/lotes/{id}")
    public ResponseEntity<PorcinosLoteRespuesta> obtenerLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.obtenerLote(id));
    }

    @PostMapping("/lotes")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosLoteRespuesta> crearLote(
            @Valid @RequestBody PorcinosLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioLotes.crearLote(solicitud));
    }

    @PutMapping("/lotes/{id}")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosLoteRespuesta> actualizarLote(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.actualizarLote(id, solicitud));
    }

    @PostMapping("/lotes/{id}/cierre")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosLoteRespuesta> cerrarLote(
            @PathVariable Long id,
            @RequestBody(required = false) PorcinosCierreLoteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioLotes.cerrarLote(id, solicitud));
    }

    @GetMapping("/lotes/{id}/pesadas")
    public ResponseEntity<List<PorcinosPesadaRespuesta>> listarPesadas(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarPesadas(id));
    }

    @PostMapping("/lotes/{id}/pesadas")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosPesadaRespuesta> registrarPesada(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosPesadaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarPesada(id, solicitud));
    }

    @GetMapping("/lotes/{id}/consumos")
    public ResponseEntity<List<PorcinosConsumoRespuesta>> listarConsumos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarConsumos(id));
    }

    @PostMapping("/lotes/{id}/consumos")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosConsumoRespuesta> registrarConsumo(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosConsumoSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarConsumo(id, solicitud));
    }

    @GetMapping("/lotes/{id}/muertes")
    public ResponseEntity<List<PorcinosMuerteRespuesta>> listarMuertes(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarMuertes(id));
    }

    @PostMapping("/lotes/{id}/muertes")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosMuerteRespuesta> registrarMuerte(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosMuerteSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarMuerte(id, solicitud));
    }

    @GetMapping("/eventos-sanitarios")
    public ResponseEntity<List<PorcinosEventoSanitarioRespuesta>> listarEventosSanitariosEmpresa(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitariosEmpresa());
    }

    @GetMapping("/lotes/{id}/eventos-sanitarios")
    public ResponseEntity<List<PorcinosEventoSanitarioRespuesta>> listarEventosSanitarios(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarEventosSanitarios(id));
    }

    @PostMapping("/lotes/{id}/eventos-sanitarios")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosEventoSanitarioRespuesta> registrarEventoSanitario(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosEventoSanitarioSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioOperaciones.registrarEventoSanitario(id, solicitud));
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<PorcinosVentaRespuesta>> listarVentas(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentas());
    }

    @GetMapping("/lotes/{id}/ventas")
    public ResponseEntity<List<PorcinosVentaRespuesta>> listarVentasPorLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioOperaciones.listarVentasPorLote(id));
    }

    @PostMapping("/lotes/{id}/ventas")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosVentaRespuesta> registrarVenta(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosVentaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOperaciones.registrarVenta(id, solicitud));
    }

    @GetMapping("/panel/resumen")
    public ResponseEntity<PorcinosPanelResumen> panelResumen(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioPanel.resumenPeriodoActivo());
    }

    @GetMapping("/dietas")
    public ResponseEntity<List<PorcinosDietaRespuesta>> listarDietas(
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.listarDietas());
    }

    @GetMapping("/dietas/{id}")
    public ResponseEntity<PorcinosDietaRespuesta> obtenerDieta(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.obtenerDieta(id));
    }

    @PostMapping("/dietas")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosDietaRespuesta> crearDieta(
            @Valid @RequestBody PorcinosDietaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioDietas.crearDieta(solicitud));
    }

    @PutMapping("/dietas/{id}")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosDietaRespuesta> actualizarDieta(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosDietaSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.actualizarDieta(id, solicitud));
    }

    @PostMapping("/dietas/{id}/fases")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosDietaFaseRespuesta> crearFaseDieta(
            @PathVariable Long id,
            @Valid @RequestBody PorcinosDietaFaseSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioDietas.crearFase(id, solicitud));
    }

    @PutMapping("/dietas/{id}/fases/{faseId}")
    @RequiresModule(value = "PORCINOS", permission = "write")
    public ResponseEntity<PorcinosDietaFaseRespuesta> actualizarFaseDieta(
            @PathVariable Long id,
            @PathVariable Long faseId,
            @Valid @RequestBody PorcinosDietaFaseSolicitud solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioDietas.actualizarFase(id, faseId, solicitud));
    }

    @GetMapping("/lotes/{id}/resumen-economico")
    public ResponseEntity<PorcinosResumenEconomicoRespuesta> resumenEconomicoLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioEconomia.resumenEconomicoLote(id));
    }

    @GetMapping("/reportes/{tipo}")
    public ResponseEntity<Map<String, Object>> reporte(
            @PathVariable String tipo,
            @AuthenticationPrincipal UserDetails detalles) {
        resolvedorUsuarioPeticion.requerirUsuario(detalles);
        return ResponseEntity.ok(servicioReportes.obtenerReporte(tipo));
    }
}
