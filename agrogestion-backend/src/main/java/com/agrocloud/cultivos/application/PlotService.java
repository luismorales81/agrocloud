package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.CicloCultivoRepository;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.config.CampanaRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("plotServicioCultivos")
@Transactional
public class PlotService {

    @Autowired
    private PlotRepository plotRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private ConfiguracionEstadosService configuracionEstadosService;
    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private EstadoLoteUpdater estadoLoteUpdater;
    @Autowired
    private EmpresaContextService empresaContextService;
    @Autowired
    private LaborQueryService laborQueryService;

    @Autowired
    @Qualifier("cicloCultivoRepositoryCultivos")
    private CicloCultivoRepository cicloCultivoRepository;

    public List<Plot> getLotesByUser(User user) {
        try {
            if (user == null) return new ArrayList<>();
            if (user.isSuperAdmin()) {
                List<Plot> lotes = plotRepository.findAll().stream()
                        .filter(plot -> plot.getActivo() != null && plot.getActivo())
                        .toList();
                validarYCorregirLotes(lotes);
                lotes.forEach(plot -> {
                    if (plot.getCampo() != null) plot.getCampo().getNombre();
                    if (plot.getEstadoConfigurado() != null) plot.getEstadoConfigurado().getNombre();
                });
                return lotes;
            } else if (user.esAdministradorEmpresa(user.getEmpresa() != null ? user.getEmpresa().getId() : null) ||
                    user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
                    user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
                    user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)) {
                Empresa empresa = user.getEmpresa() != null
                        ? user.getEmpresa()
                        : empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId()).orElse(null);
                if (empresa != null) {
                    List<Plot> lotesPorEmpresa = plotRepository.findByCampo_Empresa_IdAndActivoTrue(empresa.getId());
                    if (lotesPorEmpresa != null && !lotesPorEmpresa.isEmpty()) {
                        validarYCorregirLotes(lotesPorEmpresa);
                        lotesPorEmpresa.forEach(plot -> {
                            if (plot.getCampo() != null) plot.getCampo().getNombre();
                            if (plot.getEstadoConfigurado() != null) plot.getEstadoConfigurado().getNombre();
                        });
                        return lotesPorEmpresa;
                    }
                }
                List<Plot> userPlots = plotRepository.findByUserIdAndActivoTrue(user.getId());
                if (userPlots == null) userPlots = new ArrayList<>();
                List<User> usuariosEmpresa = (empresa != null) ? userService.findAll().stream()
                        .filter(u -> u.perteneceAEmpresa(empresa.getId()))
                        .collect(Collectors.toList()) : List.of(user);
                for (User userEmpresa : usuariosEmpresa) {
                    if (userEmpresa.getId().equals(user.getId())) continue;
                    List<Plot> lotesUsuario = plotRepository.findByUserIdAndActivoTrue(userEmpresa.getId());
                    if (lotesUsuario != null) userPlots.addAll(lotesUsuario);
                }
                validarYCorregirLotes(userPlots);
                userPlots.forEach(plot -> {
                    if (plot.getCampo() != null) plot.getCampo().getNombre();
                    if (plot.getEstadoConfigurado() != null) plot.getEstadoConfigurado().getNombre();
                });
                return userPlots;
            } else {
                List<Plot> userPlots = plotRepository.findByUserIdAndActivoTrue(user.getId());
                if (userPlots == null) userPlots = new ArrayList<>();
                List<User> usuariosDependientes = userService.findByParentUserId(user.getId());
                if (usuariosDependientes != null) {
                    for (User dependiente : usuariosDependientes) {
                        List<Plot> lotesDependiente = plotRepository.findByUserIdAndActivoTrue(dependiente.getId());
                        if (lotesDependiente != null) userPlots.addAll(lotesDependiente);
                    }
                }
                validarYCorregirLotes(userPlots);
                userPlots.forEach(plot -> {
                    if (plot.getCampo() != null) plot.getCampo().getNombre();
                    if (plot.getEstadoConfigurado() != null) plot.getEstadoConfigurado().getNombre();
                });
                return userPlots;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener lotes del usuario: " + e.getMessage(), e);
        }
    }

    public List<Plot> getLotesPorcinosByUser(User user) {
        return getLotesByUser(user).stream()
                .filter(p -> p.getTipoUso() != null && p.getTipoUso() == Plot.TipoUsoLote.PORCINO)
                .collect(Collectors.toList());
    }

    public List<Plot> getLotesPorcinosByEmpresaId(Long empresaId) {
        if (empresaId == null) return new ArrayList<>();
        return plotRepository.findByCampo_Empresa_IdAndTipoUsoAndActivoTrue(empresaId, Plot.TipoUsoLote.PORCINO);
    }

    public List<Plot> getLotesCultivoByUser(User user) {
        List<Plot> lotes = getLotesByUser(user).stream()
                .filter(p -> p.getTipoUso() == null || p.getTipoUso() == Plot.TipoUsoLote.CULTIVO)
                .collect(Collectors.toList());
        return filtrarLotesPorCampanaActiva(lotes);
    }

    private List<Plot> filtrarLotesPorCampanaActiva(List<Plot> lotes) {
        Long campanaId = CampanaRequestContext.getCampanaId();
        if (campanaId == null || lotes.isEmpty()) {
            return lotes;
        }
        List<Long> loteIdsCampana = cicloCultivoRepository.findDistinctLoteIdsByCampanaId(campanaId);
        if (loteIdsCampana.isEmpty()) {
            return lotes;
        }
        return lotes.stream()
                .filter(p -> loteIdsCampana.contains(p.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Lotes de cultivo con fecha de cosecha esperada dentro del rango (consulta en BD).
     */
    @Transactional(readOnly = true)
    public List<Plot> getLotesCultivoConCosechaEnRango(User user, LocalDate desde, LocalDate hasta) {
        List<Long> loteIds = laborQueryService.getLoteIdsByUser(user);
        if (loteIds.isEmpty()) {
            return List.of();
        }
        return plotRepository.findByIdInAndFechaCosechaEsperadaBetween(loteIds, desde, hasta).stream()
                .filter(p -> p.getTipoUso() == null || p.getTipoUso() == Plot.TipoUsoLote.CULTIVO)
                .collect(Collectors.toList());
    }

    public List<Plot> getAllLotesIncludingInactive(User user) {
        if (!user.isSuperAdmin()) return getLotesByUser(user);
        return plotRepository.findAll();
    }

    public Optional<Plot> getLoteById(Long id, User user) {
        Optional<Plot> lote = plotRepository.findById(id);
        if (lote.isPresent()) {
            Plot p = lote.get();
            if (user.isSuperAdmin() || p.getUser().getId().equals(user.getId())) {
                validarYCorregirLotes(List.of(p));
                return lote;
            }
        }
        return Optional.empty();
    }

    public Plot saveLote(Plot lote) {
        if (lote.getCampo() != null)
            validarSuperficieDisponible(lote.getCampo().getId(), lote.getAreaHectareas(), lote.getId());
        return plotRepository.save(lote);
    }

    public Optional<Plot> updateLote(Long id, Plot loteData, User user) {
        Optional<Plot> existingLote = getLoteById(id, user);
        if (existingLote.isPresent()) {
            Plot lote = existingLote.get();
            if (loteData.getCampo() != null)
                validarSuperficieDisponible(loteData.getCampo().getId(), loteData.getAreaHectareas(), id);
            lote.setNombre(loteData.getNombre());
            lote.setDescripcion(loteData.getDescripcion());
            lote.setAreaHectareas(loteData.getAreaHectareas());
            // Estado y estadoConfigurado no se modifican por PUT; solo por flujos de negocio (siembra, cosecha, reset, liberar)
            lote.setTipoSuelo(loteData.getTipoSuelo());
            lote.setActivo(loteData.getActivo());
            lote.setCampo(loteData.getCampo());
            return Optional.of(plotRepository.save(lote));
        }
        return Optional.empty();
    }

    public boolean deleteLote(Long id, User user) {
        Optional<Plot> existingLote = getLoteById(id, user);
        if (existingLote.isPresent()) {
            Plot lote = existingLote.get();
            lote.setActivo(false);
            plotRepository.save(lote);
            return true;
        }
        return false;
    }

    public boolean deleteLoteFisicamente(Long id, User user) {
        if (!user.isSuperAdmin()) return false;
        if (plotRepository.existsById(id)) {
            plotRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Plot> searchLotesByNombre(String nombre, User user) {
        if (user.isSuperAdmin())
            return plotRepository.findAll().stream()
                    .filter(lote -> lote.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        return plotRepository.findByUserIdAndNombreContaining(user.getId(), nombre);
    }

    public long countLotesByUser(User user) {
        return user.isSuperAdmin() ? plotRepository.count() : plotRepository.countByUserId(user.getId());
    }

    public long countLotesActivosByUser(User user) {
        if (user.isSuperAdmin()) return plotRepository.findByActivoTrue().size();
        return plotRepository.countByUserIdAndActivoTrue(user.getId());
    }

    public List<Plot> getLotesByCampo(Long campoId, User user) {
        if (user.isSuperAdmin())
            return plotRepository.findAll().stream()
                    .filter(lote -> lote.getCampo() != null && lote.getCampo().getId().equals(campoId))
                    .toList();
        return plotRepository.findByCampoIdAndUserId(campoId, user.getId());
    }

    public BigDecimal calcularSuperficieDisponible(Long campoId) {
        Field campo = fieldRepository.findById(campoId)
                .orElseThrow(() -> new RuntimeException("Campo no encontrado con ID: " + campoId));
        return campo.getAreaHectareas().subtract(plotRepository.calcularSuperficieOcupadaPorCampo(campoId));
    }

    private void validarSuperficieDisponible(Long campoId, BigDecimal superficieLote, Long loteIdActual) {
        Field campo = fieldRepository.findById(campoId)
                .orElseThrow(() -> new RuntimeException("Campo no encontrado con ID: " + campoId));
        BigDecimal superficieTotal = campo.getAreaHectareas();
        BigDecimal superficieOcupada = plotRepository.calcularSuperficieOcupadaPorCampo(campoId);
        if (loteIdActual != null) {
            Optional<Plot> loteActual = plotRepository.findById(loteIdActual);
            if (loteActual.isPresent() && loteActual.get().getActivo())
                superficieOcupada = superficieOcupada.subtract(loteActual.get().getAreaHectareas());
        }
        BigDecimal superficieDisponible = superficieTotal.subtract(superficieOcupada);
        if (superficieLote.compareTo(superficieDisponible) > 0)
            throw new RuntimeException(String.format("La superficie del lote (%.2f ha) excede la disponible del campo (%.2f ha).",
                    superficieLote, superficieDisponible));
    }

    public Plot resetearLote(Long loteId, Long estadoInicialId, String motivo, User usuario) {
        Plot lote = plotRepository.findById(loteId).orElseThrow(() -> new RuntimeException("Lote no encontrado"));
        if (!usuario.isSuperAdmin() && !lote.getUser().getId().equals(usuario.getId()))
            throw new RuntimeException("No tienes permisos para resetear este lote");
        configuracionEstadosService.obtenerEstadoPorId(estadoInicialId)
                .orElseThrow(() -> new RuntimeException("Estado inicial no encontrado"));
        List<com.agrocloud.cultivos.domain.Labor> labores = laborRepository.findByLoteId(loteId);
        for (com.agrocloud.cultivos.domain.Labor labor : labores) {
            if (labor.getEstado() != com.agrocloud.cultivos.domain.Labor.EstadoLabor.COMPLETADA
                    && labor.getEstado() != com.agrocloud.cultivos.domain.Labor.EstadoLabor.CANCELADA
                    && labor.getEstado() != com.agrocloud.cultivos.domain.Labor.EstadoLabor.ANULADA) {
                labor.setEstado(com.agrocloud.cultivos.domain.Labor.EstadoLabor.CANCELADA);
                labor.setObservaciones((labor.getObservaciones() != null ? labor.getObservaciones() + "\n" : "") + "[CANCELADA POR RESET DE LOTE] " + motivo);
                labor.setActivo(false);
                laborRepository.save(labor);
            }
        }
        for (com.agrocloud.cultivos.domain.Labor labor : labores) {
            if (labor.getActivo() == null || labor.getActivo()) {
                if (labor.getEstado() == com.agrocloud.cultivos.domain.Labor.EstadoLabor.CANCELADA
                        || labor.getEstado() == com.agrocloud.cultivos.domain.Labor.EstadoLabor.ANULADA) {
                    labor.setActivo(false);
                    labor.setObservaciones((labor.getObservaciones() != null ? labor.getObservaciones() + "\n" : "") + "[ARCHIVADA POR RESET DE LOTE] " + motivo);
                    laborRepository.save(labor);
                }
            }
        }
        lote.setCultivo(null);
        lote.setTipoCultivo(null);
        lote.setCultivoActual(null);
        lote.setFechaSiembra(null);
        lote.setFechaCosechaEsperada(null);
        lote.setFechaCosechaReal(null);
        lote.setRendimientoEsperado(null);
        lote.setRendimientoReal(null);
        lote.setLiberadoParaSiembra(true);
        plotRepository.save(lote);
        estadoLoteUpdater.recalcularEstado(loteId);
        return plotRepository.findById(loteId).orElse(lote);
    }

    private void validarYCorregirLotes(List<Plot> lotes) {
        for (Plot lote : lotes) {
            if (lote.getEstado() == EstadoLote.DISPONIBLE && lote.getCultivoActual() != null && !lote.getCultivoActual().isEmpty()) {
                lote.setCultivoActual(null);
                lote.setCultivo(null);
                lote.setTipoCultivo(null);
                plotRepository.save(lote);
            }
        }
    }
}
