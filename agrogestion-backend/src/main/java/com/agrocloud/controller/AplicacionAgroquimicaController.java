package com.agrocloud.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aplicaciones-agroquimicas")
@CrossOrigin(origins = "*")
public class AplicacionAgroquimicaController {
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAplicaciones(
            @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("🔍 [AplicacionAgroquimicaController] getAllAplicaciones - INICIO");
        System.out.println("🔍 [AplicacionAgroquimicaController] Usuario: " + userDetails.getUsername());
        
        // Retornar una lista vacía por ahora
        List<Map<String, Object>> aplicaciones = new ArrayList<>();
        
        System.out.println("🔍 [AplicacionAgroquimicaController] Retornando lista vacía");
        return ResponseEntity.ok(aplicaciones);
    }
}
