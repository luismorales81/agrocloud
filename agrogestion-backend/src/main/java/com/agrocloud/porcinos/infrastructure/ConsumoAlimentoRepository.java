package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.ConsumoAlimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConsumoAlimentoRepository extends JpaRepository<ConsumoAlimento, Long> {

    List<ConsumoAlimento> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT c FROM ConsumoAlimento c WHERE c.empresa = :empresa AND c.activo = true AND c.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<ConsumoAlimento> findByEmpresaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT SUM(c.cantidadKg) FROM ConsumoAlimento c WHERE c.empresa = :empresa AND c.activo = true AND c.categoria = :categoria AND c.fecha BETWEEN :fechaDesde AND :fechaHasta")
    java.math.BigDecimal sumCantidadByCategoriaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("categoria") ConsumoAlimento.CategoriaAlimento categoria,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );
}
