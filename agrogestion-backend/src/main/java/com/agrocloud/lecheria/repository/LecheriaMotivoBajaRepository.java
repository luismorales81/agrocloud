package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaMotivoBaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaMotivoBajaRepository extends JpaRepository<LecheriaMotivoBaja, Long> {

    @Query("SELECT m FROM LecheriaMotivoBaja m WHERE m.empresaId = :empresaId ORDER BY m.nombre")
    List<LecheriaMotivoBaja> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT m FROM LecheriaMotivoBaja m WHERE m.id = :id AND m.empresaId = :empresaId")
    Optional<LecheriaMotivoBaja> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
