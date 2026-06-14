package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Faena;
import com.agrocloud.porcinos.domain.Recria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@SuppressWarnings("deprecation") // Faena deprecado: unificado con VentaPorcino; se mantiene por datos históricos
public interface FaenaRepository extends JpaRepository<Faena, Long> {

    List<Faena> findByRecria(Recria recria);

    List<Faena> findByEmpresa(Empresa empresa);

    @Query("SELECT f FROM Faena f WHERE f.empresa = :empresa AND f.fechaEnvio BETWEEN :fechaDesde AND :fechaHasta")
    List<Faena> findByEmpresaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT SUM(f.ingresoTotal) FROM Faena f WHERE f.empresa = :empresa AND f.fechaEnvio BETWEEN :fechaDesde AND :fechaHasta")
    java.math.BigDecimal sumIngresosByRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );
}
