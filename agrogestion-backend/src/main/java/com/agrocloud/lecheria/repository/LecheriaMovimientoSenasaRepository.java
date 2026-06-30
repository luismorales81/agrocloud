package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaMovimientoSenasa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaMovimientoSenasaRepository extends JpaRepository<LecheriaMovimientoSenasa, Long> {

    @Query("SELECT m FROM LecheriaMovimientoSenasa m WHERE m.empresaId = :empresaId ORDER BY m.fecha DESC")
    List<LecheriaMovimientoSenasa> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT m FROM LecheriaMovimientoSenasa m WHERE m.id = :id AND m.empresaId = :empresaId")
    Optional<LecheriaMovimientoSenasa> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT m FROM LecheriaMovimientoSenasa m WHERE m.empresaId = :empresaId AND m.exportado = false ORDER BY m.fecha")
    List<LecheriaMovimientoSenasa> listarPendientesExportacion(@Param("empresaId") Long empresaId);
}
