package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.MuerteLechon;
import com.agrocloud.porcinos.domain.Parto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MuerteLechonRepository extends JpaRepository<MuerteLechon, Long> {

    List<MuerteLechon> findByPartoAndActivoTrue(Parto parto);

    List<MuerteLechon> findByMadreAndActivoTrue(Madre madre);

    List<MuerteLechon> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT m FROM MuerteLechon m WHERE m.empresa = :empresa AND m.activo = true AND m.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<MuerteLechon> findByEmpresaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT SUM(m.cantidad) FROM MuerteLechon m WHERE m.parto = :parto AND m.activo = true")
    Integer sumCantidadByParto(@Param("parto") Parto parto);
}
