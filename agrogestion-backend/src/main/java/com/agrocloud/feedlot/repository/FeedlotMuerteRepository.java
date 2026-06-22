package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotMuerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotMuerteRepository extends JpaRepository<FeedlotMuerte, Long> {

    @Query("SELECT m FROM FeedlotMuerte m WHERE m.lote.id = :loteId AND m.empresaId = :empresaId ORDER BY m.fecha DESC, m.id DESC")
    List<FeedlotMuerte> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT m FROM FeedlotMuerte m WHERE m.id = :id AND m.empresaId = :empresaId")
    Optional<FeedlotMuerte> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(m.cabezas), 0) FROM FeedlotMuerte m WHERE m.lote.id = :loteId AND m.empresaId = :empresaId")
    Integer sumarCabezasPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
