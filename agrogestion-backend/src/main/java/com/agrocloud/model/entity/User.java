package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entidad que representa un usuario del sistema.
 * Implementa UserDetails para integración con Spring Security.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "usuarios")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id")
    @JsonIgnore
    private User creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id")
    @JsonIgnore
    private User parentUser;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con UserCompanyRole (tabla intermedia) - Sistema antiguo
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCompanyRole> userCompanyRoles = new ArrayList<>();
    
    // Relación con UsuarioEmpresa (nueva tabla multiempresa) - Sistema nuevo
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UsuarioEmpresa> usuarioEmpresas = new ArrayList<>();

    // Constructors
    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.activo = true;
    }

    // Implementación de UserDetails para Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> authorities = userCompanyRoles.stream()
                .flatMap(ucr -> ucr.getRol().getRolePermissions().stream())
                .map(rp -> rp.getPermiso().getNombre())
                .collect(Collectors.toSet());
        
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Para el login usamos email como username
    }
    
    // Getter para el campo username real
    public String getActualUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    // Métodos de utilidad
    public boolean hasRoleInCompany(String roleName, Long companyId) {
        return userCompanyRoles.stream()
                .anyMatch(ucr -> ucr.getRol().getNombre().equals(roleName) && 
                               ucr.getEmpresa().getId().equals(companyId));
    }

    public List<Empresa> getCompanies() {
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getEmpresa)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Role> getRolesInCompany(Long companyId) {
        return userCompanyRoles.stream()
                .filter(ucr -> ucr.getEmpresa().getId().equals(companyId))
                .map(UserCompanyRole::getRol)
                .collect(Collectors.toList());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<UserCompanyRole> getUserCompanyRoles() {
        return userCompanyRoles;
    }

    public void setUserCompanyRoles(List<UserCompanyRole> userCompanyRoles) {
        this.userCompanyRoles = userCompanyRoles;
    }
    
    public List<UsuarioEmpresa> getUsuarioEmpresas() {
        return usuarioEmpresas;
    }
    
    public void setUsuarioEmpresas(List<UsuarioEmpresa> usuarioEmpresas) {
        this.usuarioEmpresas = usuarioEmpresas;
    }

    // Getters y Setters para los nuevos campos
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordTokenExpiry() {
        return resetPasswordTokenExpiry;
    }

    public void setResetPasswordTokenExpiry(LocalDateTime resetPasswordTokenExpiry) {
        this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
    }

    public User getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(User creadoPor) {
        this.creadoPor = creadoPor;
    }

    public User getParentUser() {
        return parentUser;
    }

    public void setParentUser(User parentUser) {
        this.parentUser = parentUser;
    }

    // Métodos de compatibilidad con el código existente
    @JsonIgnore
    public Set<Role> getRoles() {
        // Retorna todos los roles únicos del usuario a través de las empresas
        return userCompanyRoles.stream()
                .map(UserCompanyRole::getRol)
                .collect(Collectors.toSet());
    }

    public boolean isAdmin() {
        return getRoles().stream()
                .anyMatch(role -> role.getNombre().equals("ADMINISTRADOR") || 
                                role.getNombre().equals("SUPERADMIN"));
    }

    public boolean isSuperAdmin() {
        return getRoles().stream()
                .anyMatch(role -> role.getNombre().equals("SUPERADMIN"));
    }

    public boolean canAccessUser(User targetUser) {
        // Un usuario puede acceder a otro si es admin o si es el mismo usuario
        return isAdmin() || this.getId().equals(targetUser.getId());
    }

    public Empresa getEmpresa() {
        // Retorna la primera empresa del usuario (para compatibilidad)
        return userCompanyRoles.stream()
                .findFirst()
                .map(UserCompanyRole::getEmpresa)
                .orElse(null);
    }

    public LocalDateTime getCreatedAt() {
        return fechaCreacion;
    }

    public LocalDateTime getUpdatedAt() {
        return fechaActualizacion;
    }

    // Métodos adicionales requeridos por el código existente
    public void setRoles(Set<Role> roles) {
        System.out.println("🔧 [User.setRoles] Actualizando roles del usuario (sin empresa específica)");
        System.out.println("🔧 [User.setRoles] Roles a establecer: " + roles);
        
        // Obtener la primera empresa del usuario, si existe
        final Empresa empresaParaRol;
        if (!userCompanyRoles.isEmpty()) {
            empresaParaRol = userCompanyRoles.get(0).getEmpresa();
            System.out.println("📍 [User.setRoles] Usando empresa existente: " + (empresaParaRol != null ? empresaParaRol.getId() : "null"));
        } else {
            empresaParaRol = null;
        }
        
        // Si no tiene empresa, necesitamos obtenerla del contexto (esto debería manejarse desde el servicio)
        // Por ahora, simplemente limpiamos y agregamos roles para las empresas existentes
        if (empresaParaRol != null) {
            setRolesConEmpresa(roles, empresaParaRol);
        } else {
            System.out.println("⚠️ [User.setRoles] No se puede asignar rol sin empresa. El usuario debe estar asociado a al menos una empresa.");
        }
    }
    
    /**
     * Método sobrecargado para asignar roles con una empresa específica
     */
    public void setRolesConEmpresa(Set<Role> roles, Empresa empresa) {
        System.out.println("🔧 [User.setRolesConEmpresa] Actualizando roles del usuario");
        System.out.println("🔧 [User.setRolesConEmpresa] Roles a establecer: " + roles);
        System.out.println("🔧 [User.setRolesConEmpresa] Empresa: " + (empresa != null ? empresa.getId() : "null"));
        
        if (empresa == null) {
            System.out.println("⚠️ [User.setRolesConEmpresa] Empresa es null, no se pueden asignar roles");
            return;
        }
        
        // Limpiar roles existentes para esta empresa
        userCompanyRoles.removeIf(ucr -> ucr.getEmpresa() != null && ucr.getEmpresa().equals(empresa));
        System.out.println("🗑️ [User.setRolesConEmpresa] Roles anteriores eliminados para empresa: " + empresa.getId());
        
        // Agregar nuevos roles
        if (roles != null) {
            for (Role role : roles) {
                UserCompanyRole ucr = new UserCompanyRole();
                ucr.setUsuario(this);
                ucr.setRol(role);
                ucr.setEmpresa(empresa);
                ucr.setActivo(true);
                ucr.setFechaCreacion(LocalDateTime.now());
                ucr.setFechaActualizacion(LocalDateTime.now());
                userCompanyRoles.add(ucr);
                System.out.println("➕ [User.setRolesConEmpresa] Agregado rol: " + role.getNombre() + " para empresa: " + empresa.getId());
            }
        }
        
        System.out.println("✅ [User.setRolesConEmpresa] Total userCompanyRoles: " + userCompanyRoles.size());
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.fechaActualizacion = updatedAt;
    }

    public List<User> getChildUsers() {
        // Retorna usuarios hijos (para compatibilidad)
        return new ArrayList<>();
    }

    public boolean perteneceAEmpresa(Long empresaId) {
        return userCompanyRoles.stream()
                .anyMatch(ucr -> ucr.getEmpresa().getId().equals(empresaId));
    }

    public boolean esAdministradorEmpresa(Long empresaId) {
        return userCompanyRoles.stream()
                .anyMatch(ucr -> ucr.getEmpresa().getId().equals(empresaId) && 
                                ucr.getRol().getNombre().equals("ADMINISTRADOR"));
    }
    
    /**
     * Verifica si el usuario tiene un rol específico en su empresa actual
     * Busca en ambos sistemas: el nuevo (usuario_empresas) y el antiguo (usuarios_empresas_roles)
     */
    public boolean tieneRolEnEmpresa(com.agrocloud.model.enums.RolEmpresa rolBuscado) {
        String nombreRolBuscado = rolBuscado.name();
        
        // PRIMERO: Buscar en el sistema nuevo (tabla usuario_empresas con enum RolEmpresa)
        if (usuarioEmpresas != null && !usuarioEmpresas.isEmpty()) {
            boolean encontradoEnNuevo = usuarioEmpresas.stream()
                    .filter(ue -> ue.getEstado() == com.agrocloud.model.enums.EstadoUsuarioEmpresa.ACTIVO)
                    .anyMatch(ue -> {
                        com.agrocloud.model.enums.RolEmpresa rolActual = ue.getRol();
                        if (rolActual == null) {
                            return false;
                        }
                        
                        // Aplicar mapeo de roles antiguos a nuevos
                        com.agrocloud.model.enums.RolEmpresa rolActualizado = rolActual.getRolActualizado();
                        
                        // Comparar con el rol buscado
                        return rolActualizado == rolBuscado || rolActualizado.name().equals(nombreRolBuscado);
                    });
            
            if (encontradoEnNuevo) {
                return true;
            }
        }
        
        // SEGUNDO: Buscar en el sistema antiguo (tabla usuarios_empresas_roles con tabla roles)
        if (userCompanyRoles != null && !userCompanyRoles.isEmpty()) {
            return userCompanyRoles.stream()
                    .anyMatch(ucr -> {
                        Role role = ucr.getRol();
                        if (role == null || role.getNombre() == null) {
                            return false;
                        }
                        
                        String nombreRolActual = role.getNombre();
                        
                        // Mapeo de roles antiguos a nuevos
                        if ("PRODUCTOR".equals(nombreRolActual) || 
                            "ASESOR".equals(nombreRolActual) || 
                            "TECNICO".equals(nombreRolActual)) {
                            return "JEFE_CAMPO".equals(nombreRolBuscado);
                        } else if ("CONTADOR".equals(nombreRolActual)) {
                            return "JEFE_FINANCIERO".equals(nombreRolBuscado);
                        } else if ("LECTURA".equals(nombreRolActual)) {
                            return "CONSULTOR_EXTERNO".equals(nombreRolBuscado);
                        }
                        
                        // Comparación directa para roles nuevos
                        return nombreRolActual.equals(nombreRolBuscado);
                    });
        }
        
        // No se encontró el rol en ningún sistema
        return false;
    }

    public void setEmpresa(Empresa empresa) {
        // Este método no se implementa completamente ya que las empresas se manejan a través de UserCompanyRole
        // Se mantiene para compatibilidad con el código existente
    }

    // Métodos faltantes para los tests
    public void setNombreUsuario(String nombreUsuario) {
        this.username = nombreUsuario;
    }
    
    public void setNombre(String nombre) {
        this.firstName = nombre;
    }
    
    public void setApellido(String apellido) {
        this.lastName = apellido;
    }
    
    public String getNombreUsuario() {
        return this.username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}