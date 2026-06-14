package com.agrocloud.controller;

import com.agrocloud.dto.AdminUsuarioDTO;
import com.agrocloud.dto.RoleDTO;
import com.agrocloud.core.domain.EstadoUsuario;
import com.agrocloud.core.domain.Role;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.application.AdminUsuarioService;
import com.agrocloud.core.application.RoleService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.model.enums.RolEmpresa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Administración de Usuarios", description = "Endpoints para gestión de usuarios por administradores")
public class AdminUsuarioController {

    @Autowired
    @Qualifier("adminUsuarioServiceCore")
    private AdminUsuarioService adminUsuarioService;

    @Autowired
    @Qualifier("roleServiceCore")
    private RoleService roleService;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;

    /**
     * Endpoint de prueba para diagnosticar
     */
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Endpoint de prueba para verificar que el controlador funcione")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AdminUsuarioController funcionando correctamente");
    }

    /**
     * Endpoint simple para obtener usuarios básicos (sin información de admin)
     */
    @GetMapping("/basic")
    @Operation(summary = "Listar usuarios básicos", description = "Obtener lista básica de usuarios para AdminEmpresas")
    public ResponseEntity<List<Map<String, Object>>> obtenerUsuariosBasicos() {
        try {
            List<AdminUsuarioDTO> usuarios = adminUsuarioService.obtenerTodosLosUsuarios();
            List<Map<String, Object>> usuariosBasicos = usuarios.stream()
                .map(usuario -> {
                    Map<String, Object> usuarioBasico = new java.util.HashMap<>();
                    usuarioBasico.put("id", usuario.getId());
                    usuarioBasico.put("username", usuario.getUsername());
                    usuarioBasico.put("firstName", usuario.getFirstName());
                    usuarioBasico.put("lastName", usuario.getLastName());
                    usuarioBasico.put("email", usuario.getEmail());
                    usuarioBasico.put("activo", usuario.getActivo());
                    return usuarioBasico;
                })
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(usuariosBasicos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener usuarios según permisos del usuario autenticado
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtener lista de usuarios según permisos del usuario autenticado")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<AdminUsuarioDTO>> obtenerUsuariosSegunPermisos(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🔍 [AdminUsuarioController] Iniciando obtención de usuarios para: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener el usuario autenticado con todas las relaciones (incluyendo userCompanyRoles)
            User usuarioAutenticado;
            try {
                usuarioAutenticado = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioController] Error al usar findByEmailWithAllRelationsCombined, intentando método simple: " + e.getMessage());
                usuarioAutenticado = userService.findByEmailWithAllRelations(userDetails.getUsername());
            }
            
            if (usuarioAutenticado == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            // Inicializar relaciones lazy antes de verificar roles
            try {
                if (usuarioAutenticado.getUsuarioEmpresas() != null) {
                    usuarioAutenticado.getUsuarioEmpresas().size();
                    usuarioAutenticado.getUsuarioEmpresas().forEach(ue -> {
                        if (ue.getRol() != null) ue.getRol().name();
                        if (ue.getEmpresa() != null) ue.getEmpresa().getId();
                    });
                }
                if (usuarioAutenticado.getUserCompanyRoles() != null) {
                    usuarioAutenticado.getUserCompanyRoles().size();
                    usuarioAutenticado.getUserCompanyRoles().forEach(ucr -> {
                        if (ucr.getRol() != null) ucr.getRol().getNombre();
                        if (ucr.getEmpresa() != null) ucr.getEmpresa().getId();
                    });
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioController] Error al inicializar relaciones lazy: " + e.getMessage());
            }
            
            System.out.println("🔍 [AdminUsuarioController] Usuario autenticado: " + usuarioAutenticado.getEmail() + ", esAdmin: " + usuarioAutenticado.isAdmin() + ", esSuperAdmin: " + usuarioAutenticado.isSuperAdmin());
            
            List<AdminUsuarioDTO> usuarios;
            
            // Verificar si es SUPERADMIN (puede ver todos los usuarios)
            if (usuarioAutenticado.isSuperAdmin()) {
                System.out.println("🔍 [AdminUsuarioController] Usuario es SUPERADMIN, mostrando todos los usuarios");
                usuarios = adminUsuarioService.obtenerTodosLosUsuarios();
            } else if (usuarioAutenticado.isAdmin()) {
                // ADMINISTRADOR solo puede ver sus usuarios subordinados
                System.out.println("🔍 [AdminUsuarioController] Usuario es ADMINISTRADOR, mostrando solo usuarios subordinados");
                usuarios = adminUsuarioService.obtenerUsuariosSubordinados(usuarioAutenticado.getId());
            } else {
                // Otros usuarios no pueden acceder a la administración de usuarios
                System.out.println("❌ [AdminUsuarioController] Usuario no tiene permisos para administrar usuarios");
                return ResponseEntity.status(403).build();
            }
            
            System.out.println("🔍 [AdminUsuarioController] Usuarios obtenidos: " + usuarios.size());
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            System.err.println("❌ [AdminUsuarioController] Error obteniendo usuarios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener usuario por ID (con validación de permisos)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario", description = "Obtener usuario específico por ID según permisos")
    public ResponseEntity<AdminUsuarioDTO> obtenerUsuarioPorId(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🔍 [AdminUsuarioController] Obteniendo usuario ID: " + id + " para: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener el usuario autenticado
            User usuarioAutenticado = userService.findByEmailWithAllRelations(userDetails.getUsername());
            if (usuarioAutenticado == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            // Verificar si el usuario autenticado puede gestionar este usuario
            if (!adminUsuarioService.puedeGestionarUsuario(usuarioAutenticado, id)) {
                System.err.println("❌ [AdminUsuarioController] ERROR: Usuario " + usuarioAutenticado.getEmail() + " no tiene permisos para ver usuario ID: " + id);
                return ResponseEntity.status(403).build();
            }
            
            AdminUsuarioDTO usuario = adminUsuarioService.obtenerUsuarioPorId(id);
            System.out.println("✅ [AdminUsuarioController] Usuario obtenido exitosamente: " + usuario.getUsername());
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            System.err.println("❌ [AdminUsuarioController] Error obteniendo usuario ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(404).body(null);
        }
    }

    /**
     * Crear nuevo usuario (SOLO ADMIN)
     */
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crear un nuevo usuario en el sistema (Solo ADMIN)")
    public ResponseEntity<AdminUsuarioDTO> crearUsuario(@RequestBody AdminUsuarioDTO usuarioDTO,
                                                       Authentication authentication) {
        try {
            // Obtener usuario actual del contexto de autenticación (en Spring Security, getName() retorna el email)
            String username = authentication.getName();
            System.out.println("🔍 [AdminUsuarioController] Username del contexto: " + username);
            
            // Usar siempre findByEmailWithAllRelationsCombined para cargar todas las relaciones
            // ya que en Spring Security getName() retorna el email
            User usuarioActual = null;
            try {
                usuarioActual = userService.findByEmailWithAllRelationsCombined(username);
                System.out.println("✅ [AdminUsuarioController] Usuario encontrado por email: " + username);
            } catch (Exception e) {
                System.err.println("❌ [AdminUsuarioController] Usuario no encontrado: " + username);
                System.err.println("❌ [AdminUsuarioController] Error: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(401).body(null);
            }
            
            // Inicializar relaciones lazy antes de verificar permisos
            try {
                if (usuarioActual.getUsuarioEmpresas() != null) {
                    usuarioActual.getUsuarioEmpresas().size();
                    usuarioActual.getUsuarioEmpresas().forEach(ue -> {
                        if (ue.getRol() != null) ue.getRol().name();
                        if (ue.getEmpresa() != null) ue.getEmpresa().getId();
                    });
                }
                if (usuarioActual.getUserCompanyRoles() != null) {
                    usuarioActual.getUserCompanyRoles().size();
                    usuarioActual.getUserCompanyRoles().forEach(ucr -> {
                        if (ucr.getRol() != null) ucr.getRol().getNombre();
                        if (ucr.getEmpresa() != null) ucr.getEmpresa().getId();
                    });
                }
                // Inicializar también la relación roles (sistema legacy)
                if (usuarioActual.getRoles() != null) {
                    usuarioActual.getRoles().size();
                    usuarioActual.getRoles().forEach(role -> {
                        if (role != null) role.getNombre();
                    });
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioController] Error al inicializar relaciones lazy: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("✅ [AdminUsuarioController] Usuario autenticado: " + usuarioActual.getEmail());
            System.out.println("🔍 [AdminUsuarioController] esAdmin: " + usuarioActual.isAdmin() + ", esSuperAdmin: " + usuarioActual.isSuperAdmin());
            
            // VALIDACIÓN DE PERMISOS: Solo ADMIN puede crear usuarios
            if (!adminUsuarioService.puedeGestionarUsuario(usuarioActual, null)) {
                System.err.println("❌ [AdminUsuarioController] Usuario sin permisos para gestionar usuarios");
                System.err.println("  - Usuario ID: " + usuarioActual.getId());
                System.err.println("  - Email: " + usuarioActual.getEmail());
                System.err.println("  - esSuperAdmin: " + usuarioActual.isSuperAdmin());
                System.err.println("  - esAdmin: " + usuarioActual.isAdmin());
                return ResponseEntity.status(403).body(null);
            }
            
            AdminUsuarioDTO usuarioCreado = adminUsuarioService.crearUsuario(usuarioDTO, usuarioActual);
            return ResponseEntity.ok(usuarioCreado);
        } catch (Exception e) {
            System.err.println("❌ [AdminUsuarioController] Error creando usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Actualizar usuario existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualizar datos de un usuario existente")
    public ResponseEntity<AdminUsuarioDTO> actualizarUsuario(@PathVariable Long id,
                                                             @RequestBody AdminUsuarioDTO usuarioDTO,
                                                             Authentication authentication) {
        try {
            // Obtener usuario actual del contexto de autenticación
            String username = authentication.getName();
            User usuarioActual = null;
            try {
                usuarioActual = userService.findByUsername(username);
            } catch (Exception e) {
                usuarioActual = userService.findByEmailWithAllRelations(username);
            }
            
            if (usuarioActual == null) {
                System.err.println("❌ [AdminUsuarioController] Usuario no encontrado: " + username);
                return ResponseEntity.status(401).body(null);
            }
            
            AdminUsuarioDTO usuarioActualizado = adminUsuarioService.actualizarUsuario(id, usuarioDTO, usuarioActual);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            System.err.println("❌ [AdminUsuarioController] Error actualizando usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Cambiar estado de usuario
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado", description = "Cambiar el estado de un usuario")
    public ResponseEntity<AdminUsuarioDTO> cambiarEstadoUsuario(@PathVariable Long id,
                                                               @RequestParam EstadoUsuario estado,
                                                               Authentication authentication) {
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("🔄 [AdminUsuarioController] CAMBIAR ESTADO USUARIO - VERSION 2.0");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("🆔 Usuario ID: " + id);
        System.out.println("📊 Nuevo Estado: " + estado);
        
        try {
            // Obtener usuario actual del contexto de autenticación
            String username = authentication.getName();
            System.out.println("🔍 Username del JWT: " + username);
            
            User usuarioActual = null;
            try {
                System.out.println("🔍 Intentando buscar por username...");
                usuarioActual = userService.findByUsername(username);
                System.out.println("✅ Usuario encontrado por username");
            } catch (Exception e) {
                System.out.println("⚠️ No encontrado por username, intentando por email...");
                usuarioActual = userService.findByEmailWithAllRelations(username);
                System.out.println("✅ Usuario encontrado por email");
            }
            
            if (usuarioActual == null) {
                System.err.println("❌ [AdminUsuarioController] Usuario no encontrado: " + username);
                return ResponseEntity.status(401).body(null);
            }
            
            System.out.println("✅ Usuario autenticado: " + usuarioActual.getEmail());
            System.out.println("🔧 Llamando a adminUsuarioService.cambiarEstadoUsuario...");
            
            AdminUsuarioDTO usuarioActualizado = adminUsuarioService.cambiarEstadoUsuario(id, estado, usuarioActual);
            
            System.out.println("✅ Estado cambiado exitosamente");
            System.out.println("════════════════════════════════════════════════════════");
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            System.err.println("❌❌❌ [AdminUsuarioController] ERROR CAMBIANDO ESTADO ❌❌❌");
            System.err.println("❌ Mensaje: " + e.getMessage());
            System.err.println("❌ Tipo: " + e.getClass().getName());
            System.err.println("❌ Stack trace:");
            e.printStackTrace();
            System.err.println("════════════════════════════════════════════════════════");
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Activar/Desactivar usuario
     */
    @PatchMapping("/{id}/activo")
    @Operation(summary = "Cambiar estado activo", description = "Activar o desactivar un usuario")
    public ResponseEntity<AdminUsuarioDTO> cambiarEstadoActivo(@PathVariable Long id,
                                                              @RequestParam Boolean activo,
                                                              Authentication authentication) {
        try {
            // Obtener usuario actual del contexto de autenticación
            String username = authentication.getName();
            User usuarioActual = null;
            try {
                usuarioActual = userService.findByUsername(username);
            } catch (Exception e) {
                usuarioActual = userService.findByEmailWithAllRelations(username);
            }
            
            if (usuarioActual == null) {
                System.err.println("❌ [AdminUsuarioController] Usuario no encontrado: " + username);
                return ResponseEntity.status(401).body(null);
            }
            
            AdminUsuarioDTO usuarioActualizado = adminUsuarioService.cambiarEstadoActivo(id, activo, usuarioActual);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Resetear contraseña de usuario
     */
    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "Resetear contraseña", description = "Resetear la contraseña de un usuario")
    public ResponseEntity<AdminUsuarioDTO> resetearContraseña(@PathVariable Long id,
                                                             @RequestParam String nuevaContraseña,
                                                             Authentication authentication) {
        try {
            // Obtener usuario actual del contexto de autenticación
            String username = authentication.getName();
            User usuarioActual = null;
            try {
                usuarioActual = userService.findByUsername(username);
            } catch (Exception e) {
                usuarioActual = userService.findByEmailWithAllRelations(username);
            }
            
            if (usuarioActual == null) {
                System.err.println("❌ [AdminUsuarioController] Usuario no encontrado: " + username);
                return ResponseEntity.status(401).body(null);
            }
            
            AdminUsuarioDTO usuarioActualizado = adminUsuarioService.resetearContraseña(id, nuevaContraseña, usuarioActual);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            System.err.println("❌ [AdminUsuarioController] Error reseteando contraseña: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener estadísticas de usuarios según permisos del usuario autenticado
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas", description = "Obtener estadísticas de usuarios según permisos del usuario autenticado")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsuarios(@AuthenticationPrincipal UserDetails userDetails) {
        // Log al inicio del método antes del try-catch para confirmar que se ejecuta
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("🚀 [AdminUsuarioController] MÉTODO obtenerEstadisticasUsuarios INICIADO");
        System.out.println("🔍 [AdminUsuarioController] UserDetails recibido: " + (userDetails != null ? "NOT NULL" : "NULL"));
        System.out.println("🔍 [AdminUsuarioController] Email del usuario: " + (userDetails != null ? userDetails.getUsername() : "N/A"));
        System.out.println("════════════════════════════════════════════════════════");
        
        try {
            System.out.println("🔍 [AdminUsuarioController] Obteniendo estadísticas para: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener el usuario autenticado con todas las relaciones (incluyendo userCompanyRoles)
            // Usar el método combinado para asegurar que se carguen ambos sistemas de roles
            User usuarioAutenticado;
            try {
                usuarioAutenticado = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioController] Error al usar findByEmailWithAllRelationsCombined, intentando método simple: " + e.getMessage());
                usuarioAutenticado = userService.findByEmailWithAllRelations(userDetails.getUsername());
            }
            
            if (usuarioAutenticado == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            // Inicializar relaciones lazy de manera exhaustiva dentro de la transacción activa
            try {
                System.out.println("🔍 [AdminUsuarioController] Inicializando relaciones lazy para usuario: " + usuarioAutenticado.getEmail());
                
                // Inicializar usuarioEmpresas con JOIN FETCH ya debería estar cargado, pero verificamos
                if (usuarioAutenticado.getUsuarioEmpresas() != null) {
                    int size = usuarioAutenticado.getUsuarioEmpresas().size(); // Forzar inicialización
                    System.out.println("🔍 [AdminUsuarioController] usuarioEmpresas.size() = " + size);
                    
                    // Inicializar cada UsuarioEmpresa y sus relaciones
                    for (com.agrocloud.core.domain.UsuarioEmpresa ue : usuarioAutenticado.getUsuarioEmpresas()) {
                        // Forzar inicialización del enum rol
                        if (ue.getRol() != null) {
                            RolEmpresa rol = ue.getRol();
                            System.out.println("🔍 [AdminUsuarioController] UsuarioEmpresa - Rol: " + rol + ", Estado: " + ue.getEstado());
                            // Verificar si es ADMINISTRADOR o SUPERADMIN
                            RolEmpresa rolActualizado = rol.getRolActualizado();
                            System.out.println("🔍 [AdminUsuarioController] Rol actualizado: " + rolActualizado);
                        }
                        // Forzar inicialización de empresa
                        if (ue.getEmpresa() != null) {
                            Long empresaId = ue.getEmpresa().getId();
                            System.out.println("🔍 [AdminUsuarioController] Empresa ID: " + empresaId);
                        }
                        // Forzar inicialización del estado
                        com.agrocloud.model.enums.EstadoUsuarioEmpresa estado = ue.getEstado();
                        System.out.println("🔍 [AdminUsuarioController] Estado UsuarioEmpresa: " + estado);
                    }
                } else {
                    System.out.println("⚠️ [AdminUsuarioController] usuarioEmpresas es null");
                }
                
                // También inicializar userCompanyRoles (sistema legacy)
                if (usuarioAutenticado.getUserCompanyRoles() != null) {
                    int size = usuarioAutenticado.getUserCompanyRoles().size();
                    System.out.println("🔍 [AdminUsuarioController] userCompanyRoles.size() = " + size);
                    
                    for (com.agrocloud.core.domain.UserCompanyRole ucr : usuarioAutenticado.getUserCompanyRoles()) {
                        if (ucr.getRol() != null) {
                            String rolNombre = ucr.getRol().getNombre();
                            System.out.println("🔍 [AdminUsuarioController] UserCompanyRole - Rol nombre: " + rolNombre);
                        }
                        if (ucr.getEmpresa() != null) {
                            Long empresaId = ucr.getEmpresa().getId();
                            System.out.println("🔍 [AdminUsuarioController] Empresa ID (legacy): " + empresaId);
                        }
                    }
                } else {
                    System.out.println("⚠️ [AdminUsuarioController] userCompanyRoles es null");
                }
                
                System.out.println("✅ [AdminUsuarioController] Relaciones lazy inicializadas correctamente");
            } catch (org.hibernate.LazyInitializationException e) {
                System.err.println("❌ [AdminUsuarioController] LazyInitializationException al inicializar relaciones lazy: " + e.getMessage());
                System.err.println("⚠️ [AdminUsuarioController] Esto indica que las relaciones no se cargaron dentro de la transacción");
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioController] Error inesperado al inicializar relaciones lazy: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Ahora verificar roles después de inicializar todas las relaciones
            boolean esSuperAdmin = usuarioAutenticado.isSuperAdmin();
            boolean esAdmin = usuarioAutenticado.isAdmin();
            
            System.out.println("🔍 [AdminUsuarioController] Usuario autenticado para estadísticas:");
            System.out.println("  - Email: " + usuarioAutenticado.getEmail());
            System.out.println("  - ID: " + usuarioAutenticado.getId());
            System.out.println("  - esSuperAdmin: " + esSuperAdmin);
            System.out.println("  - esAdmin: " + esAdmin);
            
            Map<String, Object> estadisticas;
            
            // Verificar si es SUPERADMIN (puede ver estadísticas de todos los usuarios)
            if (esSuperAdmin) {
                System.out.println("🔍 [AdminUsuarioController] Usuario es SUPERADMIN, mostrando estadísticas de todos los usuarios");
                estadisticas = adminUsuarioService.obtenerEstadisticasUsuarios();
            } else if (esAdmin) {
                // ADMINISTRADOR solo puede ver estadísticas de sus usuarios subordinados
                System.out.println("🔍 [AdminUsuarioController] Usuario es ADMINISTRADOR, mostrando estadísticas de usuarios subordinados");
                estadisticas = adminUsuarioService.obtenerEstadisticasUsuariosSubordinados(usuarioAutenticado.getId());
            } else {
                // Otros usuarios no pueden acceder a las estadísticas
                System.out.println("❌ [AdminUsuarioController] Usuario no tiene permisos para ver estadísticas");
                System.out.println("  - Usuario ID: " + usuarioAutenticado.getId());
                System.out.println("  - Email: " + usuarioAutenticado.getEmail());
                System.out.println("  - esSuperAdmin: " + esSuperAdmin);
                System.out.println("  - esAdmin: " + esAdmin);
                
                // Retornar un mensaje de error más descriptivo
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Acceso denegado");
                errorResponse.put("mensaje", "Solo usuarios con rol SUPERADMIN o ADMINISTRADOR pueden ver estadísticas de usuarios");
                errorResponse.put("usuarioEmail", usuarioAutenticado.getEmail());
                errorResponse.put("esSuperAdmin", esSuperAdmin);
                errorResponse.put("esAdmin", esAdmin);
                
                return ResponseEntity.status(403).body(errorResponse);
            }
            
            System.out.println("🔍 [AdminUsuarioController] Estadísticas obtenidas: " + estadisticas);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.err.println("❌ [AdminUsuarioController] Error obteniendo estadísticas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener roles disponibles (filtrados por jerarquía)
     */
    @GetMapping("/roles")
    @Operation(summary = "Listar roles", description = "Obtener lista de roles que el usuario puede asignar según su jerarquía")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<RoleDTO>> obtenerTodosLosRoles(Authentication authentication) {
        try {
            System.out.println("🔍 [AdminUsuarioController] Iniciando obtención de roles");
            
            // Validar autenticación
            if (authentication == null) {
                System.err.println("❌ [AdminUsuarioController] Authentication es null");
                return ResponseEntity.status(401).body(null);
            }
            
            // Obtener usuario actual (en Spring Security, getName() retorna el email)
            String username = authentication.getName();
            System.out.println("🔍 [AdminUsuarioController] Username del contexto: " + username);
            
            // Usar siempre findByEmailWithAllRelationsCombined para cargar todas las relaciones
            // ya que en Spring Security getName() retorna el email
            User usuarioActual = null;
            try {
                usuarioActual = userService.findByEmailWithAllRelationsCombined(username);
                System.out.println("✅ [AdminUsuarioController] Usuario encontrado por email: " + username);
            } catch (Exception e) {
                System.err.println("❌ [AdminUsuarioController] Usuario no encontrado: " + username);
                System.err.println("❌ [AdminUsuarioController] Error: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(401).body(null);
            }
            
            // Inicializar relaciones lazy antes de usar getRoles()
            try {
                if (usuarioActual.getUsuarioEmpresas() != null) {
                    usuarioActual.getUsuarioEmpresas().size();
                    usuarioActual.getUsuarioEmpresas().forEach(ue -> {
                        if (ue.getRol() != null) ue.getRol().name();
                        if (ue.getEmpresa() != null) ue.getEmpresa().getId();
                    });
                }
                if (usuarioActual.getUserCompanyRoles() != null) {
                    usuarioActual.getUserCompanyRoles().size();
                    usuarioActual.getUserCompanyRoles().forEach(ucr -> {
                        if (ucr.getRol() != null) ucr.getRol().getNombre();
                        if (ucr.getEmpresa() != null) ucr.getEmpresa().getId();
                    });
                }
                // Inicializar también la relación roles (sistema legacy)
                if (usuarioActual.getRoles() != null) {
                    usuarioActual.getRoles().size();
                    usuarioActual.getRoles().forEach(role -> {
                        if (role != null) role.getNombre();
                    });
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AdminUsuarioController] Error al inicializar relaciones lazy: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("✅ [AdminUsuarioController] Usuario encontrado: " + usuarioActual.getEmail());
            System.out.println("🔍 [AdminUsuarioController] esAdmin: " + usuarioActual.isAdmin() + ", esSuperAdmin: " + usuarioActual.isSuperAdmin());
            
            // Lista de roles válidos según el enum RolEmpresa
            Set<String> rolesValidos = Set.of(
                "SUPERADMIN", 
                "ADMINISTRADOR", 
                "JEFE_CAMPO", 
                "JEFE_FINANCIERO", 
                "OPERARIO", 
                "CONSULTOR_EXTERNO"
            );
            
            // Obtener solo los roles activos (getAllRoles() ya filtra por activo = 1)
            List<Role> rolesActivos = roleService.getAllRoles();
            System.out.println("✅ [AdminUsuarioController] Total de roles activos en el sistema: " + rolesActivos.size());
            System.out.println("✅ [AdminUsuarioController] Roles activos: " + 
                rolesActivos.stream().map(Role::getNombre).collect(Collectors.toList()));
            
            // Filtrar solo roles válidos según RolEmpresa que también estén activos
            List<Role> rolesValidosYActivos = rolesActivos.stream()
                    .filter(role -> role.getActivo() != null && role.getActivo()) // Asegurar que esté activo
                    .filter(role -> rolesValidos.contains(role.getNombre())) // Filtrar por roles válidos
                    .collect(Collectors.toList());
            
            System.out.println("✅ [AdminUsuarioController] Roles válidos y activos: " + 
                rolesValidosYActivos.stream().map(Role::getNombre).collect(Collectors.toList()));
            
            // Intentar obtener rol del usuario actual (sistema nuevo primero, luego legacy)
            String rolActual = "";
            try {
                // PRIMERO: Buscar en el sistema nuevo (usuario_empresas)
                if (usuarioActual.getUsuarioEmpresas() != null && !usuarioActual.getUsuarioEmpresas().isEmpty()) {
                    for (com.agrocloud.core.domain.UsuarioEmpresa ue : usuarioActual.getUsuarioEmpresas()) {
                        if (ue.getEstado() == com.agrocloud.model.enums.EstadoUsuarioEmpresa.ACTIVO && ue.getRol() != null) {
                            com.agrocloud.model.enums.RolEmpresa rolEmpresa = ue.getRol();
                            // Aplicar mapeo de roles deprecated a roles nuevos
                            com.agrocloud.model.enums.RolEmpresa rolActualizado = rolEmpresa.getRolActualizado();
                            rolActual = rolActualizado.name(); // ADMINISTRADOR, SUPERADMIN, etc.
                            System.out.println("✅ [AdminUsuarioController] Rol encontrado en usuario_empresas: " + rolActual);
                            break;
                        }
                    }
                }
                
                // SEGUNDO: Si no se encontró en el sistema nuevo, buscar en el sistema legacy
                if (rolActual.isEmpty() && usuarioActual.getRoles() != null && !usuarioActual.getRoles().isEmpty()) {
                    String rolLegacy = usuarioActual.getRoles().stream()
                            .findFirst()
                            .map(role -> role.getNombre())
                            .orElse("");
                    
                    // Mapear roles deprecated del sistema legacy a roles nuevos
                    if ("PRODUCTOR".equals(rolLegacy) || "ASESOR".equals(rolLegacy) || "TECNICO".equals(rolLegacy)) {
                        rolActual = "JEFE_CAMPO";
                    } else if ("CONTADOR".equals(rolLegacy)) {
                        rolActual = "JEFE_FINANCIERO";
                    } else if ("LECTURA".equals(rolLegacy)) {
                        rolActual = "CONSULTOR_EXTERNO";
                    } else if (rolesValidos.contains(rolLegacy)) {
                        rolActual = rolLegacy;
                    }
                    
                    System.out.println("✅ [AdminUsuarioController] Rol encontrado en sistema legacy: " + rolLegacy + " -> mapeado a: " + rolActual);
                }
            } catch (Exception e) {
                System.err.println("❌ [AdminUsuarioController] Error obteniendo rol del usuario: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Convertir roles válidos y activos a DTOs (sin filtrar por jerarquía en este endpoint)
            // El frontend debe mostrar todos los roles válidos y activos disponibles
            List<RoleDTO> rolesDTO;
            try {
                rolesDTO = rolesValidosYActivos.stream()
                        .map(role -> {
                            RoleDTO dto = new RoleDTO();
                            dto.setId(role.getId());
                            dto.setName(role.getNombre());
                            dto.setDescription(role.getDescription());
                            dto.setCreatedAt(role.getCreatedAt());
                            dto.setUpdatedAt(role.getCreatedAt());
                            return dto;
                        })
                        .collect(Collectors.toList());
                
                System.out.println("✅ [AdminUsuarioController] Total de roles válidos disponibles: " + rolesDTO.size());
                System.out.println("✅ [AdminUsuarioController] Roles disponibles: " + 
                    rolesDTO.stream().map(RoleDTO::getName).collect(Collectors.toList()));
            } catch (Exception e) {
                System.err.println("❌ [AdminUsuarioController] Error convirtiendo roles a DTOs: " + e.getMessage());
                e.printStackTrace();
                // Si falla, devolver lista vacía
                rolesDTO = new ArrayList<>();
            }
            
            return ResponseEntity.ok(rolesDTO);
        } catch (Exception e) {
            System.err.println("❌ [AdminUsuarioController] Error general obteniendo roles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Verifica si un usuario con un rol puede asignar otro rol según la jerarquía
     * Basado en el enum RolEmpresa:
     * 
     * SUPERADMIN         → Puede asignar TODOS los roles válidos
     * ADMINISTRADOR      → Puede asignar: JEFE_CAMPO, JEFE_FINANCIERO, OPERARIO, CONSULTOR_EXTERNO
     * JEFE_CAMPO         → No puede asignar roles
     * JEFE_FINANCIERO    → No puede asignar roles
     * OPERARIO           → No puede asignar roles
     * CONSULTOR_EXTERNO  → No puede asignar roles
     */
    private boolean puedeAsignarRol(String rolUsuario, String rolAAsignar) {
        // Lista de roles válidos según el enum RolEmpresa
        Set<String> rolesValidos = Set.of(
            "SUPERADMIN", 
            "ADMINISTRADOR", 
            "JEFE_CAMPO", 
            "JEFE_FINANCIERO", 
            "OPERARIO", 
            "CONSULTOR_EXTERNO"
        );
        
        // Solo permitir asignar roles válidos
        if (!rolesValidos.contains(rolAAsignar)) {
            return false;
        }
        
        // SUPERADMIN puede asignar cualquier rol válido
        if ("SUPERADMIN".equals(rolUsuario)) {
            return true;
        }
        
        // ADMINISTRADOR puede asignar roles subordinados (no SUPERADMIN ni ADMINISTRADOR)
        if ("ADMINISTRADOR".equals(rolUsuario)) {
            return !("SUPERADMIN".equals(rolAAsignar) || 
                     "ADMINISTRADOR".equals(rolAAsignar));
        }
        
        // Otros roles no pueden asignar roles
        return false;
    }

    /**
     * Buscar usuarios con filtros
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar usuarios", description = "Buscar usuarios con filtros avanzados")
    public ResponseEntity<List<AdminUsuarioDTO>> buscarUsuarios(
            @RequestParam(required = false) EstadoUsuario estado,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Long creadoPorId,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String searchTerm) {
        try {
            List<AdminUsuarioDTO> usuarios = adminUsuarioService.buscarUsuariosConFiltros(
                    estado, roleName, creadoPorId, activo, searchTerm);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // ========================================
    // ENDPOINTS DE VALIDACIÓN Y PREVENCIÓN
    // ========================================

    /**
     * Validar y corregir jerarquía de un usuario específico
     */
    @PostMapping("/{id}/validar-jerarquia")
    @Operation(summary = "Validar jerarquía", description = "Validar y corregir la jerarquía de un usuario específico")
    public ResponseEntity<AdminUsuarioDTO> validarJerarquiaUsuario(@PathVariable Long id) {
        try {
            AdminUsuarioDTO usuarioCorregido = adminUsuarioService.validarYCorregirJerarquia(id);
            return ResponseEntity.ok(usuarioCorregido);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Validar y corregir jerarquía de todos los usuarios
     */
    @PostMapping("/validar-jerarquia-global")
    @Operation(summary = "Validar jerarquía global", description = "Validar y corregir la jerarquía de todos los usuarios del sistema")
    public ResponseEntity<List<AdminUsuarioDTO>> validarJerarquiaGlobal() {
        try {
            List<AdminUsuarioDTO> usuariosCorregidos = adminUsuarioService.validarYCorregirJerarquiaGlobal();
            return ResponseEntity.ok(usuariosCorregidos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener reporte de jerarquía del sistema
     */
    @GetMapping("/reporte-jerarquia")
    @Operation(summary = "Reporte de jerarquía", description = "Obtener reporte completo de la jerarquía de usuarios")
    public ResponseEntity<Map<String, Object>> obtenerReporteJerarquia() {
        try {
            // Obtener todos los usuarios
            List<AdminUsuarioDTO> usuarios = adminUsuarioService.obtenerTodosLosUsuarios();
            
            // Analizar jerarquía
            Map<String, Object> reporte = new HashMap<>();
            reporte.put("totalUsuarios", usuarios.size());
            
            // Usuarios raíz (sin padre)
            long usuariosRaiz = usuarios.stream()
                    .filter(u -> u.getParentUserId() == null)
                    .count();
            reporte.put("usuariosRaiz", usuariosRaiz);
            
            // Usuarios hijos (con padre)
            long usuariosHijos = usuarios.stream()
                    .filter(u -> u.getParentUserId() != null)
                    .count();
            reporte.put("usuariosHijos", usuariosHijos);
            
            // Usuarios sin creador
            long usuariosSinCreador = usuarios.stream()
                    .filter(u -> u.getCreadoPorId() == null)
                    .count();
            reporte.put("usuariosSinCreador", usuariosSinCreador);
            
            // Usuarios con SUPERADMIN o ADMINISTRADOR como padre
            long usuariosConAdminPadre = usuarios.stream()
                    .filter(u -> {
                        if (u.getParentUserId() == null) return false;
                        // Buscar el usuario padre para ver si es SUPERADMIN o ADMINISTRADOR
                        try {
                            AdminUsuarioDTO padre = adminUsuarioService.obtenerUsuarioPorId(u.getParentUserId());
                            return padre.getRoles() != null && padre.getRoles().stream()
                                    .anyMatch(r -> "SUPERADMIN".equals(r) || "ADMINISTRADOR".equals(r));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
            reporte.put("usuariosConAdministradorPadre", usuariosConAdminPadre);
            
            // Usuarios que necesitan corrección
            long usuariosNecesitanCorreccion = usuariosRaiz + usuariosSinCreador;
            reporte.put("usuariosNecesitanCorreccion", usuariosNecesitanCorreccion);
            
            // Estado general
            String estadoGeneral = usuariosNecesitanCorreccion == 0 ? "✅ CORRECTO" : "❌ REQUIERE CORRECCIÓN";
            reporte.put("estadoGeneral", estadoGeneral);
            
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
