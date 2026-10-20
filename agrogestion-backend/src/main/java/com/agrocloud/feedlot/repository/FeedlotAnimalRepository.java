package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotAnimal;
import com.agrocloud.feedlot.model.enums.FeedlotEstadoAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotAnimalRepository extends JpaRepository<FeedlotAnimal, Long> {

    @Query("SELECT a FROM FeedlotAnimal a WHERE a.lote.id = :loteId AND a.empresaId = :empresaId ORDER BY a.caravanaVisual, a.eid")
    List<FeedlotAnimal> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT a FROM FeedlotAnimal a WHERE a.id = :id AND a.empresaId = :empresaId")
    Optional<FeedlotAnimal> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT a FROM FeedlotAnimal a WHERE a.empresaId = :empresaId AND a.eid = :eid")
    Optional<FeedlotAnimal> buscarPorEmpresaYEid(@Param("empresaId") Long empresaId, @Param("eid") String eid);

    @Query("SELECT a FROM FeedlotAnimal a WHERE a.empresaId = :empresaId AND LOWER(a.caravanaVisual) = LOWER(:caravana)")
    Optional<FeedlotAnimal> buscarPorEmpresaYCaravanaVisual(@Param("empresaId") Long empresaId, @Param("caravana") String caravana);

    @Query("SELECT COUNT(a) FROM FeedlotAnimal a WHERE a.lote.id = :loteId AND a.empresaId = :empresaId AND a.estado = :estado")
    long contarPorLoteEstado(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId, @Param("estado") FeedlotEstadoAnimal estado);

    @Query("SELECT a FROM FeedlotAnimal a WHERE a.lote.id = :loteId AND a.empresaId = :empresaId AND a.estado = :estado")
    List<FeedlotAnimal> listarPorLoteYEstado(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId, @Param("estado") FeedlotEstadoAnimal estado);

    @Query("SELECT a FROM FeedlotAnimal a JOIN FETCH a.lote WHERE a.empresaId = :empresaId AND a.estado = :estado AND a.eid IN :eids")
    List<FeedlotAnimal> listarActivosPorEids(
            @Param("empresaId") Long empresaId,
            @Param("estado") FeedlotEstadoAnimal estado,
            @Param("eids") List<String> eids);
}
