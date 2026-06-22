package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotDieta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotDietaRepository extends JpaRepository<FeedlotDieta, Long> {

    @Query("SELECT d FROM FeedlotDieta d LEFT JOIN FETCH d.fases WHERE d.empresaId = :empresaId ORDER BY d.nombre")
    List<FeedlotDieta> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT d FROM FeedlotDieta d LEFT JOIN FETCH d.fases WHERE d.id = :id AND d.empresaId = :empresaId")
    Optional<FeedlotDieta> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
