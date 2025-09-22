package com.agrocloud.service;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
}
