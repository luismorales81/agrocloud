package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosPadrillo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosPadrilloRepository extends JpaRepository<PorcinosPadrillo, Long> {

    @Query("SELECT p FROM PorcinosPadrillo p WHERE p.empresaId = :empresaId ORDER BY p.nombre")
    List<PorcinosPadrillo> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM PorcinosPadrillo p WHERE p.id = :id AND p.empresaId = :empresaId")
    Optional<PorcinosPadrillo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
