package com.agrocloud.model.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Códigos canónicos de módulos ({@code modules.code}, {@code @RequiresModule}, interceptor de acceso).
 * Los literales en anotaciones deben coincidir con {@link #getCodigo()}.
 * <p>
 * Cada código corresponde a una fila en {@code company_modules} por empresa. {@link #AVICOLA_CRIANZA}
 * cubre engorde parrillero; {@link #AVICOLA_PONEDORAS} y {@link #AVICOLA_HUEVOS} son módulos distintos.
 * </p>
 */
public enum ModuloSistema {

    CULTIVOS("CULTIVOS"),
    PORCINOS("PORCINOS"),
    AVICOLA_CRIANZA("AVICOLA_CRIANZA"),
    AVICOLA_HUEVOS("AVICOLA_HUEVOS"),
    /** Ponedoras / recría (módulo propio; no confundir con {@link #AVICOLA_HUEVOS}). */
    AVICOLA_PONEDORAS("AVICOLA_PONEDORAS"),
    /** Engorde bovino a corral (API {@code /api/feedlot}). */
    FEEDLOT("FEEDLOT"),
    /** Explotación lechera multi-especie (API {@code /api/lecheria}). */
    LECHERIA("LECHERIA");

    private final String codigo;

    ModuloSistema(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static Optional<ModuloSistema> desdeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        String normalizado = codigo.trim();
        return Arrays.stream(values())
                .filter(m -> m.codigo.equalsIgnoreCase(normalizado))
                .findFirst();
    }
}
