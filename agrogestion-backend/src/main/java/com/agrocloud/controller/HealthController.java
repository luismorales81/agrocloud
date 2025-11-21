package com.agrocloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    private static final String BACKEND_VERSION = "1.1.0";
    private static final String FRONTEND_VERSION = "1.0.0";

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("service", "AgroCloud Backend");
        healthStatus.put("version", BACKEND_VERSION);
        
        return ResponseEntity.ok(healthStatus);
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getVersion() {
        Map<String, Object> versionInfo = new HashMap<>();
        
        // Versiones principales
        Map<String, String> versions = new HashMap<>();
        versions.put("backend", BACKEND_VERSION);
        versions.put("frontend", FRONTEND_VERSION);
        versionInfo.put("versions", versions);
        
        // Versiones por módulo (matriz de versiones)
        Map<String, String> modules = new HashMap<>();
        modules.put("auth", "1.0.0");
        modules.put("admin-global", "1.0.0");
        modules.put("empresas", "1.0.0");
        modules.put("campos", "1.0.0");
        modules.put("cultivos", "1.0.0");
        modules.put("cosechas", "1.0.0");
        modules.put("insumos", "1.0.0");
        modules.put("agroquimicos", "1.1.0");
        modules.put("labores", "1.0.0");
        modules.put("maquinaria", "1.0.0");
        modules.put("finanzas", "1.0.0");
        modules.put("rendimientos", "1.0.0");
        modules.put("dashboard", "1.0.0");
        modules.put("weather", "1.0.0");
        modules.put("roles", "1.0.0");
        versionInfo.put("modules", modules);
        
        // Información adicional
        versionInfo.put("timestamp", LocalDateTime.now());
        versionInfo.put("environment", System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "default"));
        
        return ResponseEntity.ok(versionInfo);
    }
}
