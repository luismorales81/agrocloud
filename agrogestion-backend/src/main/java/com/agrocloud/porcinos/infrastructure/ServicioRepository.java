package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByMadreAndActivoTrue(Madre madre);

    @Query("SELECT s FROM Servicio s WHERE s.madre IN :madres AND s.activo = true")
    List<Servicio> findByMadreInAndActivoTrue(@Param("madres") List<Madre> madres);

    @Query("SELECT s FROM Servicio s JOIN FETCH s.madre WHERE s.empresa = :empresa AND s.activo = true")
    List<Servicio> findByEmpresaAndActivoTrue(@Param("empresa") Empresa empresa);

    @Query("SELECT s FROM Servicio s JOIN FETCH s.madre WHERE s.empresa = :empresa AND s.activo = true AND s.estadoServicio = 'PENDIENTE_CONTROL'")
    List<Servicio> findPendientesControlByEmpresa(@Param("empresa") Empresa empresa);

    @Query("SELECT s FROM Servicio s WHERE s.madre = :madre AND s.activo = true AND s.estadoServicio = 'PENDIENTE_CONTROL'")
    Optional<Servicio> findPendienteControlByMadre(@Param("madre") Madre madre);

    Optional<Servicio> findByIdAndActivoTrue(Long id);

    @Query("SELECT s FROM Servicio s LEFT JOIN FETCH s.madre LEFT JOIN FETCH s.empresa WHERE s.id = :id")
    Optional<Servicio> findByIdWithRelations(@Param("id") Long id);
}
