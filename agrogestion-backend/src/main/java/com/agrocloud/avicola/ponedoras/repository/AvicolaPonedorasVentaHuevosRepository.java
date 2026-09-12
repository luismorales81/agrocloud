package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasVentaHuevos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Ventas de huevos. Solo consultas con {@code empresaId}.
 */
public interface AvicolaPonedorasVentaHuevosRepository extends JpaRepository<AvicolaPonedorasVentaHuevos, Long> {

    @Query("SELECT v FROM AvicolaPonedorasVentaHuevos v WHERE v.galpon.id = :galponId AND v.empresaId = :empresaId ORDER BY v.fecha DESC, v.id DESC")
    List<AvicolaPonedorasVentaHuevos> buscarPorGalponIdYEmpresaId(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);

    @Query("SELECT v FROM AvicolaPonedorasVentaHuevos v WHERE v.id = :id AND v.empresaId = :empresaId")
    Optional<AvicolaPonedorasVentaHuevos> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("""
            SELECT COALESCE(SUM(v.total), 0)
            FROM AvicolaPonedorasVentaHuevos v
            WHERE v.galpon.id = :galponId
              AND v.empresaId = :empresaId
            """)
    BigDecimal sumarIngresosPorGalpon(@Param("galponId") Long galponId, @Param("empresaId") Long empresaId);
}
