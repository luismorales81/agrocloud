package com.agrocloud.controller;

import com.agrocloud.model.enums.RolEmpresa;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles-empresa")
public class RolEmpresaController {

    /**
     * Obtiene todos los roles de empresa disponibles
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> obtenerRolesEmpresa() {
        List<Map<String, String>> roles = Arrays.stream(RolEmpresa.values())
                .map(rol -> Map.of(
                        "value", rol.name(),
                        "label", rol.getDescripcion()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(roles);
    }
}
