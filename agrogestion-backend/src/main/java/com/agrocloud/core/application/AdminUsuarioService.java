package com.agrocloud.core.application;

import com.agrocloud.dto.AdminUsuarioDTO;
import com.agrocloud.dto.RoleDTO;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.EstadoUsuario;
import com.agrocloud.core.domain.Role;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UserCompanyRole;
import com.agrocloud.core.domain.UsuarioEmpresaRol;
import com.agrocloud.core.infrastructure.RoleRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.infrastructure.UserCompanyRoleRepository;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para la administración de usuarios
 */
@Service("adminUsuarioServiceCore")
@Transactional
public class AdminUsuarioService {

    @Autowired
    @Qualifier("userRepositoryCore")
    private UserRepository userRepository;

    @Autowired
    @Qualifier("roleRepositoryCore")
    private RoleRepository roleRepository;

    @Autowired
    @Qualifier("userCompanyRoleRepositoryCore")
    private UserCompanyRoleRepository userCompanyRoleRepository;
    
    @Autowired
    @Qualifier("usuarioEmpresaRolRepositoryCore")
    private UsuarioEmpresaRolRepository usuarioEmpresaRolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    @Qualifier("empresaContextServiceCore")
    private com.agrocloud.core.application.EmpresaContextService empresaContextService;
    
    @Autowired
    @Qualifier("empresaUsuarioServiceCore")
    private com.agrocloud.core.application.EmpresaUsuarioService empresaUsuarioService;
    
    @Autowired
    @Qualifier("userServiceCore")
    private com.agrocloud.core.application.UserService userService;


    /**
     * Obtener todos los usuarios para administración
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AdminUsuarioDTO> obtenerTodosLosUsuarios() {
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("📋 [AdminUsuarioService] OBTENER TODOS LOS USUARIOS - VERSION 3.0");
        System.out.println("════════════════════════════════════════════════════════");
        List<User> usuarios = userRepository.findAllWithRoles();
        System.out.println("✅ Usuarios encontrados en BD: " + usuarios.size());
        
        // Inicializar relaciones lazy para todos los usuarios dentro de la transacción
        for (User usuario : usuarios) {
            try {
                // Inicializar usuarioEmpresas (ya debería estar cargado con JOIN FETCH)
                if (usuario.getUsuarioEmpresas() != null) {
                    usuario.getUsuarioEmpresas().size();
                    usuario.getUsuarioEmpresas().forEach(ue -> {
                        if (ue.getRol() != null) ue.getRol().name();
                        if (ue.getEmpresa() != null) ue.getEmpresa().getId();
                    });
                }
                // Inicializar roles del sistema legacy
                if (usuario.getRoles() != null) {
                    usuario.getRoles().size();
                    usuario.getRoles().forEach(role -> {
                        if (role != null) role.getNombre();
                    });
                }
                // Inicializar userCompanyRoles (sistema antiguo)
                if (usuario.getUserCompanyRoles() != null) {
                    usuario.getUserCompanyRoles().size();
                    usuario.getUserCompanyRoles().forEach(ucr -> {
                        if (ucr.getRol() != null) ucr.getRol().getNombre();
                        if (ucr.getEmpresa() != null) ucr.getEmpresa().getId();
                    });
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioService] Error inicializando relaciones lazy para usuario " + usuario.getEmail() + ": " + e.getMessage());
            }
        }
        
        if (!usuarios.isEmpty()) {
            User primerUsuario = usuarios.get(0);
            System.out.println("👤 Primer usuario: " + primerUsuario.getEmail());
            System.out.println("🎭 Roles del primer usuario (legacy): " + primerUsuario.getRoles());
            if (primerUsuario.getUsuarioEmpresas() != null) {
                System.out.println("🎭 UsuarioEmpresas del primer usuario: " + primerUsuario.getUsuarioEmpresas().size());
                primerUsuario.getUsuarioEmpresas().forEach(ue -> {
                    if (ue.getRol() != null) {
                        System.out.println("  - Rol: " + ue.getRol().name() + ", Estado: " + ue.getEstado());
                    }
                });
            }
        }
        
        List<AdminUsuarioDTO> usuariosDTO = usuarios.stream()
                .map(this::convertirAAdminDTO)
                .collect(Collectors.toList());
        
        if (!usuariosDTO.isEmpty()) {
            System.out.println("📊 Primer DTO - Roles: " + usuariosDTO.get(0).getRoles());
        }
        
        System.out.println("✅ Usuarios convertidos a DTO: " + usuariosDTO.size());
        System.out.println("════════════════════════════════════════════════════════");
        return usuariosDTO;
    }

    /**
     * Obtener usuarios subordinados de un usuario específico
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AdminUsuarioDTO> obtenerUsuariosSubordinados(Long usuarioId) {
        System.out.println("🔍 [AdminUsuarioService] Obteniendo usuarios subordinados para usuario ID: " + usuarioId);
        
        // Obtener usuarios donde el usuario actual es el padre (parentUser)
        List<User> usuariosSubordinados = userRepository.findByParentUserId(usuarioId);
        System.out.println("🔍 [AdminUsuarioService] Usuarios subordinados encontrados: " + usuariosSubordinados.size());
        
        // También obtener usuarios creados por este usuario
        User usuarioPadre = userRepository.findById(usuarioId).orElse(null);
        if (usuarioPadre != null) {
            List<User> usuariosCreados = userRepository.findByCreadoPorOrderByFechaCreacionDesc(usuarioPadre);
            System.out.println("🔍 [AdminUsuarioService] Usuarios creados encontrados: " + usuariosCreados.size());
            
            // Combinar ambas listas sin duplicados
            Set<User> usuariosUnicos = new HashSet<>();
            usuariosUnicos.addAll(usuariosSubordinados);
            usuariosUnicos.addAll(usuariosCreados);
            
            usuariosSubordinados = new ArrayList<>(usuariosUnicos);
        }
        
        // Inicializar relaciones lazy para todos los usuarios dentro de la transacción
        for (User usuario : usuariosSubordinados) {
            try {
                // Cargar usuario con todas las relaciones si no están cargadas
                if (usuario.getUsuarioEmpresas() == null || usuario.getUsuarioEmpresas().isEmpty()) {
                    User usuarioCompleto = userRepository.findByEmailWithAllRelations(usuario.getEmail()).orElse(usuario);
                    // Copiar relaciones si están cargadas
                    if (usuarioCompleto.getUsuarioEmpresas() != null && !usuarioCompleto.getUsuarioEmpresas().isEmpty()) {
                        // Las relaciones ya están en el objeto original si se usó findAllWithRoles
                    }
                }
                // Inicializar relaciones lazy
                if (usuario.getUsuarioEmpresas() != null) {
                    usuario.getUsuarioEmpresas().size();
                    usuario.getUsuarioEmpresas().forEach(ue -> {
                        if (ue.getRol() != null) ue.getRol().name();
                        if (ue.getEmpresa() != null) ue.getEmpresa().getId();
                    });
                }
                if (usuario.getRoles() != null) {
                    usuario.getRoles().size();
                    usuario.getRoles().forEach(role -> {
                        if (role != null) role.getNombre();
                    });
                }
                if (usuario.getUserCompanyRoles() != null) {
                    usuario.getUserCompanyRoles().size();
                    usuario.getUserCompanyRoles().forEach(ucr -> {
                        if (ucr.getRol() != null) ucr.getRol().getNombre();
                        if (ucr.getEmpresa() != null) ucr.getEmpresa().getId();
                    });
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioService] Error inicializando relaciones lazy para usuario " + usuario.getEmail() + ": " + e.getMessage());
            }
        }
        
        List<AdminUsuarioDTO> usuariosDTO = usuariosSubordinados.stream()
                .map(this::convertirAAdminDTO)
                .collect(Collectors.toList());
        
        System.out.println("🔍 [AdminUsuarioService] Total usuarios subordinados convertidos a DTO: " + usuariosDTO.size());
        return usuariosDTO;
    }

    /**
     * Obtener usuario por ID para administración
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AdminUsuarioDTO obtenerUsuarioPorId(Long id) {
        // Cargar usuario con todas las relaciones
        User usuario = userService.findByEmailWithAllRelationsCombined(
            userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id))
                .getEmail()
        );
        
        // Inicializar relaciones lazy dentro de la transacción
        try {
            if (usuario.getUsuarioEmpresas() != null) {
                usuario.getUsuarioEmpresas().size();
                usuario.getUsuarioEmpresas().forEach(ue -> {
                    if (ue.getRol() != null) ue.getRol().name();
                    if (ue.getEmpresa() != null) ue.getEmpresa().getId();
                });
            }
            if (usuario.getRoles() != null) {
                usuario.getRoles().size();
                usuario.getRoles().forEach(role -> {
                    if (role != null) role.getNombre();
                });
            }
            if (usuario.getUserCompanyRoles() != null) {
                usuario.getUserCompanyRoles().size();
                usuario.getUserCompanyRoles().forEach(ucr -> {
                    if (ucr.getRol() != null) ucr.getRol().getNombre();
                    if (ucr.getEmpresa() != null) ucr.getEmpresa().getId();
                });
            }
        } catch (Exception e) {
            System.err.println("⚠️ [AdminUsuarioService] Error inicializando relaciones lazy para usuario ID " + id + ": " + e.getMessage());
        }
        
        return convertirAAdminDTO(usuario);
    }

    /**
     * Crear nuevo usuario con prevención automática de jerarquía
     */
    public AdminUsuarioDTO crearUsuario(AdminUsuarioDTO usuarioDTO, User creadoPor) {
        // VALIDACIÓN DE PERMISOS: Solo ADMIN o SUPERADMIN pueden crear usuarios
        if (creadoPor == null) {
            throw new RuntimeException("No se pudo identificar al usuario que está creando el nuevo usuario.");
        }
        
        // Agregar logging para debug
        System.out.println("🔍 [AdminUsuarioService] Verificando permisos para crear usuario:");
        System.out.println("  - Usuario: " + creadoPor.getEmail());
        System.out.println("  - isSuperAdmin: " + creadoPor.isSuperAdmin());
        System.out.println("  - isAdmin: " + creadoPor.isAdmin());
        System.out.println("  - tienePermisoParaCrearUsuarios: " + tienePermisoParaCrearUsuarios(creadoPor));
        
        if (!tienePermisoParaCrearUsuarios(creadoPor)) {
            System.err.println("❌ [AdminUsuarioService] Usuario sin permisos para crear usuarios:");
            System.err.println("  - Email: " + creadoPor.getEmail());
            System.err.println("  - isSuperAdmin: " + creadoPor.isSuperAdmin());
            System.err.println("  - isAdmin: " + creadoPor.isAdmin());
            throw new RuntimeException("No tienes permisos para crear usuarios. Solo los administradores (ADMINISTRADOR o SUPERADMIN) pueden crear nuevos usuarios en el sistema.");
        }

        // Validar que el email y username no existan
        if (userRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + usuarioDTO.getEmail());
        }
        if (userRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe: " + usuarioDTO.getUsername());
        }

