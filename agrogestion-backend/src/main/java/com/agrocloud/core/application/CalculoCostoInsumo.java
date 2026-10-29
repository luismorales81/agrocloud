package com.agrocloud.core.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Promedio ponderado del costo unitario al comprar o al anular una compra.
 */
public final class CalculoCostoInsumo {

    private static final int ESCALA = 4;

    private CalculoCostoInsumo() {
    }

    public static BigDecimal costoTrasCompra(BigDecimal saldoAnterior, BigDecimal costoAnterior,
                                             BigDecimal cantidad, BigDecimal costoTotal) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0 || costoTotal == null) {
            throw new IllegalArgumentException("La cantidad y el costo total de la compra son obligatorios");
        }
        BigDecimal saldo = saldoAnterior != null ? saldoAnterior : BigDecimal.ZERO;
        if (saldo.compareTo(BigDecimal.ZERO) <= 0 || costoAnterior == null) {
            return costoTotal.divide(cantidad, ESCALA, RoundingMode.HALF_UP);
        }
        BigDecimal saldoNuevo = saldo.add(cantidad);
        return saldo.multiply(costoAnterior).add(costoTotal).divide(saldoNuevo, ESCALA, RoundingMode.HALF_UP);
    }

    public static BigDecimal costoTrasAnularCompra(BigDecimal saldoActual, BigDecimal costoActual,
                                                   BigDecimal cantidad, BigDecimal costoUnitarioCompra) {
        BigDecimal saldo = saldoActual != null ? saldoActual : BigDecimal.ZERO;
        BigDecimal cantidadSegura = cantidad != null ? cantidad : BigDecimal.ZERO;
        BigDecimal saldoNuevo = saldo.subtract(cantidadSegura);
        if (saldoNuevo.compareTo(BigDecimal.ZERO) <= 0 || costoActual == null || costoUnitarioCompra == null) {
            return costoUnitarioCompra != null ? costoUnitarioCompra : costoActual;
        }
        BigDecimal numerador = saldo.multiply(costoActual).subtract(cantidadSegura.multiply(costoUnitarioCompra));
        if (numerador.compareTo(BigDecimal.ZERO) < 0) {
            return costoUnitarioCompra;
        }
        return numerador.divide(saldoNuevo, ESCALA, RoundingMode.HALF_UP);
    }
}
