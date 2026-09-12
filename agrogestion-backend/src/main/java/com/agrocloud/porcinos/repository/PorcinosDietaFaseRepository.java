package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosDietaFase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PorcinosDietaFaseRepository extends JpaRepository<PorcinosDietaFase, Long> {

    @Query("SELECT f FROM PorcinosDietaFase f WHERE f.dieta.id = :dietaId ORDER BY f.diasDesdeIngreso ASC")
    List<PorcinosDietaFase> listarPorDietaId(@Param("dietaId") Long dietaId);

    @Query("SELECT f FROM PorcinosDietaFase f WHERE f.id = :id AND f.dieta.id = :dietaId AND f.dieta.empresaId = :empresaId")
    java.util.Optional<PorcinosDietaFase> buscarPorIdYDietaIdYEmpresaId(
            @Param("id") Long id,
            @Param("dietaId") Long dietaId,
            @Param("empresaId") Long empresaId);
}
