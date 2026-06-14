package com.agrocloud.cultivos.application;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para cálculos de rendimiento de cultivos
 * Migrado desde el frontend para centralizar la lógica de negocio
 */
@Service("rendimientoServiceCultivos")
public class RendimientoService {

    public BigDecimal calcularRendimientoReal(
            BigDecimal cantidad,
            String unidadCantidad,
            BigDecimal superficie,
            String unidadCultivo) {

        if (superficie == null || superficie.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal cantidadEnKg = convertirAKilogramos(cantidad, unidadCantidad);
        BigDecimal cantidadEnUnidadCultivo = convertirDesdeKilogramos(cantidadEnKg, unidadCultivo);
        BigDecimal rendimiento = cantidadEnUnidadCultivo.divide(superficie, 2, RoundingMode.HALF_UP);

        return rendimiento;
    }

    private BigDecimal convertirAKilogramos(BigDecimal cantidad, String unidad) {
        if (unidad == null) {
            return cantidad;
        }
        String unidadLower = unidad.toLowerCase();
        if (unidadLower.contains("tn") || unidadLower.contains("tonelada")) {
            return cantidad.multiply(new BigDecimal("1000"));
        } else if (unidadLower.contains("qq") || unidadLower.contains("quintal")) {
            return cantidad.multiply(new BigDecimal("46"));
        } else {
            return cantidad;
        }
    }

    private BigDecimal convertirDesdeKilogramos(BigDecimal cantidadEnKg, String unidadObjetivo) {
        if (unidadObjetivo == null) {
            return cantidadEnKg;
        }
        String unidadLower = unidadObjetivo.toLowerCase();
        if (unidadLower.contains("tn") || unidadLower.contains("tonelada")) {
            return cantidadEnKg.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        } else if (unidadLower.contains("qq") || unidadLower.contains("quintal")) {
            return cantidadEnKg.divide(new BigDecimal("46"), 2, RoundingMode.HALF_UP);
        } else {
            return cantidadEnKg;
        }
    }

    public BigDecimal calcularDiferenciaPorcentual(BigDecimal rendimientoEsperado, BigDecimal rendimientoReal) {
        if (rendimientoEsperado == null || rendimientoEsperado.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (rendimientoReal == null) {
            return new BigDecimal("-100");
        }
        BigDecimal diferencia = rendimientoReal.subtract(rendimientoEsperado);
        BigDecimal porcentaje = diferencia
                .divide(rendimientoEsperado, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return porcentaje.setScale(2, RoundingMode.HALF_UP);
    }
}
