package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosDieta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosDietaRepository extends JpaRepository<PorcinosDieta, Long> {

    @Query("SELECT d FROM PorcinosDieta d LEFT JOIN FETCH d.fases WHERE d.empresaId = :empresaId ORDER BY d.nombre")
    List<PorcinosDieta> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT d FROM PorcinosDieta d LEFT JOIN FETCH d.fases WHERE d.id = :id AND d.empresaId = :empresaId")
    Optional<PorcinosDieta> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
