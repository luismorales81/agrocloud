package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotSesionMangaRepository extends JpaRepository<FeedlotSesionManga, Long> {

    @Query("SELECT s FROM FeedlotSesionManga s WHERE s.id = :id AND s.empresaId = :empresaId")
    Optional<FeedlotSesionManga> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT s FROM FeedlotSesionManga s WHERE s.lote.id = :loteId AND s.empresaId = :empresaId ORDER BY s.fecha DESC, s.id DESC")
    List<FeedlotSesionManga> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
