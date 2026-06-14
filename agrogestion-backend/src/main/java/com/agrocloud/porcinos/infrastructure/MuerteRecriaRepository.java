package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.MuerteRecria;
import com.agrocloud.porcinos.domain.Recria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MuerteRecriaRepository extends JpaRepository<MuerteRecria, Long> {

    List<MuerteRecria> findByRecriaAndActivoTrue(Recria recria);

    List<MuerteRecria> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT m FROM MuerteRecria m WHERE m.empresa = :empresa AND m.activo = true AND m.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<MuerteRecria> findByEmpresaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT SUM(m.cantidad) FROM MuerteRecria m WHERE m.recria = :recria AND m.activo = true")
    Integer sumCantidadByRecria(@Param("recria") Recria recria);
}
