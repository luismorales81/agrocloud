package com.agrocloud.avicola;

/**
 * Valores de {@code moduloOrigen} en movimientos de inventario compartido (CORE) por submódulo avícola.
 */
public final class ModuloOrigenInventarioAvicola {

    public static final String CRIANZA = "AVICOLA_CRIANZA";
    /** Mismo valor simbólico que InventoryOrigin.AVICOLA_CARNE (egreso CORE). */
    public static final String CARNE = "AVICOLA_CARNE";
    public static final String HUEVOS = "AVICOLA_HUEVOS";
    /** Mismo valor simbólico que {@link com.agrocloud.core.inventory.domain.InventoryOrigin#AVICOLA_PONEDORAS}. */
    public static final String PONEDORAS = "AVICOLA_PONEDORAS";

    private ModuloOrigenInventarioAvicola() {
    }
}
