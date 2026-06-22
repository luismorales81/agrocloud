package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotLecturaComedero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotLecturaComederoRepository extends JpaRepository<FeedlotLecturaComedero, Long> {

    @Query("SELECT l FROM FeedlotLecturaComedero l WHERE l.lote.id = :loteId AND l.empresaId = :empresaId "
            + "ORDER BY l.fecha DESC, l.id DESC")
    List<FeedlotLecturaComedero> listarPorLoteIdYEmpresaId(
            @Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT l FROM FeedlotLecturaComedero l WHERE l.id = :id AND l.lote.id = :loteId AND l.empresaId = :empresaId")
    Optional<FeedlotLecturaComedero> buscarPorIdYLoteIdYEmpresaId(
            @Param("id") Long id, @Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
