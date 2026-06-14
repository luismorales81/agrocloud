package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.TransicionEstadoConfig;
import com.agrocloud.cultivos.domain.TipoCultivo;

import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.cultivos.infrastructure.TransicionEstadoConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestionar las transiciones automáticas de estado de los lotes
 * basadas en las labores realizadas.
 */
@Service("transicionEstadoServiceCultivos")
@Transactional
public class TransicionEstadoService {

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private TransicionEstadoConfigRepository transicionEstadoConfigRepository;

    @Autowired
    @SuppressWarnings("unused")
    private CultivoRepository cultivoRepository;

    @Autowired
    private ConfiguracionEstadosService configuracionEstadosService;

    @Autowired
    private EstadoLoteUpdater estadoLoteUpdater;

    /**
     * Evalúa transiciones y actualiza el estado del lote de forma derivada (solo recalcula).
     * No escribe estado ni estadoConfigurado directamente.
     */
    public boolean evaluarYAplicarTransicion(Plot lote, Labor labor) {
        if (lote == null || labor == null) {
            return false;
        }
        EstadoLoteConfig nuevoEstadoConfigurado = evaluarTransicionConfigurada(lote, labor);
        if (nuevoEstadoConfigurado != null) {
            estadoLoteUpdater.recalcularEstado(lote.getId());
            return true;
        }

        // Si el lote usa configuración de estados por tipo de cultivo, NO aplicar reglas tradicionales.
        // Las transiciones deben estar gobernadas por la configuración definida para ese cultivo.
        boolean usaConfiguracionPorCultivo =
            lote.getEstadoConfigurado() != null &&
            (lote.getTipoCultivo() != null ||
             (lote.getCultivo() != null && lote.getCultivo().getTipo() != null));
        if (usaConfiguracionPorCultivo) {
            return false;
        }

        EstadoLote estadoAnterior = lote.getEstado();
        EstadoLote nuevoEstado = evaluarTransicionTradicional(lote, labor);
        if (nuevoEstado != null && nuevoEstado != estadoAnterior) {
            estadoLoteUpdater.recalcularEstado(lote.getId());
            return true;
        }
        return false;
    }

    private EstadoLoteConfig evaluarTransicionConfigurada(Plot lote, Labor labor) {
        if (lote.getEstadoConfigurado() == null) {
            return null;
        }
        Long estadoOrigenId = lote.getEstadoConfigurado().getId();
        Long empresaId = null;
        if (lote.getCampo() != null && lote.getCampo().getEmpresa() != null) {
            empresaId = lote.getCampo().getEmpresa().getId();
        }
        if (empresaId == null) {
            return null;
        }
        List<TransicionEstadoConfig> transiciones = transicionEstadoConfigRepository
            .findTransicionesValidasDesdeEstado(estadoOrigenId, empresaId);
        if (transiciones == null || transiciones.isEmpty()) {
            return null;
        }
        final Long tipoCultivoIdFinal;
        if (lote.getTipoCultivo() != null) {
            tipoCultivoIdFinal = lote.getTipoCultivo().getId();
        } else if (lote.getCultivo() != null && lote.getCultivo().getTipo() != null) {
            List<TipoCultivo> tiposCultivo = configuracionEstadosService.obtenerTodosLosTiposCultivo();
            tipoCultivoIdFinal = tiposCultivo.stream()
                .filter(tc -> tc.getNombre().equalsIgnoreCase(lote.getCultivo().getTipo()))
                .map(TipoCultivo::getId)
                .findFirst()
                .orElse(null);
        } else {
            tipoCultivoIdFinal = null;
        }
        List<TransicionEstadoConfig> transicionesFiltradas = transiciones;
        if (tipoCultivoIdFinal != null) {
            final Long tipoCultivoId = tipoCultivoIdFinal;
            transicionesFiltradas = transiciones.stream()
                .filter(t -> t.getTipoCultivoId() == null || t.getTipoCultivoId().equals(tipoCultivoId))
                .toList();
        }
        if (!transicionesFiltradas.isEmpty()) {
            return transicionesFiltradas.get(0).getEstadoDestino();
        }
        return null;
    }

    private EstadoLote evaluarTransicionTradicional(Plot lote, Labor laborActual) {
        EstadoLote estadoActual = lote.getEstado();
        Labor.TipoLabor tipoLabor = laborActual.getTipoLabor();
        switch (estadoActual) {
            case DISPONIBLE: return evaluarDesdeDisponible(lote, tipoLabor);
            case EN_PREPARACION: return evaluarDesdeEnPreparacion(lote, tipoLabor);
            case SEMBRADO: return evaluarDesdeSembrado(lote, tipoLabor);
            case EN_CRECIMIENTO: return evaluarDesdeEnCrecimiento(lote, tipoLabor);
            case EN_FLORACION: return evaluarDesdeEnFloracion(lote, tipoLabor);
            case EN_FRUTIFICACION: return evaluarDesdeEnFrutificacion(lote, tipoLabor);
            case COSECHADO: return evaluarDesdeCosechado(lote, tipoLabor);
            case EN_DESCANSO: return evaluarDesdeEnDescanso(lote, tipoLabor);
            case ENFERMO: return evaluarDesdeEnfermo(lote, tipoLabor);
            case ABANDONADO: return evaluarDesdeAbandonado(lote, tipoLabor);
            default: return null;
        }
    }

