package com.agrocloud.service;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar el contexto de empresa en el sistema multiempresa.
 * Este servicio ayuda a determinar qué empresa está activa para un usuario.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Service
@Transactional
public class EmpresaContextService {

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    /**
     * Obtiene las empresas activas de un usuario
     */
    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> obtenerEmpresasActivasDelUsuario(Long usuarioId) {
        return usuarioEmpresaRepository.findEmpresasActivasByUsuarioId(usuarioId);
    }

    /**
     * Obtiene la empresa principal de un usuario (la primera empresa activa)
     */
    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPrincipalDelUsuario(Long usuarioId) {
        List<UsuarioEmpresa> empresasActivas = usuarioEmpresaRepository.findEmpresasActivasByUsuarioId(usuarioId);
        if (empresasActivas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(empresasActivas.get(0).getEmpresa());
    }

    /**
     * Verifica si un usuario pertenece a una empresa específica
     */
    @Transactional(readOnly = true)
    public boolean usuarioPerteneceAEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioActivoEnEmpresa(usuarioId, empresaId);
    }

    /**
     * Obtiene el rol de un usuario en una empresa específica
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioEmpresa> obtenerRolUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
    }

    /**
     * Verifica si un usuario tiene permisos de administración en una empresa
     */
    @Transactional(readOnly = true)
    public boolean usuarioEsAdministradorEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR);
    }

    /**
     * Verifica si un usuario tiene permisos de asesoría en una empresa
     */
    @Transactional(readOnly = true)
    public boolean usuarioEsAsesorEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.ASESOR);
    }

    /**
     * Verifica si un usuario tiene permisos de operación en una empresa
     */
    @Transactional(readOnly = true)
    public boolean usuarioEsOperarioEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.OPERARIO);
    }

    /**
     * Verifica si un usuario tiene permisos de contabilidad en una empresa
     */
    @Transactional(readOnly = true)
    public boolean usuarioEsContadorEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.CONTADOR);
    }

    /**
     * Verifica si un usuario tiene permisos técnicos en una empresa
     */
    @Transactional(readOnly = true)
    public boolean usuarioEsTecnicoEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.TECNICO);
    }

    /**
     * Verifica si un usuario tiene solo permisos de lectura en una empresa
     */
    @Transactional(readOnly = true)
    public boolean usuarioEsSoloLecturaEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.LECTURA);
    }

    /**
     * Obtiene todas las empresas de un usuario con sus roles
     */
    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> obtenerTodasLasEmpresasDelUsuario(Long usuarioId) {
        return usuarioEmpresaRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Verifica si un usuario puede acceder a los datos de una empresa
     */
    @Transactional(readOnly = true)
    public boolean usuarioPuedeAccederAEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioActivoEnEmpresa(usuarioId, empresaId);
    }

    /**
     * Obtiene la información completa de la relación usuario-empresa
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioEmpresa> obtenerInformacionCompletaUsuarioEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
    }
}
