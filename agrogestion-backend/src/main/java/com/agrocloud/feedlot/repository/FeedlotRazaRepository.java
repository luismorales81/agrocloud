package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotRaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotRazaRepository extends JpaRepository<FeedlotRaza, Long> {

    @Query("SELECT r FROM FeedlotRaza r WHERE r.empresaId = :empresaId ORDER BY r.nombre")
    List<FeedlotRaza> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT r FROM FeedlotRaza r WHERE r.id = :id AND r.empresaId = :empresaId")
    Optional<FeedlotRaza> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
