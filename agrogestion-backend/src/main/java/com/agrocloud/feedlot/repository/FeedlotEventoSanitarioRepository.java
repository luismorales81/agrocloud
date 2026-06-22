package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotEventoSanitarioRepository extends JpaRepository<FeedlotEventoSanitario, Long> {

    @Query("SELECT e FROM FeedlotEventoSanitario e WHERE e.lote.id = :loteId AND e.empresaId = :empresaId ORDER BY e.fecha DESC, e.id DESC")
    List<FeedlotEventoSanitario> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT e FROM FeedlotEventoSanitario e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<FeedlotEventoSanitario> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
