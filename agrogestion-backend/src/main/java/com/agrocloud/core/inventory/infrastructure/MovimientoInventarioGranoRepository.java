package com.agrocloud.core.inventory.infrastructure;

import com.agrocloud.core.inventory.domain.MovimientoInventarioGrano;
import com.agrocloud.core.inventory.domain.MovimientoInventarioGrano.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository("movimientoInventarioGranoRepositoryInventario")
public interface MovimientoInventarioGranoRepository extends JpaRepository<MovimientoInventarioGrano, Long> {

    List<MovimientoInventarioGrano> findByInventarioIdOrderByFechaMovimientoDesc(Long inventarioId);

    List<MovimientoInventarioGrano> findByUsuarioIdOrderByFechaMovimientoDesc(Long usuarioId);

    List<MovimientoInventarioGrano> findByTipoMovimientoOrderByFechaMovimientoDesc(TipoMovimiento tipoMovimiento);

    List<MovimientoInventarioGrano> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
        LocalDate fechaInicio, LocalDate fechaFin);

    List<MovimientoInventarioGrano> findByReferenciaTipoAndReferenciaId(String referenciaTipo, Long referenciaId);

    @Query("SELECT m FROM MovimientoInventarioGrano m " +
           "WHERE m.usuario.id = :usuarioId " +
           "ORDER BY m.createdAt DESC")
    List<MovimientoInventarioGrano> findMovimientosRecientesByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT SUM(m.montoTotal) " +
           "FROM MovimientoInventarioGrano m " +
           "WHERE m.usuario.id = :usuarioId " +
           "AND m.tipoMovimiento = 'SALIDA_VENTA' " +
           "AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalVentasPorPeriodo(
        @Param("usuarioId") Long usuarioId,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin);

    long countByTipoMovimientoAndUsuarioId(TipoMovimiento tipoMovimiento, Long usuarioId);
}
