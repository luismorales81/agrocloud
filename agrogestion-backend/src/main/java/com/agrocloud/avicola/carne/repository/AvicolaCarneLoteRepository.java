package com.agrocloud.avicola.carne.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de lotes avícola-carne. Todos los métodos declarados filtran por {@code empresaId}.
 * No usar {@link JpaRepository#findAll()} ni {@link JpaRepository#findById(java.io.Serializable)} sin comprobar empresa en servicio.
 */
public interface AvicolaCarneLoteRepository extends JpaRepository<AvicolaLote, Long> {

    @Query(value = """
            SELECT * FROM avicola_lote
            WHERE empresa_id = :empresaId
            ORDER BY fecha_ingreso DESC, id DESC
            """, nativeQuery = true)
    List<AvicolaLote> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM avicola_lote
            WHERE id = :id AND empresa_id = :empresaId
            LIMIT 1
            """, nativeQuery = true)
    Optional<AvicolaLote> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM avicola_lote
            WHERE empresa_id = :empresaId AND estado = :estado
            ORDER BY fecha_ingreso DESC, id DESC
            """, nativeQuery = true)
    List<AvicolaLote> listarPorEmpresaIdYEstado(
            @Param("empresaId") Long empresaId,
            @Param("estado") AvicolaLoteEstado estado);
}
