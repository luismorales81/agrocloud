package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.TareaPorEstadoConfig;
import com.agrocloud.cultivos.domain.TipoCultivo;
import com.agrocloud.cultivos.domain.TransicionEstadoConfig;
import com.agrocloud.cultivos.util.MapeadorEstadoLoteConfig;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.cultivos.domain.HistorialCosecha;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.cultivos.infrastructure.HistorialCosechaRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

/**
 * Actualiza el estado del lote de forma derivada.
 * Cuando el lote tiene tipo de cultivo con configuración, usa EstadoLoteConfigCalculator (config por cultivo).
 * Cuando no tiene config, usa EstadoLoteCalculator (enum fijo).
 */
@Service
public class EstadoLoteUpdater {

    private final PlotRepository plotRepository;
    private final LaborRepository laborRepository;
    private final HistorialCosechaRepository historialCosechaRepository;
    private final ConfiguracionEstadosService configuracionEstadosService;

    public EstadoLoteUpdater(PlotRepository plotRepository,
                             LaborRepository laborRepository,
                             HistorialCosechaRepository historialCosechaRepository,
                             ConfiguracionEstadosService configuracionEstadosService) {
        this.plotRepository = plotRepository;
        this.laborRepository = laborRepository;
        this.historialCosechaRepository = historialCosechaRepository;
        this.configuracionEstadosService = configuracionEstadosService;
    }

    @Transactional
    public void recalcularEstado(Long loteId) {
        Plot lote = plotRepository.findById(loteId).orElse(null);
        if (lote == null) {
            return;
        }
        List<Labor> labores = laborRepository.findByLoteId(loteId);
        Optional<HistorialCosecha> cosechaVigente = obtenerCosechaVigente(lote);

        Long tipoCultivoId = obtenerTipoCultivoId(lote);
        Long empresaId = lote.getCampo() != null && lote.getCampo().getEmpresa() != null
                ? lote.getCampo().getEmpresa().getId() : null;

        // Prioridad: usar configuración por tipo de cultivo cuando exista
        if (tipoCultivoId != null && empresaId != null) {
            List<EstadoLoteConfig> estadosConfig = configuracionEstadosService.obtenerEstadosPorTipoCultivo(tipoCultivoId, empresaId);
            Optional<EstadoLoteConfig> estadoConfigOpt = EstadoLoteConfigCalculator.calcularEstadoConfigurado(lote, labores, cosechaVigente, estadosConfig);
            if (estadoConfigOpt.isPresent()) {
                EstadoLoteConfig estadoBase = estadoConfigOpt.get();
                EstadoLoteConfig nuevo = considerarTransicionPorTareasCompletadas(
                    estadoBase, labores, tipoCultivoId, empresaId);
                boolean cambia = lote.getEstadoConfigurado() == null
                        || !nuevo.getId().equals(lote.getEstadoConfigurado().getId());
                if (cambia) {
                    lote.setEstadoConfigurado(nuevo);
                    lote.setEstado(MapeadorEstadoLoteConfig.mapearAEnum(nuevo));
                    lote.setFechaUltimoCambioEstado(LocalDateTime.now());
                    lote.setMotivoCambioEstado("Recálculo derivado (config)");
                    plotRepository.save(lote);
                }
                return;
            }
        }

        // Fallback: enum fijo cuando no hay configuración
        EstadoLote nuevoEstado = EstadoLoteCalculator.calcularEstado(lote, labores, cosechaVigente);
        if (nuevoEstado.equals(lote.getEstado()) && lote.getEstadoConfigurado() == null) {
            return;
        }
        lote.setEstadoConfigurado(null);
        lote.setEstado(nuevoEstado);
        lote.setFechaUltimoCambioEstado(LocalDateTime.now());
        lote.setMotivoCambioEstado("Recálculo derivado");
        plotRepository.save(lote);
    }

