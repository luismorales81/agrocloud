package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.MovimientoStockPorcino;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoStockPorcinoRepository extends JpaRepository<MovimientoStockPorcino, Long> {

    List<MovimientoStockPorcino> findByEmpresaAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
        Empresa empresa, LocalDate fechaDesde, LocalDate fechaHasta);

    List<MovimientoStockPorcino> findByEmpresaAndTipoMovimientoOrderByFechaMovimientoDesc(
        Empresa empresa, MovimientoStockPorcino.TipoMovimiento tipoMovimiento);

    @Query("SELECT m FROM MovimientoStockPorcino m WHERE m.empresa = :empresa " +
           "AND m.tipoInsumo = :tipoInsumo " +
           "AND ((:tipoInsumo = 'INSUMO' AND m.insumo.id = :insumoId) OR " +
           "     (:tipoInsumo = 'GRANO_PROPIO' AND m.cultivoId = :insumoId) OR " +
           "     (:tipoInsumo = 'INSUMO_COMPUESTO' AND m.insumoCompuesto.id = :insumoId)) " +
           "ORDER BY m.fechaMovimiento DESC")
    List<MovimientoStockPorcino> findByEmpresaAndInsumo(
        @Param("empresa") Empresa empresa,
        @Param("tipoInsumo") MovimientoStockPorcino.TipoInsumo tipoInsumo,
        @Param("insumoId") Long insumoId);

    @Query("SELECT m FROM MovimientoStockPorcino m WHERE m.empresa = :empresa " +
           "ORDER BY m.fechaMovimiento DESC, m.fechaCreacion DESC")
    List<MovimientoStockPorcino> findByEmpresaOrderByFechaMovimientoDescFechaCreacionDesc(
        @Param("empresa") Empresa empresa, Pageable pageable);
}
