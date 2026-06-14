package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.RegistroPeso;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.RegistroPesoService;
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
public class RegistroPesoController {

    @Autowired
    private RegistroPesoService registroPesoService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/{recriaId}/pesos")
    public ResponseEntity<RegistroPeso> registrarPeso(
            @PathVariable Long recriaId,
            @RequestBody RegistroPeso registroPesoData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            RegistroPeso registro = registroPesoService.registrarPeso(recriaId, registroPesoData, user);
            return ResponseEntity.ok(registro);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar peso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{recriaId}/pesos")
    public ResponseEntity<List<RegistroPeso>> obtenerRegistrosPorRecria(
            @PathVariable Long recriaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<RegistroPeso> registros = registroPesoService.obtenerRegistrosPorRecria(recriaId, user);
            return ResponseEntity.ok(registros);
        } catch (Exception e) {
            System.err.println("Error al obtener registros de peso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{recriaId}/gdp")
    public ResponseEntity<Map<String, Object>> calcularGDP(
            @PathVariable Long recriaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            BigDecimal gdp = registroPesoService.calcularGDP(recriaId, user);
            return ResponseEntity.ok(Map.of("gdp", gdp, "unidad", "kg/dÃ­a"));
        } catch (Exception e) {
            System.err.println("Error al calcular GDP: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







