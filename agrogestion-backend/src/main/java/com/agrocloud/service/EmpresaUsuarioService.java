package com.agrocloud.service;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.entity.UsuarioEmpresaRol;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.model.entity.Role;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.RoleRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import com.agrocloud.repository.UsuarioEmpresaRolRepository;
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
 * Servicio para gestionar la relación entre usuarios y empresas
 * Maneja los roles específicos por empresa
 */
@Service
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
    
    /**
     * Asignar un usuario a una empresa con un rol específico
     */
    public UsuarioEmpresa asignarUsuarioAEmpresa(Long usuarioId, Long empresaId, RolEmpresa rol, User creadoPor) {
        logger.info("Asignando usuario {} a empresa {} con rol {}", usuarioId, empresaId, rol);
        
        // Validar que el usuario existe
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        // Validar que la empresa existe
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        
        // Verificar si ya existe la relación
        Optional<UsuarioEmpresa> relacionExistente = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa);
        
        if (relacionExistente.isPresent()) {
            // Actualizar rol existente
            UsuarioEmpresa relacion = relacionExistente.get();
            relacion.setRol(rol);
            relacion.setEstado(EstadoUsuarioEmpresa.ACTIVO);
            logger.info("Rol actualizado para usuario {} en empresa {}", usuario.getEmail(), empresa.getNombre());
            return usuarioEmpresaRepository.save(relacion);
        } else {
            // Crear nueva relación
            UsuarioEmpresa nuevaRelacion = new UsuarioEmpresa();
            nuevaRelacion.setUsuario(usuario);
            nuevaRelacion.setEmpresa(empresa);
            nuevaRelacion.setRol(rol);
            nuevaRelacion.setEstado(EstadoUsuarioEmpresa.ACTIVO);
            nuevaRelacion.setFechaInicio(LocalDate.now());
            nuevaRelacion.setCreadoPor(creadoPor);
            
            logger.info("Nueva relación creada: usuario {} en empresa {} con rol {}", 
                       usuario.getEmail(), empresa.getNombre(), rol);
            return usuarioEmpresaRepository.save(nuevaRelacion);
        }
    }
    
    /**
     * Remover un usuario de una empresa
     */
    public void removerUsuarioDeEmpresa(Long usuarioId, Long empresaId) {
        logger.info("Removiendo usuario {} de empresa {}", usuarioId, empresaId);
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        
        UsuarioEmpresa relacion = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new RuntimeException("El usuario no está asignado a esta empresa"));
        
        usuarioEmpresaRepository.delete(relacion);
        logger.info("Usuario {} removido de empresa {}", usuario.getEmail(), empresa.getNombre());
    }
    
    /**
     * Obtener todas las empresas de un usuario
     */
    public List<UsuarioEmpresa> obtenerEmpresasDeUsuario(Long usuarioId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        return usuarioEmpresaRepository.findByUsuario(usuario);
    }
    
    /**
     * Obtener todas las empresas activas de un usuario
     */
    public List<UsuarioEmpresa> obtenerEmpresasActivasDeUsuario(Long usuarioId) {
        return usuarioEmpresaRepository.findEmpresasActivasByUsuarioId(usuarioId);
    }
    
    /**
     * Obtener el rol de un usuario en una empresa específica
     */
    public Optional<RolEmpresa> obtenerRolUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        Optional<UsuarioEmpresa> relacion = usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        
        return relacion.map(UsuarioEmpresa::getRol);
    }
    
    /**
     * Verificar si un usuario tiene un rol específico en una empresa
     */
    public boolean usuarioTieneRolEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa rol) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, rol);
    }
    
    /**
     * Verificar si un usuario está activo en una empresa
     */
    public boolean usuarioActivoEnEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioActivoEnEmpresa(usuarioId, empresaId);
    }
    
    /**
     * Obtener todos los usuarios de una empresa
     */
    public List<UsuarioEmpresa> obtenerUsuariosDeEmpresa(Long empresaId) {
        return usuarioEmpresaRepository.findByEmpresaId(empresaId);
    }
    
    /**
     * Obtener usuarios activos de una empresa
     */
    public List<UsuarioEmpresa> obtenerUsuariosActivosDeEmpresa(Long empresaId) {
        return usuarioEmpresaRepository.findUsuariosActivosByEmpresaId(empresaId);
    }
    
    /**
     * Obtener usuarios por rol en una empresa
     */
    public List<UsuarioEmpresa> obtenerUsuariosPorRolEnEmpresa(Long empresaId, RolEmpresa rol) {
        return usuarioEmpresaRepository.findByEmpresaIdAndRol(empresaId, rol);
    }
    
    /**
     * Cambiar el rol de un usuario en una empresa
     */
    public UsuarioEmpresa cambiarRolUsuarioEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa nuevoRol) {
        logger.info("Cambiando rol de usuario {} en empresa {} a {}", usuarioId, empresaId, nuevoRol);
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        
        UsuarioEmpresa relacion = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new RuntimeException("El usuario no está asignado a esta empresa"));
        
        RolEmpresa rolAnterior = relacion.getRol();
        relacion.setRol(nuevoRol);
        
        UsuarioEmpresa relacionActualizada = usuarioEmpresaRepository.save(relacion);
        
        logger.info("Rol cambiado de {} a {} para usuario {} en empresa {}", 
                   rolAnterior, nuevoRol, usuario.getEmail(), empresa.getNombre());
        
        return relacionActualizada;
    }
    
    /**
     * Activar/desactivar usuario en una empresa
     */
    public UsuarioEmpresa cambiarEstadoUsuarioEnEmpresa(Long usuarioId, Long empresaId, EstadoUsuarioEmpresa nuevoEstado) {
        logger.info("Cambiando estado de usuario {} en empresa {} a {}", usuarioId, empresaId, nuevoEstado);
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        
        UsuarioEmpresa relacion = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new RuntimeException("El usuario no está asignado a esta empresa"));
        
        relacion.setEstado(nuevoEstado);
        
        UsuarioEmpresa relacionActualizada = usuarioEmpresaRepository.save(relacion);
        
        logger.info("Estado cambiado a {} para usuario {} en empresa {}", 
                   nuevoEstado, usuario.getEmail(), empresa.getNombre());
        
        return relacionActualizada;
    }
    
    /**
     * Obtener todas las relaciones usuario-empresa del sistema
     */
    public List<UsuarioEmpresa> obtenerTodasLasRelaciones() {
        logger.info("Obteniendo todas las relaciones usuario-empresa");
        return usuarioEmpresaRepository.findAll();
    }
    
    // ========================================================================
    // MÉTODOS PARA GESTIÓN DE MÚLTIPLES ROLES (UsuarioEmpresaRol)
    // ========================================================================
    
    /**
     * Asignar múltiples roles a un usuario en una empresa
     */
    public List<UsuarioEmpresaRol> asignarRolesAUsuarioEnEmpresa(Long usuarioId, Long empresaId, Set<RolEmpresa> roles) {
        logger.info("Asignando {} roles al usuario {} en empresa {}", roles.size(), usuarioId, empresaId);
        logger.info("Roles a asignar: {}", roles.stream().map(RolEmpresa::name).collect(Collectors.joining(", ")));
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        
        // Eliminar roles existentes para esta combinación usuario-empresa
        logger.info("Eliminando roles existentes para usuario {} en empresa {}", usuarioId, empresaId);
        usuarioEmpresaRolRepository.deleteByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        
        // Verificar que se eliminaron correctamente
        List<UsuarioEmpresaRol> rolesAntes = usuarioEmpresaRolRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        logger.info("Roles existentes después de DELETE: {}", rolesAntes.size());
        
        // Crear nuevos roles
        List<UsuarioEmpresaRol> rolesCreados = new ArrayList<>();
        for (RolEmpresa rolEmpresa : roles) {
            // Buscar el Role correspondiente en la base de datos
            Role role = roleRepository.findByNombre(rolEmpresa.name())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado en la base de datos: " + rolEmpresa.name()));
            
            logger.info("Creando UsuarioEmpresaRol: usuario={}, empresa={}, rol={} (ID: {}), activo=true", 
                    usuario.getEmail(), empresa.getNombre(), role.getNombre(), role.getId());
            
            UsuarioEmpresaRol usuarioEmpresaRol = new UsuarioEmpresaRol(usuario, empresa, role, true);
            UsuarioEmpresaRol guardado = usuarioEmpresaRolRepository.save(usuarioEmpresaRol);
            rolesCreados.add(guardado);
            logger.info("Rol {} guardado. UsuarioEmpresaRol ID: {}, Activo: {}", 
                    rolEmpresa, guardado.getId(), guardado.getActivo());
        }
        
        logger.info("Total de roles creados: {}", rolesCreados.size());
        
        // Forzar flush para asegurar que los cambios se persistan
        usuarioEmpresaRolRepository.flush();
        
        // Verificar que se guardaron correctamente (usando findAll para ver todos, no solo activos)
        List<UsuarioEmpresaRol> todosLosRoles = usuarioEmpresaRolRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        logger.info("Verificación TOTAL: Se encontraron {} registros en la BD (todos los estados)", todosLosRoles.size());
        todosLosRoles.forEach(uer -> {
            logger.info("Registro en BD: ID={}, Rol nombre={}, Activo={}", 
                    uer.getId(), uer.getRol() != null ? uer.getRol().getNombre() : "null", uer.getActivo());
        });
        
        // Verificar solo los activos
        List<UsuarioEmpresaRol> rolesVerificados = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        logger.info("Verificación ACTIVOS: Se encontraron {} roles activos después de guardar", rolesVerificados.size());
        rolesVerificados.forEach(uer -> {
            logger.info("Rol activo verificado: ID={}, Rol nombre={}, Activo={}", 
                    uer.getId(), uer.getRol() != null ? uer.getRol().getNombre() : "null", uer.getActivo());
        });
        
        return rolesCreados;
    }
    
    /**
     * Obtener todos los roles de un usuario en una empresa
     */
    public List<RolEmpresa> obtenerRolesDeUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        logger.info("Obteniendo roles para usuario {} en empresa {}", usuarioId, empresaId);
        List<UsuarioEmpresaRol> roles = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        logger.info("Encontrados {} registros de UsuarioEmpresaRol para usuario {} en empresa {}", roles.size(), usuarioId, empresaId);
        
        List<RolEmpresa> rolesEmpresa = roles.stream()
                .map(uer -> {
                    RolEmpresa rolEmpresa = uer.getRolEmpresa();
                    if (rolEmpresa == null) {
                        logger.warn("UsuarioEmpresaRol ID {} tiene rol null o no se pudo convertir. Rol nombre: {}", 
                                uer.getId(), uer.getRol() != null ? uer.getRol().getNombre() : "null");
                    } else {
                        logger.info("Rol convertido: {} para UsuarioEmpresaRol ID {}", rolEmpresa.name(), uer.getId());
                    }
                    return rolEmpresa;
                })
                .filter(rol -> rol != null)
                .collect(Collectors.toList());
        
        logger.info("Roles finales para usuario {} en empresa {}: {}", usuarioId, empresaId, 
                rolesEmpresa.stream().map(RolEmpresa::name).collect(Collectors.joining(", ")));
        
        return rolesEmpresa;
    }
    
    /**
     * Verificar si un usuario tiene un rol específico en una empresa
     */
    public boolean usuarioTieneRolEnEmpresaMultiples(Long usuarioId, Long empresaId, RolEmpresa rolEmpresa) {
        // Buscar el Role correspondiente
        Role role = roleRepository.findByNombre(rolEmpresa.name())
                .orElse(null);
        if (role == null) {
            return false;
        }
        // Buscar en la tabla usuarios_empresas_roles usando rol_id
        List<UsuarioEmpresaRol> roles = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        return roles.stream()
                .anyMatch(uer -> uer.getRol() != null && uer.getRol().getId().equals(role.getId()));
    }
    
    /**
     * Agregar un rol adicional a un usuario en una empresa (sin eliminar los existentes)
     */
    public UsuarioEmpresaRol agregarRolAUsuarioEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa rolEmpresa) {
        logger.info("Agregando rol {} al usuario {} en empresa {}", rolEmpresa, usuarioId, empresaId);
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        
        // Buscar el Role correspondiente
        Role role = roleRepository.findByNombre(rolEmpresa.name())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en la base de datos: " + rolEmpresa.name()));
        
        // Verificar si el rol ya existe
        List<UsuarioEmpresaRol> rolesExistentes = usuarioEmpresaRolRepository
                .findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        
        Optional<UsuarioEmpresaRol> rolExistente = rolesExistentes.stream()
                .filter(uer -> uer.getRol() != null && uer.getRol().getId().equals(role.getId()))
                .findFirst();
        
        if (rolExistente.isPresent()) {
            UsuarioEmpresaRol rolActual = rolExistente.get();
            if (!rolActual.getActivo()) {
                // Reactivar rol existente
                rolActual.setActivo(true);
                return usuarioEmpresaRolRepository.save(rolActual);
            }
            return rolActual;
        }
        
        // Crear nuevo rol
        UsuarioEmpresaRol nuevoRol = new UsuarioEmpresaRol(usuario, empresa, role, true);
        return usuarioEmpresaRolRepository.save(nuevoRol);
    }
    
    /**
     * Remover un rol específico de un usuario en una empresa
     */
    public void removerRolDeUsuarioEnEmpresa(Long usuarioId, Long empresaId, RolEmpresa rolEmpresa) {
        logger.info("Removiendo rol {} del usuario {} en empresa {}", rolEmpresa, usuarioId, empresaId);
        
        // Buscar el Role correspondiente
        Role role = roleRepository.findByNombre(rolEmpresa.name())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en la base de datos: " + rolEmpresa.name()));
        
        // Buscar y eliminar el UsuarioEmpresaRol correspondiente
        List<UsuarioEmpresaRol> roles = usuarioEmpresaRolRepository.findRolesActivosByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        roles.stream()
                .filter(uer -> uer.getRol() != null && uer.getRol().getId().equals(role.getId()))
                .findFirst()
                .ifPresent(uer -> usuarioEmpresaRolRepository.delete(uer));
    }
}
