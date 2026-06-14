package com.agrocloud.core.application;

import com.agrocloud.core.domain.Module;
import com.agrocloud.core.infrastructure.ModuleRepository;
import com.agrocloud.dto.ModuleDTO;
import com.agrocloud.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio Core para gestionar módulos del sistema.
 */
@Service
@Transactional
public class ModuleService {

    private static final Logger logger = LoggerFactory.getLogger(ModuleService.class);

    @Autowired
    private ModuleRepository moduleRepository;

    public List<ModuleDTO> getAllModules() {
        logger.info("Obteniendo todos los módulos");
        return moduleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ModuleDTO> getActiveModules() {
        logger.info("Obteniendo módulos activos");
        return moduleRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ModuleDTO getModuleById(Long id) {
        logger.info("Obteniendo módulo con ID: {}", id);
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con ID: " + id));
        return convertToDTO(module);
    }

    public ModuleDTO getModuleByCode(String code) {
        logger.info("Obteniendo módulo con código: {}", code);
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con código: " + code));
        return convertToDTO(module);
    }

    public ModuleDTO createModule(ModuleDTO moduleDTO) {
        logger.info("Creando nuevo módulo: {}", moduleDTO.getCode());
        if (moduleRepository.existsByCode(moduleDTO.getCode())) {
            throw new IllegalArgumentException("Ya existe un módulo con el código: " + moduleDTO.getCode());
        }
        Module module = new Module();
        module.setName(moduleDTO.getName());
        module.setCode(moduleDTO.getCode());
        module.setDescription(moduleDTO.getDescription());
        module.setActive(moduleDTO.getActive() != null ? moduleDTO.getActive() : true);
        Module saved = moduleRepository.save(module);
        logger.info("Módulo creado exitosamente con ID: {}", saved.getId());
        return convertToDTO(saved);
    }

    public ModuleDTO updateModule(Long id, ModuleDTO moduleDTO) {
        logger.info("Actualizando módulo con ID: {}", id);
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con ID: " + id));
        if (!module.getCode().equals(moduleDTO.getCode()) && moduleRepository.existsByCode(moduleDTO.getCode())) {
            throw new IllegalArgumentException("Ya existe un módulo con el código: " + moduleDTO.getCode());
        }
        module.setName(moduleDTO.getName());
        module.setCode(moduleDTO.getCode());
        module.setDescription(moduleDTO.getDescription());
        if (moduleDTO.getActive() != null) {
            module.setActive(moduleDTO.getActive());
        }
        Module updated = moduleRepository.save(module);
        logger.info("Módulo actualizado exitosamente");
        return convertToDTO(updated);
    }

    public void deleteModule(Long id) {
        logger.info("Eliminando módulo con ID: {}", id);
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con ID: " + id));
        module.setActive(false);
        moduleRepository.save(module);
        logger.info("Módulo desactivado exitosamente");
    }

    public boolean isModuleActive(String code) {
        return moduleRepository.findByCode(code)
                .map(Module::getActive)
                .orElse(false);
    }

    private ModuleDTO convertToDTO(Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setName(module.getName());
        dto.setCode(module.getCode());
        dto.setDescription(module.getDescription());
        dto.setActive(module.getActive());
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        return dto;
    }

    public Module getModuleEntityByCode(String code) {
        return moduleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con código: " + code));
    }

    public Module getModuleEntityById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con ID: " + id));
    }
}
