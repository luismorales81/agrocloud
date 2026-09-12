package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosEstablecimientoRepository extends JpaRepository<PorcinosEstablecimiento, Long> {

    @Query("SELECT e FROM PorcinosEstablecimiento e WHERE e.empresaId = :empresaId ORDER BY e.nombre")
    List<PorcinosEstablecimiento> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM PorcinosEstablecimiento e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<PorcinosEstablecimiento> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
