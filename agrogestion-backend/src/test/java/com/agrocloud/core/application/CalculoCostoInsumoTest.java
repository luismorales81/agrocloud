package com.agrocloud.core.application;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculoCostoInsumoTest {

    @Test
    void primeraCompraUsaElPrecioDeEsaCompra() {
        BigDecimal costo = CalculoCostoInsumo.costoTrasCompra(
                BigDecimal.ZERO, null, new BigDecimal("10"), new BigDecimal("100"));
        assertEquals(new BigDecimal("10.0000"), costo);
    }

    @Test
    void segundaCompraPromediaElSaldo() {
        BigDecimal costo = CalculoCostoInsumo.costoTrasCompra(
                new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("200"));
        assertEquals(new BigDecimal("15.0000"), costo);
    }

    @Test
    void saldoNegativoPrevioTomaElPrecioDeLaCompra() {
        BigDecimal costo = CalculoCostoInsumo.costoTrasCompra(
                new BigDecimal("-2"), new BigDecimal("8"), new BigDecimal("10"), new BigDecimal("50"));
        assertEquals(new BigDecimal("5.0000"), costo);
    }
}
