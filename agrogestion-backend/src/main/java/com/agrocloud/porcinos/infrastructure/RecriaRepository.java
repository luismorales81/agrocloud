package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Recria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecriaRepository extends JpaRepository<Recria, Long> {

    List<Recria> findByLoteIdAndActivoTrue(Long loteId);

    List<Recria> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT r FROM Recria r WHERE r.empresa = :empresa AND r.activo = true AND r.fechaSalida IS NULL ORDER BY r.fechaIngreso DESC")
    List<Recria> findByEmpresaAndActivas(@Param("empresa") Empresa empresa);

    @Query("SELECT r FROM Recria r WHERE r.empresa = :empresa AND r.activo = true ORDER BY r.fechaIngreso DESC")
    List<Recria> findByEmpresaTodasActivas(@Param("empresa") Empresa empresa);

    Optional<Recria> findByIdAndActivoTrue(Long id);

    Optional<Recria> findByIdAndEmpresa(Long id, Empresa empresa);
}
