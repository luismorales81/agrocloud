package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Consumos de alimento por galpón. Solo consultas con {@code empresaId}.
 */
public interface AvicolaPonedorasConsumoRepository extends JpaRepository<AvicolaPonedorasConsumo, Long> {

    @Query("SELECT c FROM AvicolaPonedorasConsumo c WHERE c.galpon.id = :galponId AND c.empresaId = :empresaId ORDER BY c.fecha DESC, c.id DESC")
    List<AvicolaPonedorasConsumo> buscarPorGalponIdYEmpresaId(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);
}
