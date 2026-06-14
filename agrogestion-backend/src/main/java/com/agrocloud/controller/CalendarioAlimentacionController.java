package com.agrocloud.controller;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.porcinos.application.CalendarioAlimentacionService;
import com.agrocloud.porcinos.application.DerramePerdidaService;
import com.agrocloud.porcinos.application.ConsumoDiarioAutomaticoService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.porcinos.domain.DerramePerdida;
import com.agrocloud.core.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestiÃ³n de calendario de alimentaciÃ³n diaria
 * Incluye: calendario mensual, detalle de dÃ­a, confirmaciÃ³n, derrames/pÃ©rdidas, alertas
 */
@RestController
@RequestMapping("/api/porcinos/calendario-alimentacion")
public class CalendarioAlimentacionController {

    @Autowired
    private CalendarioAlimentacionService calendarioService;

    @Autowired
    private DerramePerdidaService derrameService;

    @Autowired
    private ConsumoDiarioAutomaticoService consumoDiarioService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.agrocloud.core.application.EmpresaContextService empresaContextService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    /**
     * Obtener calendario mensual con estados
     * GET /api/porcinos/calendario-alimentacion/mensual?ano=2025&mes=1
     */
    @GetMapping("/mensual")
    public ResponseEntity<Map<LocalDate, CalendarioAlimentacionService.DiaAlimentacionDTO>> obtenerCalendarioMensual(
            @RequestParam int ano,
            @RequestParam int mes,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<LocalDate, CalendarioAlimentacionService.DiaAlimentacionDTO> calendario = 
                calendarioService.obtenerCalendarioMensual(ano, mes, user.getId());

            return ResponseEntity.ok(calendario);
        } catch (Exception e) {
            System.err.println("Error al obtener calendario mensual: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener detalle completo de un dÃ­a
     * GET /api/porcinos/calendario-alimentacion/dia/2025-01-15
     */
    @GetMapping("/dia/{fecha}")
    public ResponseEntity<CalendarioAlimentacionService.DiaAlimentacionDetalleDTO> obtenerDetalleDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            CalendarioAlimentacionService.DiaAlimentacionDetalleDTO detalle = 
                calendarioService.obtenerDetalleDia(fecha, user.getId());

            if (detalle == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            System.err.println("Error al obtener detalle del dÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Confirmar dÃ­a de alimentaciÃ³n
     * POST /api/porcinos/calendario-alimentacion/dia/{fecha}/confirmar
     * Body: { "observaciones": "..." }
     */
    /**
     * Registrar cantidad real de raciÃ³n (kg) para un consumo del dÃ­a (dÃ­a pendiente).
     * POST /api/porcinos/calendario-alimentacion/consumo/{consumoId}/cantidad-real
     * Body: { "cantidadRecetaKg": 125.5 }
     */
    @PostMapping("/consumo/{consumoId}/cantidad-real")
    public ResponseEntity<CalendarioAlimentacionService.ConsumoDiarioDTO> registrarCantidadRecetaReal(
            @PathVariable Long consumoId,
            @RequestBody CantidadRecetaRealRequest cuerpo,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null || cuerpo == null || cuerpo.getCantidadRecetaKg() == null) {
                return ResponseEntity.badRequest().build();
            }
            CalendarioAlimentacionService.ConsumoDiarioDTO actualizado =
                calendarioService.registrarCantidadRecetaReal(consumoId, cuerpo.getCantidadRecetaKg(), user.getId());
            return ResponseEntity.ok(actualizado);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar cantidad real de raciÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/dia/{fecha}/confirmar")
    public ResponseEntity<?> confirmarDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestBody(required = false) ConfirmacionDiaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            String observaciones = request != null ? request.getObservaciones() : null;

            CalendarioAlimentacionService.DiaAlimentacionDTO diaConfirmado = 
                calendarioService.confirmarDia(fecha, observaciones, user.getId());

            return ResponseEntity.ok(diaConfirmado);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Map<String, String> cuerpo = new HashMap<>();
            cuerpo.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(cuerpo);
        } catch (Exception e) {
            System.err.println("Error al confirmar dÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener alertas de stock insuficiente
     * GET /api/porcinos/calendario-alimentacion/alertas?fechaDesde=2025-01-01&fechaHasta=2025-01-31
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<CalendarioAlimentacionService.AlertaStockDTO>> obtenerAlertas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<CalendarioAlimentacionService.AlertaStockDTO> alertas = 
                calendarioService.obtenerAlertasStock(fechaDesde, fechaHasta, user.getId());

            return ResponseEntity.ok(alertas);
        } catch (Exception e) {
            System.err.println("Error al obtener alertas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Registrar derrame/pÃ©rdida
     * POST /api/porcinos/calendario-alimentacion/derrames-perdidas
     */
    @PostMapping("/derrames-perdidas")
    public ResponseEntity<DerramePerdida> registrarDerrame(
            @RequestBody DerramePerdida derrameData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            DerramePerdida derrameGuardado = derrameService.registrarDerrame(derrameData, user);

            return ResponseEntity.ok(derrameGuardado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar derrame: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener derrames/pÃ©rdidas por rango de fechas
     * GET /api/porcinos/calendario-alimentacion/derrames-perdidas?fechaDesde=2025-01-01&fechaHasta=2025-01-31
     */
    @GetMapping("/derrames-perdidas")
    public ResponseEntity<List<DerramePerdida>> obtenerDerrames(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<DerramePerdida> derrames = 
                derrameService.obtenerDerrames(fechaDesde, fechaHasta, user.getId());

            return ResponseEntity.ok(derrames);
        } catch (Exception e) {
            System.err.println("Error al obtener derrames: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener derrames/pÃ©rdidas por dÃ­a
     * GET /api/porcinos/calendario-alimentacion/derrames-perdidas/dia/2025-01-15
     */
    @GetMapping("/derrames-perdidas/dia/{fecha}")
    public ResponseEntity<List<DerramePerdida>> obtenerDerramesPorDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<DerramePerdida> derrames = 
                derrameService.obtenerDerramesPorDia(fecha, user.getId());

            return ResponseEntity.ok(derrames);
        } catch (Exception e) {
            System.err.println("Error al obtener derrames del dÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generar consumo diario manualmente (para reprocesar o forzar generaciÃ³n)
     * Solo permite generar si el dÃ­a estÃ¡ pendiente o no existe
     * POST /api/porcinos/calendario-alimentacion/generar-consumo?fecha=2025-01-15
     */
    @PostMapping("/generar-consumo")
    public ResponseEntity<CalendarioAlimentacionService.DiaAlimentacionDTO> generarConsumoManual(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar si el dÃ­a existe y estÃ¡ confirmado (no se puede regenerar)
            Optional<com.agrocloud.porcinos.domain.DiaAlimentacion> diaOpt =
                consumoDiarioService.obtenerDiaPorFecha(fecha, user.getId());

            if (diaOpt.isPresent() && diaOpt.get().estaCerrado()) {
                return ResponseEntity.badRequest().build();
            }

            // Obtener empresa del usuario
            Optional<com.agrocloud.core.domain.Empresa> empresaOpt =
                empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());

            if (empresaOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Generar consumo para la fecha (solo si estÃ¡ pendiente o no existe)
            com.agrocloud.porcinos.domain.DiaAlimentacion diaGenerado = 
                consumoDiarioService.generarConsumoDiarioParaFecha(fecha, empresaOpt.get());

            // Obtener DTO del dÃ­a generado
            CalendarioAlimentacionService.DiaAlimentacionDetalleDTO detalleDTO = 
                calendarioService.obtenerDetalleDia(fecha, user.getId());
            
            if (detalleDTO == null || detalleDTO.getDia() == null) {
                return ResponseEntity.internalServerError().build();
            }
            
            CalendarioAlimentacionService.DiaAlimentacionDTO diaDTO = detalleDTO.getDia();

            return ResponseEntity.ok(diaDTO);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al generar consumo manual: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * DTO para confirmaciÃ³n de dÃ­a
     */
    public static class ConfirmacionDiaRequest {
        private String observaciones;

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }

    public static class CantidadRecetaRealRequest {
        private BigDecimal cantidadRecetaKg;

        public BigDecimal getCantidadRecetaKg() { return cantidadRecetaKg; }
        public void setCantidadRecetaKg(BigDecimal cantidadRecetaKg) { this.cantidadRecetaKg = cantidadRecetaKg; }
    }
}
