package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Eventos sanitarios por lote; toda consulta explícita filtra por empresa y lote.
 */
public interface AvicolaEventoSanitarioRepository extends JpaRepository<AvicolaEventoSanitario, Long> {

    @Query("SELECT s FROM AvicolaEventoSanitario s WHERE s.lote.id = :loteId AND s.empresaId = :empresaId ORDER BY s.fecha DESC, s.id DESC")
    List<AvicolaEventoSanitario> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT s FROM AvicolaEventoSanitario s WHERE s.id = :id AND s.empresaId = :empresaId")
    Optional<AvicolaEventoSanitario> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