    /**
     * Si el estado actual tiene tareas configuradas y todas tienen al menos una labor COMPLETADA
     * en el lote, y existe una transición al siguiente estado, devuelve el estado destino.
     * En caso contrario devuelve el estado base (calculado por tiempo/cosecha).
     */
    private EstadoLoteConfig considerarTransicionPorTareasCompletadas(
            EstadoLoteConfig estadoBase,
            List<Labor> labores,
            Long tipoCultivoId,
            Long empresaId) {
        List<TareaPorEstadoConfig> tareas = configuracionEstadosService
            .obtenerTareasPorEstado(estadoBase.getId(), empresaId);
        if (tareas == null || tareas.isEmpty()) {
            return estadoBase;
        }
        List<TareaPorEstadoConfig> tareasRelevantes = tareas.stream()
            .filter(t -> Boolean.TRUE.equals(t.getEsObligatoria()))
            .toList();
        if (tareasRelevantes.isEmpty()) {
            tareasRelevantes = tareas;
        }
        List<Labor> completadas = labores.stream()
            .filter(l -> Boolean.TRUE.equals(l.getActivo()))
            .filter(l -> l.getEstado() == Labor.EstadoLabor.COMPLETADA)
            .toList();
        for (TareaPorEstadoConfig tarea : tareasRelevantes) {
            String tipoTarea = tarea.getTipoLabor();
            if (tipoTarea == null) continue;
            boolean hayLaborCompletada = completadas.stream()
                .anyMatch(l -> l.getTipoLabor() != null
                    && l.getTipoLabor().name().equalsIgnoreCase(tipoTarea));
            if (!hayLaborCompletada) {
                return estadoBase;
            }
        }
        List<TransicionEstadoConfig> transiciones = configuracionEstadosService
            .obtenerTransicionesValidasDesdeEstado(estadoBase.getId(), empresaId);
        if (transiciones == null || transiciones.isEmpty()) {
            return estadoBase;
        }
        List<TransicionEstadoConfig> filtradas = transiciones.stream()
            .filter(t -> t.getTipoCultivoId() == null || tipoCultivoId != null && t.getTipoCultivoId().equals(tipoCultivoId))
            .filter(t -> t.getEstadoDestino() != null)
            .sorted(Comparator.comparing(t -> t.getEstadoDestino().getOrden()))
            .toList();
        if (filtradas.isEmpty()) {
            return estadoBase;
        }
        EstadoLoteConfig destino = filtradas.get(0).getEstadoDestino();
        return destino != null ? destino : estadoBase;
    }

    private Long obtenerTipoCultivoId(Plot lote) {
        if (lote.getTipoCultivo() != null) {
            return lote.getTipoCultivo().getId();
        }
        if (lote.getCultivo() != null) {
            String tipo = lote.getCultivo().getTipo();
            String nombre = lote.getCultivo().getNombre();
            return configuracionEstadosService.obtenerTodosLosTiposCultivo().stream()
                    .filter(tc -> tc.getNombre() != null && (
                            (tipo != null && tc.getNombre().equalsIgnoreCase(tipo)) ||
                            (nombre != null && tc.getNombre().equalsIgnoreCase(nombre))))
                    .map(TipoCultivo::getId)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /** Mapeo para compatibilidad con código que lee Plot.estado (enum). */
    private EstadoLote mapearConfigAEnumParaLegacy(EstadoLoteConfig config) {
        return MapeadorEstadoLoteConfig.mapearAEnum(config);
    }

    private Optional<HistorialCosecha> obtenerCosechaVigente(Plot lote) {
        if (lote.getCicloActivoId() != null) {
            return Optional.empty();
        }
        // Si el lote está liberado para una nueva siembra, no considerar cosechas previas
        if (Boolean.TRUE.equals(lote.getLiberadoParaSiembra())) {
            return Optional.empty();
        }
        Optional<HistorialCosecha> ultimaCosechaOpt =
            historialCosechaRepository.findFirstByLoteIdOrderByFechaCosechaDesc(lote.getId());
        if (ultimaCosechaOpt.isEmpty()) {
            return Optional.empty();
        }

        HistorialCosecha ultima = ultimaCosechaOpt.get();
        // Regla: una cosecha es "vigente" solo si es posterior (o igual) a la fecha de siembra actual del lote.
        // Si la cosecha es de un ciclo anterior (fechaCosecha < fechaSiembra), no debe forzar estado COSECHADO.
        if (lote.getFechaSiembra() != null
                && ultima.getFechaCosecha() != null
                && ultima.getFechaCosecha().isBefore(lote.getFechaSiembra())) {
            return Optional.empty();
        }

        return ultimaCosechaOpt;
    }
}
