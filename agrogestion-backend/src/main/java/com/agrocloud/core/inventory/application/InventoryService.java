package com.agrocloud.core.inventory.application;

import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.inventory.domain.MovimientoInventarioDTO;
import com.agrocloud.core.inventory.domain.ProductoInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contrato del módulo core-inventory.
 * Único punto de entrada para consumo, reposición y consulta de stock.
 * Cultivos y Porcinos NO deben modificar stock ni acceder a repositorios de inventario;
 * solo usan este servicio.
 */
public interface InventoryService {

    /**
     * Consumir (descontar) cantidad de un producto.
     *
     * @param empresaId   ID de la empresa
     * @param productoId  ID del producto (insumo)
     * @param cantidad    Cantidad a consumir
     * @param origen      Origen del consumo (CULTIVOS, PORCINOS)
     * @param referenciaId ID de la entidad que origina el consumo (labor, evento sanitario, etc.)
     * @return Resultado con éxito/error y stock restante
     */
    InventoryResult consumir(
        Long empresaId,
        Long productoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Reponer (devolver) cantidad a un producto.
     *
     * @param empresaId   ID de la empresa
     * @param productoId  ID del producto (insumo)
     * @param cantidad   Cantidad a reponer
     * @param origen     Origen de la reposición
     * @param referenciaId ID de la entidad de referencia (ej. cancelación de labor/evento)
     * @param usuarioId  ID del usuario que realiza la operación (opcional, para auditoría)
     * @return Resultado con éxito/error y stock restante
     */
    InventoryResult reponer(
        Long empresaId,
        Long productoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Consultar stock disponible de un producto (solo lectura).
     *
     * @param empresaId  ID de la empresa
     * @param productoId ID del producto (insumo)
     * @return Cantidad disponible o null si no existe / no pertenece a la empresa
     */
    BigDecimal consultarStock(Long empresaId, Long productoId);

    /**
     * Verificar si hay stock suficiente sin modificar.
     *
     * @param empresaId  ID de la empresa
     * @param productoId ID del producto
     * @param cantidad   Cantidad requerida
     * @return true si hay stock suficiente
     */
    boolean hayStockSuficiente(Long empresaId, Long productoId, BigDecimal cantidad);

    /**
     * Consumir permitiendo stock negativo (ej. consumo automático diario).
     * Registra el movimiento y actualiza stock sin validar suficiencia.
     *
     * @param empresaId   ID de la empresa
     * @param productoId  ID del producto
     * @param cantidad    Cantidad a consumir
     * @param origen      Origen
     * @param referenciaId ID de referencia (ej. consumo diario, día alimentación)
     * @param usuarioId   Usuario (opcional)
     * @return Resultado con stock restante (puede ser negativo)
     */
    InventoryResult consumirPermitiendoNegativo(
        Long empresaId,
        Long productoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Consultar nombre y unidad de medida de un producto (solo lectura).
     */
    Optional<ProductoInfo> consultarProducto(Long empresaId, Long productoId);

    /**
     * Obtener empresa del producto (para llamadas que solo tienen productoId).
     */
    Optional<Long> obtenerEmpresaIdDeProducto(Long productoId);

    /**
     * Listar movimientos de un insumo (solo lectura).
     */
    List<MovimientoInventarioDTO> listarMovimientosPorInsumo(Long empresaId, Long productoId);

    /**
     * Listar movimientos asociados a una labor de Cultivos.
     */
    List<MovimientoInventarioDTO> listarMovimientosPorLaborCultivos(Long laborId);

    /**
     * Calcular saldo por movimientos de un insumo (solo lectura).
     */
    BigDecimal calcularSaldoPorInsumo(Long empresaId, Long productoId);

    // ----- Insumo compuesto (receta/ración) -----

    /**
     * Consultar stock de un insumo compuesto.
     */
    BigDecimal consultarStockInsumoCompuesto(Long empresaId, Long insumoCompuestoId);

    /**
     * Consumir (descontar) cantidad de un insumo compuesto. No permite stock negativo.
     */
    InventoryResult consumirInsumoCompuesto(
        Long empresaId,
        Long insumoCompuestoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Consumir insumo compuesto permitiendo stock negativo (ej. consumo automático diario).
     */
    InventoryResult consumirInsumoCompuestoPermitiendoNegativo(
        Long empresaId,
        Long insumoCompuestoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Reponer cantidad a un insumo compuesto.
     */
    InventoryResult reponerInsumoCompuesto(
        Long empresaId,
        Long insumoCompuestoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Verificar si hay stock suficiente de insumo compuesto.
     */
    boolean hayStockSuficienteInsumoCompuesto(Long empresaId, Long insumoCompuestoId, BigDecimal cantidad);

    /**
     * Consultar nombre y unidad de un insumo compuesto.
     */
    Optional<ProductoInfo> consultarProductoInsumoCompuesto(Long empresaId, Long insumoCompuestoId);

    /**
     * Obtener empresa de un insumo compuesto por ID.
     */
    Optional<Long> obtenerEmpresaIdDeInsumoCompuesto(Long insumoCompuestoId);

    // ----- Grano propio (InventarioGrano por cultivo, FIFO) -----

    /**
     * Consultar stock total disponible de grano por cultivo (empresa + cultivo).
     */
    BigDecimal consultarStockGrano(Long empresaId, Long cultivoId);

    /**
     * Consumir grano (FIFO). No permite stock negativo.
     */
    InventoryResult consumirGrano(
        Long empresaId,
        Long cultivoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Consumir grano permitiendo stock negativo (ej. consumo automático diario).
     */
    InventoryResult consumirGranoPermitiendoNegativo(
        Long empresaId,
        Long cultivoId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );

    /**
     * Verificar si hay stock suficiente de grano.
     */
    boolean hayStockSuficienteGrano(Long empresaId, Long cultivoId, BigDecimal cantidad);

    /**
     * Nombre del cultivo (para trazabilidad en movimientos).
     */
    Optional<String> consultarNombreCultivo(Long empresaId, Long cultivoId);

    /**
     * Consumir grano de un registro concreto de inventario (por ID).
     * Usado para venta desde un batch específico. No permite negativo.
     */
    InventoryResult consumirGranoPorInventarioId(
        Long empresaId,
        Long inventarioId,
        BigDecimal cantidad,
        InventoryOrigin origen,
        Long referenciaId,
        Long usuarioId
    );
}
