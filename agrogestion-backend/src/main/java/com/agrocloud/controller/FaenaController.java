package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.Faena;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.FaenaService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestiÃ³n de faenas
 * 
 * @deprecated Este controlador ha sido unificado con VentaPorcinoController.
 *            Para registrar faenas, usar POST /api/v1/porcinos/ventas 
 *            con tipo = "FAENA" en el body.
 *            Este controlador se mantiene por compatibilidad temporal.
 * 
 * @see VentaPorcinoController
 */
@Deprecated
@SuppressWarnings("deprecation")
@RestController
@RequestMapping("/api/v1/porcinos/faena")
public class FaenaController {

    @Autowired
    private FaenaService faenaService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    /** Escritura deshabilitada: usar POST /api/v1/porcinos/ventas con tipo FAENA. */
    @PostMapping("/{recriaId}")
    public ResponseEntity<Faena> registrarFaena(
            @PathVariable Long recriaId,
            @RequestBody Faena faenaData,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    @GetMapping("/{recriaId}")
    public ResponseEntity<List<Faena>> obtenerFaenasPorRecria(
            @PathVariable Long recriaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Faena> faenas = faenaService.obtenerFaenasPorRecria(recriaId, user);
            return ResponseEntity.ok(faenas);
        } catch (Exception e) {
            System.err.println("Error al obtener faenas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Faena>> obtenerTodasLasFaenas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Faena> faenas = faenaService.obtenerTodasLasFaenas(user);
            return ResponseEntity.ok(faenas);
        } catch (Exception e) {
            System.err.println("Error al obtener faenas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ingresos-totales")
    public ResponseEntity<Map<String, Object>> obtenerIngresosTotales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            BigDecimal ingresos = faenaService.obtenerIngresosTotales(fechaDesde, fechaHasta, user);
            return ResponseEntity.ok(Map.of("ingresosTotales", ingresos));
        } catch (Exception e) {
            System.err.println("Error al obtener ingresos totales: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







