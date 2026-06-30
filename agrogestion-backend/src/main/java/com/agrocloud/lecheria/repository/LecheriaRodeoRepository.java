package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaRodeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaRodeoRepository extends JpaRepository<LecheriaRodeo, Long> {

    @Query("SELECT r FROM LecheriaRodeo r WHERE r.empresaId = :empresaId ORDER BY r.nombre")
    List<LecheriaRodeo> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT r FROM LecheriaRodeo r WHERE r.id = :id AND r.empresaId = :empresaId")
    Optional<LecheriaRodeo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT r FROM LecheriaRodeo r WHERE r.establecimiento.id = :establecimientoId ORDER BY r.nombre")
    List<LecheriaRodeo> listarPorEstablecimientoId(@Param("establecimientoId") Long establecimientoId);
}
