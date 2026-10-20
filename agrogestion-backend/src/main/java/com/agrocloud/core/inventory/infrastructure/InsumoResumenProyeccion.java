package com.agrocloud.core.inventory.infrastructure;

import java.math.BigDecimal;

/**
 * Proyección nativa de {@code cultivo_insumos} sin hidratar el enum {@code tipo}.
 */
public interface InsumoResumenProyeccion {

    Long getId();

    String getNombre();

    String getDescripcion();

    String getTipo();

    String getUnidadMedida();

    BigDecimal getPrecioUnitario();

    BigDecimal getStockActual();

    BigDecimal getStockMinimo();

    String getProveedor();

    Boolean getActivo();
}
