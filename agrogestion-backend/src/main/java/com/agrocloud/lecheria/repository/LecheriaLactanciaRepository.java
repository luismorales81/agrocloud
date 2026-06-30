package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaLactancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaLactanciaRepository extends JpaRepository<LecheriaLactancia, Long> {

    @Query("SELECT l FROM LecheriaLactancia l WHERE l.empresaId = :empresaId")
    List<LecheriaLactancia> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT l FROM LecheriaLactancia l WHERE l.id = :id AND l.empresaId = :empresaId")
    Optional<LecheriaLactancia> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT l FROM LecheriaLactancia l WHERE l.animal.id = :animalId AND l.activa = true")
    Optional<LecheriaLactancia> buscarActivaPorAnimalId(@Param("animalId") Long animalId);

    @Query("SELECT COUNT(l) FROM LecheriaLactancia l WHERE l.animal.id = :animalId")
    Integer contarPorAnimalId(@Param("animalId") Long animalId);
}
