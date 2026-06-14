package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasDescarteAves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Descartes de aves. Solo consultas con {@code empresaId}.
 */
public interface AvicolaPonedorasDescarteAvesRepository extends JpaRepository<AvicolaPonedorasDescarteAves, Long> {

    @Query("SELECT d FROM AvicolaPonedorasDescarteAves d WHERE d.galpon.id = :galponId AND d.empresaId = :empresaId ORDER BY d.fecha DESC, d.id DESC")
    List<AvicolaPonedorasDescarteAves> buscarPorGalponIdYEmpresaId(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);
}
