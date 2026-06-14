package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Madre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MadreRepository extends JpaRepository<Madre, Long> {

    List<Madre> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<Madre> findByIdentificacionAndActivoTrue(String identificacion);

    Optional<Madre> findByIdentificacionAndEmpresaAndActivoTrue(String identificacion, Empresa empresa);

    List<Madre> findByEmpresaAndEstadoActualAndActivoTrue(Empresa empresa, Madre.EstadoMadre estado);

    @Query("SELECT m FROM Madre m WHERE m.empresa = :empresa AND m.activo = true AND (:estado IS NULL OR m.estadoActual = :estado)")
    List<Madre> findByEmpresaAndEstadoOptional(@Param("empresa") Empresa empresa, @Param("estado") Madre.EstadoMadre estado);

    Optional<Madre> findByIdAndActivoTrue(Long id);

    Optional<Madre> findByIdAndEmpresa(Long id, Empresa empresa);
}
