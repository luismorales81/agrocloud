package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotCorral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotCorralRepository extends JpaRepository<FeedlotCorral, Long> {

    @Query("SELECT c FROM FeedlotCorral c WHERE c.establecimiento.id = :establecimientoId ORDER BY c.nombre")
    List<FeedlotCorral> listarPorEstablecimientoId(@Param("establecimientoId") Long establecimientoId);

    @Query("SELECT c FROM FeedlotCorral c WHERE c.id = :id AND c.establecimiento.id = :establecimientoId")
    Optional<FeedlotCorral> buscarPorIdYEstablecimientoId(@Param("id") Long id, @Param("establecimientoId") Long establecimientoId);

    @Query("SELECT c FROM FeedlotCorral c WHERE c.id = :id AND c.establecimiento.empresaId = :empresaId")
    Optional<FeedlotCorral> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
