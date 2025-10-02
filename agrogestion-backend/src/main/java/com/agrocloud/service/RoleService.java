package com.agrocloud.service;

import com.agrocloud.model.entity.Role;
import com.agrocloud.repository.RoleRepository;
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
     * Inicializar roles globales por defecto
     * Estos roles se usan solo para el acceso general al sistema
     */
    public void initializeDefaultRoles() {
        logger.info("Inicializando roles globales por defecto...");
        
        // Crear rol Super Administrador
        createRoleIfNotExists("SUPERADMIN", "Controla el sistema completo, puede crear/eliminar empresas y usuarios");
        
        // Crear rol Usuario Registrado
        createRoleIfNotExists("USUARIO_REGISTRADO", "Usuario común que puede loguearse y acceder a empresas");
        
        // Crear rol Invitado
        createRoleIfNotExists("INVITADO", "Acceso muy limitado o de prueba");
        
        logger.info("Roles globales inicializados correctamente");
    }
    
    /**
     * Crear rol si no existe
     */
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
        return roleRepository.findByNombre(name)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + name));
    }
    
    /**
     * Verificar si un rol existe por nombre
     */
    public boolean roleExists(String name) {
        return roleRepository.existsByNombre(name);
    }
    
    /**
     * Crear nuevo rol
     */
    public Role createRole(String name, String description) {
        if (roleRepository.existsByNombre(name)) {
            throw new RuntimeException("El rol ya existe: " + name);
        }
        
        Role role = new Role();
        role.setNombre(name);
        role.setDescripcion(description);
        
        return roleRepository.save(role);
    }
    
    /**
     * Actualizar rol
     */
    public Role updateRole(Long id, String name, String description) {
        Role role = getRoleById(id);
        
        // Verificar nombre único si cambió
        if (!role.getNombre().equals(name) && roleRepository.existsByNombre(name)) {
            throw new RuntimeException("El nombre del rol ya existe: " + name);
        }
        
        role.setNombre(name);
        role.setDescripcion(description);
        
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
            roleUserCount.put(role.getNombre(), 0);
        }
        stats.put("usersPerRole", roleUserCount);
        
        return stats;
    }
    
    /**
     * Obtener permisos de un rol
     */
    public List<String> getRolePermissions(Long roleId) {
        getRoleById(roleId); // Validar que el rol existe
        // Por ahora retornamos una lista vacía ya que el modelo actual no tiene permisos implementados
        // En el futuro esto debería consultar la tabla de permisos del rol
        return new ArrayList<>();
    }
    
    /**
     * Asignar permisos a un rol
     */
    public void assignPermissions(Long roleId, List<String> permissions) {
        getRoleById(roleId); // Validar que el rol existe
        // Por ahora no hacemos nada ya que el modelo actual no tiene permisos implementados
        // En el futuro esto debería guardar los permisos en la tabla correspondiente
        // rolePermissionRepository.saveAll(permissions.stream()
        //     .map(permission -> new RolePermission(role, permission))
        //     .collect(Collectors.toList()));
    }
}
