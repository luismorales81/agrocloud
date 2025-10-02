package com.agrocloud.repository;

import com.agrocloud.model.entity.WeatherApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repositorio para la gestión del uso de la API del clima
 */
@Repository
public interface WeatherApiUsageRepository extends JpaRepository<WeatherApiUsage, Long> {
    
    /**
     * Busca el registro de uso para una fecha específica
     */
    Optional<WeatherApiUsage> findByFecha(LocalDate fecha);
    
    /**
     * Busca el registro de uso para hoy
     */
    @Query("SELECT w FROM WeatherApiUsage w WHERE w.fecha = :hoy")
    Optional<WeatherApiUsage> findTodayUsage(@Param("hoy") LocalDate hoy);
    
    /**
     * Obtiene el total de usos de la API del clima en un rango de fechas
     */
    @Query("SELECT COALESCE(SUM(w.usosHoy), 0) FROM WeatherApiUsage w WHERE w.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long getTotalUsosEnRango(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    /**
     * Obtiene el promedio de usos diarios en los últimos N días
     */
    @Query("SELECT COALESCE(AVG(w.usosHoy), 0) FROM WeatherApiUsage w WHERE w.fecha >= :fechaInicio")
    Double getPromedioUsosDiarios(@Param("fechaInicio") LocalDate fechaInicio);
}
