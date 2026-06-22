package com.agrocloud.cultivos.util;

import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.model.enums.EstadoLote;

/**
 * Mapea estados configurados al enum legacy para compatibilidad.
 */
public final class MapeadorEstadoLoteConfig {

    private MapeadorEstadoLoteConfig() {}

    public static EstadoLote mapearAEnum(EstadoLoteConfig config) {
        if (config == null || config.getNombre() == null) {
            return EstadoLote.DISPONIBLE;
        }
        String n = config.getNombre().toLowerCase().replace(" ", "");
        if (n.contains("disponible")) return EstadoLote.DISPONIBLE;
        if (n.contains("preparado") || n.contains("preparacion")) return EstadoLote.PREPARADO;
        if (n.contains("sembrado")) return EstadoLote.SEMBRADO;
        if (n.contains("emergencia")) return EstadoLote.SEMBRADO;
        if (n.contains("establecimiento") || n.contains("crecimiento") || n.contains("rebrote")) return EstadoLote.EN_CRECIMIENTO;
        if (n.contains("flor")) return EstadoLote.EN_FLORACION;
        if (n.contains("frutif")) return EstadoLote.EN_FRUTIFICACION;
        if (n.contains("corte") || n.contains("listo") || n.contains("cosecha")) return EstadoLote.LISTO_PARA_COSECHA;
        if (n.contains("dormancia") || n.contains("levantado") || n.contains("cosechado")) return EstadoLote.COSECHADO;
        if (n.contains("abandonado")) return EstadoLote.ABANDONADO;
        if (n.contains("enfermo")) return EstadoLote.ENFERMO;
        if (n.contains("descanso")) return EstadoLote.EN_DESCANSO;
        return EstadoLote.EN_CRECIMIENTO;
    }

    public static boolean esEstadoDerivadoPorEvento(EstadoLoteConfig config) {
        if (config == null || config.getNombre() == null) {
            return false;
        }
        String n = config.getNombre().toLowerCase().replace(" ", "");
        return n.contains("sembrado") || n.contains("cosechado");
    }
}
