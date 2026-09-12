package com.agrocloud.chatia.herramientas;

import com.agrocloud.cultivos.domain.Plot;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class FiltroEmpresaChatIa {

    private FiltroEmpresaChatIa() {
    }

    public static <T> List<T> filtrarPorEmpresa(List<T> elementos, Long empresaId, Predicate<T> perteneceEmpresa) {
        if (elementos == null || elementos.isEmpty()) {
            return List.of();
        }
        if (empresaId == null) {
            return elementos;
        }
        return elementos.stream()
                .filter(perteneceEmpresa)
                .toList();
    }

    public static boolean mismaEmpresa(Long empresaId, Long empresaEntidad) {
        return empresaId != null && Objects.equals(empresaId, empresaEntidad);
    }

    public static List<Long> idsLotesEmpresa(List<Plot> lotes) {
        return lotes.stream().map(Plot::getId).toList();
    }
}
