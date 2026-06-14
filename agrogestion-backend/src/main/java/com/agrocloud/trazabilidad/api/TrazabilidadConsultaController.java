package com.agrocloud.trazabilidad.api;

import com.agrocloud.trazabilidad.infrastructure.TrazabilidadCertificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de consulta para explorar certificaciones y reglas activas.
 * No modifica datos (solo lectura).
 */
@RestController
@RequestMapping("/api/trazabilidad")
public class TrazabilidadConsultaController {

    @Autowired
    private TrazabilidadCertificacionRepository certificacionRepository;

    @GetMapping("/certificaciones")
    public ResponseEntity<List<Map<String, Object>>> listarCertificaciones() {
        List<Map<String, Object>> r = certificacionRepository.findActivasConReglasOrdenadas().stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "codigo", c.getCodigo(),
                        "descripcion", c.getDescripcion() != null ? c.getDescripcion() : "",
                        "activa", c.isActiva(),
                        "cantidadReglas", c.getReglas() != null ? c.getReglas().size() : 0
                ))
                .toList();
        return ResponseEntity.ok(r);
    }

    @GetMapping("/certificaciones/{codigo}")
    public ResponseEntity<Map<String, Object>> obtenerCertificacion(@PathVariable String codigo) {
        var c = certificacionRepository.findByCodigoConReglas(codigo)
                .orElseThrow(() -> new IllegalArgumentException("CertificaciÃ³n no encontrada: " + codigo));
        var reglas = c.getReglas().stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "tipoRegla", r.getTipoRegla(),
                        "parametrosJson", r.getParametrosJson(),
                        "ordenEjecucion", r.getOrdenEjecucion(),
                        "activa", r.isActiva()
                ))
                .toList();
        return ResponseEntity.ok(Map.of(
                "id", c.getId(),
                "codigo", c.getCodigo(),
                "descripcion", c.getDescripcion() != null ? c.getDescripcion() : "",
                "activa", c.isActiva(),
                "reglas", reglas
        ));
    }
}

