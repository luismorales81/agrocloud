package com.agrocloud.controller;

import com.agrocloud.model.entity.*;
import com.agrocloud.service.MultiTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controlador para la gestión del sistema multiempresa.
 * Demuestra cómo usar las entidades y servicios del sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/multitenant")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
@Tag(name = "Multi-Tenant", description = "Gestión del sistema multiempresa")
public class MultiTenantController {

    @Autowired
    private MultiTenantService multiTenantService;

    // Endpoints para gestión de usuarios
    @GetMapping("/usuarios")
    @Operation(summary = "Obtener todos los usuarios", description = "Lista todos los usuarios del sistema")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = multiTenantService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/usuarios")
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = multiTenantService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(user);
    }

    // Endpoints para gestión de empresas
    @GetMapping("/empresas")
    @Operation(summary = "Obtener todas las empresas", description = "Lista todas las empresas del sistema")
    @PreAuthorize("hasAuthority('COMPANY_READ')")
    public ResponseEntity<List<Empresa>> getAllCompanies() {
        List<Empresa> companies = multiTenantService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    @PostMapping("/empresas")
    @Operation(summary = "Crear empresa", description = "Crea una nueva empresa en el sistema")
    @PreAuthorize("hasAuthority('COMPANY_CREATE')")
    public ResponseEntity<Empresa> createCompany(@RequestBody CreateCompanyRequest request) {
        Empresa empresa = multiTenantService.createCompany(request.getNombre(), request.getCuit(), request.getEmailContacto());
        return ResponseEntity.ok(empresa);
    }

    // Endpoints para gestión de roles
    @GetMapping("/roles")
    @Operation(summary = "Obtener todos los roles", description = "Lista todos los roles del sistema")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = multiTenantService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/roles")
    @Operation(summary = "Crear rol", description = "Crea un nuevo rol en el sistema")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<Role> createRole(@RequestBody CreateRoleRequest request) {
        Role role = multiTenantService.createRole(request.getNombre(), request.getDescripcion());
        return ResponseEntity.ok(role);
    }

    // Endpoints para gestión de permisos
    @GetMapping("/permisos")
    @Operation(summary = "Obtener todos los permisos", description = "Lista todos los permisos del sistema")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = multiTenantService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @PostMapping("/permisos")
    @Operation(summary = "Crear permiso", description = "Crea un nuevo permiso en el sistema")
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<Permission> createPermission(@RequestBody CreatePermissionRequest request) {
        Permission permission = multiTenantService.createPermission(
            request.getNombre(), 
            request.getDescripcion(), 
            request.getModulo(), 
            request.getAccion()
        );
        return ResponseEntity.ok(permission);
    }

    // Endpoints para asignación de roles
    @PostMapping("/asignar-rol")
    @Operation(summary = "Asignar rol a usuario en empresa", description = "Asigna un rol específico a un usuario en una empresa")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserCompanyRole> assignRoleToUser(
            @RequestBody AssignRoleRequest request) {
        UserCompanyRole userCompanyRole = multiTenantService.assignRoleToUserInCompany(
            request.getUserId(), 
            request.getCompanyId(), 
            request.getRoleId()
        );
        return ResponseEntity.ok(userCompanyRole);
    }

    @DeleteMapping("/asignar-rol")
    @Operation(summary = "Remover rol de usuario en empresa", description = "Remueve un rol específico de un usuario en una empresa")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> removeRoleFromUser(@RequestBody AssignRoleRequest request) {
        multiTenantService.removeRoleFromUserInCompany(request.getUserId(), request.getCompanyId(), request.getRoleId());
        return ResponseEntity.ok().build();
    }

    // Endpoints para asignación de permisos
    @PostMapping("/asignar-permiso")
    @Operation(summary = "Asignar permiso a rol", description = "Asigna un permiso específico a un rol")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<RolePermission> assignPermissionToRole(
            @RequestBody AssignPermissionRequest request) {
        RolePermission rolePermission = multiTenantService.assignPermissionToRole(
            request.getRoleId(), 
            request.getPermissionId()
        );
        return ResponseEntity.ok(rolePermission);
    }

    @DeleteMapping("/asignar-permiso")
    @Operation(summary = "Remover permiso de rol", description = "Remueve un permiso específico de un rol")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<Void> removePermissionFromRole(@RequestBody AssignPermissionRequest request) {
        multiTenantService.removePermissionFromRole(request.getRoleId(), request.getPermissionId());
        return ResponseEntity.ok().build();
    }

    // Endpoints para consultas específicas del usuario autenticado
    @GetMapping("/mi-empresa/{companyId}/roles")
    @Operation(summary = "Obtener mis roles en empresa", description = "Obtiene los roles del usuario autenticado en una empresa específica")
    public ResponseEntity<List<Role>> getMyRolesInCompany(
            @PathVariable Long companyId, 
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<Role> roles = multiTenantService.getUserRolesInCompany(userId, companyId);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/mi-empresa/{companyId}/permisos")
    @Operation(summary = "Obtener mis permisos en empresa", description = "Obtiene los permisos del usuario autenticado en una empresa específica")
    public ResponseEntity<Set<String>> getMyPermissionsInCompany(
            @PathVariable Long companyId, 
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Set<String> permissions = multiTenantService.getUserPermissionsInCompany(userId, companyId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/mis-empresas")
    @Operation(summary = "Obtener mis empresas", description = "Obtiene todas las empresas del usuario autenticado")
    public ResponseEntity<List<Empresa>> getMyCompanies(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<Empresa> companies = multiTenantService.getUserCompanies(userId);
        return ResponseEntity.ok(companies);
    }

    // Endpoints para verificación de permisos
    @GetMapping("/verificar-permiso/{companyId}/{permissionName}")
    @Operation(summary = "Verificar permiso", description = "Verifica si el usuario autenticado tiene un permiso específico en una empresa")
    public ResponseEntity<Boolean> checkPermission(
            @PathVariable Long companyId, 
            @PathVariable String permissionName, 
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean hasPermission = multiTenantService.userHasPermissionInCompany(userId, companyId, permissionName);
        return ResponseEntity.ok(hasPermission);
    }

    // Endpoints para estadísticas
    @GetMapping("/empresa/{companyId}/estadisticas")
    @Operation(summary = "Estadísticas de empresa", description = "Obtiene estadísticas de usuarios en una empresa")
    @PreAuthorize("hasAuthority('COMPANY_READ')")
    public ResponseEntity<CompanyStats> getCompanyStats(@PathVariable Long companyId) {
        long totalUsers = multiTenantService.countUsersInCompany(companyId);
        long activeUsers = multiTenantService.countActiveUsersInCompany(companyId);
        
        CompanyStats stats = new CompanyStats();
        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(activeUsers);
        stats.setInactiveUsers(totalUsers - activeUsers);
        
        return ResponseEntity.ok(stats);
    }

    // Clases DTO para requests
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class CreateCompanyRequest {
        private String nombre;
        private String cuit;
        private String emailContacto;

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCuit() { return cuit; }
        public void setCuit(String cuit) { this.cuit = cuit; }
        public String getEmailContacto() { return emailContacto; }
        public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    }

    public static class CreateRoleRequest {
        private String nombre;
        private String descripcion;

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    public static class CreatePermissionRequest {
        private String nombre;
        private String descripcion;
        private String modulo;
        private String accion;

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getModulo() { return modulo; }
        public void setModulo(String modulo) { this.modulo = modulo; }
        public String getAccion() { return accion; }
        public void setAccion(String accion) { this.accion = accion; }
    }

    public static class AssignRoleRequest {
        private Long userId;
        private Long companyId;
        private Long roleId;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
    }

    public static class AssignPermissionRequest {
        private Long roleId;
        private Long permissionId;

        // Getters and Setters
        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
        public Long getPermissionId() { return permissionId; }
        public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
    }

    public static class CompanyStats {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;

        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
    }
}
