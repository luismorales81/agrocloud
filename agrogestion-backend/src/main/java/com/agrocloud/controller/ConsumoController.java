package com.agrocloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/consumos")
public class ConsumoController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllConsumos(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/historial")
    public ResponseEntity<List<Map<String, Object>>> getHistorialConsumos(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearConsumo(@RequestBody Map<String, Object> consumo, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new java.util.HashMap<>());
    }
}







