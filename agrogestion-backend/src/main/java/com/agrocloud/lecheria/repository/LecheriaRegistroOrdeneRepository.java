package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaRegistroOrdene;
import com.agrocloud.lecheria.model.enums.LecheriaTurnoOrdene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LecheriaRegistroOrdeneRepository extends JpaRepository<LecheriaRegistroOrdene, Long> {

    @Query("SELECT o FROM LecheriaRegistroOrdene o WHERE o.empresaId = :empresaId ORDER BY o.fecha DESC")
    List<LecheriaRegistroOrdene> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT o FROM LecheriaRegistroOrdene o WHERE o.id = :id AND o.empresaId = :empresaId")
    Optional<LecheriaRegistroOrdene> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT o FROM LecheriaRegistroOrdene o WHERE o.animal.id = :animalId ORDER BY o.fecha DESC, o.turno")
    List<LecheriaRegistroOrdene> listarPorAnimalId(@Param("animalId") Long animalId);

    boolean existsByAnimalIdAndFechaAndTurno(Long animalId, LocalDate fecha, LecheriaTurnoOrdene turno);

    @Query("SELECT COALESCE(SUM(o.litros), 0) FROM LecheriaRegistroOrdene o WHERE o.empresaId = :empresaId AND o.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumarLitrosPorEmpresaYFechas(@Param("empresaId") Long empresaId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT o FROM LecheriaRegistroOrdene o WHERE o.lactancia.id = :lactanciaId ORDER BY o.fecha")
    List<LecheriaRegistroOrdene> listarPorLactanciaId(@Param("lactanciaId") Long lactanciaId);
}
