package com.agrocloud.core.inventory.domain;

import java.math.BigDecimal;

/**
 * Resultado de una operación de inventario (consumir o reponer).
 * Inmutable para garantizar trazabilidad.
 */
public record InventoryResult(
    boolean exito,
    String mensaje,
    BigDecimal stockRestante
) {
    public static InventoryResult ok(BigDecimal stockRestante) {
        return new InventoryResult(true, null, stockRestante);
    }

    public static InventoryResult error(String mensaje) {
        return new InventoryResult(false, mensaje, null);
    }
}
