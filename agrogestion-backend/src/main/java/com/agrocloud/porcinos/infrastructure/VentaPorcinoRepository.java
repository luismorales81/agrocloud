package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.VentaPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentaPorcinoRepository extends JpaRepository<VentaPorcino, Long> {

    List<VentaPorcino> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT v FROM VentaPorcino v WHERE v.empresa = :empresa AND v.activo = true AND v.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<VentaPorcino> findByEmpresaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT SUM(v.ingresoTotal) FROM VentaPorcino v WHERE v.empresa = :empresa AND v.activo = true AND v.fecha BETWEEN :fechaDesde AND :fechaHasta")
    java.math.BigDecimal sumIngresosByRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );
}
