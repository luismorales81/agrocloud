package com.agrocloud.core.inventory.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de lectura para movimientos de inventario (sin exponer entidad JPA).
 */
public record MovimientoInventarioDTO(
    Long id,
    LocalDateTime fechaMovimiento,
    String tipoMovimiento,
    BigDecimal cantidad,
    String motivo,
    String origen,
    Long referenciaId,
    Long insumoId
) {}
