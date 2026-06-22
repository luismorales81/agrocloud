package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotEstablecimientoRepository extends JpaRepository<FeedlotEstablecimiento, Long> {

    @Query("SELECT e FROM FeedlotEstablecimiento e WHERE e.empresaId = :empresaId ORDER BY e.nombre")
    List<FeedlotEstablecimiento> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM FeedlotEstablecimiento e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<FeedlotEstablecimiento> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
