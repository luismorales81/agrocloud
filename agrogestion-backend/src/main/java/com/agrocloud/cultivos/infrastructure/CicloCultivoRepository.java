package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.cultivos.domain.CicloCultivo;
import com.agrocloud.cultivos.domain.EstadoCicloCultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("cicloCultivoRepositoryCultivos")
public interface CicloCultivoRepository extends JpaRepository<CicloCultivo, Long> {

    List<CicloCultivo> findByCampanaIdOrderByFechaSiembraDesc(Long campanaId);

    List<CicloCultivo> findByLoteIdOrderByFechaSiembraDesc(Long loteId);

    @Query("SELECT c FROM CicloCultivo c WHERE c.lote.id = :loteId AND c.estado IN :estados")
    Optional<CicloCultivo> findCicloAbiertoPorLote(
            @Param("loteId") Long loteId,
            @Param("estados") List<EstadoCicloCultivo> estados);

    default Optional<CicloCultivo> findCicloActivoPorLote(Long loteId) {
        return findCicloAbiertoPorLote(loteId, List.of(EstadoCicloCultivo.PLANIFICADO, EstadoCicloCultivo.EN_CULTIVO));
    }

    List<CicloCultivo> findByCampanaIdAndLoteId(Long campanaId, Long loteId);

    @Query("SELECT DISTINCT c.lote.id FROM CicloCultivo c WHERE c.campanaId = :campanaId")
    List<Long> findDistinctLoteIdsByCampanaId(@Param("campanaId") Long campanaId);
}
