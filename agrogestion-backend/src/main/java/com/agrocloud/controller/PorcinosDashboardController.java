package com.agrocloud.controller;

import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.DashboardPorcinosService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/dashboard")
public class PorcinosDashboardController {

    @Autowired
    private DashboardPorcinosService dashboardPorcinosService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKPIs(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> kpis = dashboardPorcinosService.obtenerKPIs(user);
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            System.err.println("Error al obtener KPIs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/alertas")
    public ResponseEntity<Map<String, Object>> getAlertas(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> kpis = dashboardPorcinosService.obtenerKPIs(user);
            Map<String, Object> response = new HashMap<>();
            response.put("alertas", kpis.getOrDefault("alertas", new java.util.ArrayList<>()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al obtener alertas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}



