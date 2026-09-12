package com.agrocloud.controller;

import com.agrocloud.core.application.CampanaService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.dto.CampanaDTO;
import com.agrocloud.dto.CrearCampanaRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campanas")
public class CampanaController {

    private static final Logger logger = LoggerFactory.getLogger(CampanaController.class);

    @Autowired
    @Qualifier("campanaServiceCore")
    private CampanaService campanaService;

    @Autowired
    private ServicioSeguridadContexto servicioSeguridadContexto;

    @GetMapping
    public ResponseEntity<List<CampanaDTO>> listar(Authentication authentication) {
        try {
            Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
            return ResponseEntity.ok(campanaService.listarPorEmpresa(empresaId));
        } catch (IllegalStateException e) {
            logger.warn("Listar campañas — contexto inválido: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error listando campañas: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/activa")
    public ResponseEntity<CampanaDTO> obtenerActiva(Authentication authentication) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return ResponseEntity.ok(campanaService.obtenerActiva(empresaId));
    }

    @PostMapping
    public ResponseEntity<CampanaDTO> crear(@Valid @RequestBody CrearCampanaRequest request,
                                            Authentication authentication) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return ResponseEntity.ok(campanaService.crear(empresaId, request));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<CampanaDTO> activar(@PathVariable Long id, Authentication authentication) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return ResponseEntity.ok(campanaService.activar(empresaId, id));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<CampanaDTO> cerrar(@PathVariable Long id, Authentication authentication) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return ResponseEntity.ok(campanaService.cerrar(empresaId, id));
    }
}
