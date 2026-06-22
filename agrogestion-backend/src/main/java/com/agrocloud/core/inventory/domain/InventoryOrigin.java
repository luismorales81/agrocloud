package com.agrocloud.core.inventory.domain;

/**
 * Origen del movimiento de inventario para trazabilidad.
 * Incluye cultivos, porcinos y submódulos avícolas (crianza / huevos).
 */
public enum InventoryOrigin {
    CULTIVOS,
    PORCINOS,
    AVICOLA_CRIANZA,
    AVICOLA_CARNE,
    AVICOLA_HUEVOS,
    /** Ponedoras / postura (consumo de alimento en galpón). */
    AVICOLA_PONEDORAS,
    /** Engorde bovino a corral (feedlot). */
    FEEDLOT
}
