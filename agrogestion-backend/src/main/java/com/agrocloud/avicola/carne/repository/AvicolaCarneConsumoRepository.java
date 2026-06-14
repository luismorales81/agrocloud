package com.agrocloud.avicola.carne.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Consumos de insumo avícola-carne. Métodos declarados exigen {@code empresaId}. Evitar {@code findAll()} sin filtro.
 */
public interface AvicolaCarneConsumoRepository extends JpaRepository<AvicolaConsumo, Long> {

    @Query(value = """
            SELECT * FROM avicola_consumo
            WHERE lote_id = :loteId AND empresa_id = :empresaId
            ORDER BY fecha DESC, id DESC
            """, nativeQuery = true)
    List<AvicolaConsumo> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM avicola_consumo
            WHERE id = :id AND empresa_id = :empresaId
            LIMIT 1
            """, nativeQuery = true)
    Optional<AvicolaConsumo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Suma de cantidades consumidas del lote (unidad del insumo; p. ej. conversión alimenticia).
     */
    @Query(value = """
            SELECT COALESCE(SUM(cantidad), 0) FROM avicola_consumo
            WHERE lote_id = :loteId AND empresa_id = :empresaId
            """, nativeQuery = true)
    BigDecimal sumarCantidadConsumidaTotalPorLoteIdYEmpresaId(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId);
}
