package com.agrocloud.repository;

import com.agrocloud.model.entity.AlquilerMaquinaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la gestión de alquileres de maquinaria
 */
@Repository
public interface AlquilerMaquinariaRepository extends JpaRepository<AlquilerMaquinaria, Long> {

    /**
     * Obtiene todos los alquileres de un usuario
     */
    List<AlquilerMaquinaria> findByUserIdOrderByFechaInicioDesc(Long userId);

    /**
     * Obtiene todos los alquileres de una maquinaria específica
     */
    List<AlquilerMaquinaria> findByMaquinariaIdOrderByFechaInicioDesc(Long maquinariaId);

    /**
     * Obtiene todos los alquileres de un lote específico
     */
    List<AlquilerMaquinaria> findByLoteIdOrderByFechaInicioDesc(Long loteId);

    /**
     * Obtiene alquileres en un rango de fechas
     */
    List<AlquilerMaquinaria> findByFechaInicioBetweenOrderByFechaInicioDesc(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene alquileres activos (fecha fin mayor a hoy)
     */
    @Query("SELECT a FROM AlquilerMaquinaria a WHERE a.fechaFin >= :hoy ORDER BY a.fechaInicio DESC")
    List<AlquilerMaquinaria> findAlquileresActivos(@Param("hoy") LocalDate hoy);

    /**
     * Calcula el total de costos de alquiler en un rango de fechas para un usuario
     */
    @Query("SELECT COALESCE(SUM(a.costoTotal), 0) FROM AlquilerMaquinaria a WHERE a.user.id = :userId AND a.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Double calcularTotalAlquileresPorUsuarioYFecha(@Param("userId") Long userId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de costos de alquiler en un rango de fechas para un lote
     */
    @Query("SELECT COALESCE(SUM(a.costoTotal), 0) FROM AlquilerMaquinaria a WHERE a.lote.id = :loteId AND a.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Double calcularTotalAlquileresPorLoteYFecha(@Param("loteId") Long loteId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
}
