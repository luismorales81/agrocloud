package com.agrocloud.core.application;

import com.agrocloud.dto.CompanyModuleDTO;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.core.domain.CompanyModule;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Module;
import com.agrocloud.core.infrastructure.CompanyModuleRepository;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.porcinos.application.InicializacionPorcinoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio Core para gestionar módulos habilitados por empresa.
 */
@Service
@Transactional
public class CompanyModuleService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyModuleService.class);

    @Autowired
    private CompanyModuleRepository companyModuleRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ModuleService moduleService;

    @Autowired(required = false)
    private InicializacionPorcinoService inicializacionPorcinoService;

    public CompanyModuleDTO enableModuleForCompany(Long companyId, Long moduleId) {
        logger.info("Habilitando módulo {} para empresa {}", moduleId, companyId);

        Empresa empresa = empresaRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + companyId));

        Module module = moduleService.getModuleEntityById(moduleId);

        if (!module.getActive()) {
            throw new IllegalArgumentException("El módulo no está activo");
        }

        CompanyModule companyModule = companyModuleRepository.findByCompanyAndModule(empresa, module)
                .orElse(null);

        boolean esPrimeraVez = companyModule == null || !companyModule.getEnabled();

        if (companyModule != null) {
            companyModule.setEnabled(true);
            companyModule = companyModuleRepository.save(companyModule);
        } else {
            companyModule = new CompanyModule(empresa, module, true);
            companyModule = companyModuleRepository.save(companyModule);
        }

        if (esPrimeraVez && "pigs".equalsIgnoreCase(module.getCode()) && inicializacionPorcinoService != null) {
            try {
                logger.info("Inicializando datos por defecto del módulo Porcinos para empresa {}", companyId);
                inicializacionPorcinoService.inicializarDatosPorDefecto(empresa);
                logger.info("Datos por defecto inicializados exitosamente");
            } catch (Exception e) {
                logger.error("Error al inicializar datos por defecto del módulo Porcinos", e);
            }
        }

        logger.info("Módulo habilitado exitosamente");
        return convertToDTO(companyModule);
    }

    public void disableModuleForCompany(Long companyId, Long moduleId) {
        logger.info("Deshabilitando módulo {} para empresa {}", moduleId, companyId);

        CompanyModule companyModule = companyModuleRepository.findByCompanyIdAndModuleId(companyId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación empresa-módulo no encontrada"));

        companyModule.setEnabled(false);
        companyModuleRepository.save(companyModule);

        logger.info("Módulo deshabilitado exitosamente");
    }

    public List<CompanyModuleDTO> getEnabledModulesForCompany(Long companyId) {
        logger.info("Obteniendo módulos habilitados para empresa {}", companyId);
        return companyModuleRepository.findEnabledModulesByCompanyId(companyId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean hasModuleEnabled(Long companyId, String moduleCode) {
        Module module = moduleService.getModuleEntityByCode(moduleCode);
        return companyModuleRepository.existsByCompanyIdAndModuleIdAndEnabledTrue(companyId, module.getId());
    }

    public boolean hasModuleEnabled(Long companyId, Long moduleId) {
        return companyModuleRepository.existsByCompanyIdAndModuleIdAndEnabledTrue(companyId, moduleId);
    }

    public List<CompanyModuleDTO> getAllModulesForCompany(Long companyId) {
        logger.info("Obteniendo todos los módulos para empresa {}", companyId);
        return companyModuleRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CompanyModuleDTO convertToDTO(CompanyModule companyModule) {
        CompanyModuleDTO dto = new CompanyModuleDTO();
        dto.setId(companyModule.getId());
        dto.setCompanyId(companyModule.getCompany().getId());
        dto.setCompanyName(companyModule.getCompany().getNombre());
        dto.setModuleId(companyModule.getModule().getId());
        dto.setModuleCode(companyModule.getModule().getCode());
        dto.setModuleName(companyModule.getModule().getName());
        dto.setEnabled(companyModule.getEnabled());
        dto.setCreatedAt(companyModule.getCreatedAt());
        dto.setUpdatedAt(companyModule.getUpdatedAt());
        return dto;
    }
}
