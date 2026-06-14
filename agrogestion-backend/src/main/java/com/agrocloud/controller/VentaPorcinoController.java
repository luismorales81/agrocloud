package com.agrocloud.controller;

import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.domain.VentaPorcino;
import com.agrocloud.porcinos.application.VentaPorcinoService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/ventas")
public class VentaPorcinoController {

    @Autowired
    private VentaPorcinoService ventaPorcinoService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<VentaPorcino> registrarVenta(
            @RequestBody VentaPorcino ventaData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            VentaPorcino venta = ventaPorcinoService.registrarVenta(ventaData, user);
            return ResponseEntity.ok(venta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar venta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<VentaPorcino>> obtenerVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<VentaPorcino> ventas = ventaPorcinoService.obtenerVentas(user, fechaDesde, fechaHasta);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            System.err.println("Error al obtener ventas: " + e.getMessage());
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

            BigDecimal ingresos = ventaPorcinoService.obtenerIngresosTotales(user, fechaDesde, fechaHasta);
            return ResponseEntity.ok(Map.of("ingresosTotales", ingresos));
        } catch (Exception e) {
            System.err.println("Error al obtener ingresos totales: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







