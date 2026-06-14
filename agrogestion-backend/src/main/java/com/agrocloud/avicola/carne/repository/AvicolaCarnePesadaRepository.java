package com.agrocloud.avicola.carne.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaPesada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Pesadas avícola-carne. Métodos declarados exigen {@code empresaId}. Evitar {@code findAll()} sin filtro.
 */
public interface AvicolaCarnePesadaRepository extends JpaRepository<AvicolaPesada, Long> {

    @Query(value = """
            SELECT * FROM avicola_pesada
            WHERE lote_id = :loteId AND empresa_id = :empresaId
            ORDER BY fecha DESC, id DESC
            """, nativeQuery = true)
    List<AvicolaPesada> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM avicola_pesada
            WHERE id = :id AND empresa_id = :empresaId
            LIMIT 1
            """, nativeQuery = true)
    Optional<AvicolaPesada> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
