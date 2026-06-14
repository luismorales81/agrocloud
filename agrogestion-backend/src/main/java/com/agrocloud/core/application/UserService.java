package com.agrocloud.core.application;

import com.agrocloud.dto.UsuarioDTO;
import com.agrocloud.core.domain.EstadoUsuario;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Servicio Core para gestión de usuarios.
 */
@Service("userServiceCore")
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public User findByEmailWithAllRelationsCombined(String email) {
        User user = userRepository.findByEmailWithAllRelations(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        User userWithRoles = userRepository.findByEmailWithUserCompanyRoles(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        user.setUserCompanyRoles(userWithRoles.getUserCompanyRoles());
        return user;
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

    public User crearUsuario(User usuario, User creadoPor) {
        if (userRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + usuario.getEmail());
        }
        if (userRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe: " + usuario.getUsername());
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setCreadoPor(creadoPor);
        usuario.setEstado(EstadoUsuario.PENDIENTE);
        usuario.setActivo(true);
        usuario.setEmailVerified(false);
        return userRepository.save(usuario);
    }

    public User actualizarUsuario(Long id, User usuarioActualizado, User actualizadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setFirstName(usuarioActualizado.getFirstName());
        usuario.setLastName(usuarioActualizado.getLastName());
        usuario.setEmail(usuarioActualizado.getEmail());
        usuario.setPhone(usuarioActualizado.getPhone());
        usuario.setRoles(usuarioActualizado.getRoles());
        return userRepository.save(usuario);
    }

    public User cambiarEstadoUsuario(Long id, EstadoUsuario nuevoEstado, User cambiadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setEstado(nuevoEstado);
        if (EstadoUsuario.ACTIVO.equals(nuevoEstado)) {
            usuario.setActivo(true);
        } else if (EstadoUsuario.SUSPENDIDO.equals(nuevoEstado)) {
            usuario.setActivo(false);
        }
        return userRepository.save(usuario);
    }

    public void resetearContraseña(Long id, String nuevaContraseña, User reseteadoPor) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setPassword(passwordEncoder.encode(nuevaContraseña));
        userRepository.save(usuario);
    }

    public List<User> buscarUsuariosConFiltros(EstadoUsuario estado, String roleName,
                                               User creadoPor, Boolean activo, String searchTerm) {
        return userRepository.findUsersWithAdvancedFilters(estado, roleName, creadoPor, activo, searchTerm);
    }

    public List<User> obtenerUsuariosPorEstado(EstadoUsuario estado) {
        return userRepository.findByEstado(estado);
    }

    public List<User> obtenerUsuariosPorRol(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    public List<User> obtenerUsuariosCreadosPor(User creadoPor) {
        return userRepository.findByCreadoPorOrderByFechaCreacionDesc(creadoPor);
    }

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
        if (user.getCreadoPor() != null) {
            dto.setCreadoPorId(user.getCreadoPor().getId());
            dto.setCreadoPorNombre(user.getCreadoPor().getFirstName() + " " + user.getCreadoPor().getLastName());
        }
        return dto;
    }

    public List<User> findByParentUserId(Long parentUserId) {
        return userRepository.findByParentUserId(parentUserId);
    }

    public Map<String, Object> obtenerEstadisticasUsuarios() {
        Map<String, Object> estadisticas = new HashMap<>();
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

        if (totalUsuarios > 0) {
            estadisticas.put("porcentajeActivos", Math.round((double) usuariosActivos / totalUsuarios * 100));
            estadisticas.put("porcentajePendientes", Math.round((double) usuariosPendientes / totalUsuarios * 100));
            estadisticas.put("porcentajeSuspendidos", Math.round((double) usuariosSuspendidos / totalUsuarios * 100));
            estadisticas.put("porcentajeEliminados", Math.round((double) usuariosEliminados / totalUsuarios * 100));
        }
        return estadisticas;
    }
}
