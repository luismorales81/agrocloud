package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaScoreCorporal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaScoreCorporalRepository extends JpaRepository<LecheriaScoreCorporal, Long> {

    @Query("SELECT s FROM LecheriaScoreCorporal s WHERE s.empresaId = :empresaId")
    List<LecheriaScoreCorporal> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT s FROM LecheriaScoreCorporal s WHERE s.id = :id AND s.empresaId = :empresaId")
    Optional<LecheriaScoreCorporal> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT s FROM LecheriaScoreCorporal s WHERE s.animal.id = :animalId ORDER BY s.fecha DESC")
    List<LecheriaScoreCorporal> listarPorAnimalId(@Param("animalId") Long animalId);
}
