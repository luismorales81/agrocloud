package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaEstablecimientoRepository extends JpaRepository<LecheriaEstablecimiento, Long> {

    @Query("SELECT e FROM LecheriaEstablecimiento e WHERE e.empresaId = :empresaId ORDER BY e.nombre")
    List<LecheriaEstablecimiento> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM LecheriaEstablecimiento e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<LecheriaEstablecimiento> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
