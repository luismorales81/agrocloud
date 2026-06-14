package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.RegistroPeso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegistroPesoRepository extends JpaRepository<RegistroPeso, Long> {

    List<RegistroPeso> findByRecriaOrderByFechaPesajeDesc(Recria recria);

    List<RegistroPeso> findByEmpresa(Empresa empresa);

    @Query("SELECT rp FROM RegistroPeso rp WHERE rp.recria = :recria AND rp.fechaPesaje BETWEEN :fechaDesde AND :fechaHasta ORDER BY rp.fechaPesaje DESC")
    List<RegistroPeso> findByRecriaAndRangoFechas(
        @Param("recria") Recria recria,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );
}
