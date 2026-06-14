package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de establecimientos avícolas; toda consulta explícita filtra por empresa.
 */
public interface AvicolaEstablecimientoRepository extends JpaRepository<AvicolaEstablecimiento, Long> {

    @Query("SELECT e FROM AvicolaEstablecimiento e WHERE e.empresaId = :empresaId ORDER BY e.nombre ASC")
    List<AvicolaEstablecimiento> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM AvicolaEstablecimiento e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<AvicolaEstablecimiento> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
