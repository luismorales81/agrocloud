package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LecheriaEventoSanitarioRepository extends JpaRepository<LecheriaEventoSanitario, Long> {

    @Query("SELECT e FROM LecheriaEventoSanitario e WHERE e.empresaId = :empresaId")
    List<LecheriaEventoSanitario> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM LecheriaEventoSanitario e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<LecheriaEventoSanitario> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT e FROM LecheriaEventoSanitario e WHERE e.animal.id = :animalId ORDER BY e.fecha DESC")
    List<LecheriaEventoSanitario> listarPorAnimalId(@Param("animalId") Long animalId);

    @Query("SELECT e FROM LecheriaEventoSanitario e JOIN FETCH e.animal WHERE e.empresaId = :empresaId AND e.diasRetiro IS NOT NULL AND e.fecha >= :desdeRetiro")
    List<LecheriaEventoSanitario> listarConRetiroVigente(@Param("empresaId") Long empresaId, @Param("desdeRetiro") LocalDate desdeRetiro);
}
