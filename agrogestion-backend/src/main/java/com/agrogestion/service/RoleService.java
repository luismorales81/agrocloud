package com.agrogestion.service;

import com.agrogestion.model.entity.Role;
import com.agrogestion.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class RoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * Inicializar roles por defecto
     */
    public void initializeDefaultRoles() {
        logger.info("Inicializando roles por defecto...");
        
        // Crear rol Administrador
        createRoleIfNotExists("ADMINISTRADOR", "Acceso total al sistema");
        
        // Crear rol Operario
        createRoleIfNotExists("OPERARIO", "Registrar trabajos en lotes y consumir insumos");
        
        // Crear rol Ingeniero Agrónomo
        createRoleIfNotExists("INGENIERO_AGRONOMO", "Ver lotes y registrar recomendaciones técnicas");
        
        // Crear rol Invitado
        createRoleIfNotExists("INVITADO", "Solo lectura");
        
        logger.info("Roles por defecto inicializados correctamente");
    }
    
    /**
     * Crear rol si no existe
     */
    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            logger.info("Rol creado: {}", name);
        } else {
            logger.info("Rol ya existe: {}", name);
        }
    }
    
    /**
     * Obtener todos los roles
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * Obtener rol por ID
     */
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }
    
    /**
     * Obtener rol por nombre
     */
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + name));
    }
    
    /**
     * Crear nuevo rol
     */
    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("El rol ya existe: " + name);
        }
        
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        
        return roleRepository.save(role);
    }
    
    /**
     * Actualizar rol
     */
    public Role updateRole(Long id, String name, String description) {
        Role role = getRoleById(id);
        
        // Verificar nombre único si cambió
        if (!role.getName().equals(name) && roleRepository.existsByName(name)) {
            throw new RuntimeException("El nombre del rol ya existe: " + name);
        }
        
        role.setName(name);
        role.setDescription(description);
        
        return roleRepository.save(role);
    }
    
    /**
     * Eliminar rol
     */
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        roleRepository.delete(role);
    }
    

    
    /**
     * Obtener permisos disponibles
     */
    public List<String> getAvailablePermissions() {
        return new java.util.ArrayList<>();
    }
    
    /**
     * Obtener estadísticas de roles
     */
    public Map<String, Object> getRoleStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Role> roles = getAllRoles();
        stats.put("totalRoles", roles.size());
        
        Map<String, Integer> roleUserCount = new HashMap<>();
        for (Role role : roles) {
            roleUserCount.put(role.getName(), 0);
        }
        stats.put("usersPerRole", roleUserCount);
        
        return stats;
    }
}
