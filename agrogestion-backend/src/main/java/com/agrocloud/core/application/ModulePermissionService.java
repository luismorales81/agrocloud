package com.agrocloud.core.application;

import com.agrocloud.core.domain.Role;
import com.agrocloud.core.infrastructure.RoleRepository;
import com.agrocloud.dto.ModuleRolePermissionDTO;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.core.domain.Module;
import com.agrocloud.core.domain.ModuleRolePermission;
import com.agrocloud.core.infrastructure.ModuleRolePermissionRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.infrastructure.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio Core para gestionar permisos de roles sobre módulos.
 */
@Service
@Transactional
public class ModulePermissionService {

    private static final Logger logger = LoggerFactory.getLogger(ModulePermissionService.class);

    @Autowired
    private ModuleRolePermissionRepository moduleRolePermissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public ModuleRolePermissionDTO assignPermissions(Long roleId, Long moduleId,
                                                     Boolean read, Boolean write, Boolean manage) {
        logger.info("Asignando permisos al rol {} sobre módulo {}", roleId, moduleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));

        Module module = moduleService.getModuleEntityById(moduleId);

        ModuleRolePermission permission = moduleRolePermissionRepository.findByRoleAndModule(role, module)
                .orElse(null);

        if (permission != null) {
            permission.setRead(read != null ? read : false);
            permission.setWrite(write != null ? write : false);
            permission.setManage(manage != null ? manage : false);
            permission = moduleRolePermissionRepository.save(permission);
        } else {
            permission = new ModuleRolePermission(role, module, read, write, manage);
            permission = moduleRolePermissionRepository.save(permission);
        }

        logger.info("Permisos asignados exitosamente");
        return convertToDTO(permission);
    }

    public ModuleRolePermissionDTO getPermissions(Long roleId, Long moduleId) {
        ModuleRolePermission permission = moduleRolePermissionRepository.findByRoleIdAndModuleId(roleId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Permisos no encontrados"));
        return convertToDTO(permission);
    }

    public List<ModuleRolePermissionDTO> getPermissionsByRole(Long roleId) {
        return moduleRolePermissionRepository.findByRoleId(roleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean hasReadPermission(Long userId, String moduleCode) {
        return checkPermission(userId, moduleCode, "read");
    }

    public boolean hasWritePermission(Long userId, String moduleCode) {
        return checkPermission(userId, moduleCode, "write");
    }

    public boolean hasManagePermission(Long userId, String moduleCode) {
        return checkPermission(userId, moduleCode, "manage");
    }

    private boolean checkPermission(Long userId, String moduleCode, String permissionType) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        List<Role> userRoles = userRoleRepository.findActiveRolesForUser(userId);
        if (userRoles.isEmpty()) {
            return false;
        }

        for (Role role : userRoles) {
            ModuleRolePermission permission = moduleRolePermissionRepository
                    .findByRoleIdAndModuleCode(role.getId(), moduleCode)
                    .orElse(null);

            if (permission != null) {
                switch (permissionType) {
                    case "read":
                        if (permission.hasReadPermission()) return true;
                        break;
                    case "write":
                        if (permission.hasWritePermission()) return true;
                        break;
                    case "manage":
                        if (permission.hasManagePermission()) return true;
                        break;
                }
            }
        }
        return false;
    }

    private ModuleRolePermissionDTO convertToDTO(ModuleRolePermission permission) {
        ModuleRolePermissionDTO dto = new ModuleRolePermissionDTO();
        dto.setId(permission.getId());
        dto.setRoleId(permission.getRole().getId());
        dto.setRoleName(permission.getRole().getNombre());
        dto.setModuleId(permission.getModule().getId());
        dto.setModuleCode(permission.getModule().getCode());
        dto.setModuleName(permission.getModule().getName());
        dto.setRead(permission.getRead());
        dto.setWrite(permission.getWrite());
        dto.setManage(permission.getManage());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        return dto;
    }
}
