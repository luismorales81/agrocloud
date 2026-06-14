package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaMuerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Muertes por lote; toda consulta explícita filtra por empresa y lote.
 */
public interface AvicolaMuerteRepository extends JpaRepository<AvicolaMuerte, Long> {

    @Query("SELECT m FROM AvicolaMuerte m WHERE m.lote.id = :loteId AND m.empresaId = :empresaId ORDER BY m.fecha DESC, m.id DESC")
    List<AvicolaMuerte> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT m FROM AvicolaMuerte m WHERE m.id = :id AND m.empresaId = :empresaId")
    Optional<AvicolaMuerte> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Suma de aves muertas del lote (mortalidad acumulada); siempre acotado a empresa.
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM AvicolaMuerte m WHERE m.lote.id = :loteId AND m.empresaId = :empresaId")
    Long sumarCantidadMuertesPorLoteYEmpresa(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
