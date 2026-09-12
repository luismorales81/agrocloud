package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosPesada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosPesadaRepository extends JpaRepository<PorcinosPesada, Long> {

    @Query("SELECT p FROM PorcinosPesada p WHERE p.empresaId = :empresaId ORDER BY p.fecha DESC, p.id DESC")
    List<PorcinosPesada> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM PorcinosPesada p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId ORDER BY p.fecha DESC, p.id DESC")
    List<PorcinosPesada> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT p FROM PorcinosPesada p WHERE p.id = :id AND p.empresaId = :empresaId")
    Optional<PorcinosPesada> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
