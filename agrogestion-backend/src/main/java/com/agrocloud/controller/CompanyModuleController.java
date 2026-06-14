package com.agrocloud.controller;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.dto.CompanyModuleDTO;
import com.agrocloud.core.application.CompanyModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestiÃ³n de mÃ³dulos de empresas
 */
@RestController
@RequestMapping("/api/admin/companies")
public class CompanyModuleController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyModuleController.class);

    @Autowired
    private CompanyModuleService companyModuleService;

    /**
     * Habilita un mÃ³dulo para una empresa
     * POST /api/admin/companies/{companyId}/modules/{moduleId}/enable
     */
    @PostMapping("/{companyId}/modules/{moduleId}/enable")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<CompanyModuleDTO> enableModule(@PathVariable Long companyId, 
                                                          @PathVariable Long moduleId) {
        logger.info("Habilitando mÃ³dulo {} para empresa {}", moduleId, companyId);
        CompanyModuleDTO result = companyModuleService.enableModuleForCompany(companyId, moduleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Deshabilita un mÃ³dulo para una empresa
     * DELETE /api/admin/companies/{companyId}/modules/{moduleId}/disable
     */
    @DeleteMapping("/{companyId}/modules/{moduleId}/disable")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Void> disableModule(@PathVariable Long companyId, 
                                               @PathVariable Long moduleId) {
        logger.info("Deshabilitando mÃ³dulo {} para empresa {}", moduleId, companyId);
        companyModuleService.disableModuleForCompany(companyId, moduleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene todos los mÃ³dulos habilitados para una empresa
     * GET /api/admin/companies/{companyId}/modules
     * IMPORTANTE: Solo SuperAdmin puede ver la configuraciÃ³n de mÃ³dulos
     */
    @GetMapping("/{companyId}/modules")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<CompanyModuleDTO>> getCompanyModules(@PathVariable Long companyId) {
        logger.info("Obteniendo mÃ³dulos para empresa {}", companyId);
        List<CompanyModuleDTO> modules = companyModuleService.getEnabledModulesForCompany(companyId);
        return ResponseEntity.ok(modules);
    }
}

