package com.agrocloud.service;

import com.agrocloud.dto.AdminUsuarioDTO;
import com.agrocloud.dto.RoleDTO;
import com.agrocloud.model.entity.EstadoUsuario;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para la administración de usuarios
 */
@Service
@Transactional
public class AdminUsuarioService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LogAccesoService logAccesoService;

    /**
     * Obtener todos los usuarios para administración
     */
    public List<AdminUsuarioDTO> obtenerTodosLosUsuarios() {
        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .map(this::convertirAAdminDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener usuario por ID para administración
     */
    public AdminUsuarioDTO obtenerUsuarioPorId(Long id) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return convertirAAdminDTO(usuario);
    }

    /**
     * Crear nuevo usuario con prevención automática de jerarquía
     */
    public AdminUsuarioDTO crearUsuario(AdminUsuarioDTO usuarioDTO, User creadoPor) {
        // VALIDACIÓN DE PERMISOS: Solo ADMIN puede crear usuarios
        if (creadoPor == null || !tienePermisoParaCrearUsuarios(creadoPor)) {
            throw new RuntimeException("No tienes permisos para crear usuarios. Solo los administradores pueden crear nuevos usuarios en el sistema.");
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
        nuevoUsuario.setPassword(passwordEncoder.encode("password123")); // Contraseña por defecto
        nuevoUsuario.setFirstName(usuarioDTO.getFirstName());
        nuevoUsuario.setLastName(usuarioDTO.getLastName());
        nuevoUsuario.setEmail(usuarioDTO.getEmail());
        nuevoUsuario.setPhone(usuarioDTO.getPhone());
        nuevoUsuario.setEstado(EstadoUsuario.PENDIENTE);
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setEmailVerified(false);
        
        // PREVENCIÓN AUTOMÁTICA: Siempre asignar creador
        nuevoUsuario.setCreadoPor(creadoPor != null ? creadoPor : obtenerUsuarioAdminPorDefecto());

        // PREVENCIÓN AUTOMÁTICA: Asignar jerarquía automáticamente
        nuevoUsuario.setParentUser(asignarUsuarioPadreAutomaticamente(usuarioDTO, creadoPor));

        // Asignar roles si se especifican
        if (usuarioDTO.getRoles() != null && !usuarioDTO.getRoles().isEmpty()) {
            Set<Role> roles = usuarioDTO.getRoles().stream()
                    .map(roleDTO -> roleRepository.findById(roleDTO.getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            nuevoUsuario.setRoles(roles);
        }

        User usuarioCreado = userRepository.save(nuevoUsuario);
        return convertirAAdminDTO(usuarioCreado);
    }

    /**
     * Actualizar usuario existente
     */
    public AdminUsuarioDTO actualizarUsuario(Long id, AdminUsuarioDTO usuarioDTO, User actualizadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Actualizar campos permitidos
        usuario.setFirstName(usuarioDTO.getFirstName());
        usuario.setLastName(usuarioDTO.getLastName());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPhone(usuarioDTO.getPhone());
        usuario.setEstado(usuarioDTO.getEstado());
        usuario.setActivo(usuarioDTO.getActivo());
        usuario.setEmailVerified(usuarioDTO.getEmailVerified());

        // Actualizar roles si se especifican
        if (usuarioDTO.getRoles() != null) {
            Set<Role> roles = usuarioDTO.getRoles().stream()
                    .map(roleDTO -> roleRepository.findById(roleDTO.getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            usuario.setRoles(roles);
        }

        // Actualizar usuario padre si se especifica
        if (usuarioDTO.getParentUserId() != null) {
            User parentUser = userRepository.findById(usuarioDTO.getParentUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario padre no encontrado"));
            usuario.setParentUser(parentUser);
        }

        User usuarioActualizado = userRepository.save(usuario);
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

        // Roles
        if (user.getRoles() != null) {
            Set<RoleDTO> rolesDTO = user.getRoles().stream()
                    .map(this::convertirARoleDTO)
                    .collect(Collectors.toSet());
            dto.setRoles(rolesDTO);
        }

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

    // ========================================
    // MÉTODOS DE PREVENCIÓN AUTOMÁTICA
    // ========================================

    /**
     * Obtener usuario ADMIN por defecto para asignaciones automáticas
     */
    private User obtenerUsuarioAdminPorDefecto() {
        return userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("No se encontró usuario ADMIN para asignaciones automáticas"));
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

        // Si el creador es ADMIN, asignar ADMIN como padre
        if (creadoPor != null && "admin".equals(creadoPor.getUsername())) {
            return creadoPor;
        }

        // Si el creador no es ADMIN, asignar ADMIN como padre por defecto
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
        if (usuario == null || usuario.getRoles() == null) {
            return false;
        }

        // Solo ADMIN puede crear usuarios
        return usuario.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getNombre()));
    }

    /**
     * Verificar si un usuario puede gestionar otro usuario específico
     */
    public boolean puedeGestionarUsuario(User usuarioGestor, Long usuarioIdAGestionar) {
        if (usuarioGestor == null || usuarioIdAGestionar == null) {
            return false;
        }

        // ADMIN puede gestionar cualquier usuario
        if (tienePermisoParaCrearUsuarios(usuarioGestor)) {
            return true;
        }

        // Un usuario solo puede gestionar sus propios datos
        return usuarioGestor.getId().equals(usuarioIdAGestionar);
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
