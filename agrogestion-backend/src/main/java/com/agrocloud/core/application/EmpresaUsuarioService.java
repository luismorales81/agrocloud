package com.agrocloud.core.application;

import com.agrocloud.core.domain.Role;
import com.agrocloud.core.infrastructure.RoleRepository;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.core.domain.UsuarioEmpresaRol;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRepository;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio Core para la relación usuarios-empresas y roles por empresa.
 */
@Service("empresaUsuarioServiceCore")
@Transactional
public class EmpresaUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaUsuarioService.class);

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioEmpresaRolRepository usuarioEmpresaRolRepository;

    @Autowired
    private RoleRepository roleRepository;

    public UsuarioEmpresa asignarUsuarioAEmpresa(Long usuarioId, Long empresaId, RolEmpresa rol, User creadoPor) {
        logger.info("Asignando usuario {} a empresa {} con rol {}", usuarioId, empresaId, rol);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        Optional<UsuarioEmpresa> relacionExistente = usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario, empresa);
        if (relacionExistente.isPresent()) {
            UsuarioEmpresa relacion = relacionExistente.get();
            relacion.setRol(rol);
            relacion.setEstado(EstadoUsuarioEmpresa.ACTIVO);
            logger.info("Rol actualizado para usuario {} en empresa {}", usuario.getEmail(), empresa.getNombre());
            return usuarioEmpresaRepository.save(relacion);
        } else {
            UsuarioEmpresa nuevaRelacion = new UsuarioEmpresa();
            nuevaRelacion.setUsuario(usuario);
            nuevaRelacion.setEmpresa(empresa);
            nuevaRelacion.setRol(rol);
            nuevaRelacion.setEstado(EstadoUsuarioEmpresa.ACTIVO);
            nuevaRelacion.setFechaInicio(LocalDate.now());
            nuevaRelacion.setCreadoPor(creadoPor);
            logger.info("Nueva relación creada: usuario {} en empresa {} con rol {}", usuario.getEmail(), empresa.getNombre(), rol);
            return usuarioEmpresaRepository.save(nuevaRelacion);
        }
    }

    public void removerUsuarioDeEmpresa(Long usuarioId, Long empresaId) {
        logger.info("Removiendo usuario {} de empresa {}", usuarioId, empresaId);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        UsuarioEmpresa relacion = usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new RuntimeException("El usuario no está asignado a esta empresa"));
        usuarioEmpresaRepository.delete(relacion);
        logger.info("Usuario {} removido de empresa {}", usuario.getEmail(), empresa.getNombre());
    }

    public List<UsuarioEmpresa> obtenerEmpresasDeUsuario(Long usuarioId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        return usuarioEmpresaRepository.findByUsuario(usuario);
    }

    public List<UsuarioEmpresa> obtenerEmpresasActivasDeUsuario(Long usuarioId) {
        return usuarioEmpresaRepository.findEmpresasActivasByUsuarioId(usuarioId);
    }

    public Optional<RolEmpresa> obtenerRolUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId).map(UsuarioEmpresa::getRol);
    }

    public boolean usuarioTieneRolEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa rol) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, rol);
    }

    public boolean usuarioActivoEnEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioActivoEnEmpresa(usuarioId, empresaId);
    }

    public List<UsuarioEmpresa> obtenerUsuariosDeEmpresa(Long empresaId) {
        return usuarioEmpresaRepository.findByEmpresaId(empresaId);
    }

    public List<UsuarioEmpresa> obtenerUsuariosActivosDeEmpresa(Long empresaId) {
        return usuarioEmpresaRepository.findUsuariosActivosByEmpresaId(empresaId);
    }

    public List<UsuarioEmpresa> obtenerUsuariosPorRolEnEmpresa(Long empresaId, RolEmpresa rol) {
        return usuarioEmpresaRepository.findByEmpresaIdAndRol(empresaId, rol);
    }

    public UsuarioEmpresa cambiarRolUsuarioEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa nuevoRol) {
        logger.info("Cambiando rol de usuario {} en empresa {} a {}", usuarioId, empresaId, nuevoRol);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        UsuarioEmpresa relacion = usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new RuntimeException("El usuario no está asignado a esta empresa"));
        RolEmpresa rolAnterior = relacion.getRol();
        relacion.setRol(nuevoRol);
        UsuarioEmpresa relacionActualizada = usuarioEmpresaRepository.save(relacion);
        logger.info("Rol cambiado de {} a {} para usuario {} en empresa {}", rolAnterior, nuevoRol, usuario.getEmail(), empresa.getNombre());
        return relacionActualizada;
    }

    public UsuarioEmpresa cambiarEstadoUsuarioEnEmpresa(Long usuarioId, Long empresaId, EstadoUsuarioEmpresa nuevoEstado) {
        logger.info("Cambiando estado de usuario {} en empresa {} a {}", usuarioId, empresaId, nuevoEstado);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        UsuarioEmpresa relacion = usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new RuntimeException("El usuario no está asignado a esta empresa"));
        relacion.setEstado(nuevoEstado);
        return usuarioEmpresaRepository.save(relacion);
    }

    public List<UsuarioEmpresa> obtenerTodasLasRelaciones() {
        logger.info("Obteniendo todas las relaciones usuario-empresa");
        return usuarioEmpresaRepository.findAll();
    }

    public List<UsuarioEmpresaRol> asignarRolesAUsuarioEnEmpresa(Long usuarioId, Long empresaId, Set<RolEmpresa> roles) {
        logger.info("Asignando {} roles al usuario {} en empresa {}", roles.size(), usuarioId, empresaId);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        usuarioEmpresaRolRepository.deleteByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        List<UsuarioEmpresaRol> rolesCreados = new ArrayList<>();
        for (RolEmpresa rolEmpresa : roles) {
            Role role = roleRepository.findByNombre(rolEmpresa.name())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado en la base de datos: " + rolEmpresa.name()));
            UsuarioEmpresaRol usuarioEmpresaRol = new UsuarioEmpresaRol(usuario, empresa, role, true);
            rolesCreados.add(usuarioEmpresaRolRepository.save(usuarioEmpresaRol));
        }
        usuarioEmpresaRolRepository.flush();
        return rolesCreados;
    }

    public List<RolEmpresa> obtenerRolesDeUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        List<UsuarioEmpresaRol> roles = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        return roles.stream()
                .map(UsuarioEmpresaRol::getRolEmpresa)
                .filter(rol -> rol != null)
                .collect(Collectors.toList());
    }

    public boolean usuarioTieneRolEnEmpresaMultiples(Long usuarioId, Long empresaId, RolEmpresa rolEmpresa) {
        Role role = roleRepository.findByNombre(rolEmpresa.name()).orElse(null);
        if (role == null) return false;
        List<UsuarioEmpresaRol> roles = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        return roles.stream().anyMatch(uer -> uer.getRol() != null && uer.getRol().getId().equals(role.getId()));
    }

    public UsuarioEmpresaRol agregarRolAUsuarioEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa rolEmpresa) {
        logger.info("Agregando rol {} al usuario {} en empresa {}", rolEmpresa, usuarioId, empresaId);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        Role role = roleRepository.findByNombre(rolEmpresa.name())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en la base de datos: " + rolEmpresa.name()));
        List<UsuarioEmpresaRol> rolesExistentes = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        Optional<UsuarioEmpresaRol> rolExistente = rolesExistentes.stream()
                .filter(uer -> uer.getRol() != null && uer.getRol().getId().equals(role.getId()))
                .findFirst();
        if (rolExistente.isPresent()) {
            UsuarioEmpresaRol rolActual = rolExistente.get();
            if (!rolActual.getActivo()) {
                rolActual.setActivo(true);
                return usuarioEmpresaRolRepository.save(rolActual);
            }
            return rolActual;
        }
        UsuarioEmpresaRol nuevoRol = new UsuarioEmpresaRol(usuario, empresa, role, true);
        return usuarioEmpresaRolRepository.save(nuevoRol);
    }

    public void removerRolDeUsuarioEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa rolEmpresa) {
        logger.info("Removiendo rol {} del usuario {} en empresa {}", rolEmpresa, usuarioId, empresaId);
        Role role = roleRepository.findByNombre(rolEmpresa.name())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en la base de datos: " + rolEmpresa.name()));
        List<UsuarioEmpresaRol> roles = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        roles.stream()
                .filter(uer -> uer.getRol() != null && uer.getRol().getId().equals(role.getId()))
                .findFirst()
                .ifPresent(usuarioEmpresaRolRepository::delete);
    }
}
