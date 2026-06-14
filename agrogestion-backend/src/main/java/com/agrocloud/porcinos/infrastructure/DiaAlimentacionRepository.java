package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.DiaAlimentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaAlimentacionRepository extends JpaRepository<DiaAlimentacion, Long> {

    Optional<DiaAlimentacion> findByEmpresaAndFecha(Empresa empresa, LocalDate fecha);

    List<DiaAlimentacion> findByEmpresaAndFechaBetweenOrderByFechaDesc(
        Empresa empresa, LocalDate fechaDesde, LocalDate fechaHasta);

    List<DiaAlimentacion> findByEmpresaAndEstadoOrderByFechaDesc(
        Empresa empresa, DiaAlimentacion.EstadoDia estado);

    @Query("SELECT COUNT(d) FROM DiaAlimentacion d WHERE d.empresa = :empresa " +
           "AND d.fecha BETWEEN :fechaDesde AND :fechaHasta AND d.estado = :estado")
    Long countByEmpresaAndFechaBetweenAndEstado(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta,
        @Param("estado") DiaAlimentacion.EstadoDia estado);

    List<DiaAlimentacion> findByEmpresaAndTieneAlertasStockInsuficienteTrueOrderByFechaDesc(Empresa empresa);
}
