package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.porcinos.domain.Parto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesteteRepository extends JpaRepository<Destete, Long> {

    @Query("SELECT d FROM Destete d LEFT JOIN FETCH d.parto LEFT JOIN FETCH d.empresa WHERE d.parto = :parto AND d.activo = true")
    Optional<Destete> findByPartoAndActivoTrue(@Param("parto") Parto parto);

    @Query("SELECT d FROM Destete d LEFT JOIN FETCH d.parto LEFT JOIN FETCH d.empresa WHERE d.empresa = :empresa AND d.activo = true")
    List<Destete> findByEmpresaAndActivoTrue(@Param("empresa") Empresa empresa);

    @Query("SELECT d FROM Destete d LEFT JOIN FETCH d.parto LEFT JOIN FETCH d.empresa WHERE d.id = :id")
    Optional<Destete> findByIdWithRelations(@Param("id") Long id);
}
