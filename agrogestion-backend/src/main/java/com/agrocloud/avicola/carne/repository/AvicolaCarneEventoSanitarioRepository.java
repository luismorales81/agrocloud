package com.agrocloud.avicola.carne.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Eventos sanitarios avícola-carne. Métodos declarados exigen {@code empresaId}. Evitar {@code findAll()} sin filtro.
 */
public interface AvicolaCarneEventoSanitarioRepository extends JpaRepository<AvicolaEventoSanitario, Long> {

    @Query(value = """
            SELECT * FROM avicola_evento_sanitario
            WHERE lote_id = :loteId AND empresa_id = :empresaId
            ORDER BY fecha DESC, id DESC
            """, nativeQuery = true)
    List<AvicolaEventoSanitario> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM avicola_evento_sanitario
            WHERE id = :id AND empresa_id = :empresaId
            LIMIT 1
            """, nativeQuery = true)
    Optional<AvicolaEventoSanitario> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
