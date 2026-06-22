package com.agrocloud.controller;

import com.agrocloud.dto.CompanyModuleDTO;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.application.CompanyModuleService;
import com.agrocloud.core.application.EmpresaContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para obtener mÃ³dulos disponibles para el usuario actual
 */
@RestController
@RequestMapping("/api/modules")
public class ModuleUserController {

    private static final Logger logger = LoggerFactory.getLogger(ModuleUserController.class);

    @Autowired
    private CompanyModuleService companyModuleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    /**
     * Obtiene los mÃ³dulos disponibles para el usuario autenticado
     * GET /api/modules/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableModules(
            Authentication authentication,
            @RequestHeader(value = "X-Company-Id", required = false) Long empresaEncabezadoId) {
        try {
            logger.debug("Iniciando obtenciÃ³n de mÃ³dulos disponibles");
            
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                logger.warn("Usuario no autenticado intentando acceder a mÃ³dulos");
                return ResponseEntity.ok(List.of()); // Retornar lista vacÃ­a si no estÃ¡ autenticado
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            logger.debug("Email del usuario autenticado: {}", email);

            // Usar findByEmailWithRelations para cargar usuarioEmpresas y evitar LazyInitializationException
            User user = userRepository.findByEmailWithRelations(email)
                    .orElse(null);

            if (user == null) {
                logger.warn("Usuario no encontrado: {}", email);
                return ResponseEntity.ok(List.of());
            }

            logger.debug("Usuario encontrado: {}", user.getId());
            
            // Empresa: priorizar cabecera X-Company-Id (selector en frontend) si el usuario puede acceder
            Empresa empresaActiva = null;
            try {
                if (empresaEncabezadoId != null
                        && empresaContextService.usuarioPuedeAccederAEmpresa(user.getId(), empresaEncabezadoId)) {
                    empresaActiva = empresaRepository.findById(empresaEncabezadoId).orElse(null);
                    logger.debug("Empresa desde X-Company-Id: {}", empresaEncabezadoId);
                }
                if (empresaActiva == null) {
                    empresaActiva = user.getEmpresa();
                    logger.debug("Empresa desde usuario.getEmpresa(): {}", empresaActiva != null ? empresaActiva.getId() : "null");
                }
            } catch (Exception e) {
                logger.error("Error al obtener empresa del usuario: {}", e.getMessage(), e);
                return ResponseEntity.ok(List.of());
            }
            
            if (empresaActiva == null) {
                logger.warn("Usuario {} no tiene empresa asociada", email);
                return ResponseEntity.ok(List.of());
            }
            
            if (empresaActiva.getId() == null) {
                logger.error("La empresa del usuario tiene ID nulo");
                return ResponseEntity.ok(List.of());
            }
            
            logger.debug("Obteniendo mÃ³dulos para empresa: {}", empresaActiva.getId());
            
            // Obtener mÃ³dulos habilitados para la empresa
            List<CompanyModuleDTO> companyModules;
            try {
                companyModules = companyModuleService.getEnabledModulesForCompany(empresaActiva.getId());
                logger.debug("MÃ³dulos obtenidos del servicio: {}", companyModules != null ? companyModules.size() : "null");
            } catch (Exception e) {
                logger.error("Error al obtener mÃ³dulos del servicio para empresa {}: {}", 
                    empresaActiva.getId(), e.getMessage(), e);
                return ResponseEntity.ok(List.of());
            }

            if (companyModules == null) {
                logger.warn("El servicio retornÃ³ null para mÃ³dulos de empresa {}", empresaActiva.getId());
                return ResponseEntity.ok(List.of());
            }

            // Convertir a formato esperado por el frontend
            Map<String, Map<String, Object>> modulesMap = new HashMap<>();
            for (CompanyModuleDTO cm : companyModules) {
                try {
                    String moduleCode = cm.getModuleCode() != null ? cm.getModuleCode() : "";
                    // Mapear cÃ³digos en inglÃ©s a espaÃ±ol
                    String moduleId = mapModuleCodeToId(moduleCode);
                    
                    // Si ya existe un mÃ³dulo con este ID, mantener el primero
                    if (modulesMap.containsKey(moduleId)) {
                        continue;
                    }
                    
                    Map<String, Object> moduleMap = new HashMap<>();
                    moduleMap.put("id", moduleId); // "cultivos", "porcinos", etc.
                    moduleMap.put("nombre", getModuleName(moduleCode)); // Usar nombre en espaÃ±ol
                    moduleMap.put("descripcion", getModuleDescription(moduleCode));
                    moduleMap.put("icono", getModuleIcon(moduleCode));
                    moduleMap.put("color", getModuleColor(moduleCode));
                    moduleMap.put("habilitado", cm.getEnabled() != null ? cm.getEnabled() : false);
                    modulesMap.put(moduleId, moduleMap);
                } catch (Exception e) {
                    logger.error("Error al convertir mÃ³dulo {}: {}", cm.getModuleCode(), e.getMessage(), e);
                }
            }
            
            List<Map<String, Object>> modules = new java.util.ArrayList<>(modulesMap.values());

            logger.info("Retornando {} mÃ³dulos para usuario {}", modules.size(), email);
            return ResponseEntity.ok(modules);

        } catch (Exception e) {
            logger.error("Error obteniendo mÃ³dulos disponibles: {}", e.getMessage(), e);
            e.printStackTrace();
            // Retornar lista vacÃ­a con 200 para que el frontend use mÃ³dulos por defecto
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Mapea el cÃ³digo del mÃ³dulo (puede estar en inglÃ©s o espaÃ±ol) al ID en espaÃ±ol
     */
    private String mapModuleCodeToId(String moduleCode) {
        switch (moduleCode.toUpperCase()) {
            case "CROPS":
            case "CULTIVOS":
                return "cultivos";
            case "PIGS":
            case "PORCINOS":
                return "porcinos";
            case "AVICOLA_CRIANZA":
                return "avicola-crianza";
            case "AVICOLA_HUEVOS":
                return "avicola-huevos";
            case "AVICOLA_CARNE":
                return "avicola-carne";
            case "AVICOLA_PONEDORAS":
                return "avicola-ponedoras";
            case "FEEDLOT":
                return "feedlot";
            default:
                return moduleCode.toLowerCase();
        }
    }

