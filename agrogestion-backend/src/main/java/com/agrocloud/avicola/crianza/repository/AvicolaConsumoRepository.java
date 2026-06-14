package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Consumos por lote; toda consulta explícita filtra por empresa y lote.
 */
public interface AvicolaConsumoRepository extends JpaRepository<AvicolaConsumo, Long> {

    @Query("SELECT c FROM AvicolaConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId ORDER BY c.fecha DESC, c.id DESC")
    List<AvicolaConsumo> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT c FROM AvicolaConsumo c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<AvicolaConsumo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Suma de cantidades consumidas del lote (conversión alimenticia); siempre acotado a empresa.
     */
    @Query("SELECT COALESCE(SUM(c.cantidad), 0) FROM AvicolaConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId")
    BigDecimal sumarCantidadConsumidaPorLoteYEmpresa(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
