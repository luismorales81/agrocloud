package com.agrocloud.controller;

import com.agrocloud.model.dto.TransferenciaLechonDTO;
import com.agrocloud.porcinos.domain.TransferenciaLechon;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.TransferenciaLechonService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/partos")
public class TransferenciaLechonController {

    @Autowired
    private TransferenciaLechonService transferenciaService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/transferencias")
    public ResponseEntity<TransferenciaLechon> registrarTransferencia(
            @RequestBody Map<String, Object> transferenciaData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Long partoOrigenId = Long.valueOf(transferenciaData.get("partoOrigenId").toString());
            Long partoDestinoId = Long.valueOf(transferenciaData.get("partoDestinoId").toString());
            Integer cantidad = Integer.valueOf(transferenciaData.get("cantidad").toString());
            String motivo = (String) transferenciaData.getOrDefault("motivo", "");
            String observaciones = (String) transferenciaData.getOrDefault("observaciones", "");

            TransferenciaLechon transferencia = transferenciaService.registrarTransferencia(
                partoOrigenId, partoDestinoId, cantidad, motivo, observaciones, user);
            return ResponseEntity.ok(transferencia);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar transferencia: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{partoId}/transferencias")
    public ResponseEntity<List<TransferenciaLechonDTO>> obtenerTransferenciasPorParto(
            @PathVariable Long partoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<TransferenciaLechonDTO> transferencias = transferenciaService.obtenerTransferenciasPorParto(partoId, user);
            return ResponseEntity.ok(transferencias);
        } catch (Exception e) {
            System.err.println("Error al obtener transferencias: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/transferencias")
    public ResponseEntity<List<TransferenciaLechonDTO>> obtenerTodasLasTransferencias(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<TransferenciaLechonDTO> transferencias = transferenciaService.obtenerTodasLasTransferencias(user);
            return ResponseEntity.ok(transferencias);
        } catch (Exception e) {
            System.err.println("Error al obtener transferencias: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







