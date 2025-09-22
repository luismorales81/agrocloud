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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * Obtener todos los usuarios
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtener lista de todos los usuarios del sistema")
    public ResponseEntity<List<AdminUsuarioDTO>> obtenerTodosLosUsuarios() {
        try {
            List<AdminUsuarioDTO> usuarios = adminUsuarioService.obtenerTodosLosUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario", description = "Obtener usuario específico por ID")
    public ResponseEntity<AdminUsuarioDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        try {
            AdminUsuarioDTO usuario = adminUsuarioService.obtenerUsuarioPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
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
                                                       HttpServletRequest httpRequest) {
        try {
            // TODO: En producción, obtener usuario actual del JWT
            // Por ahora, validamos que sea ADMIN
            User usuarioActual = userService.findByUsername("admin");
            
            // VALIDACIÓN DE PERMISOS: Solo ADMIN puede crear usuarios
            if (!adminUsuarioService.puedeGestionarUsuario(usuarioActual, null)) {
                return ResponseEntity.status(403).body(null);
            }
            
            AdminUsuarioDTO usuarioCreado = adminUsuarioService.crearUsuario(usuarioDTO, usuarioActual);
            return ResponseEntity.ok(usuarioCreado);
        } catch (Exception e) {
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
                                                             HttpServletRequest httpRequest) {
        try {
            // Por ahora, usamos el usuario admin como actualizador
            User usuarioActual = userService.findByUsername("admin");
            
            AdminUsuarioDTO usuarioActualizado = adminUsuarioService.actualizarUsuario(id, usuarioDTO, usuarioActual);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
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
                                                               HttpServletRequest httpRequest) {
        try {
            User usuarioActual = userService.findByUsername("admin");
            
            AdminUsuarioDTO usuarioActualizado = adminUsuarioService.cambiarEstadoUsuario(id, estado, usuarioActual);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            e.printStackTrace();
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
                                                              HttpServletRequest httpRequest) {
        try {
            User usuarioActual = userService.findByUsername("admin");
            
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
                                                             HttpServletRequest httpRequest) {
        try {
            User usuarioActual = userService.findByUsername("admin");
            
            AdminUsuarioDTO usuarioActualizado = adminUsuarioService.resetearContraseña(id, nuevaContraseña, usuarioActual);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener estadísticas de usuarios
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas", description = "Obtener estadísticas generales de usuarios")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsuarios() {
        try {
            Map<String, Object> estadisticas = adminUsuarioService.obtenerEstadisticasUsuarios();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtener todos los roles disponibles
     */
    @GetMapping("/roles")
    @Operation(summary = "Listar roles", description = "Obtener lista de todos los roles disponibles")
    public ResponseEntity<List<RoleDTO>> obtenerTodosLosRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            List<RoleDTO> rolesDTO = roles.stream()
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
            return ResponseEntity.ok(rolesDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
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
            
            // Usuarios con ADMIN como padre
            long usuariosConAdminPadre = usuarios.stream()
                    .filter(u -> {
                        if (u.getParentUserId() == null) return false;
                        // Buscar el usuario padre para ver si es ADMIN
                        try {
                            AdminUsuarioDTO padre = adminUsuarioService.obtenerUsuarioPorId(u.getParentUserId());
                            return "admin".equals(padre.getUsername());
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
            reporte.put("usuariosConAdminPadre", usuariosConAdminPadre);
            
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
