package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.HistorialCosecha;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.model.enums.EstadoLote;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Calcula el estado del lote de forma determinística a partir de labores activas,
 * historial de cosecha vigente y fecha de siembra. Clase pura, sin repositorios ni efectos secundarios.
 */
public final class EstadoLoteCalculator {

    private static final int DIAS_CRECIMIENTO = 15;
    private static final int DIAS_FLORACION = 45;
    private static final int DIAS_PARA_COSECHA = 100;

    private EstadoLoteCalculator() {}

    /**
     * Orden de prioridad obligatorio:
     * 1. Labor ABANDONO activa → ABANDONADO
     * 2. HistorialCosecha vigente → COSECHADO
     * 3. No Labor SIEMBRA activa → DISPONIBLE
     * 4. Labor COSECHA activa → EN_COSECHA
     * 5. días >= díasParaCosecha → LISTO_PARA_COSECHA
     * 6. días >= díasFloracion → EN_FLORACION
     * 7. días >= díasCrecimiento → EN_CRECIMIENTO
     * 8. Caso contrario → SEMBRADO
     */
    public static EstadoLote calcularEstado(
            Plot lote,
            List<Labor> labores,
            Optional<HistorialCosecha> cosechaVigente) {

        List<Labor> activas = labores.stream()
                .filter(EstadoLoteCalculator::esLaborActiva)
                .toList();

        boolean hayAbandonoActivo = activas.stream().anyMatch(EstadoLoteCalculator::esLaborAbandono);
        if (hayAbandonoActivo) {
            return EstadoLote.ABANDONADO;
        }

        if (cosechaVigente.isPresent()) {
            return EstadoLote.COSECHADO;
        }

        boolean haySiembraActiva = activas.stream()
                .anyMatch(l -> l.getTipoLabor() == Labor.TipoLabor.SIEMBRA);
        if (!haySiembraActiva) {
            return EstadoLote.DISPONIBLE;
        }

        boolean hayCosechaActiva = activas.stream()
                .anyMatch(l -> l.getTipoLabor() == Labor.TipoLabor.COSECHA);
        if (hayCosechaActiva) {
            return EstadoLote.EN_COSECHA;
        }

        LocalDate fechaSiembra = lote.getFechaSiembra();
        if (fechaSiembra == null) {
            return EstadoLote.SEMBRADO;
        }
        long dias = ChronoUnit.DAYS.between(fechaSiembra, LocalDate.now());

        if (dias >= DIAS_PARA_COSECHA) {
            return EstadoLote.LISTO_PARA_COSECHA;
        }
        if (dias >= DIAS_FLORACION) {
            return EstadoLote.EN_FLORACION;
        }
        if (dias >= DIAS_CRECIMIENTO) {
            return EstadoLote.EN_CRECIMIENTO;
        }
        return EstadoLote.SEMBRADO;
    }

    private static boolean esLaborActiva(Labor labor) {
        if (labor.getActivo() == null || !labor.getActivo()) {
            return false;
        }
        Labor.EstadoLabor e = labor.getEstado();
        return e != Labor.EstadoLabor.CANCELADA && e != Labor.EstadoLabor.ANULADA;
    }

    private static boolean esLaborAbandono(Labor labor) {
        if (labor.getTipoLabor() != Labor.TipoLabor.OTROS) {
            return false;
        }
        String d = labor.getDescripcion() != null ? labor.getDescripcion().toUpperCase() : "";
        String o = labor.getObservaciones() != null ? labor.getObservaciones().toUpperCase() : "";
        return d.contains("ABANDONO") || o.contains("ABANDONO");
    }
}
