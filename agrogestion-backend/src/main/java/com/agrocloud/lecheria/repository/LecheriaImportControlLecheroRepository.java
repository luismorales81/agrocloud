package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaImportControlLechero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaImportControlLecheroRepository extends JpaRepository<LecheriaImportControlLechero, Long> {

    @Query("SELECT i FROM LecheriaImportControlLechero i WHERE i.empresaId = :empresaId ORDER BY i.createdAt DESC")
    List<LecheriaImportControlLechero> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT i FROM LecheriaImportControlLechero i WHERE i.id = :id AND i.empresaId = :empresaId")
    Optional<LecheriaImportControlLechero> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
