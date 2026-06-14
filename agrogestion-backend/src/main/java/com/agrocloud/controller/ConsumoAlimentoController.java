package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.ConsumoAlimento;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.ConsumoAlimentoService;
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
@RequestMapping("/api/v1/porcinos/alimentacion")
public class ConsumoAlimentoController {

    @Autowired
    private ConsumoAlimentoService consumoAlimentoService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/consumos")
    public ResponseEntity<ConsumoAlimento> registrarConsumo(
            @RequestBody ConsumoAlimento consumoData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            ConsumoAlimento consumo = consumoAlimentoService.registrarConsumo(consumoData, user);
            return ResponseEntity.ok(consumo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar consumo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/consumos")
    public ResponseEntity<List<ConsumoAlimento>> obtenerConsumos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<ConsumoAlimento> consumos = consumoAlimentoService.obtenerConsumos(user, fechaDesde, fechaHasta);
            return ResponseEntity.ok(consumos);
        } catch (Exception e) {
            System.err.println("Error al obtener consumos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/consumos/total-categoria")
    public ResponseEntity<Map<String, Object>> obtenerConsumoTotalPorCategoria(
            @RequestParam String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            ConsumoAlimento.CategoriaAlimento categoriaEnum;
            try {
                categoriaEnum = ConsumoAlimento.CategoriaAlimento.valueOf(categoria.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }

            BigDecimal total = consumoAlimentoService.obtenerConsumoTotalPorCategoria(
                user, categoriaEnum, fechaDesde, fechaHasta);
            
            return ResponseEntity.ok(Map.of(
                "categoria", categoria,
                "totalKg", total
            ));
        } catch (Exception e) {
            System.err.println("Error al obtener consumo total: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