    private EstadoLote evaluarDesdeDisponible(Plot lote, Labor.TipoLabor tipoLabor) {
        if (tipoLabor == Labor.TipoLabor.MANTENIMIENTO || tipoLabor == Labor.TipoLabor.FERTILIZACION) {
            return EstadoLote.EN_PREPARACION;
        }
        return null;
    }

    private EstadoLote evaluarDesdeEnPreparacion(Plot lote, Labor.TipoLabor tipoLabor) {
        List<Labor> laboresPreparacion = laborRepository.findByLoteAndTipoLaborAndEstado(lote, Labor.TipoLabor.MANTENIMIENTO, Labor.EstadoLabor.COMPLETADA);
        if (laboresPreparacion.size() >= 2) {
            return EstadoLote.PREPARADO;
        }
        List<Labor> laboresFertilizacion = laborRepository.findByLoteAndTipoLaborAndEstado(lote, Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.COMPLETADA);
        if (laboresPreparacion.size() >= 1 && laboresFertilizacion.size() >= 1) {
            return EstadoLote.PREPARADO;
        }
        return null;
    }

    private EstadoLote evaluarDesdeSembrado(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaSiembra() != null) {
            long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(lote.getFechaSiembra(), LocalDate.now());
            if (diasDesdeSiembra >= 15) return EstadoLote.EN_CRECIMIENTO;
            if (diasDesdeSiembra >= 7 && (tipoLabor == Labor.TipoLabor.RIEGO || tipoLabor == Labor.TipoLabor.FERTILIZACION)) {
                return EstadoLote.EN_CRECIMIENTO;
            }
        }
        return null;
    }

    private EstadoLote evaluarDesdeEnCrecimiento(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaSiembra() != null && java.time.temporal.ChronoUnit.DAYS.between(lote.getFechaSiembra(), LocalDate.now()) >= 45) {
            return EstadoLote.EN_FLORACION;
        }
        return null;
    }

    private EstadoLote evaluarDesdeEnFloracion(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaSiembra() != null && java.time.temporal.ChronoUnit.DAYS.between(lote.getFechaSiembra(), LocalDate.now()) >= 65) {
            return EstadoLote.EN_FRUTIFICACION;
        }
        return null;
    }

    private EstadoLote evaluarDesdeEnFrutificacion(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaSiembra() != null && java.time.temporal.ChronoUnit.DAYS.between(lote.getFechaSiembra(), LocalDate.now()) >= 100) {
            return EstadoLote.LISTO_PARA_COSECHA;
        }
        return null;
    }

    private EstadoLote evaluarDesdeCosechado(Plot lote, Labor.TipoLabor tipoLabor) {
        return tipoLabor == Labor.TipoLabor.MANTENIMIENTO ? EstadoLote.EN_PREPARACION : null;
    }

    private EstadoLote evaluarDesdeEnDescanso(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaCosechaReal() != null) {
            long diasDesdeDescanso = java.time.temporal.ChronoUnit.DAYS.between(lote.getFechaCosechaReal(), LocalDate.now());
            if (diasDesdeDescanso >= 30 && tipoLabor == Labor.TipoLabor.MANTENIMIENTO) {
                return EstadoLote.EN_PREPARACION;
            }
        }
        return null;
    }

    private EstadoLote evaluarDesdeEnfermo(Plot lote, Labor.TipoLabor tipoLabor) {
        List<Labor> laborTratamiento = laborRepository.findByLoteAndTipoLaborInAndEstado(lote,
            List.of(Labor.TipoLabor.CONTROL_PLAGAS, Labor.TipoLabor.CONTROL_MALEZAS), Labor.EstadoLabor.COMPLETADA);
        if (laborTratamiento.size() >= 2 && lote.getFechaSiembra() != null) {
            long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(lote.getFechaSiembra(), LocalDate.now());
            if (diasDesdeSiembra < 30) return EstadoLote.SEMBRADO;
            if (diasDesdeSiembra < 60) return EstadoLote.EN_CRECIMIENTO;
            if (diasDesdeSiembra < 80) return EstadoLote.EN_FLORACION;
            if (diasDesdeSiembra < 110) return EstadoLote.EN_FRUTIFICACION;
            return EstadoLote.LISTO_PARA_COSECHA;
        }
        return null;
    }

    private EstadoLote evaluarDesdeAbandonado(Plot lote, Labor.TipoLabor tipoLabor) {
        return tipoLabor == Labor.TipoLabor.MANTENIMIENTO ? EstadoLote.EN_PREPARACION : null;
    }

    public boolean requiereAtencion(Plot lote) {
        if (lote.getEstado() == EstadoLote.LISTO_PARA_COSECHA) {
            return lote.calcularDiasDesdeUltimoCambioEstado() > 15;
        }
        return lote.getEstado() == EstadoLote.ENFERMO || lote.getEstado() == EstadoLote.ABANDONADO;
    }
}
