package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaEventoReproductivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LecheriaEventoReproductivoRepository extends JpaRepository<LecheriaEventoReproductivo, Long> {

    @Query("SELECT e FROM LecheriaEventoReproductivo e WHERE e.empresaId = :empresaId")
    List<LecheriaEventoReproductivo> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM LecheriaEventoReproductivo e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<LecheriaEventoReproductivo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT e FROM LecheriaEventoReproductivo e WHERE e.animal.id = :animalId ORDER BY e.fecha DESC")
    List<LecheriaEventoReproductivo> listarPorAnimalId(@Param("animalId") Long animalId);

    @Query("SELECT e FROM LecheriaEventoReproductivo e WHERE e.empresaId = :empresaId AND e.fechaPrevistaParto BETWEEN :desde AND :hasta")
    List<LecheriaEventoReproductivo> listarPartosPrevistos(@Param("empresaId") Long empresaId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