        // Crear nuevo usuario
        User nuevoUsuario = new User();
        nuevoUsuario.setUsername(usuarioDTO.getUsername());
        // Contraseña temporal por defecto - el usuario debe cambiarla en el primer login
        nuevoUsuario.setPassword(passwordEncoder.encode("admin123")); // Contraseña temporal unificada
        nuevoUsuario.setFirstName(usuarioDTO.getFirstName());
        nuevoUsuario.setLastName(usuarioDTO.getLastName());
        nuevoUsuario.setEmail(usuarioDTO.getEmail());
        nuevoUsuario.setPhone(usuarioDTO.getPhone());
        nuevoUsuario.setEstado(EstadoUsuario.PENDIENTE);
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setEmailVerified(false);
        
        // PREVENCIÓN AUTOMÁTICA: Siempre asignar creador (ya validado antes que no sea null)
        nuevoUsuario.setCreadoPor(creadoPor);

        // PREVENCIÓN AUTOMÁTICA: Asignar jerarquía automáticamente
        nuevoUsuario.setParentUser(asignarUsuarioPadreAutomaticamente(usuarioDTO, creadoPor));

        // Guardar el usuario primero (sin roles) para obtener un ID
        User usuarioCreado = userRepository.save(nuevoUsuario);
        System.out.println("✅ Usuario creado con ID: " + usuarioCreado.getId());

