package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.DerramePerdida;
import com.agrocloud.porcinos.domain.DiaAlimentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DerramePerdidaRepository extends JpaRepository<DerramePerdida, Long> {

    List<DerramePerdida> findByEmpresaAndFechaBetweenOrderByFechaDesc(
        Empresa empresa, LocalDate fechaDesde, LocalDate fechaHasta);

    List<DerramePerdida> findByEmpresaAndTipoOrderByFechaDesc(
        Empresa empresa, DerramePerdida.TipoPerdida tipo);

    List<DerramePerdida> findByDiaAlimentacionOrderByFechaDesc(DiaAlimentacion diaAlimentacion);

    @Query("SELECT d FROM DerramePerdida d WHERE d.empresa = :empresa " +
           "AND d.tipoInsumo = :tipoInsumo " +
           "AND ((:tipoInsumo = 'INSUMO' AND d.insumo.id = :insumoId) OR " +
           "     (:tipoInsumo = 'GRANO_PROPIO' AND d.cultivoId = :insumoId) OR " +
           "     (:tipoInsumo = 'INSUMO_COMPUESTO' AND d.insumoCompuesto.id = :insumoId)) " +
           "ORDER BY d.fecha DESC")
    List<DerramePerdida> findByEmpresaAndInsumo(
        @Param("empresa") Empresa empresa,
        @Param("tipoInsumo") DerramePerdida.TipoInsumo tipoInsumo,
        @Param("insumoId") Long insumoId);
}
