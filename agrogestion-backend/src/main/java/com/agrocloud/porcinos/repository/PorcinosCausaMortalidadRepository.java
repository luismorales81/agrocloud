package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosCausaMortalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosCausaMortalidadRepository extends JpaRepository<PorcinosCausaMortalidad, Long> {

    @Query("SELECT c FROM PorcinosCausaMortalidad c WHERE c.empresaId = :empresaId ORDER BY c.nombre")
    List<PorcinosCausaMortalidad> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM PorcinosCausaMortalidad c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<PorcinosCausaMortalidad> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