    /**
     * Obtiene el nombre en espaÃ±ol de un mÃ³dulo por su cÃ³digo
     */
    private String getModuleName(String moduleCode) {
        switch (moduleCode.toUpperCase()) {
            case "CROPS":
            case "CULTIVOS":
                return "Cultivos";
            case "PIGS":
            case "PORCINOS":
                return "Porcinos";
            case "AVICOLA_CRIANZA":
                return "Avicola crianza";
            case "AVICOLA_HUEVOS":
                return "Avicola huevos";
            case "AVICOLA_CARNE":
                return "Avicola carne";
            case "AVICOLA_PONEDORAS":
                return "Avicola ponedoras";
            case "FEEDLOT":
                return "Engorde a corral (Feedlot)";
            default:
                return moduleCode;
        }
    }

    /**
     * Obtiene la descripcion de un modulo por su codigo
     */
    private String getModuleDescription(String moduleCode) {
        switch (moduleCode.toUpperCase()) {
            case "CROPS":
            case "CULTIVOS":
                return "Gestion de campos, lotes, cultivos y labores";
            case "PIGS":
            case "PORCINOS":
                return "Gestion de produccion porcina";
            case "AVICOLA_CRIANZA":
                return "Parrilleros y crianza: lotes, pesadas, mortalidad, ventas, consumos y sanidad.";
            case "AVICOLA_HUEVOS":
                return "Postura: lotes de puesta, produccion diaria, consumos y sanidad.";
            case "AVICOLA_CARNE":
                return "Carne avicola: lotes y operaciones (pesadas, mortalidad, ventas, consumos, sanidad).";
            case "AVICOLA_PONEDORAS":
                return "Ponedoras y recria: gestion de explotaciones independiente de carne y huevos.";
            case "FEEDLOT":
                return "Engorde bovino a corral: lotes, pesadas, alimento, sanidad, faena y closeout.";
            default:
                return "Modulo " + moduleCode;
        }
    }

    /**
     * Obtiene el icono (nombre Lucide) de un modulo por su codigo
     */
    private String getModuleIcon(String moduleCode) {
        switch (moduleCode.toUpperCase()) {
            case "CROPS":
            case "CULTIVOS":
                return "Wheat";
            case "PIGS":
            case "PORCINOS":
                return "PiggyBank";
            case "AVICOLA_CRIANZA":
                return "Bird";
            case "AVICOLA_HUEVOS":
                return "Egg";
            case "AVICOLA_CARNE":
                return "Drumstick";
            case "AVICOLA_PONEDORAS":
                return "Egg";
            case "FEEDLOT":
                return "Beef";
            default:
                return "Package";
        }
    }

    /**
     * Obtiene el color de un mÃ³dulo por su cÃ³digo
     */
    private String getModuleColor(String moduleCode) {
        switch (moduleCode.toUpperCase()) {
            case "CROPS":
            case "CULTIVOS":
                return "#10b981";
            case "PIGS":
            case "PORCINOS":
                return "#f59e0b";
            case "AVICOLA_CRIANZA":
                return "#0ea5e9";
            case "AVICOLA_HUEVOS":
                return "#eab308";
            case "AVICOLA_CARNE":
                return "#dc2626";
            case "AVICOLA_PONEDORAS":
                return "#7c3aed";
            case "FEEDLOT":
                return "#b45309";
            default:
                return "#3b82f6";
        }
    }
}

