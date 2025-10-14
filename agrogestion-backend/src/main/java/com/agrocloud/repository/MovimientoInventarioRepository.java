package com.agrocloud.repository;

import com.agrocloud.model.entity.MovimientoInventario;
import com.agrocloud.model.entity.MovimientoInventario.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para gestionar movimientos de inventario de INSUMOS.
 */
@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    // Buscar por insumo
    List<MovimientoInventario> findByInsumoId(Long insumoId);

    // Buscar por labor
    List<MovimientoInventario> findByLaborId(Long laborId);

    // Buscar por tipo de movimiento
    List<MovimientoInventario> findByTipoMovimiento(TipoMovimiento tipoMovimiento);

    // Query para calcular saldo por insumo
    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipoMovimiento = 'ENTRADA' THEN m.cantidad ELSE -m.cantidad END), 0) " +
           "FROM MovimientoInventario m WHERE m.insumo.id = :insumoId")
    BigDecimal calcularSaldoByInsumoId(@Param("insumoId") Long insumoId);

    // Buscar movimientos recientes
    List<MovimientoInventario> findTop50ByOrderByCreatedAtDesc();
}
