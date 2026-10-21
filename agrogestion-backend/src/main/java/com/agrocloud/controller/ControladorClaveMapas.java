package com.agrocloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrega la clave de Maps JavaScript API solo a una sesión autenticada.
 * La clave no se incluye en el build del frontend.
 */
@RestController
@RequestMapping("/api/mapas")
public class ControladorClaveMapas {

    private final String claveApi;

    public ControladorClaveMapas(@Value("${app.maps.clave-api:}") String claveApi) {
        this.claveApi = claveApi == null ? "" : claveApi.trim();
    }

    @GetMapping("/clave")
    public ResponseEntity<RespuestaClaveMapa> obtenerClave() {
        if (claveApi.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new RespuestaClaveMapa(claveApi));
    }

    public record RespuestaClaveMapa(String clave) {
    }
}
