package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.MadreMuerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MadreMuerteRepository extends JpaRepository<MadreMuerte, Long> {

    List<MadreMuerte> findByMadreAndActivoTrue(Madre madre);

    List<MadreMuerte> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT m FROM MadreMuerte m WHERE m.empresa = :empresa AND m.activo = true AND m.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<MadreMuerte> findByEmpresaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT m FROM MadreMuerte m WHERE m.empresa = :empresa AND m.activo = true AND m.causa = :causa")
    List<MadreMuerte> findByEmpresaAndCausa(@Param("empresa") Empresa empresa, @Param("causa") MadreMuerte.CausaMuerteMadre causa);
}
