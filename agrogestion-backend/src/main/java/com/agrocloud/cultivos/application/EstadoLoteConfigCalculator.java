package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.HistorialCosecha;
import com.agrocloud.cultivos.domain.Labor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Calcula el EstadoLoteConfig (estado de la configuración) para un lote cuando tiene tipo de cultivo configurado.
 * Usa la configuración definida para cada cultivo en lugar del enum fijo.
 */
public final class EstadoLoteConfigCalculator {

    private static final int DIAS_EMERGENCIA = 7;
    private static final int DIAS_ESTABLECIMIENTO = 15;

    private EstadoLoteConfigCalculator() {}

    /**
     * Deriva el estado configurado a partir de labores, historial de cosecha y fecha de siembra.
     * Prioridad: abandono → cosecha vigente (post-corte) → sin siembra → con siembra (por días).
     */
    public static Optional<EstadoLoteConfig> calcularEstadoConfigurado(
            Plot lote,
            List<Labor> labores,
            Optional<HistorialCosecha> cosechaVigente,
            List<EstadoLoteConfig> estadosConfig) {

        if (estadosConfig == null || estadosConfig.isEmpty()) {
            return Optional.empty();
        }

        List<Labor> activas = labores.stream()
                .filter(EstadoLoteConfigCalculator::esLaborActiva)
                .toList();

        // 1. Abandono activo → buscar estado "Abandonado" o similar
        boolean hayAbandono = activas.stream().anyMatch(EstadoLoteConfigCalculator::esLaborAbandono);
        if (hayAbandono) {
            return buscarEstadoPorNombre(estadosConfig, "abandonado", "abandono");
        }

        // 2. Cosecha vigente (post-corte) → Rebrote, Cosechado, Levantado o Dormancia según config
        if (cosechaVigente.isPresent()) {
            return buscarEstadoPostCosecha(estadosConfig);
        }

        // 3. Sin siembra activa → estado inicial
        boolean haySiembraActiva = activas.stream()
                .anyMatch(l -> l.getTipoLabor() == Labor.TipoLabor.SIEMBRA);
        if (!haySiembraActiva) {
            return estadosConfig.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getEsEstadoInicial()))
                    .findFirst();
        }

        // 4. Labor COSECHA en curso → buscar "Listo para Cosecha", "Primer Corte", etc.
        boolean hayCosechaActiva = activas.stream()
                .anyMatch(l -> l.getTipoLabor() == Labor.TipoLabor.COSECHA);
        if (hayCosechaActiva) {
            return buscarEstadoPorNombre(estadosConfig, "cosecha", "corte", "listo");
        }

        // 5. Con siembra: derivar por días desde fechaSiembra
        LocalDate fechaSiembra = lote.getFechaSiembra();
        if (fechaSiembra == null) {
            return buscarEstadoPorNombre(estadosConfig, "sembrado");
        }

        long dias = ChronoUnit.DAYS.between(fechaSiembra, LocalDate.now());
        if (dias < DIAS_EMERGENCIA) {
            return buscarEstadoPorNombre(estadosConfig, "sembrado");
        }
        if (dias < DIAS_ESTABLECIMIENTO) {
            return buscarEstadoPorNombre(estadosConfig, "emergencia", "sembrado");
        }
        // Establecimiento o en crecimiento
        return buscarEstadoPorNombre(estadosConfig, "establecimiento", "crecimiento", "emergencia");
    }

    private static Optional<EstadoLoteConfig> buscarEstadoPostCosecha(List<EstadoLoteConfig> estados) {
        for (EstadoLoteConfig e : estados) {
            if (e.getNombre() == null) continue;
            String n = e.getNombre().toLowerCase().replace(" ", "");
            if (n.contains("rebrote") || n.contains("cosechado") || n.contains("levantado") || n.contains("dormancia")) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    private static Optional<EstadoLoteConfig> buscarEstadoPorNombre(List<EstadoLoteConfig> estados, String... terminos) {
        for (String termino : terminos) {
            for (EstadoLoteConfig e : estados) {
                if (e.getNombre() != null && e.getNombre().toLowerCase().replace(" ", "").contains(termino)) {
                    return Optional.of(e);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean esLaborActiva(Labor labor) {
        if (labor.getActivo() == null || !labor.getActivo()) return false;
        Labor.EstadoLabor e = labor.getEstado();
        return e != Labor.EstadoLabor.CANCELADA && e != Labor.EstadoLabor.ANULADA;
    }

    private static boolean esLaborAbandono(Labor labor) {
        if (labor.getTipoLabor() != Labor.TipoLabor.OTROS) return false;
        String d = labor.getDescripcion() != null ? labor.getDescripcion().toUpperCase() : "";
        String o = labor.getObservaciones() != null ? labor.getObservaciones().toUpperCase() : "";
        return d.contains("ABANDONO") || o.contains("ABANDONO");
    }
}
