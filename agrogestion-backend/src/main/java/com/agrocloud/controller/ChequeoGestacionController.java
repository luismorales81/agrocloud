package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.ChequeoGestacion;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.ChequeoGestacionService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/porcinos/gestacion")
public class ChequeoGestacionController {

    @Autowired
    private ChequeoGestacionService chequeoGestacionService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/{gestacionId}/chequeos")
    public ResponseEntity<ChequeoGestacion> registrarChequeo(
            @PathVariable Long gestacionId,
            @RequestBody ChequeoGestacion chequeoData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            ChequeoGestacion chequeo = chequeoGestacionService.registrarChequeo(gestacionId, chequeoData, user);
            return ResponseEntity.ok(chequeo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar chequeo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{gestacionId}/chequeos")
    public ResponseEntity<List<ChequeoGestacion>> obtenerChequeos(
            @PathVariable Long gestacionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<ChequeoGestacion> chequeos = chequeoGestacionService.obtenerChequeosPorGestacion(gestacionId, user);
            return ResponseEntity.ok(chequeos);
        } catch (Exception e) {
            System.err.println("Error al obtener chequeos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







