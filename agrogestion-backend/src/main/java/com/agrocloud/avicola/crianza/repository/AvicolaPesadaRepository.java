package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaPesada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Pesadas por lote; toda consulta explícita filtra por empresa y lote.
 */
public interface AvicolaPesadaRepository extends JpaRepository<AvicolaPesada, Long> {

    @Query("SELECT p FROM AvicolaPesada p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId ORDER BY p.fecha DESC, p.id DESC")
    List<AvicolaPesada> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT p FROM AvicolaPesada p WHERE p.id = :id AND p.empresaId = :empresaId")
    Optional<AvicolaPesada> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
