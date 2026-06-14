package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de lotes avícolas; toda consulta explícita filtra por empresa.
 */
public interface AvicolaLoteRepository extends JpaRepository<AvicolaLote, Long> {

    @Query("SELECT l FROM AvicolaLote l WHERE l.empresaId = :empresaId ORDER BY l.fechaIngreso DESC, l.id DESC")
    List<AvicolaLote> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT l FROM AvicolaLote l WHERE l.id = :id AND l.empresaId = :empresaId")
    Optional<AvicolaLote> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT l FROM AvicolaLote l WHERE l.empresaId = :empresaId AND l.estado = :estado ORDER BY l.fechaIngreso DESC")
    List<AvicolaLote> listarPorEmpresaIdYEstado(@Param("empresaId") Long empresaId, @Param("estado") AvicolaLoteEstado estado);
}
