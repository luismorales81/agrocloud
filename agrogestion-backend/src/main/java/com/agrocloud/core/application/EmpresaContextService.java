package com.agrocloud.core.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio Core para el contexto de empresa (multiempresa).
 */
@Service("empresaContextServiceCore")
@Transactional
public class EmpresaContextService {

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> obtenerEmpresasActivasDelUsuario(Long usuarioId) {
        return usuarioEmpresaRepository.findEmpresasActivasByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPrincipalDelUsuario(Long usuarioId) {
        List<UsuarioEmpresa> empresasActivas = usuarioEmpresaRepository.findEmpresasActivasByUsuarioId(usuarioId);
        if (empresasActivas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(empresasActivas.get(0).getEmpresa());
    }

    @Transactional(readOnly = true)
    public boolean usuarioPerteneceAEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioActivoEnEmpresa(usuarioId, empresaId);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioEmpresa> obtenerRolUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
    }

    @Transactional(readOnly = true)
    public boolean usuarioEsAdministradorEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR);
    }

    @Transactional(readOnly = true)
    public boolean usuarioEsAsesorEmpresa(Long usuarioId, Long empresaId) {
        return tieneRolActualOLegacy(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.JEFE_CAMPO, "ASESOR");
    }

    @Transactional(readOnly = true)
    public boolean usuarioEsOperarioEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.OPERARIO);
    }

    @Transactional(readOnly = true)
    public boolean usuarioEsContadorEmpresa(Long usuarioId, Long empresaId) {
        return tieneRolActualOLegacy(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.JEFE_FINANCIERO, "CONTADOR");
    }

    @Transactional(readOnly = true)
    public boolean usuarioEsTecnicoEmpresa(Long usuarioId, Long empresaId) {
        return tieneRolActualOLegacy(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.JEFE_CAMPO, "TECNICO");
    }

    @Transactional(readOnly = true)
    public boolean usuarioEsSoloLecturaEmpresa(Long usuarioId, Long empresaId) {
        return tieneRolActualOLegacy(usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.CONSULTOR_EXTERNO, "LECTURA");
    }

    private boolean tieneRolActualOLegacy(Long usuarioId, Long empresaId,
                                            com.agrocloud.model.enums.RolEmpresa rolActual, String nombreLegacy) {
        if (usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(usuarioId, empresaId, rolActual)) {
            return true;
        }
        return usuarioEmpresaRepository.existsUsuarioConRolEnEmpresa(
                usuarioId, empresaId, com.agrocloud.model.enums.RolEmpresa.valueOf(nombreLegacy));
    }

    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> obtenerTodasLasEmpresasDelUsuario(Long usuarioId) {
        return usuarioEmpresaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public boolean usuarioPuedeAccederAEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.existsUsuarioActivoEnEmpresa(usuarioId, empresaId);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioEmpresa> obtenerInformacionCompletaUsuarioEmpresa(Long usuarioId, Long empresaId) {
        return usuarioEmpresaRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
    }
}
