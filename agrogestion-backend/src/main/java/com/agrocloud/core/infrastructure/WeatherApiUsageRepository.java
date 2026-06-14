package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.WeatherApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository("weatherApiUsageRepositoryCore")
public interface WeatherApiUsageRepository extends JpaRepository<WeatherApiUsage, Long> {

    Optional<WeatherApiUsage> findByFecha(LocalDate fecha);

    @Query("SELECT w FROM WeatherApiUsage w WHERE w.fecha = :hoy")
    Optional<WeatherApiUsage> findTodayUsage(@Param("hoy") LocalDate hoy);

    @Query("SELECT COALESCE(SUM(w.usosHoy), 0) FROM WeatherApiUsage w WHERE w.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long getTotalUsosEnRango(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(AVG(w.usosHoy), 0) FROM WeatherApiUsage w WHERE w.fecha >= :fechaInicio")
    Double getPromedioUsosDiarios(@Param("fechaInicio") LocalDate fechaInicio);
}
