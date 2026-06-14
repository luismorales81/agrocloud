package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaRaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia del catálogo de razas; toda consulta explícita filtra por empresa.
 */
public interface AvicolaRazaRepository extends JpaRepository<AvicolaRaza, Long> {

    @Query("SELECT r FROM AvicolaRaza r WHERE r.empresaId = :empresaId ORDER BY r.nombre ASC")
    List<AvicolaRaza> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT r FROM AvicolaRaza r WHERE r.id = :id AND r.empresaId = :empresaId")
    Optional<AvicolaRaza> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
