package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasMuerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Mortalidad en galpones. Solo listados con {@code empresaId}.
 */
public interface AvicolaPonedorasMuerteRepository extends JpaRepository<AvicolaPonedorasMuerte, Long> {

    @Query("SELECT m FROM AvicolaPonedorasMuerte m WHERE m.galpon.id = :galponId AND m.empresaId = :empresaId ORDER BY m.fecha DESC, m.id DESC")
    List<AvicolaPonedorasMuerte> buscarPorGalponIdYEmpresaId(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);

    @Query("""
            SELECT COALESCE(SUM(m.cantidad), 0)
            FROM AvicolaPonedorasMuerte m
            WHERE m.galpon.id = :galponId AND m.empresaId = :empresaId
            """)
    long sumarCantidadMuertesPorGalponYEmpresaId(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);
}
