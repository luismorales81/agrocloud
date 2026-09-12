package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosMuerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosMuerteRepository extends JpaRepository<PorcinosMuerte, Long> {

    @Query("SELECT m FROM PorcinosMuerte m WHERE m.empresaId = :empresaId ORDER BY m.fecha DESC, m.id DESC")
    List<PorcinosMuerte> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT m FROM PorcinosMuerte m WHERE m.lote.id = :loteId AND m.empresaId = :empresaId ORDER BY m.fecha DESC, m.id DESC")
    List<PorcinosMuerte> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT m FROM PorcinosMuerte m WHERE m.id = :id AND m.empresaId = :empresaId")
    Optional<PorcinosMuerte> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(m.cabezas), 0) FROM PorcinosMuerte m WHERE m.lote.id = :loteId AND m.empresaId = :empresaId")
    Integer sumarCabezasPorLoteId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
