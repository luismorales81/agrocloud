package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotLoteRepository extends JpaRepository<FeedlotLote, Long> {

    @Query("SELECT l FROM FeedlotLote l WHERE l.empresaId = :empresaId ORDER BY l.fechaIngreso DESC, l.id DESC")
    List<FeedlotLote> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT l FROM FeedlotLote l WHERE l.id = :id AND l.empresaId = :empresaId")
    Optional<FeedlotLote> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT l FROM FeedlotLote l WHERE l.empresaId = :empresaId AND l.estado = :estado ORDER BY l.fechaIngreso DESC")
    List<FeedlotLote> listarPorEmpresaIdYEstado(@Param("empresaId") Long empresaId, @Param("estado") FeedlotLoteEstado estado);

    @Query("SELECT l FROM FeedlotLote l WHERE l.empresaId = :empresaId AND l.campanaId = :campanaId ORDER BY l.fechaIngreso DESC")
    List<FeedlotLote> listarPorEmpresaIdYCampanaId(@Param("empresaId") Long empresaId, @Param("campanaId") Long campanaId);

    @Query("SELECT l FROM FeedlotLote l WHERE l.empresaId = :empresaId AND l.corral.id = :corralId ORDER BY l.fechaIngreso DESC")
    List<FeedlotLote> listarPorEmpresaIdYCorralId(@Param("empresaId") Long empresaId, @Param("corralId") Long corralId);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM FeedlotLote l "
            + "WHERE l.corral.id = :corralId AND l.estado = :estado")
    boolean existsByCorralIdAndEstado(@Param("corralId") Long corralId, @Param("estado") FeedlotLoteEstado estado);

    @Query("SELECT COUNT(l) FROM FeedlotLote l WHERE l.empresaId = :empresaId AND l.estado = :estado")
    long contarPorEmpresaIdYEstado(@Param("empresaId") Long empresaId, @Param("estado") FeedlotLoteEstado estado);

    @Query("SELECT COALESCE(SUM(l.cabezasActuales), 0) FROM FeedlotLote l "
            + "WHERE l.empresaId = :empresaId AND l.estado = :estado AND l.campanaId = :campanaId")
    Long sumarCabezasActualesPorEmpresaCampanaYEstado(
            @Param("empresaId") Long empresaId,
            @Param("campanaId") Long campanaId,
            @Param("estado") FeedlotLoteEstado estado);
}
