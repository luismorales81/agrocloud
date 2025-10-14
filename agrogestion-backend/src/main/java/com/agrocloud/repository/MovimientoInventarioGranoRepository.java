package com.agrocloud.repository;

import com.agrocloud.model.entity.MovimientoInventarioGrano;
import com.agrocloud.model.entity.MovimientoInventarioGrano.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para gestionar movimientos de inventario de GRANOS.
 */
@Repository
public interface MovimientoInventarioGranoRepository extends JpaRepository<MovimientoInventarioGrano, Long> {

    // Buscar por inventario
    List<MovimientoInventarioGrano> findByInventarioIdOrderByFechaMovimientoDesc(Long inventarioId);

    // Buscar por usuario
    List<MovimientoInventarioGrano> findByUsuarioIdOrderByFechaMovimientoDesc(Long usuarioId);

    // Buscar por tipo de movimiento
    List<MovimientoInventarioGrano> findByTipoMovimientoOrderByFechaMovimientoDesc(TipoMovimiento tipoMovimiento);

    // Buscar por rango de fechas
    List<MovimientoInventarioGrano> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
        LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar por referencia
    List<MovimientoInventarioGrano> findByReferenciaTipoAndReferenciaId(String referenciaTipo, Long referenciaId);

    // Query para movimientos recientes de un usuario
    @Query("SELECT m FROM MovimientoInventarioGrano m " +
           "WHERE m.usuario.id = :usuarioId " +
           "ORDER BY m.createdAt DESC")
    List<MovimientoInventarioGrano> findMovimientosRecientesByUsuario(@Param("usuarioId") Long usuarioId);

    // Query para calcular total de ventas por período
    @Query("SELECT SUM(m.montoTotal) " +
           "FROM MovimientoInventarioGrano m " +
           "WHERE m.usuario.id = :usuarioId " +
           "AND m.tipoMovimiento = 'SALIDA_VENTA' " +
           "AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalVentasPorPeriodo(
        @Param("usuarioId") Long usuarioId,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin);

    // Contar movimientos por tipo
    long countByTipoMovimientoAndUsuarioId(TipoMovimiento tipoMovimiento, Long usuarioId);
}
