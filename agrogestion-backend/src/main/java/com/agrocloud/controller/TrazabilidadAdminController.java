package com.agrocloud.controller;

import com.agrocloud.trazabilidad.application.TrazabilidadReglaGestionService;
import com.agrocloud.trazabilidad.dto.SolicitudActualizacionParametrosRegla;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Operaciones de administraciÃ³n sobre reglas de certificaciÃ³n de trazabilidad.
 * Requiere rol administrador (ruta bajo /api/admin).
 */
@RestController
@RequestMapping("/api/admin/trazabilidad")
public class TrazabilidadAdminController {

    @Autowired
    private TrazabilidadReglaGestionService reglaGestionService;

    @PutMapping("/reglas/{id}")
    public ResponseEntity<Map<String, String>> actualizarParametrosRegla(
            @PathVariable("id") long idRegla,
            @Valid @RequestBody SolicitudActualizacionParametrosRegla solicitud) {
        reglaGestionService.actualizarParametrosJson(idRegla, solicitud.getParametrosJson());
        return ResponseEntity.ok(Map.of("mensaje", "Parametros actualizados"));
    }
}
