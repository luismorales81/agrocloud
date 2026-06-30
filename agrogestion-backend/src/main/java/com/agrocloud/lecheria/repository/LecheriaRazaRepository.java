package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaRaza;
import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaRazaRepository extends JpaRepository<LecheriaRaza, Long> {

    @Query("SELECT r FROM LecheriaRaza r WHERE r.empresaId = :empresaId ORDER BY r.nombre")
    List<LecheriaRaza> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT r FROM LecheriaRaza r WHERE r.id = :id AND r.empresaId = :empresaId")
    Optional<LecheriaRaza> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT r FROM LecheriaRaza r WHERE r.empresaId = :empresaId AND r.especie = :especie ORDER BY r.nombre")
    List<LecheriaRaza> listarPorEmpresaIdYEspecie(@Param("empresaId") Long empresaId, @Param("especie") LecheriaEspecie especie);
}
