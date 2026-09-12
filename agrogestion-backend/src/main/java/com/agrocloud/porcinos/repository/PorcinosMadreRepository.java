package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosMadre;
import com.agrocloud.porcinos.model.enums.PorcinosMadreEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosMadreRepository extends JpaRepository<PorcinosMadre, Long> {

    @Query("SELECT m FROM PorcinosMadre m WHERE m.empresaId = :empresaId ORDER BY m.caravana")
    List<PorcinosMadre> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT m FROM PorcinosMadre m WHERE m.id = :id AND m.empresaId = :empresaId")
    Optional<PorcinosMadre> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT m FROM PorcinosMadre m WHERE m.empresaId = :empresaId AND m.estado = :estado ORDER BY m.caravana")
    List<PorcinosMadre> listarPorEmpresaIdYEstado(@Param("empresaId") Long empresaId, @Param("estado") PorcinosMadreEstado estado);
}
