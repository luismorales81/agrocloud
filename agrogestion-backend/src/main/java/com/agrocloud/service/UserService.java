package com.agrocloud.service;

import com.agrocloud.dto.UsuarioDTO;
import com.agrocloud.model.entity.EstadoUsuario;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
    
    public User findByEmailWithRelations(String email) {
        return userRepository.findByEmailWithRelations(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
    
    public User findByEmailWithAllRelations(String email) {
        return userRepository.findByEmailWithAllRelations(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Crear un nuevo usuario
     */
    public User crearUsuario(User usuario, User creadoPor) {
        // Validar que el email y username no existan
        if (userRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + usuario.getEmail());
        }
        if (userRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe: " + usuario.getUsername());
        }

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Establecer creado por
        usuario.setCreadoPor(creadoPor);
        
        // Establecer estado inicial
        usuario.setEstado(EstadoUsuario.PENDIENTE);
        usuario.setActivo(true);
        usuario.setEmailVerified(false);

        return userRepository.save(usuario);
    }

    /**
     * Actualizar usuario existente
     */
    public User actualizarUsuario(Long id, User usuarioActualizado, User actualizadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Actualizar campos permitidos
        usuario.setFirstName(usuarioActualizado.getFirstName());
        usuario.setLastName(usuarioActualizado.getLastName());
        usuario.setEmail(usuarioActualizado.getEmail());
        usuario.setPhone(usuarioActualizado.getPhone());
        usuario.setRoles(usuarioActualizado.getRoles());

        return userRepository.save(usuario);
    }

    /**
     * Cambiar estado de usuario
     */
    public User cambiarEstadoUsuario(Long id, EstadoUsuario nuevoEstado, User cambiadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setEstado(nuevoEstado);
        
        // Si se activa, marcar como activo
        if (EstadoUsuario.ACTIVO.equals(nuevoEstado)) {
            usuario.setActivo(true);
        } else if (EstadoUsuario.SUSPENDIDO.equals(nuevoEstado)) {
            usuario.setActivo(false);
        }

        return userRepository.save(usuario);
    }

    /**
     * Resetear contraseña de usuario
     */
    public void resetearContraseña(Long id, String nuevaContraseña, User reseteadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setPassword(passwordEncoder.encode(nuevaContraseña));
        userRepository.save(usuario);
    }

    /**
     * Buscar usuarios con filtros avanzados
     */
    public List<User> buscarUsuariosConFiltros(EstadoUsuario estado, String roleName, 
                                               User creadoPor, Boolean activo, String searchTerm) {
        return userRepository.findUsersWithAdvancedFilters(estado, roleName, creadoPor, activo, searchTerm);
    }

    /**
     * Obtener usuarios por estado
     */
    public List<User> obtenerUsuariosPorEstado(EstadoUsuario estado) {
        return userRepository.findByEstado(estado);
    }

    /**
     * Obtener usuarios por rol
     */
    public List<User> obtenerUsuariosPorRol(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    /**
     * Obtener usuarios creados por un usuario específico
     */
    public List<User> obtenerUsuariosCreadosPor(User creadoPor) {
        return userRepository.findByCreadoPorOrderByFechaCreacionDesc(creadoPor);
    }

    /**
     * Convertir User a UsuarioDTO
     */
    public UsuarioDTO convertirADTO(User user) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setEstado(user.getEstado());
        dto.setActivo(user.getActivo());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setRoles(user.getRoles());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Información del creador
        if (user.getCreadoPor() != null) {
            dto.setCreadoPorId(user.getCreadoPor().getId());
            dto.setCreadoPorNombre(user.getCreadoPor().getFirstName() + " " + user.getCreadoPor().getLastName());
        }

        return dto;
    }

    /**
     * Buscar usuarios que tienen a un usuario específico como parentUser
     */
    public List<User> findByParentUserId(Long parentUserId) {
        return userRepository.findByParentUserId(parentUserId);
    }

    /**
     * Obtener estadísticas de usuarios
     */
    public Map<String, Object> obtenerEstadisticasUsuarios() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Contar usuarios por estado
        long totalUsuarios = userRepository.count();
        long usuariosActivos = userRepository.countByEstado(EstadoUsuario.ACTIVO);
        long usuariosPendientes = userRepository.countByEstado(EstadoUsuario.PENDIENTE);
        long usuariosSuspendidos = userRepository.countByEstado(EstadoUsuario.SUSPENDIDO);
        long usuariosEliminados = userRepository.countByEstado(EstadoUsuario.ELIMINADO);

        estadisticas.put("totalUsuarios", totalUsuarios);
        estadisticas.put("usuariosActivos", usuariosActivos);
        estadisticas.put("usuariosPendientes", usuariosPendientes);
        estadisticas.put("usuariosSuspendidos", usuariosSuspendidos);
        estadisticas.put("usuariosEliminados", usuariosEliminados);

        // Calcular porcentajes
        if (totalUsuarios > 0) {
            estadisticas.put("porcentajeActivos", Math.round((double) usuariosActivos / totalUsuarios * 100));
            estadisticas.put("porcentajePendientes", Math.round((double) usuariosPendientes / totalUsuarios * 100));
            estadisticas.put("porcentajeSuspendidos", Math.round((double) usuariosSuspendidos / totalUsuarios * 100));
            estadisticas.put("porcentajeEliminados", Math.round((double) usuariosEliminados / totalUsuarios * 100));
        }

        return estadisticas;
    }
}
