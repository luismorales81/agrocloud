package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotCategoriaRepository extends JpaRepository<FeedlotCategoria, Long> {

    @Query("SELECT c FROM FeedlotCategoria c WHERE c.empresaId = :empresaId ORDER BY c.nombre")
    List<FeedlotCategoria> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM FeedlotCategoria c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<FeedlotCategoria> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
