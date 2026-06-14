package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvicolaHuevoEstablecimientoRepository extends JpaRepository<AvicolaHuevoEstablecimiento, Long> {

    @Query("SELECT e FROM AvicolaHuevoEstablecimiento e WHERE e.empresaId = :empresaId ORDER BY e.nombre ASC")
    List<AvicolaHuevoEstablecimiento> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM AvicolaHuevoEstablecimiento e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<AvicolaHuevoEstablecimiento> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
