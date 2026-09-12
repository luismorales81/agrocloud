package com.agrocloud.core.inventory.infrastructure;

import com.agrocloud.core.inventory.domain.MovimientoInventario;
import com.agrocloud.model.enums.TipoMovimiento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository("movimientoInventarioRepositoryInventario")
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByInsumoIdOrderByFechaMovimientoDesc(Long insumoId);

    List<MovimientoInventario> findByTipoMovimientoAndFechaMovimientoBetween(
        TipoMovimiento tipoMovimiento,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
    );

    @Query("SELECT m FROM MovimientoInventario m WHERE m.labor.id = :laborId")
    List<MovimientoInventario> findByLaborId(Long laborId);

    @Deprecated
    @Query("SELECT m FROM MovimientoInventario m WHERE m.labor.id = :referenciaTareaId")
    List<MovimientoInventario> findByReferenciaTareaId(Long referenciaTareaId);

    List<MovimientoInventario> findByInsumoId(Long insumoId);

    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipoMovimiento = 'ENTRADA' THEN m.cantidad ELSE -m.cantidad END), 0) FROM MovimientoInventario m WHERE m.insumo.id = :insumoId")
    BigDecimal calcularSaldoByInsumoId(Long insumoId);

    default List<MovimientoInventario> findByCultivosYLaborId(Long laborId) {
        return findByLaborId(laborId);
    }

    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.insumo "
            + "WHERE m.origen = :origen AND m.referenciaId IN :referenciaIds AND m.tipoMovimiento = 'SALIDA'")
    List<MovimientoInventario> listarSalidasPorOrigenYReferencias(
            @Param("origen") String origen,
            @Param("referenciaIds") List<Long> referenciaIds);

    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.insumo i "
            + "WHERE i.empresa.id = :empresaId AND i.activo = true "
            + "ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findRecientesPorEmpresa(@Param("empresaId") Long empresaId, Pageable pageable);
}
