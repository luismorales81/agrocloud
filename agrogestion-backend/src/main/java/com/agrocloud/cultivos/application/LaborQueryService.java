package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Role;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.model.enums.Rol;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.core.application.UserService;
import com.agrocloud.config.CampanaRequestContext;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.cultivos.domain.CicloCultivo;
import com.agrocloud.cultivos.infrastructure.CicloCultivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de solo lectura y verificación de acceso para labores.
 * Reduce la complejidad de LaborService delegando consultas y permisos.
 */
@Service("laborQueryServiceCultivos")
public class LaborQueryService {

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("campanaContextServiceCore")
    private CampanaContextService campanaContextService;

    @Autowired
    @Qualifier("cicloCultivoRepositoryCultivos")
    private CicloCultivoRepository cicloCultivoRepository;

    /**
     * Obtiene los IDs de lotes accesibles por el usuario.
     */
    public List<Long> getLoteIdsByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        List<Plot> lotesUsuario;
        if (user.isSuperAdmin()) {
            lotesUsuario = plotRepository.findAll().stream()
                    .filter(plot -> {
                        Boolean activo = plot.getActivo();
                        return activo != null && activo;
                    })
                    .toList();
        } else if (user.esAdministradorEmpresa(user.getEmpresa() != null ? user.getEmpresa().getId() : null) ||
                   user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
                   user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
                   user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)) {
            Empresa empresa = user.getEmpresa();
            if (empresa == null) {
                return new ArrayList<>();
            }
            List<User> todosUsuarios = userService.findAll();
            List<User> usuariosEmpresa = todosUsuarios.stream()
                    .filter(u -> u.perteneceAEmpresa(empresa.getId()))
                    .collect(Collectors.toList());
            lotesUsuario = new ArrayList<>();
            for (User userEmpresa : usuariosEmpresa) {
                List<Plot> lotesUsuarioEmpresa = plotRepository.findByUserIdAndActivoTrue(userEmpresa.getId());
                if (lotesUsuarioEmpresa != null) {
                    lotesUsuario.addAll(lotesUsuarioEmpresa);
                }
            }
        } else {
            lotesUsuario = plotRepository.findByUserIdAndActivoTrue(user.getId());
            if (lotesUsuario == null) {
                lotesUsuario = new ArrayList<>();
            }
            List<User> usuariosDependientes = userService.findByParentUserId(user.getId());
            if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
                for (User dependiente : usuariosDependientes) {
                    List<Plot> lotesDependiente = plotRepository.findByUserIdAndActivoTrue(dependiente.getId());
                    if (lotesDependiente != null) {
                        lotesUsuario.addAll(lotesDependiente);
                    }
                }
            }
        }
        List<Long> loteIds = lotesUsuario.stream()
                .map(Plot::getId)
                .toList();
        return loteIds.isEmpty() ? new ArrayList<>() : loteIds;
    }

    /**
     * Verifica si el usuario tiene acceso al lote.
     */
    public boolean tieneAccesoAlLote(Plot lote, User usuario) {
        if (usuario == null || lote == null) {
            return false;
        }
        if (lote.getUser() != null && lote.getUser().getId().equals(usuario.getId())) {
            return true;
        }
        if (lote.getUser() != null && lote.getUser().getParentUser() != null &&
                lote.getUser().getParentUser().getId().equals(usuario.getId())) {
            return true;
        }
        if (perteneceAMismaEmpresa(usuario, lote)) {
            Rol rolUsuario = obtenerRolUsuario(usuario);
            return tienePermisoParaLabores(rolUsuario);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Labor> getLaboresByUser(User user) {
        try {
            List<Long> loteIds = getLoteIdsByUser(user);
            if (loteIds.isEmpty()) {
                return new ArrayList<>();
            }
            Long campanaHeader = CampanaRequestContext.getCampanaId();
            if (campanaHeader != null) {
                List<Long> cicloIds = cicloCultivoRepository.findByCampanaIdOrderByFechaSiembraDesc(campanaHeader)
                        .stream().map(CicloCultivo::getId).toList();
                if (!cicloIds.isEmpty()) {
                    return laborRepository.findByLoteIdInAndCicloCultivoIdIn(loteIds, cicloIds);
                }
            }
            return laborRepository.findByLoteIdInWithFetch(loteIds);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene labor por ID verificando permisos.
     */
    public Optional<Labor> getLaborById(Long id, User user) {
        Optional<Labor> labor = laborRepository.findById(id);
        if (labor.isPresent()) {
            Labor laborEntity = labor.get();
            Plot lote = laborEntity.getLote();
            if (lote != null && tieneAccesoAlLote(lote, user)) {
                return labor;
            }
        }
        return Optional.empty();
    }

    private boolean perteneceAMismaEmpresa(User usuario, Plot lote) {
        Empresa empresaUsuario = usuario.getEmpresa();
        if (empresaUsuario != null) {
            Long empresaUsuarioId = empresaUsuario.getId();
            Long empresaLote = obtenerEmpresaDelLote(lote);
            return empresaUsuarioId.equals(empresaLote);
        }
        return true;
    }

    private Long obtenerEmpresaDelLote(Plot lote) {
        if (lote.getUser() != null) {
            Empresa empresa = lote.getUser().getEmpresa();
            if (empresa != null) {
                return empresa.getId();
            }
        }
        return 1L;
    }

    private Rol obtenerRolUsuario(User usuario) {
        if (usuario == null) {
            return Rol.PRODUCTOR;
        }
        try {
            Set<Role> roles = usuario.getRoles();
            if (roles != null && !roles.isEmpty()) {
                Role role = roles.iterator().next();
                if (role != null && role.getNombre() != null) {
                    try {
                        return Rol.valueOf(role.getNombre());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            Role role = usuario.getRoles().iterator().next();
            if (role != null && role.getNombre() != null) {
                try {
                    return Rol.valueOf(role.getNombre());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return Rol.PRODUCTOR;
    }

    private boolean tienePermisoParaLabores(Rol rol) {
        switch (rol) {
            case SUPERADMIN:
            case ADMINISTRADOR:
            case PRODUCTOR:
            case TECNICO:
            case ASESOR:
            case OPERARIO:
                return true;
            case INVITADO:
                return false;
            default:
                return false;
        }
    }
}
