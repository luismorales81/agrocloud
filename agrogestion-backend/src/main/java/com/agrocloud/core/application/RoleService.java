package com.agrocloud.core.application;

import com.agrocloud.core.domain.Role;
import com.agrocloud.core.infrastructure.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Servicio de aplicación Core para la gestión de roles.
 */
@Service("roleServiceCore")
@Transactional
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Inicializar roles globales por defecto
     * Estos roles se usan solo para el acceso general al sistema
     */
    public void initializeDefaultRoles() {
        logger.info("Inicializando roles globales por defecto...");

        createRoleIfNotExists("SUPERADMIN", "Controla el sistema completo, puede crear/eliminar empresas y usuarios");
        createRoleIfNotExists("USUARIO_REGISTRADO", "Usuario común que puede loguearse y acceder a empresas");
        createRoleIfNotExists("INVITADO", "Acceso muy limitado o de prueba");

        logger.info("Roles globales inicializados correctamente");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByNombre(name)) {
            Role role = new Role();
            role.setNombre(name);
            role.setDescripcion(description);
            roleRepository.save(role);
            logger.info("Rol creado: {}", name);
        } else {
            logger.info("Rol ya existe: {}", name);
        }
    }

    public List<Role> getAllRoles() {
        return roleRepository.findByActivoTrue();
    }

    public List<Role> getAllRolesIncludingInactive() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByNombre(name)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + name));
    }

    public boolean roleExists(String name) {
        return roleRepository.existsByNombre(name);
    }

    public Role createRole(String name, String description) {
        if (roleRepository.existsByNombre(name)) {
            throw new RuntimeException("El rol ya existe: " + name);
        }
        Role role = new Role();
        role.setNombre(name);
        role.setDescripcion(description);
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, String name, String description) {
        Role role = getRoleById(id);
        if (!role.getNombre().equals(name) && roleRepository.existsByNombre(name)) {
            throw new RuntimeException("El nombre del rol ya existe: " + name);
        }
        role.setNombre(name);
        role.setDescripcion(description);
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        roleRepository.delete(role);
    }

    public List<String> getAvailablePermissions() {
        return new ArrayList<>();
    }

    public Map<String, Object> getRoleStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Role> roles = getAllRoles();
        stats.put("totalRoles", roles.size());
        Map<String, Integer> roleUserCount = new HashMap<>();
        for (Role role : roles) {
            roleUserCount.put(role.getNombre(), 0);
        }
        stats.put("usersPerRole", roleUserCount);
        return stats;
    }

    public List<String> getRolePermissions(Long roleId) {
        getRoleById(roleId);
        return new ArrayList<>();
    }

    public void assignPermissions(Long roleId, List<String> permissions) {
        getRoleById(roleId);
    }
}