        // Asignar roles si se especifican (desde roleIds o roles)
        if (usuarioDTO.getRoleIds() != null && !usuarioDTO.getRoleIds().isEmpty()) {
            System.out.println("🎭 RoleIds recibidos en creación: " + usuarioDTO.getRoleIds());
            Set<Role> roles = usuarioDTO.getRoleIds().stream()
                    .map(roleId -> {
                        System.out.println("🔍 Buscando rol con ID: " + roleId);
                        return roleRepository.findById(roleId).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            System.out.println("✅ Roles encontrados: " + roles.size() + " roles");
            
            if (roles.isEmpty()) {
                System.err.println("❌ No se encontraron roles válidos para los IDs proporcionados: " + usuarioDTO.getRoleIds());
                throw new RuntimeException("No se encontraron roles válidos para los IDs proporcionados. Verifica que los roles existan y estén activos.");
            }
            
            // Obtener la empresa del administrador que está creando el usuario
            var empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(creadoPor.getId());
            
            if (empresaOpt.isPresent()) {
                System.out.println("🏢 Asignando roles para empresa del administrador: " + empresaOpt.get().getId());
                sincronizarRolesLegacy(usuarioCreado, empresaOpt.get(), roles);
                usuarioCreado = userRepository.save(usuarioCreado);
                
                // Asignar usuario a la empresa en la tabla usuario_empresas con TODOS los roles
                System.out.println("🏢 Asignando usuario a empresa en tabla usuario_empresas con múltiples roles...");
                try {
                    // Mapear TODOS los roles del sistema a roles de empresa
                    Set<com.agrocloud.model.enums.RolEmpresa> rolesEmpresa = roles.stream()
                            .map(this::mapearRolSistemaARolEmpresa)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    
                    System.out.println("🎭 Roles de empresa a asignar: " + rolesEmpresa.stream()
                            .map(com.agrocloud.model.enums.RolEmpresa::name)
                            .collect(Collectors.joining(", ")));
                    
                    if (!rolesEmpresa.isEmpty()) {
                        empresaUsuarioService.asignarRolesAUsuarioEnEmpresa(
                                usuarioCreado.getId(), 
                                empresaOpt.get().getId(), 
                                rolesEmpresa
                        );
                        System.out.println("✅ Usuario asignado exitosamente a empresa: " + empresaOpt.get().getId() + " con " + rolesEmpresa.size() + " roles");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error asignando usuario a empresa: " + e.getMessage());
                    e.printStackTrace();
                    // No lanzar excepción para no fallar la creación del usuario
                }
            } else {
                System.out.println("❌ El administrador no tiene empresa asignada, no se pueden asignar roles");
                throw new RuntimeException("El administrador no tiene empresa asignada. No se pueden asignar roles al nuevo usuario.");
            }
        } else if (usuarioDTO.getRoles() != null && !usuarioDTO.getRoles().isEmpty()) {
            System.out.println("🎭 Roles (DTO) recibidos en creación: " + usuarioDTO.getRoles());
            Set<Role> roles = usuarioDTO.getRoles().stream()
                    .map(roleDTO -> roleRepository.findById(roleDTO.getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            System.out.println("✅ Roles encontrados: " + roles.size() + " roles");
            
            if (roles.isEmpty()) {
                System.err.println("❌ No se encontraron roles válidos para los IDs proporcionados en el DTO");
                throw new RuntimeException("No se encontraron roles válidos para los IDs proporcionados. Verifica que los roles existan y estén activos.");
            }
            
            // Obtener la empresa del administrador que está creando el usuario
            var empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(creadoPor.getId());
            
            if (empresaOpt.isPresent()) {
                System.out.println("🏢 Asignando roles para empresa del administrador: " + empresaOpt.get().getId());
                sincronizarRolesLegacy(usuarioCreado, empresaOpt.get(), roles);
                usuarioCreado = userRepository.save(usuarioCreado);
                
                // Asignar usuario a la empresa en la tabla usuario_empresas con TODOS los roles
                System.out.println("🏢 Asignando usuario a empresa en tabla usuario_empresas con múltiples roles...");
                try {
                    // Mapear TODOS los roles del sistema a roles de empresa
                    Set<com.agrocloud.model.enums.RolEmpresa> rolesEmpresa = roles.stream()
                            .map(this::mapearRolSistemaARolEmpresa)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    
                    System.out.println("🎭 Roles de empresa a asignar: " + rolesEmpresa.stream()
                            .map(com.agrocloud.model.enums.RolEmpresa::name)
                            .collect(Collectors.joining(", ")));
                    
                    if (!rolesEmpresa.isEmpty()) {
                        empresaUsuarioService.asignarRolesAUsuarioEnEmpresa(
                                usuarioCreado.getId(), 
                                empresaOpt.get().getId(), 
                                rolesEmpresa
                        );
                        System.out.println("✅ Usuario asignado exitosamente a empresa: " + empresaOpt.get().getId() + " con " + rolesEmpresa.size() + " roles");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error asignando usuario a empresa: " + e.getMessage());
                    e.printStackTrace();
                    // No lanzar excepción para no fallar la creación del usuario
                }
            } else {
                System.out.println("❌ El administrador no tiene empresa asignada, no se pueden asignar roles");
                throw new RuntimeException("El administrador no tiene empresa asignada. No se pueden asignar roles al nuevo usuario.");
            }
        }

        System.out.println("✅ Usuario creado exitosamente con roles: " + usuarioCreado.getRoles());
        return convertirAAdminDTO(usuarioCreado);
    }

    /**
     * Actualizar usuario existente
     */
    public AdminUsuarioDTO actualizarUsuario(Long id, AdminUsuarioDTO usuarioDTO, User actualizadoPor) {
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("📝 [AdminUsuarioService] ACTUALIZAR USUARIO - VERSION 2.0");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("🆔 Usuario ID: " + id);
        System.out.println("📊 Datos recibidos: " + usuarioDTO);
        
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        System.out.println("👤 Usuario encontrado: " + usuario.getEmail());
        System.out.println("🎭 Roles actuales: " + usuario.getRoles());

        // Actualizar campos permitidos
        usuario.setFirstName(usuarioDTO.getFirstName());
        usuario.setLastName(usuarioDTO.getLastName());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPhone(usuarioDTO.getPhone());
        usuario.setEstado(usuarioDTO.getEstado());
        usuario.setActivo(usuarioDTO.getActivo());
        usuario.setEmailVerified(usuarioDTO.getEmailVerified());

        // Actualizar roles si se especifican (desde roles o roleIds)
        if (usuarioDTO.getRoleIds() != null && !usuarioDTO.getRoleIds().isEmpty()) {
            System.out.println("🎭 RoleIds recibidos: " + usuarioDTO.getRoleIds());
            Set<Role> roles = usuarioDTO.getRoleIds().stream()
                    .map(roleId -> {
                        System.out.println("🔍 Buscando rol con ID: " + roleId);
                        return roleRepository.findById(roleId).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            System.out.println("✅ Roles encontrados: " + roles);
            
            // Obtener la empresa principal del usuario
            var empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId());
            
            // Si el usuario no tiene empresa, intentar usar la empresa del administrador que lo está editando
            if (empresaOpt.isEmpty() && actualizadoPor != null) {
                System.out.println("⚠️ Usuario sin empresa, buscando empresa del administrador que lo está editando...");
                empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(actualizadoPor.getId());
                if (empresaOpt.isPresent()) {
                    System.out.println("🏢 Usando empresa del administrador: " + empresaOpt.get().getId());
                }
            }
            
            if (empresaOpt.isPresent()) {
                System.out.println("🏢 Asignando roles para empresa: " + empresaOpt.get().getId());
                sincronizarRolesLegacy(usuario, empresaOpt.get(), roles);
                
                // Actualizar o crear entrada en la tabla usuario_empresas con TODOS los roles
                System.out.println("🏢 Actualizando usuario en tabla usuario_empresas con múltiples roles...");
                try {
                    // Mapear TODOS los roles del sistema a roles de empresa
                    Set<com.agrocloud.model.enums.RolEmpresa> rolesEmpresa = roles.stream()
                            .map(this::mapearRolSistemaARolEmpresa)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    
                    System.out.println("🎭 Roles de empresa a asignar: " + rolesEmpresa.stream()
                            .map(com.agrocloud.model.enums.RolEmpresa::name)
                            .collect(Collectors.joining(", ")));
                    
                    if (!rolesEmpresa.isEmpty()) {
                        empresaUsuarioService.asignarRolesAUsuarioEnEmpresa(
                                usuario.getId(), 
                                empresaOpt.get().getId(), 
                                rolesEmpresa
                        );
                        System.out.println("✅ Usuario actualizado exitosamente en empresa: " + empresaOpt.get().getId() + " con " + rolesEmpresa.size() + " roles");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error actualizando usuario en empresa: " + e.getMessage());
                    e.printStackTrace();
                    // No lanzar excepción para no fallar la actualización del usuario
                }
            } else {
                System.out.println("❌ No se pudo determinar empresa para asignar roles");
                throw new RuntimeException("No se puede asignar rol sin empresa. El usuario debe estar asociado a al menos una empresa.");
            }
        } else if (usuarioDTO.getRoles() != null && !usuarioDTO.getRoles().isEmpty()) {
            System.out.println("🎭 Roles (DTO) recibidos: " + usuarioDTO.getRoles());
            Set<Role> roles = usuarioDTO.getRoles().stream()
                    .map(roleDTO -> roleRepository.findById(roleDTO.getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            System.out.println("✅ Roles encontrados: " + roles);
            
            // Obtener la empresa principal del usuario
            var empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId());
            
            // Si el usuario no tiene empresa, intentar usar la empresa del administrador que lo está editando
            if (empresaOpt.isEmpty() && actualizadoPor != null) {
                System.out.println("⚠️ Usuario sin empresa, buscando empresa del administrador que lo está editando...");
                empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(actualizadoPor.getId());
                if (empresaOpt.isPresent()) {
                    System.out.println("🏢 Usando empresa del administrador: " + empresaOpt.get().getId());
                }
            }
            
            if (empresaOpt.isPresent()) {
                System.out.println("🏢 Asignando roles para empresa: " + empresaOpt.get().getId());
                sincronizarRolesLegacy(usuario, empresaOpt.get(), roles);
                
                // Actualizar o crear entrada en la tabla usuario_empresas con TODOS los roles
                System.out.println("🏢 Actualizando usuario en tabla usuario_empresas con múltiples roles...");
                try {
                    // Mapear TODOS los roles del sistema a roles de empresa
                    Set<com.agrocloud.model.enums.RolEmpresa> rolesEmpresa = roles.stream()
                            .map(this::mapearRolSistemaARolEmpresa)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    
                    System.out.println("🎭 Roles de empresa a asignar: " + rolesEmpresa.stream()
                            .map(com.agrocloud.model.enums.RolEmpresa::name)
                            .collect(Collectors.joining(", ")));
                    
                    if (!rolesEmpresa.isEmpty()) {
                        empresaUsuarioService.asignarRolesAUsuarioEnEmpresa(
                                usuario.getId(), 
                                empresaOpt.get().getId(), 
                                rolesEmpresa
                        );
                        System.out.println("✅ Usuario actualizado exitosamente en empresa: " + empresaOpt.get().getId() + " con " + rolesEmpresa.size() + " roles");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error actualizando usuario en empresa: " + e.getMessage());
                    e.printStackTrace();
                    // No lanzar excepción para no fallar la actualización del usuario
                }
            } else {
                System.out.println("❌ No se pudo determinar empresa para asignar roles");
                throw new RuntimeException("No se puede asignar rol sin empresa. El usuario debe estar asociado a al menos una empresa.");
            }
        }

        // Actualizar usuario padre si se especifica
        if (usuarioDTO.getParentUserId() != null) {
            User parentUser = userRepository.findById(usuarioDTO.getParentUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario padre no encontrado"));
            usuario.setParentUser(parentUser);
        }

        System.out.println("💾 Guardando usuario con roles: " + usuario.getRoles());
        User usuarioActualizado = userRepository.save(usuario);
        System.out.println("✅ Usuario actualizado exitosamente");
        System.out.println("🎭 Roles finales: " + usuarioActualizado.getRoles());
        System.out.println("════════════════════════════════════════════════════════");
        
        return convertirAAdminDTO(usuarioActualizado);
    }

    /**
     * Cambiar estado de usuario
     */
    public AdminUsuarioDTO cambiarEstadoUsuario(Long id, EstadoUsuario nuevoEstado, User cambiadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setEstado(nuevoEstado);
        usuario.setUpdatedAt(LocalDateTime.now());

        User usuarioActualizado = userRepository.save(usuario);
        return convertirAAdminDTO(usuarioActualizado);
    }

    /**
     * Activar/Desactivar usuario
     */
    public AdminUsuarioDTO cambiarEstadoActivo(Long id, Boolean activo, User cambiadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(activo);
        usuario.setUpdatedAt(LocalDateTime.now());

        User usuarioActualizado = userRepository.save(usuario);
        return convertirAAdminDTO(usuarioActualizado);
    }

    /**
     * Resetear contraseña de usuario
     */
    public AdminUsuarioDTO resetearContraseña(Long id, String nuevaContraseña, User reseteadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setPassword(passwordEncoder.encode(nuevaContraseña));
        usuario.setUpdatedAt(LocalDateTime.now());

        User usuarioActualizado = userRepository.save(usuario);
        return convertirAAdminDTO(usuarioActualizado);
    }

    /**
     * Obtener estadísticas de usuarios
     */
    public Map<String, Object> obtenerEstadisticasUsuarios() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("totalUsuarios", userRepository.count());
        estadisticas.put("usuariosActivos", userRepository.countByEstado(EstadoUsuario.ACTIVO));
        estadisticas.put("usuariosPendientes", userRepository.countByEstado(EstadoUsuario.PENDIENTE));
        estadisticas.put("usuariosSuspendidos", userRepository.countByEstado(EstadoUsuario.SUSPENDIDO));
        estadisticas.put("usuariosEliminados", userRepository.countByEstado(EstadoUsuario.ELIMINADO));
        
        // Calcular porcentajes
        long total = userRepository.count();
        if (total > 0) {
            estadisticas.put("porcentajeActivos", (double) userRepository.countByEstado(EstadoUsuario.ACTIVO) / total * 100);
            estadisticas.put("porcentajePendientes", (double) userRepository.countByEstado(EstadoUsuario.PENDIENTE) / total * 100);
            estadisticas.put("porcentajeSuspendidos", (double) userRepository.countByEstado(EstadoUsuario.SUSPENDIDO) / total * 100);
            estadisticas.put("porcentajeEliminados", (double) userRepository.countByEstado(EstadoUsuario.ELIMINADO) / total * 100);
        }
        
        return estadisticas;
    }

    /**
     * Obtener estadísticas de usuarios subordinados
     */
    public Map<String, Object> obtenerEstadisticasUsuariosSubordinados(Long usuarioId) {
        System.out.println("🔍 [AdminUsuarioService] Obteniendo estadísticas de usuarios subordinados para usuario ID: " + usuarioId);
        
        // Obtener usuarios subordinados
        List<User> usuariosSubordinados = new ArrayList<>();
        
        // Obtener usuarios donde el usuario actual es el padre (parentUser)
        List<User> usuariosHijos = userRepository.findByParentUserId(usuarioId);
        usuariosSubordinados.addAll(usuariosHijos);
        
        // También obtener usuarios creados por este usuario
        User usuarioPadre = userRepository.findById(usuarioId).orElse(null);
        if (usuarioPadre != null) {
            List<User> usuariosCreados = userRepository.findByCreadoPorOrderByFechaCreacionDesc(usuarioPadre);
            usuariosSubordinados.addAll(usuariosCreados);
        }
        
        // Eliminar duplicados
        Set<User> usuariosUnicos = new HashSet<>(usuariosSubordinados);
        usuariosSubordinados = new ArrayList<>(usuariosUnicos);
        
        System.out.println("🔍 [AdminUsuarioService] Total usuarios subordinados para estadísticas: " + usuariosSubordinados.size());
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Calcular estadísticas de los usuarios subordinados
        long total = usuariosSubordinados.size();
        long activos = usuariosSubordinados.stream().mapToLong(u -> u.getEstado() == EstadoUsuario.ACTIVO ? 1 : 0).sum();
        long pendientes = usuariosSubordinados.stream().mapToLong(u -> u.getEstado() == EstadoUsuario.PENDIENTE ? 1 : 0).sum();
        long suspendidos = usuariosSubordinados.stream().mapToLong(u -> u.getEstado() == EstadoUsuario.SUSPENDIDO ? 1 : 0).sum();
        long eliminados = usuariosSubordinados.stream().mapToLong(u -> u.getEstado() == EstadoUsuario.ELIMINADO ? 1 : 0).sum();
        
        estadisticas.put("totalUsuarios", total);
        estadisticas.put("usuariosActivos", activos);
        estadisticas.put("usuariosPendientes", pendientes);
        estadisticas.put("usuariosSuspendidos", suspendidos);
        estadisticas.put("usuariosEliminados", eliminados);
        
        // Calcular porcentajes
        if (total > 0) {
            estadisticas.put("porcentajeActivos", (double) activos / total * 100);
            estadisticas.put("porcentajePendientes", (double) pendientes / total * 100);
            estadisticas.put("porcentajeSuspendidos", (double) suspendidos / total * 100);
            estadisticas.put("porcentajeEliminados", (double) eliminados / total * 100);
        } else {
            estadisticas.put("porcentajeActivos", 0.0);
            estadisticas.put("porcentajePendientes", 0.0);
            estadisticas.put("porcentajeSuspendidos", 0.0);
            estadisticas.put("porcentajeEliminados", 0.0);
        }
        
        System.out.println("🔍 [AdminUsuarioService] Estadísticas calculadas: " + estadisticas);
        return estadisticas;
    }

    /**
     * Buscar usuarios con filtros avanzados
     */
    public List<AdminUsuarioDTO> buscarUsuariosConFiltros(
            EstadoUsuario estado,
            String roleName,
            Long creadoPorId,
            Boolean activo,
            String searchTerm) {
        
        User creadoPor = null;
        if (creadoPorId != null) {
            creadoPor = userRepository.findById(creadoPorId).orElse(null);
        }
        
        List<User> usuarios = userRepository.findUsersWithAdvancedFilters(estado, roleName, creadoPor, activo, searchTerm);
        return usuarios.stream()
                .map(this::convertirAAdminDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertir User a AdminUsuarioDTO
     */
    private AdminUsuarioDTO convertirAAdminDTO(User user) {
        AdminUsuarioDTO dto = new AdminUsuarioDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setEstado(user.getEstado());
        dto.setActivo(user.getActivo());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Información de auditoría
        if (user.getCreadoPor() != null) {
            dto.setCreadoPorId(user.getCreadoPor().getId());
            dto.setCreadoPorNombre(user.getCreadoPor().getFirstName() + " " + user.getCreadoPor().getLastName());
            dto.setCreadoPorEmail(user.getCreadoPor().getEmail());
        }

        // Roles - Buscar en todos los sistemas (múltiples roles, sistema nuevo y legacy)
        Set<RoleDTO> rolesDTO = new HashSet<>();
        
        // Lista de roles válidos según el enum RolEmpresa
        Set<String> rolesValidos = Set.of(
            "SUPERADMIN", 
            "ADMINISTRADOR", 
            "JEFE_CAMPO", 
            "JEFE_FINANCIERO", 
            "OPERARIO", 
            "CONSULTOR_EXTERNO"
        );
        
        // PRIMERO: Buscar roles múltiples en UsuarioEmpresaRol (sistema de múltiples roles)
        try {
            // Obtener todas las empresas del usuario para buscar roles múltiples
            if (user.getUsuarioEmpresas() != null && !user.getUsuarioEmpresas().isEmpty()) {
                for (com.agrocloud.core.domain.UsuarioEmpresa ue : user.getUsuarioEmpresas()) {
                    if (ue.getEstado() == com.agrocloud.model.enums.EstadoUsuarioEmpresa.ACTIVO && ue.getEmpresa() != null) {
                        // Buscar roles múltiples en UsuarioEmpresaRol
                        List<UsuarioEmpresaRol> rolesMultiples = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(
                                user.getId(), 
                                ue.getEmpresa().getId()
                        );
                        
                        for (UsuarioEmpresaRol uer : rolesMultiples) {
                            if (uer.getRol() != null && uer.getActivo() != null && uer.getActivo()) {
                                String nombreRol = uer.getRol().getNombre();
                                
                                // Solo agregar si es un rol válido
                                if (rolesValidos.contains(nombreRol)) {
                                    Role role = roleRepository.findByNombre(nombreRol).orElse(null);
                                    if (role != null) {
                                        rolesDTO.add(convertirARoleDTO(role));
                                    } else {
                                        // Si no existe el Role en la BD, crear un RoleDTO directamente
                                        RoleDTO roleDTO = new RoleDTO();
                                        roleDTO.setName(nombreRol);
                                        roleDTO.setDescription("Rol: " + nombreRol);
                                        rolesDTO.add(roleDTO);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ [AdminUsuarioService] Error obteniendo roles múltiples: " + e.getMessage());
            e.printStackTrace();
        }
        
        // SEGUNDO: Si no se encontraron roles múltiples, buscar en el sistema antiguo (usuario_empresas con un solo rol)
        if (rolesDTO.isEmpty()) {
            try {
                if (user.getUsuarioEmpresas() != null && !user.getUsuarioEmpresas().isEmpty()) {
                    for (com.agrocloud.core.domain.UsuarioEmpresa ue : user.getUsuarioEmpresas()) {
                        if (ue.getEstado() == com.agrocloud.model.enums.EstadoUsuarioEmpresa.ACTIVO && ue.getRol() != null) {
                            com.agrocloud.model.enums.RolEmpresa rolEmpresa = ue.getRol();
                            // Aplicar mapeo de roles deprecated a roles nuevos
                            com.agrocloud.model.enums.RolEmpresa rolActualizado = rolEmpresa.getRolActualizado();
                            String nombreRol = rolActualizado.name(); // ADMINISTRADOR, SUPERADMIN, etc.
                            
                            // Solo agregar si es un rol válido
                            if (rolesValidos.contains(nombreRol)) {
                                // Buscar el Role correspondiente en la base de datos por nombre
                                Role role = roleRepository.findByNombre(nombreRol).orElse(null);
                                if (role != null) {
                                    rolesDTO.add(convertirARoleDTO(role));
                                } else {
                                    // Si no existe el Role en la BD, crear un RoleDTO directamente desde el enum
                                    RoleDTO roleDTO = new RoleDTO();
                                    roleDTO.setName(nombreRol);
                                    roleDTO.setDescription(rolActualizado.getDescripcion());
                                    // ID será null si no existe en BD
                                    rolesDTO.add(roleDTO);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioService] Error obteniendo roles del sistema nuevo: " + e.getMessage());
            }
        }
        
        // TERCERO: Si no se encontraron roles en los sistemas anteriores, buscar en el sistema legacy
        // Filtrar solo roles válidos según el enum RolEmpresa
        if (rolesDTO.isEmpty() && user.getRoles() != null && !user.getRoles().isEmpty()) {
            rolesDTO = user.getRoles().stream()
                    .filter(role -> {
                        String nombre = role.getNombre();
                        // Solo incluir roles válidos o roles que se mapean correctamente
                        return rolesValidos.contains(nombre) || 
                               // Mapear roles deprecated a sus equivalentes nuevos
                               (nombre != null && (
                                   nombre.equals("PRODUCTOR") || nombre.equals("ASESOR") || nombre.equals("TECNICO") ||
                                   nombre.equals("CONTADOR") || nombre.equals("LECTURA")
                               ));
                    })
                    .map(role -> {
                        String nombre = role.getNombre();
                        // Mapear roles deprecated a roles nuevos
                        String rolMapeado = nombre;
                        if (nombre.equals("PRODUCTOR") || nombre.equals("ASESOR") || nombre.equals("TECNICO")) {
                            rolMapeado = "JEFE_CAMPO";
                        } else if (nombre.equals("CONTADOR")) {
                            rolMapeado = "JEFE_FINANCIERO";
                        } else if (nombre.equals("LECTURA")) {
                            rolMapeado = "CONSULTOR_EXTERNO";
                        }
                        
                        // Buscar el rol mapeado en la BD
                        Role roleMapeado = roleRepository.findByNombre(rolMapeado).orElse(role);
                        RoleDTO roleDto = convertirARoleDTO(roleMapeado);
                        // Si el nombre original es diferente, mantener el nombre original para referencia
                        if (!nombre.equals(rolMapeado)) {
                            roleDto.setName(rolMapeado); // Pero mostrar el nombre del rol nuevo
                        }
                        return roleDto;
                    })
                    .collect(Collectors.toSet());
        }
        
        dto.setRoles(rolesDTO);

        // Información de jerarquía
        if (user.getParentUser() != null) {
            dto.setParentUserId(user.getParentUser().getId());
            dto.setParentUserName(user.getParentUser().getFirstName() + " " + user.getParentUser().getLastName());
        }

        if (user.getChildUsers() != null) {
            dto.setChildUsersCount(user.getChildUsers().size());
        }

        return dto;
    }

    /**
     * Convertir Role a RoleDTO
     */
    private RoleDTO convertirARoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getNombre());
        dto.setDescription(role.getDescription());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getCreatedAt());
        return dto;
    }

    private void sincronizarRolesLegacy(User usuario, Empresa empresa, Set<Role> rolesSeleccionados) {
        if (usuario == null || empresa == null) {
            System.out.println("⚠️ [AdminUsuarioService] No se pudo sincronizar roles legacy: usuario o empresa nulos");
            return;
        }

        System.out.println("🔄 [AdminUsuarioService] Sincronizando roles legacy para usuario " + usuario.getEmail() + " en empresa " + empresa.getId());

        List<UserCompanyRole> rolesActuales = userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(usuario.getId(), empresa.getId());
        Set<Long> idsNuevos = rolesSeleccionados.stream()
                .filter(Objects::nonNull)
                .map(Role::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Eliminar roles que ya no corresponden
        for (UserCompanyRole rolActual : rolesActuales) {
            Long idRolActual = rolActual.getRol() != null ? rolActual.getRol().getId() : null;
            if (idRolActual == null || !idsNuevos.contains(idRolActual)) {
                userCompanyRoleRepository.delete(rolActual);
                System.out.println("🗑️ [AdminUsuarioService] Eliminado rol legacy " + (rolActual.getRol() != null ? rolActual.getRol().getNombre() : "null"));
            }
        }

        rolesActuales = userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(usuario.getId(), empresa.getId());

        // Agregar roles nuevos
        for (Role rol : rolesSeleccionados) {
            if (rol == null || rol.getId() == null) {
                continue;
            }

            boolean yaExiste = rolesActuales.stream()
                    .anyMatch(actual -> actual.getRol() != null && rol.getId().equals(actual.getRol().getId()));

            if (!yaExiste) {
                UserCompanyRole nuevo = new UserCompanyRole();
                nuevo.setUsuario(usuario);
                nuevo.setEmpresa(empresa);
                nuevo.setRol(rol);
                nuevo.setActivo(true);
                nuevo.setFechaCreacion(LocalDateTime.now());
                nuevo.setFechaActualizacion(LocalDateTime.now());
                userCompanyRoleRepository.save(nuevo);
                System.out.println("➕ [AdminUsuarioService] Agregado rol legacy " + rol.getNombre());
            }
        }

        // Refrescar colección en memoria para mantener compatibilidad
        List<UserCompanyRole> rolesSincronizados = userCompanyRoleRepository.findByUsuarioIdAndEmpresaId(usuario.getId(), empresa.getId());
        usuario.setUserCompanyRoles(rolesSincronizados);
        System.out.println("✅ [AdminUsuarioService] Roles legacy sincronizados: " + rolesSincronizados.size());
    }

    // ========================================
    // MÉTODOS DE PREVENCIÓN AUTOMÁTICA
    // ========================================

    /**
     * Obtener usuario ADMINISTRADOR por defecto para asignaciones automáticas
     */
    private User obtenerUsuarioAdminPorDefecto() {
        // Buscar el primer SUPERADMIN o ADMINISTRADOR disponible
        // Usar findAllWithRoles() que carga usuarioEmpresas con JOIN FETCH
        List<User> todosUsuarios = userRepository.findAllWithRoles();
        
        System.out.println("🔍 [AdminUsuarioService] Buscando usuario admin por defecto entre " + todosUsuarios.size() + " usuarios");
        
        for (User u : todosUsuarios) {
            try {
                // Inicializar relaciones lazy que no se cargaron con JOIN FETCH
                // usuarioEmpresas ya debería estar cargado por findAllWithRoles()
                if (u.getUsuarioEmpresas() != null) {
                    u.getUsuarioEmpresas().size(); // Asegurar inicialización
                    u.getUsuarioEmpresas().forEach(ue -> {
                        if (ue.getRol() != null) ue.getRol().name();
                        if (ue.getEmpresa() != null) ue.getEmpresa().getId();
                    });
                }
                
                // Cargar userCompanyRoles (sistema legacy) si es necesario
                if (u.getUserCompanyRoles() != null) {
                    u.getUserCompanyRoles().size();
                    u.getUserCompanyRoles().forEach(ucr -> {
                        if (ucr.getRol() != null) ucr.getRol().getNombre();
                        if (ucr.getEmpresa() != null) ucr.getEmpresa().getId();
                    });
                }
                
                // Cargar roles (sistema legacy directo)
                if (u.getRoles() != null) {
                    u.getRoles().size();
                    u.getRoles().forEach(role -> {
                        if (role != null) role.getNombre();
                    });
                }
                
                // Usar isSuperAdmin() e isAdmin() que revisan ambos sistemas
                if (u.isSuperAdmin() || u.isAdmin()) {
                    System.out.println("✅ [AdminUsuarioService] Usuario admin encontrado por defecto: " + u.getEmail());
                    return u;
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioService] Error verificando usuario " + u.getEmail() + ": " + e.getMessage());
                // Continuar con el siguiente usuario
            }
        }
        
        throw new RuntimeException("No se encontró usuario ADMINISTRADOR o SUPERADMIN para asignaciones automáticas");
    }

    /**
     * Asignar usuario padre automáticamente para prevenir usuarios huérfanos
     */
    private User asignarUsuarioPadreAutomaticamente(AdminUsuarioDTO usuarioDTO, User creadoPor) {
        // Si se especifica explícitamente un padre, usarlo
        if (usuarioDTO.getParentUserId() != null) {
            return userRepository.findById(usuarioDTO.getParentUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario padre especificado no encontrado"));
        }

        // Si el creador es SUPERADMIN o ADMINISTRADOR, asignarlo como padre
        if (creadoPor != null) {
            // Usar isAdmin() e isSuperAdmin() que revisan ambos sistemas de roles
            if (creadoPor.isSuperAdmin() || creadoPor.isAdmin()) {
                System.out.println("✅ [AdminUsuarioService] Asignando creador como padre (es admin): " + creadoPor.getEmail());
                return creadoPor;
            }
        }

        // Si el creador no es administrador, asignar un ADMINISTRADOR como padre por defecto
        return obtenerUsuarioAdminPorDefecto();
    }

    /**
     * Validar y corregir jerarquía de usuario existente
     */
    public AdminUsuarioDTO validarYCorregirJerarquia(Long userId) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        boolean necesitaCorreccion = false;

        // Verificar si no tiene padre asignado
        if (usuario.getParentUser() == null) {
            usuario.setParentUser(obtenerUsuarioAdminPorDefecto());
            necesitaCorreccion = true;
        }

        // Verificar si no tiene creador asignado
        if (usuario.getCreadoPor() == null) {
            usuario.setCreadoPor(obtenerUsuarioAdminPorDefecto());
            necesitaCorreccion = true;
        }

        // Si se realizaron correcciones, guardar
        if (necesitaCorreccion) {
            usuario = userRepository.save(usuario);
        }

        return convertirAAdminDTO(usuario);
    }

    /**
     * Validar y corregir jerarquía de todos los usuarios
     */
    public List<AdminUsuarioDTO> validarYCorregirJerarquiaGlobal() {
        List<User> usuarios = userRepository.findAll();
        List<AdminUsuarioDTO> usuariosCorregidos = new ArrayList<>();

        for (User usuario : usuarios) {
            try {
                AdminUsuarioDTO dto = validarYCorregirJerarquia(usuario.getId());
                usuariosCorregidos.add(dto);
            } catch (Exception e) {
                // Log del error pero continuar con otros usuarios
                System.err.println("Error corrigiendo jerarquía del usuario " + usuario.getUsername() + ": " + e.getMessage());
            }
        }

        return usuariosCorregidos;
    }

    // ========================================
    // VALIDACIÓN DE PERMISOS
    // ========================================

    /**
     * Verificar si un usuario tiene permisos para crear otros usuarios
     */
    private boolean tienePermisoParaCrearUsuarios(User usuario) {
        if (usuario == null) {
            return false;
        }

        // Usar isAdmin() y isSuperAdmin() que revisan ambos sistemas de roles (nuevo y legacy)
        // SUPERADMIN y ADMINISTRADOR pueden crear usuarios
        return usuario.isSuperAdmin() || usuario.isAdmin();
    }

    /**
     * Verificar si un usuario puede gestionar otro usuario específico
     * Si usuarioIdAGestionar es null, verifica si puede crear usuarios
     */
    public boolean puedeGestionarUsuario(User usuarioGestor, Long usuarioIdAGestionar) {
        if (usuarioGestor == null) {
            return false;
        }
        
        // Si usuarioIdAGestionar es null, estamos creando un nuevo usuario
        // Verificar si el usuario gestor es SUPERADMIN o ADMINISTRADOR
        if (usuarioIdAGestionar == null) {
            return usuarioGestor.isSuperAdmin() || esAdministrador(usuarioGestor);
        }

        // SUPERADMIN puede gestionar cualquier usuario
        if (usuarioGestor.isSuperAdmin()) {
            System.out.println("🔍 [AdminUsuarioService] Usuario " + usuarioGestor.getEmail() + " es SUPERADMIN, puede gestionar cualquier usuario");
            return true;
        }

        // Un usuario puede gestionar sus propios datos
        if (usuarioGestor.getId().equals(usuarioIdAGestionar)) {
            System.out.println("🔍 [AdminUsuarioService] Usuario " + usuarioGestor.getEmail() + " puede gestionar sus propios datos");
            return true;
        }

        // ADMINISTRADOR puede gestionar sus usuarios subordinados
        if (esAdministrador(usuarioGestor)) {
            // Verificar si el usuario a gestionar es subordinado
            User usuarioAGestionar = userRepository.findById(usuarioIdAGestionar).orElse(null);
            if (usuarioAGestionar != null) {
                boolean esSubordinado = esUsuarioSubordinado(usuarioGestor, usuarioAGestionar);
                System.out.println("🔍 [AdminUsuarioService] Usuario " + usuarioGestor.getEmail() + " es ADMINISTRADOR, puede gestionar subordinado " + usuarioAGestionar.getEmail() + ": " + esSubordinado);
                return esSubordinado;
            }
        }

        System.out.println("❌ [AdminUsuarioService] Usuario " + usuarioGestor.getEmail() + " NO puede gestionar usuario ID: " + usuarioIdAGestionar);
        return false;
    }

    /**
     * Verificar si un usuario es ADMINISTRADOR (no SUPERADMIN)
     * Usa el método isAdmin() que revisa ambos sistemas (nuevo y legacy)
     */
    private boolean esAdministrador(User usuario) {
        if (usuario == null) {
            return false;
        }
        // Usar isAdmin() que ya revisa ambos sistemas de roles
        // pero verificar que NO sea SUPERADMIN
        boolean esAdmin = usuario.isAdmin();
        boolean esSuperAdmin = usuario.isSuperAdmin();
        return esAdmin && !esSuperAdmin; // Es admin pero no superadmin
    }

    /**
     * Verificar si un usuario es subordinado de otro
     */
    private boolean esUsuarioSubordinado(User usuarioPadre, User usuarioHijo) {
        if (usuarioPadre == null || usuarioHijo == null) {
            return false;
        }

        // Verificar si es hijo directo (parentUser)
        if (usuarioHijo.getParentUser() != null && usuarioHijo.getParentUser().getId().equals(usuarioPadre.getId())) {
            return true;
        }

        // Verificar si fue creado por el usuario padre
        if (usuarioHijo.getCreadoPor() != null && usuarioHijo.getCreadoPor().getId().equals(usuarioPadre.getId())) {
            return true;
        }

        return false;
    }

    /**
     * Mapear rol del sistema a rol de empresa
     */
    private com.agrocloud.model.enums.RolEmpresa mapearRolSistemaARolEmpresa(Role rolSistema) {
        String nombreRol = rolSistema.getNombre();
        
        switch (nombreRol) {
            case "SUPERADMIN":
                return com.agrocloud.model.enums.RolEmpresa.SUPERADMIN;
            case "ADMINISTRADOR":
                return com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR;
            case "JEFE_CAMPO":
                return com.agrocloud.model.enums.RolEmpresa.JEFE_CAMPO;
            case "JEFE_FINANCIERO":
                return com.agrocloud.model.enums.RolEmpresa.JEFE_FINANCIERO;
            case "OPERARIO":
                return com.agrocloud.model.enums.RolEmpresa.OPERARIO;
            case "CONSULTOR_EXTERNO":
                return com.agrocloud.model.enums.RolEmpresa.CONSULTOR_EXTERNO;
            // Roles deprecated - mapear a roles nuevos
            case "ASESOR":
            case "TECNICO":
            case "PRODUCTOR":
                return com.agrocloud.model.enums.RolEmpresa.JEFE_CAMPO;
            case "CONTADOR":
                return com.agrocloud.model.enums.RolEmpresa.JEFE_FINANCIERO;
            case "LECTURA":
            case "INVITADO":
                return com.agrocloud.model.enums.RolEmpresa.CONSULTOR_EXTERNO;
            default:
                System.out.println("⚠️ Rol no reconocido: " + nombreRol + ", usando CONSULTOR_EXTERNO por defecto");
                return com.agrocloud.model.enums.RolEmpresa.CONSULTOR_EXTERNO;
        }
    }

    /**
     * Verificar si un usuario puede cambiar el estado de otro usuario
     */
    public boolean puedeCambiarEstadoUsuario(User usuarioGestor, Long usuarioIdACambiar) {
        // Solo ADMIN puede cambiar estados de otros usuarios
        return tienePermisoParaCrearUsuarios(usuarioGestor);
    }

    /**
     * Verificar si un usuario puede resetear contraseña de otro usuario
     */
    public boolean puedeResetearContraseña(User usuarioGestor, Long usuarioIdAResetear) {
        // Solo ADMIN puede resetear contraseñas de otros usuarios
        return tienePermisoParaCrearUsuarios(usuarioGestor);
    }
}
