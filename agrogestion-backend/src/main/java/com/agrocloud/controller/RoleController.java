package com.agrocloud.controller;

import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.model.entity.Role;
import com.agrocloud.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Endpoints para gestión de roles y permisos")
@CrossOrigin(origins = "*")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @GetMapping
    @Operation(summary = "Obtener roles", description = "Listar todos los roles disponibles")
    public ResponseEntity<List<Role>> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID", description = "Obtener información de un rol específico")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID del rol no puede ser nulo");
        }
        
        try {
            Role role = roleService.getRoleById(id);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
    }
    
    @GetMapping("/name/{name}")
    @Operation(summary = "Obtener rol por nombre", description = "Obtener rol por su nombre")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre del rol no puede ser nulo o vacío");
        }
        
        try {
            Role role = roleService.getRoleByName(name);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Rol no encontrado con nombre: " + name);
        }
    }
    
    @PostMapping
    @Operation(summary = "Crear rol", description = "Crear un nuevo rol")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<Role> createRole(@RequestBody Map<String, Object> request) {
        if (request == null) {
            throw new IllegalArgumentException("Datos del rol no pueden ser nulos");
        }
        
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre del rol es obligatorio");
        }
        
        // Verificar si el rol ya existe
        if (roleService.roleExists(name)) {
            throw new ResourceConflictException("Ya existe un rol con el nombre: " + name);
        }
        
        Role role = roleService.createRole(name, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Actualizar información de un rol")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<Role> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        if (id == null) {
            throw new IllegalArgumentException("ID del rol no puede ser nulo");
        }
        
        if (request == null) {
            throw new IllegalArgumentException("Datos del rol no pueden ser nulos");
        }
        
        // Verificar si el rol existe
        try {
            roleService.getRoleById(id);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
        
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre del rol es obligatorio");
        }
        
        Role role = roleService.updateRole(id, name, description);
        return ResponseEntity.ok(role);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Eliminar un rol del sistema")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID del rol no puede ser nulo");
        }
        
        // Verificar si el rol existe
        try {
            roleService.getRoleById(id);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
        
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
    

    
    @GetMapping("/permissions/available")
    @Operation(summary = "Permisos disponibles", description = "Listar todos los permisos disponibles")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<String>> getAvailablePermissions() {
        try {
            List<String> permissions = roleService.getAvailablePermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}/permissions")
    @Operation(summary = "Obtener permisos de rol", description = "Obtener los permisos asignados a un rol específico")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<String>> getRolePermissions(@PathVariable Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID del rol no puede ser nulo");
        }
        
        try {
            // Verificar si el rol existe
            roleService.getRoleById(id);
            List<String> permissions = roleService.getRolePermissions(id);
            return ResponseEntity.ok(permissions);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
    }
    
    @PutMapping("/{id}/permissions")
    @Operation(summary = "Asignar permisos a rol", description = "Asignar permisos a un rol específico")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<Map<String, String>> assignPermissions(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        if (id == null) {
            throw new IllegalArgumentException("ID del rol no puede ser nulo");
        }
        
        if (request == null) {
            throw new IllegalArgumentException("Datos de permisos no pueden ser nulos");
        }
        
        try {
            // Verificar si el rol existe
            roleService.getRoleById(id);
            
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) request.get("permissions");
            if (permissions == null) {
                throw new IllegalArgumentException("Lista de permisos es obligatoria");
            }
            
            roleService.assignPermissions(id, permissions);
            return ResponseEntity.ok(Map.of("message", "Permisos asignados correctamente"));
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
    }
    

    
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de roles", description = "Obtener estadísticas de roles")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<Map<String, Object>> getRoleStats() {
        try {
            Map<String, Object> stats = roleService.getRoleStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/initialize")
    @Operation(summary = "Inicializar roles", description = "Inicializar roles por defecto del sistema")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<Map<String, String>> initializeDefaultRoles() {
        try {
            roleService.initializeDefaultRoles();
            return ResponseEntity.ok(Map.of("message", "Roles inicializados correctamente"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
