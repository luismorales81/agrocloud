package com.agrocloud.avicola.carne.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaMuerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Mortalidad avícola-carne. Métodos declarados exigen {@code empresaId}. Evitar {@code findAll()} sin filtro.
 */
public interface AvicolaCarneMuerteRepository extends JpaRepository<AvicolaMuerte, Long> {

    @Query(value = """
            SELECT * FROM avicola_muerte
            WHERE lote_id = :loteId AND empresa_id = :empresaId
            ORDER BY fecha DESC, id DESC
            """, nativeQuery = true)
    List<AvicolaMuerte> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM avicola_muerte
            WHERE id = :id AND empresa_id = :empresaId
            LIMIT 1
            """, nativeQuery = true)
    Optional<AvicolaMuerte> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Suma de muertes del lote en la empresa (p. ej. mortalidad % sobre stock).
     */
    @Query(value = """
            SELECT COALESCE(SUM(cantidad), 0) FROM avicola_muerte
            WHERE lote_id = :loteId AND empresa_id = :empresaId
            """, nativeQuery = true)
    BigDecimal sumarCantidadMuertesTotalPorLoteIdYEmpresaId(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId);
}
