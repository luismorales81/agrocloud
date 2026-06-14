package com.agrocloud.controller;

import com.agrocloud.dto.ModuleDTO;
import com.agrocloud.core.application.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestiÃ³n de mÃ³dulos (solo SuperAdmin)
 */
@RestController
@RequestMapping("/api/admin/modules")
public class ModuleController {

    private static final Logger logger = LoggerFactory.getLogger(ModuleController.class);

    @Autowired
    private ModuleService moduleService;

    /**
     * Obtiene todos los mÃ³dulos
     * GET /api/admin/modules
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        logger.info("Obteniendo todos los mÃ³dulos");
        List<ModuleDTO> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    /**
     * Obtiene un mÃ³dulo por ID
     * GET /api/admin/modules/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ModuleDTO> getModuleById(@PathVariable Long id) {
        logger.info("Obteniendo mÃ³dulo con ID: {}", id);
        ModuleDTO module = moduleService.getModuleById(id);
        return ResponseEntity.ok(module);
    }

    /**
     * Crea un nuevo mÃ³dulo
     * POST /api/admin/modules
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ModuleDTO> createModule(@RequestBody ModuleDTO moduleDTO) {
        logger.info("Creando nuevo mÃ³dulo: {}", moduleDTO.getCode());
        ModuleDTO created = moduleService.createModule(moduleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un mÃ³dulo existente
     * PATCH /api/admin/modules/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable Long id, @RequestBody ModuleDTO moduleDTO) {
        logger.info("Actualizando mÃ³dulo con ID: {}", id);
        ModuleDTO updated = moduleService.updateModule(id, moduleDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina (desactiva) un mÃ³dulo
     * DELETE /api/admin/modules/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        logger.info("Eliminando mÃ³dulo con ID: {}", id);
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}











