package com.agrocloud.controller;
import com.agrocloud.core.domain.User;

import com.agrocloud.porcinos.domain.DatosEconomicosPorcino;
import com.agrocloud.porcinos.domain.ParametrosEstablecimientoPorcino;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.application.ParametrosPorcinoService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para gestionar parÃ¡metros del establecimiento, productivos y econÃ³micos
 */
@RestController
@RequestMapping("/api/v1/porcinos/parametros")
public class ParametrosPorcinoController {

    @Autowired
    private ParametrosPorcinoService parametrosService;

    @Autowired
    private UserService userService;

    private Long obtenerUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
        return user != null ? user.getId() : null;
    }

    // ============================================================================
    // PARÃMETROS DEL ESTABLECIMIENTO
    // ============================================================================

    @GetMapping("/establecimiento")
    @Transactional(readOnly = true)
    public ResponseEntity<ParametrosEstablecimientoPorcino> obtenerParametrosEstablecimiento(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            Optional<ParametrosEstablecimientoPorcino> parametros = 
                parametrosService.obtenerParametrosEstablecimiento(userId);
            // Retornar null con 200 OK si no hay datos (comportamiento esperado para configuraciÃ³n inicial)
            return ResponseEntity.ok(parametros.orElse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/establecimiento")
    public ResponseEntity<ParametrosEstablecimientoPorcino> guardarParametrosEstablecimiento(
            @RequestBody ParametrosEstablecimientoPorcino parametros,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(parametrosService.guardarParametrosEstablecimiento(parametros, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================================
    // PARÃMETROS PRODUCTIVOS
    // ============================================================================

    @GetMapping("/productivos")
    @Transactional(readOnly = true)
    public ResponseEntity<ParametrosProductivosPorcino> obtenerParametrosProductivos(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            Optional<ParametrosProductivosPorcino> parametros = 
                parametrosService.obtenerParametrosProductivos(userId);
            // Retornar null con 200 OK si no hay datos (comportamiento esperado para configuraciÃ³n inicial)
            return ResponseEntity.ok(parametros.orElse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/productivos")
    public ResponseEntity<ParametrosProductivosPorcino> guardarParametrosProductivos(
            @RequestBody ParametrosProductivosPorcino parametros,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(parametrosService.guardarParametrosProductivos(parametros, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================================
    // DATOS ECONÃ“MICOS
    // ============================================================================

    @GetMapping("/economicos")
    @Transactional(readOnly = true)
    public ResponseEntity<DatosEconomicosPorcino> obtenerDatosEconomicos(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            Optional<DatosEconomicosPorcino> datos = 
                parametrosService.obtenerDatosEconomicos(userId);
            // Retornar null con 200 OK si no hay datos (comportamiento esperado para configuraciÃ³n inicial)
            return ResponseEntity.ok(datos.orElse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/economicos")
    public ResponseEntity<DatosEconomicosPorcino> guardarDatosEconomicos(
            @RequestBody DatosEconomicosPorcino datos,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(parametrosService.guardarDatosEconomicos(datos, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}

