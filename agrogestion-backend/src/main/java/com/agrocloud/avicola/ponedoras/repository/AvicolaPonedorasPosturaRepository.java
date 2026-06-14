package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasPostura;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasHuevoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Postura de huevos. Todas las consultas filtran por {@code empresaId}.
 */
public interface AvicolaPonedorasPosturaRepository extends JpaRepository<AvicolaPonedorasPostura, Long> {

    @Query("SELECT p FROM AvicolaPonedorasPostura p WHERE p.galpon.id = :galponId AND p.empresaId = :empresaId ORDER BY p.fecha DESC, p.id DESC")
    List<AvicolaPonedorasPostura> buscarPorGalponIdYEmpresaId(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);

    @Query("""
            SELECT COALESCE(SUM(p.cantidad), 0)
            FROM AvicolaPonedorasPostura p
            WHERE p.galpon.id = :galponId
              AND p.empresaId = :empresaId
              AND p.categoriaHuevo = :categoria
            """)
    long sumarCantidadHuevosPorGalponYCategoria(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId,
            @Param("categoria") AvicolaPonedorasHuevoCategoria categoria);

    @Query("""
            SELECT COALESCE(SUM(p.cantidad), 0)
            FROM AvicolaPonedorasPostura p
            WHERE p.galpon.id = :galponId
              AND p.empresaId = :empresaId
              AND p.fecha BETWEEN :fechaDesde AND :fechaHasta
            """)
    long sumarCantidadHuevosPorGalponYRangoFechas(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta);

    @Query("""
            SELECT COALESCE(SUM(p.cantidad), 0)
            FROM AvicolaPonedorasPostura p
            WHERE p.galpon.id = :galponId AND p.empresaId = :empresaId
            """)
    long sumarTotalHuevosPorGalponYEmpresaId(@Param("galponId") Long galponId, @Param("empresaId") Long empresaId);

    @Query("""
            SELECT p.categoriaHuevo, SUM(p.cantidad)
            FROM AvicolaPonedorasPostura p
            WHERE p.galpon.id = :galponId AND p.empresaId = :empresaId
            GROUP BY p.categoriaHuevo
            """)
    List<Object[]> sumarHuevosAgrupadosPorCategoria(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);
}
