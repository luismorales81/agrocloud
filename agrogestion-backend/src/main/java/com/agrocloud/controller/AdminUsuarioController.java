package com.agrocloud.controller;

import com.agrocloud.dto.AdminUsuarioDTO;
import com.agrocloud.dto.RoleDTO;
import com.agrocloud.model.entity.EstadoUsuario;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.AdminUsuarioService;
import com.agrocloud.service.RoleService;
import com.agrocloud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Administración de Usuarios", description = "Endpoints para gestión de usuarios por administradores")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000"})
public class AdminUsuarioController {

    @Autowired
    private AdminUsuarioService adminUsuarioService;

    @Autowired
    private RoleService roleService;

    @Autowired
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
    public ResponseEntity<List<AdminUsuarioDTO>> obtenerUsuariosSegunPermisos(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🔍 [AdminUsuarioController] Iniciando obtención de usuarios para: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener el usuario autenticado
            User usuarioAutenticado = userService.findByEmailWithRelations(userDetails.getUsername());
            if (usuarioAutenticado == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            System.out.println("🔍 [AdminUsuarioController] Usuario autenticado: " + usuarioAutenticado.getEmail() + ", esAdmin: " + usuarioAutenticado.isAdmin() + ", esSuperAdmin: " + usuarioAutenticado.isSuperAdmin());
            System.out.println("🔍 [AdminUsuarioController] Roles del usuario: " + usuarioAutenticado.getRoles().stream().map(r -> r.getNombre()).toList());
            
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
            User usuarioAutenticado = userService.findByEmailWithRelations(userDetails.getUsername());
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
            // Obtener usuario actual del contexto de autenticación
            String username = authentication.getName();
            System.out.println("🔍 [AdminUsuarioController] Username del contexto: " + username);
            
            // Intentar buscar por username primero, luego por email
            User usuarioActual = null;
            try {
                usuarioActual = userService.findByUsername(username);
                System.out.println("✅ [AdminUsuarioController] Usuario encontrado por username");
            } catch (Exception e) {
                System.out.println("⚠️ [AdminUsuarioController] Usuario no encontrado por username, intentando por email...");
                try {
                    usuarioActual = userService.findByEmailWithRelations(username);
                    System.out.println("✅ [AdminUsuarioController] Usuario encontrado por email");
                } catch (Exception e2) {
                    System.err.println("❌ [AdminUsuarioController] Usuario no encontrado ni por username ni por email: " + username);
                    return ResponseEntity.status(401).body(null);
                }
            }
            
            System.out.println("✅ [AdminUsuarioController] Usuario autenticado: " + usuarioActual.getEmail());
            System.out.println("✅ [AdminUsuarioController] Roles del usuario: " + usuarioActual.getRoles());
            
            // VALIDACIÓN DE PERMISOS: Solo ADMIN puede crear usuarios
            if (!adminUsuarioService.puedeGestionarUsuario(usuarioActual, null)) {
                System.err.println("❌ [AdminUsuarioController] Usuario sin permisos para gestionar usuarios");
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
                usuarioActual = userService.findByEmailWithRelations(username);
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
                usuarioActual = userService.findByEmailWithRelations(username);
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
                usuarioActual = userService.findByEmailWithRelations(username);
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
                usuarioActual = userService.findByEmailWithRelations(username);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsuarios(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🔍 [AdminUsuarioController] Obteniendo estadísticas para: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener el usuario autenticado
            User usuarioAutenticado = userService.findByEmailWithRelations(userDetails.getUsername());
            if (usuarioAutenticado == null) {
                System.err.println("❌ [AdminUsuarioController] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            System.out.println("🔍 [AdminUsuarioController] Usuario autenticado para estadísticas: " + usuarioAutenticado.getEmail() + ", esSuperAdmin: " + usuarioAutenticado.isSuperAdmin());
            
            Map<String, Object> estadisticas;
            
            // Verificar si es SUPERADMIN (puede ver estadísticas de todos los usuarios)
            if (usuarioAutenticado.isSuperAdmin()) {
                System.out.println("🔍 [AdminUsuarioController] Usuario es SUPERADMIN, mostrando estadísticas de todos los usuarios");
                estadisticas = adminUsuarioService.obtenerEstadisticasUsuarios();
            } else if (usuarioAutenticado.isAdmin()) {
                // ADMINISTRADOR solo puede ver estadísticas de sus usuarios subordinados
                System.out.println("🔍 [AdminUsuarioController] Usuario es ADMINISTRADOR, mostrando estadísticas de usuarios subordinados");
                estadisticas = adminUsuarioService.obtenerEstadisticasUsuariosSubordinados(usuarioAutenticado.getId());
            } else {
                // Otros usuarios no pueden acceder a las estadísticas
                System.out.println("❌ [AdminUsuarioController] Usuario no tiene permisos para ver estadísticas");
                return ResponseEntity.status(403).build();
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
    public ResponseEntity<List<RoleDTO>> obtenerTodosLosRoles(Authentication authentication) {
        try {
            System.out.println("🔍 [AdminUsuarioController] Iniciando obtención de roles");
            
            // Validar autenticación
            if (authentication == null) {
                System.err.println("❌ [AdminUsuarioController] Authentication es null");
                return ResponseEntity.status(401).body(null);
            }
            
            // Obtener usuario actual
            String username = authentication.getName();
            System.out.println("🔍 [AdminUsuarioController] Username del contexto: " + username);
            
            // Intentar buscar por username primero, luego por email
            User usuarioActual = null;
            try {
                usuarioActual = userService.findByUsername(username);
                System.out.println("✅ [AdminUsuarioController] Usuario encontrado por username");
            } catch (Exception e) {
                System.out.println("⚠️ [AdminUsuarioController] Usuario no encontrado por username, intentando por email...");
                try {
                    usuarioActual = userService.findByEmailWithRelations(username);
                    System.out.println("✅ [AdminUsuarioController] Usuario encontrado por email");
                } catch (Exception e2) {
                    System.err.println("❌ [AdminUsuarioController] Usuario no encontrado ni por username ni por email: " + username);
                    return ResponseEntity.status(401).body(null);
                }
            }
            
            System.out.println("✅ [AdminUsuarioController] Usuario encontrado: " + usuarioActual.getEmail());
            
            // Obtener todos los roles primero (sin filtrar) por si hay error
            List<Role> roles = roleService.getAllRoles();
            System.out.println("✅ [AdminUsuarioController] Total de roles en el sistema: " + roles.size());
            
            // Intentar obtener rol del usuario actual
            String rolActual = "";
            try {
                rolActual = usuarioActual.getRoles().stream()
                        .findFirst()
                        .map(role -> role.getNombre())
                        .orElse("");
                System.out.println("✅ [AdminUsuarioController] Rol del usuario: " + rolActual);
            } catch (Exception e) {
                System.err.println("❌ [AdminUsuarioController] Error obteniendo rol del usuario: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Filtrar roles según jerarquía
            List<RoleDTO> rolesDTO;
            try {
                final String rolFinal = rolActual; // Variable final para lambda
                rolesDTO = roles.stream()
                        .filter(role -> puedeAsignarRol(rolFinal, role.getNombre()))
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
                
                System.out.println("✅ [AdminUsuarioController] Roles filtrados para " + rolActual + ": " + 
                    rolesDTO.stream().map(RoleDTO::getName).collect(Collectors.toList()));
            } catch (Exception e) {
                System.err.println("❌ [AdminUsuarioController] Error filtrando roles, devolviendo todos: " + e.getMessage());
                e.printStackTrace();
                // Si falla el filtrado, devolver todos los roles
                rolesDTO = roles.stream()
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
     * 
     * SUPERADMIN (7)      → Puede asignar TODOS los roles
     * ADMINISTRADOR (6)   → Puede asignar: PRODUCTOR, ASESOR, TECNICO, OPERARIO, INVITADO
     * PRODUCTOR (5)       → No puede asignar roles
     * ASESOR (4)          → No puede asignar roles
     * TECNICO (2)         → No puede asignar roles
     * OPERARIO (1)        → No puede asignar roles
     * INVITADO (0)        → No puede asignar roles
     */
    private boolean puedeAsignarRol(String rolUsuario, String rolAAsignar) {
        // SUPERADMIN puede asignar cualquier rol
        if ("SUPERADMIN".equals(rolUsuario)) {
            return true;
        }
        
        // ADMINISTRADOR puede asignar roles subordinados (no SUPERADMIN, ADMINISTRADOR ni ADMIN)
        if ("ADMINISTRADOR".equals(rolUsuario)) {
            return !("SUPERADMIN".equals(rolAAsignar) || 
                     "ADMINISTRADOR".equals(rolAAsignar) || 
                     "ADMIN".equals(rolAAsignar));
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
