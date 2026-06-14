package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GestacionRepository extends JpaRepository<Gestacion, Long> {

    List<Gestacion> findByMadreAndActivoTrue(Madre madre);

    List<Gestacion> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT g FROM Gestacion g JOIN FETCH g.madre WHERE g.empresa = :empresa AND g.activo = true AND g.estado = 'EN_CURSO'")
    List<Gestacion> findByEmpresaAndActivas(@Param("empresa") Empresa empresa);

    @Query("SELECT g FROM Gestacion g WHERE g.empresa = :empresa AND g.activo = true AND g.estado = 'EN_CURSO' AND g.fechaProbableParto BETWEEN :fechaDesde AND :fechaHasta")
    List<Gestacion> findProximasPartos(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("SELECT g FROM Gestacion g LEFT JOIN FETCH g.empresa LEFT JOIN FETCH g.madre LEFT JOIN FETCH g.servicio WHERE g.id = :id AND g.activo = true")
    Optional<Gestacion> findByIdAndActivoTrue(@Param("id") Long id);

    @Query("SELECT g FROM Gestacion g WHERE g.madre = :madre AND g.activo = true AND g.estado = 'EN_CURSO'")
    Optional<Gestacion> findActivaByMadre(@Param("madre") Madre madre);

    @Query("SELECT g FROM Gestacion g WHERE g.madre IN :madres AND g.activo = true AND g.estado = 'EN_CURSO'")
    List<Gestacion> findActivasByMadreIn(@Param("madres") List<Madre> madres);

    @Query("SELECT g FROM Gestacion g LEFT JOIN FETCH g.servicio WHERE g.servicio = :servicio AND g.activo = true")
    Optional<Gestacion> findByServicioAndActivoTrue(@Param("servicio") Servicio servicio);
}
