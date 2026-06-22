package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotPesada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotPesadaRepository extends JpaRepository<FeedlotPesada, Long> {

    @Query("SELECT p FROM FeedlotPesada p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId ORDER BY p.fecha DESC, p.id DESC")
    List<FeedlotPesada> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT p FROM FeedlotPesada p WHERE p.id = :id AND p.empresaId = :empresaId")
    Optional<FeedlotPesada> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
