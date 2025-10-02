package com.agrocloud.service;

import com.agrocloud.model.entity.*;
import com.agrocloud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión del sistema multiempresa.
 * Maneja usuarios, empresas, roles y permisos.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service
@Primary
@Transactional
public class MultiTenantService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserCompanyRoleRepository userCompanyRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    // Implementación de UserDetailsService para Spring Security
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        
        return user; // User implementa UserDetails
    }

    // Métodos para gestión de usuarios
    public User createUser(String username, String email, String password) {
        User user = new User(username, email, password);
        return userRepository.save(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Métodos para gestión de empresas
    public Empresa createCompany(String nombre, String cuit, String emailContacto) {
        Empresa empresa = new Empresa(nombre, cuit, emailContacto);
        return companyRepository.save(empresa);
    }

    public Optional<Empresa> findCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    public List<Empresa> getAllCompanies() {
        return companyRepository.findAll();
    }

    // Métodos para gestión de roles
    public Role createRole(String nombre, String descripcion) {
        Role role = new Role(nombre, descripcion);
        return roleRepository.save(role);
    }

    public Optional<Role> findRoleByName(String nombre) {
        return roleRepository.findByNombre(nombre);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Métodos para gestión de permisos
    public Permission createPermission(String nombre, String descripcion, String modulo, String accion) {
        Permission permission = new Permission(nombre, descripcion, modulo, accion);
        return permissionRepository.save(permission);
    }

    public Optional<Permission> findPermissionByName(String nombre) {
        return permissionRepository.findByNombre(nombre);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // Métodos para asignación de roles a usuarios en empresas
    public UserCompanyRole assignRoleToUserInCompany(Long userId, Long companyId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Empresa empresa = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        UserCompanyRole userCompanyRole = new UserCompanyRole(user, empresa, role);
        return userCompanyRoleRepository.save(userCompanyRole);
    }

    public void removeRoleFromUserInCompany(Long userId, Long companyId, Long roleId) {
        userCompanyRoleRepository.deleteByUsuarioIdAndEmpresaIdAndRolId(userId, companyId, roleId);
    }

    public List<UserCompanyRole> getUserCompanyRolesInCompany(Long userId, Long companyId) {
        return userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(userId, companyId);
    }

    public List<Empresa> getUserCompanies(Long userId) {
        return userCompanyRoleRepository.findByUsuarioId(userId)
                .stream()
                .map(UserCompanyRole::getEmpresa)
                .distinct()
                .collect(Collectors.toList());
    }

    // Métodos para asignación de permisos a roles
    public RolePermission assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));

        RolePermission rolePermission = new RolePermission(role, permission);
        return rolePermissionRepository.save(rolePermission);
    }

    public void removePermissionFromRole(Long roleId, Long permissionId) {
        rolePermissionRepository.deleteByRolIdAndPermisoId(roleId, permissionId);
    }

    public List<Permission> getRolePermissions(Long roleId) {
        return rolePermissionRepository.findByRolId(roleId)
                .stream()
                .map(RolePermission::getPermiso)
                .collect(Collectors.toList());
    }

    // Métodos para verificación de permisos
    public boolean userHasPermissionInCompany(Long userId, Long companyId, String permissionName) {
        List<UserCompanyRole> userRoles = userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(userId, companyId);
        
        return userRoles.stream()
                .anyMatch(ucr -> ucr.getRol().hasPermission(permissionName));
    }

    public Set<String> getUserPermissionsInCompany(Long userId, Long companyId) {
        List<UserCompanyRole> userRoles = userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(userId, companyId);
        
        return userRoles.stream()
                .flatMap(ucr -> ucr.getRol().getRolePermissions().stream())
                .map(rp -> rp.getPermiso().getNombre())
                .collect(Collectors.toSet());
    }

    public List<Role> getUserRolesInCompany(Long userId, Long companyId) {
        return userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(userId, companyId)
                .stream()
                .map(UserCompanyRole::getRol)
                .collect(Collectors.toList());
    }

    // Métodos de utilidad
    public boolean isUserActiveInCompany(Long userId, Long companyId) {
        return userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(userId, companyId)
                .stream()
                .anyMatch(UserCompanyRole::isActive);
    }

    public void deactivateUserInCompany(Long userId, Long companyId) {
        List<UserCompanyRole> userRoles = userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(userId, companyId);
        userRoles.forEach(ucr -> ucr.setActivo(false));
        userCompanyRoleRepository.saveAll(userRoles);
    }

    public void activateUserInCompany(Long userId, Long companyId) {
        List<UserCompanyRole> userRoles = userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(userId, companyId);
        userRoles.forEach(ucr -> ucr.setActivo(true));
        userCompanyRoleRepository.saveAll(userRoles);
    }

    // Métodos para estadísticas
    public long countUsersInCompany(Long companyId) {
        return userCompanyRoleRepository.findByEmpresaId(companyId)
                .stream()
                .map(UserCompanyRole::getUsuario)
                .distinct()
                .count();
    }

    public long countActiveUsersInCompany(Long companyId) {
        return userCompanyRoleRepository.findByEmpresaId(companyId)
                .stream()
                .filter(UserCompanyRole::isActive)
                .map(UserCompanyRole::getUsuario)
                .distinct()
                .count();
    }

    public List<User> getUsersInCompany(Long companyId) {
        return userCompanyRoleRepository.findByEmpresaId(companyId)
                .stream()
                .map(UserCompanyRole::getUsuario)
                .distinct()
                .collect(Collectors.toList());
    }
}
