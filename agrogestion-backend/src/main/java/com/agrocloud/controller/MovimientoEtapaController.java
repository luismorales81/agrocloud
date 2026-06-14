package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.MovimientoEtapa;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.MovimientoEtapaService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/recria")
public class MovimientoEtapaController {

    @Autowired
    private MovimientoEtapaService movimientoService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/{recriaId}/mover-etapa")
    public ResponseEntity<MovimientoEtapa> moverEntreEtapas(
            @PathVariable Long recriaId,
            @RequestBody Map<String, Object> movimientoData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            MovimientoEtapa.EtapaRecria etapaDestino = MovimientoEtapa.EtapaRecria.valueOf(
                movimientoData.get("etapaDestino").toString());
            Long loteDestinoId = movimientoData.get("loteDestinoId") != null 
                ? Long.valueOf(movimientoData.get("loteDestinoId").toString()) 
                : null;
            Integer cantidadAnimales = Integer.valueOf(movimientoData.get("cantidadAnimales").toString());
            BigDecimal pesoPromedio = movimientoData.get("pesoPromedio") != null
                ? new BigDecimal(movimientoData.get("pesoPromedio").toString())
                : null;
            String observaciones = (String) movimientoData.getOrDefault("observaciones", "");

            MovimientoEtapa movimiento = movimientoService.moverEntreEtapas(
                recriaId, etapaDestino, loteDestinoId, cantidadAnimales, pesoPromedio, observaciones, user);
            return ResponseEntity.ok(movimiento);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al mover entre etapas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{recriaId}/movimientos")
    public ResponseEntity<List<MovimientoEtapa>> obtenerMovimientosPorRecria(
            @PathVariable Long recriaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<MovimientoEtapa> movimientos = movimientoService.obtenerMovimientosPorRecria(recriaId, user);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoEtapa>> obtenerTodosLosMovimientos(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<MovimientoEtapa> movimientos = movimientoService.obtenerTodosLosMovimientos(user);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







