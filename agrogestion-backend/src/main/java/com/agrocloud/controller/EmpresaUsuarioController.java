package com.agrocloud.controller;

import com.agrocloud.dto.UsuarioEmpresaDTO;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.model.entity.UsuarioEmpresaRol;
import com.agrocloud.service.EmpresaUsuarioService;
import com.agrocloud.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de usuarios y empresas
 */
@RestController
@RequestMapping("/api/empresa-usuario")
@CrossOrigin(origins = "*")
public class EmpresaUsuarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmpresaUsuarioController.class);
    
    @Autowired
    private EmpresaUsuarioService empresaUsuarioService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Asignar un usuario a una empresa con un rol específico
     */
    @PostMapping("/asignar")
    public ResponseEntity<Map<String, Object>> asignarUsuarioAEmpresa(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener usuario actual desde el token
            String username = authentication.getName();
            User usuarioActual = userService.findByEmailWithAllRelations(username);
            
            // Verificar permisos: solo SUPERADMIN puede asignar usuarios a empresas
            if (!esSuperAdmin(usuarioActual)) {
                response.put("success", false);
                response.put("message", "No tienes permisos para asignar usuarios a empresas");
                return ResponseEntity.status(403).body(response);
            }
            
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            Long empresaId = Long.valueOf(request.get("empresaId").toString());
            RolEmpresa rol = RolEmpresa.valueOf(request.get("rol").toString());
            
            UsuarioEmpresa relacion = empresaUsuarioService.asignarUsuarioAEmpresa(
                    usuarioId,
                    empresaId,
                    rol,
                    usuarioActual
            );
            
            response.put("success", true);
            response.put("message", "Usuario asignado a empresa correctamente");
            response.put("data", convertirAUsuarioEmpresaDTO(relacion));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error asignando usuario a empresa: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Remover un usuario de una empresa
     */
    @DeleteMapping("/remover/{usuarioId}/{empresaId}")
    public ResponseEntity<Map<String, Object>> removerUsuarioDeEmpresa(
            @PathVariable Long usuarioId,
            @PathVariable Long empresaId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener usuario actual desde el token
            String username = authentication.getName();
            User usuarioActual = userService.findByEmailWithAllRelations(username);
            
            // Verificar permisos: solo SUPERADMIN puede remover usuarios de empresas
            if (!usuarioActual.isAdmin() && !"SUPERADMIN".equals(usuarioActual.getRoles().iterator().next().getNombre())) {
                response.put("success", false);
                response.put("message", "No tienes permisos para remover usuarios de empresas");
                return ResponseEntity.status(403).body(response);
            }
            
            empresaUsuarioService.removerUsuarioDeEmpresa(usuarioId, empresaId);
            
            response.put("success", true);
            response.put("message", "Usuario removido de empresa correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error removiendo usuario de empresa: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener todas las empresas de un usuario
     */
    @GetMapping("/usuario/{usuarioId}/empresas")
    public ResponseEntity<Map<String, Object>> obtenerEmpresasDeUsuario(@PathVariable Long usuarioId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UsuarioEmpresa> relaciones = empresaUsuarioService.obtenerEmpresasDeUsuario(usuarioId);
            List<UsuarioEmpresaDTO> empresasDTO = relaciones.stream()
                    .map(this::convertirAUsuarioEmpresaDTO)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", empresasDTO);
            response.put("total", empresasDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo empresas del usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener todas las empresas activas de un usuario
     */
    @GetMapping("/usuario/{usuarioId}/empresas/activas")
    public ResponseEntity<Map<String, Object>> obtenerEmpresasActivasDeUsuario(@PathVariable Long usuarioId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UsuarioEmpresa> relaciones = empresaUsuarioService.obtenerEmpresasActivasDeUsuario(usuarioId);
            List<UsuarioEmpresaDTO> empresasDTO = relaciones.stream()
                    .map(this::convertirAUsuarioEmpresaDTO)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", empresasDTO);
            response.put("total", empresasDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo empresas activas del usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener el rol de un usuario en una empresa específica
     */
    @GetMapping("/usuario/{usuarioId}/empresa/{empresaId}/rol")
    public ResponseEntity<Map<String, Object>> obtenerRolUsuarioEnEmpresa(
            @PathVariable Long usuarioId,
            @PathVariable Long empresaId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var rol = empresaUsuarioService.obtenerRolUsuarioEnEmpresa(usuarioId, empresaId);
            
            response.put("success", true);
            response.put("usuarioId", usuarioId);
            response.put("empresaId", empresaId);
            response.put("rol", rol.orElse(null));
            response.put("tieneAcceso", rol.isPresent());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo rol del usuario en empresa: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener todos los usuarios de una empresa
     */
    @GetMapping("/empresa/{empresaId}/usuarios")
    public ResponseEntity<Map<String, Object>> obtenerUsuariosDeEmpresa(@PathVariable Long empresaId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UsuarioEmpresa> relaciones = empresaUsuarioService.obtenerUsuariosDeEmpresa(empresaId);
            List<UsuarioEmpresaDTO> usuariosDTO = relaciones.stream()
                    .map(this::convertirAUsuarioEmpresaDTO)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", usuariosDTO);
            response.put("total", usuariosDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo usuarios de la empresa: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cambiar el rol de un usuario en una empresa
     */
    @PutMapping("/cambiar-rol")
    public ResponseEntity<Map<String, Object>> cambiarRolUsuarioEnEmpresa(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener usuario actual desde el token
            String username = authentication.getName();
            User usuarioActual = userService.findByEmailWithAllRelations(username);
            
            // Verificar permisos: solo SUPERADMIN puede cambiar roles
            if (!usuarioActual.isAdmin() && !"SUPERADMIN".equals(usuarioActual.getRoles().iterator().next().getNombre())) {
                response.put("success", false);
                response.put("message", "No tienes permisos para cambiar roles de usuarios");
                return ResponseEntity.status(403).body(response);
            }
            
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            Long empresaId = Long.valueOf(request.get("empresaId").toString());
            RolEmpresa nuevoRol = RolEmpresa.valueOf(request.get("rol").toString());
            
            UsuarioEmpresa relacion = empresaUsuarioService.cambiarRolUsuarioEnEmpresa(usuarioId, empresaId, nuevoRol);
            
            response.put("success", true);
            response.put("message", "Rol cambiado correctamente");
            response.put("data", convertirAUsuarioEmpresaDTO(relacion));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cambiando rol del usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Asignar múltiples roles a un usuario en una empresa
     */
    @PutMapping("/asignar-roles")
    public ResponseEntity<Map<String, Object>> asignarRolesAUsuarioEnEmpresa(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener usuario actual desde el token
            String username = authentication.getName();
            User usuarioActual = userService.findByEmailWithAllRelations(username);
            
            // Verificar permisos: solo SUPERADMIN puede cambiar roles
            if (!usuarioActual.isAdmin() && !"SUPERADMIN".equals(usuarioActual.getRoles().iterator().next().getNombre())) {
                response.put("success", false);
                response.put("message", "No tienes permisos para cambiar roles de usuarios");
                return ResponseEntity.status(403).body(response);
            }
            
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            Long empresaId = Long.valueOf(request.get("empresaId").toString());
            
            @SuppressWarnings("unchecked")
            List<String> rolesStr = (List<String>) request.get("roles");
            Set<RolEmpresa> roles = rolesStr.stream()
                    .map(RolEmpresa::valueOf)
                    .collect(java.util.stream.Collectors.toSet());
            
            List<UsuarioEmpresaRol> rolesAsignados = empresaUsuarioService.asignarRolesAUsuarioEnEmpresa(usuarioId, empresaId, roles);
            
            List<String> rolesNombres = rolesAsignados.stream()
                    .map(uer -> uer.getRol().getNombre())
                    .collect(java.util.stream.Collectors.toList());
            
            response.put("success", true);
            response.put("message", "Roles asignados correctamente");
            response.put("roles", rolesNombres);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error asignando roles al usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener todos los roles de un usuario en una empresa
     */
    @GetMapping("/roles-usuario")
    public ResponseEntity<Map<String, Object>> obtenerRolesDeUsuarioEnEmpresa(
            @RequestParam Long usuarioId,
            @RequestParam Long empresaId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<RolEmpresa> roles = empresaUsuarioService.obtenerRolesDeUsuarioEnEmpresa(usuarioId, empresaId);
            List<String> rolesNombres = roles.stream()
                    .map(RolEmpresa::name)
                    .collect(java.util.stream.Collectors.toList());
            
            response.put("success", true);
            response.put("roles", rolesNombres);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo roles del usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cambiar el estado de un usuario en una empresa
     */
    @PutMapping("/cambiar-estado")
    public ResponseEntity<Map<String, Object>> cambiarEstadoUsuarioEnEmpresa(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener usuario actual desde el token
            String username = authentication.getName();
            User usuarioActual = userService.findByEmailWithAllRelations(username);
            
            // Verificar permisos: solo SUPERADMIN puede cambiar estados
            if (!usuarioActual.isAdmin() && !"SUPERADMIN".equals(usuarioActual.getRoles().iterator().next().getNombre())) {
                response.put("success", false);
                response.put("message", "No tienes permisos para cambiar estados de usuarios");
                return ResponseEntity.status(403).body(response);
            }
            
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            Long empresaId = Long.valueOf(request.get("empresaId").toString());
            EstadoUsuarioEmpresa nuevoEstado = EstadoUsuarioEmpresa.valueOf(request.get("estado").toString());
            
            UsuarioEmpresa relacion = empresaUsuarioService.cambiarEstadoUsuarioEnEmpresa(usuarioId, empresaId, nuevoEstado);
            
            response.put("success", true);
            response.put("message", "Estado cambiado correctamente");
            response.put("data", convertirAUsuarioEmpresaDTO(relacion));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cambiando estado del usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener todas las relaciones usuario-empresa (para administradores)
     */
    @GetMapping("/todas-relaciones")
    public ResponseEntity<Map<String, Object>> obtenerTodasLasRelaciones(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener usuario actual desde el token
            String username = authentication.getName();
            User usuarioActual = userService.findByEmailWithAllRelations(username);
            
            // Verificar permisos: solo SUPERADMIN puede ver todas las relaciones
            if (!esSuperAdmin(usuarioActual)) {
                response.put("success", false);
                response.put("message", "No tienes permisos para ver todas las relaciones");
                return ResponseEntity.status(403).body(response);
            }
            
            List<UsuarioEmpresa> todasLasRelaciones = empresaUsuarioService.obtenerTodasLasRelaciones();
            List<UsuarioEmpresaDTO> relacionesDTO = todasLasRelaciones.stream()
                    .map(this::convertirAUsuarioEmpresaDTO)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", relacionesDTO);
            response.put("total", relacionesDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo todas las relaciones: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener estadísticas de usuarios por empresa
     */
    @GetMapping("/empresa/{empresaId}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasEmpresa(@PathVariable Long empresaId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UsuarioEmpresa> usuarios = empresaUsuarioService.obtenerUsuariosDeEmpresa(empresaId);
            
            Map<String, Long> estadisticas = usuarios.stream()
                    .collect(Collectors.groupingBy(
                            ue -> ue.getRol().name(),
                            Collectors.counting()
                    ));
            
            long totalUsuarios = usuarios.size();
            long usuariosActivos = usuarios.stream()
                    .filter(ue -> ue.getEstado() == EstadoUsuarioEmpresa.ACTIVO)
                    .count();
            
            response.put("success", true);
            response.put("empresaId", empresaId);
            response.put("totalUsuarios", totalUsuarios);
            response.put("usuariosActivos", usuariosActivos);
            response.put("usuariosInactivos", totalUsuarios - usuariosActivos);
            response.put("distribucionPorRol", estadisticas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de la empresa: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verificar si un usuario es SUPERADMIN
     */
    private boolean esSuperAdmin(User usuario) {
        if (usuario == null) return false;
        
        // Verificar en el sistema multitenant (UserCompanyRole)
        boolean esSuperAdmin = usuario.getUserCompanyRoles().stream()
            .anyMatch(ucr -> "SUPERADMIN".equals(ucr.getRol().getNombre()));
        
        // Fallback: verificar en el sistema antiguo (Roles directos)
        if (!esSuperAdmin && !usuario.getRoles().isEmpty()) {
            esSuperAdmin = "SUPERADMIN".equals(usuario.getRoles().iterator().next().getNombre());
        }
        
        return esSuperAdmin;
    }
    
    /**
     * Convertir UsuarioEmpresa a DTO
     */
    private UsuarioEmpresaDTO convertirAUsuarioEmpresaDTO(UsuarioEmpresa relacion) {
        UsuarioEmpresaDTO dto = new UsuarioEmpresaDTO();
        dto.setId(relacion.getId());
        dto.setUsuarioId(relacion.getUsuario().getId());
        dto.setUsuarioEmail(relacion.getUsuario().getEmail());
        dto.setUsuarioNombre(relacion.getUsuario().getFirstName() + " " + relacion.getUsuario().getLastName());
        dto.setEmpresaId(relacion.getEmpresa().getId());
        dto.setEmpresaNombre(relacion.getEmpresa().getNombre());
        dto.setRol(relacion.getRol());
        dto.setEstado(relacion.getEstado());
        dto.setFechaInicio(relacion.getFechaInicio());
        dto.setFechaFin(relacion.getFechaFin());
        
        if (relacion.getCreadoPor() != null) {
            dto.setCreadoPorId(relacion.getCreadoPor().getId());
            dto.setCreadoPorEmail(relacion.getCreadoPor().getEmail());
        }
        
        return dto;
    }
}
