package com.agrocloud.avicola.comun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reglas de la lista de razas de un lote: al menos una, sin repetir, sin cantidad por raza.
 */
public final class ComposicionRazasLote {

    private ComposicionRazasLote() {
    }

    /**
     * @return ids distintos, o {@code null} si no vino lista ni raza suelta y no es obligatorio
     */
    public static List<Long> idsSolicitados(List<Long> razaIds, Long razaId, boolean obligatorio) {
        if (razaIds != null) {
            if (razaIds.isEmpty()) {
                throw new IllegalArgumentException("El lote debe tener al menos una raza");
            }
            return normalizar(razaIds);
        }
        if (razaId != null) {
            return List.of(razaId);
        }
        if (obligatorio) {
            throw new IllegalArgumentException("El lote debe tener al menos una raza");
        }
        return null;
    }

    public static String unirNombres(Collection<String> nombres) {
        return nombres.stream()
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .map(String::trim)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(" · "));
    }

    public static String textoColumna(String nombresUnidos, int largoMaximo) {
        if (nombresUnidos == null || nombresUnidos.length() <= largoMaximo) {
            return nombresUnidos == null ? "" : nombresUnidos;
        }
        if (largoMaximo <= 3) {
            return nombresUnidos.substring(0, largoMaximo);
        }
        return nombresUnidos.substring(0, largoMaximo - 3) + "...";
    }

    private static List<Long> normalizar(List<Long> razaIds) {
        LinkedHashSet<Long> unicos = new LinkedHashSet<>();
        for (Long id : razaIds) {
            if (id == null) {
                throw new IllegalArgumentException("La raza es obligatoria");
            }
            if (!unicos.add(id)) {
                throw new IllegalArgumentException("La misma raza no se puede repetir en el lote");
            }
        }
        return new ArrayList<>(unicos);
    }
}
