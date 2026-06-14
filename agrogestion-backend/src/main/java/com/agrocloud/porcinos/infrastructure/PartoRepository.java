package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartoRepository extends JpaRepository<Parto, Long> {

    @Query("SELECT p FROM Parto p LEFT JOIN FETCH p.madre LEFT JOIN FETCH p.empresa LEFT JOIN FETCH p.tipoParto WHERE p.madre = :madre AND p.activo = true")
    List<Parto> findByMadreAndActivoTrue(@Param("madre") Madre madre);

    @Query("SELECT p FROM Parto p LEFT JOIN FETCH p.madre LEFT JOIN FETCH p.empresa LEFT JOIN FETCH p.tipoParto WHERE p.empresa = :empresa AND p.activo = true")
    List<Parto> findByEmpresaAndActivoTrue(@Param("empresa") Empresa empresa);

    @Query("SELECT p FROM Parto p WHERE p.empresa = :empresa AND p.activo = true AND p.fechaInicio BETWEEN :fechaDesde AND :fechaHasta")
    List<Parto> findByEmpresaAndRangoFechas(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDateTime fechaDesde,
        @Param("fechaHasta") LocalDateTime fechaHasta
    );

    @Query("SELECT p FROM Parto p LEFT JOIN FETCH p.madre LEFT JOIN FETCH p.empresa LEFT JOIN FETCH p.tipoParto WHERE p.id = :id AND p.activo = true")
    Optional<Parto> findByIdAndActivoTrue(@Param("id") Long id);
}
