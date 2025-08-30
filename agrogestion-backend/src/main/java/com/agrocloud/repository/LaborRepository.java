package com.agrocloud.repository;

import com.agrocloud.model.entity.Labor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LaborRepository extends JpaRepository<Labor, Long> {
    
    // Buscar labores por lote
    List<Labor> findByLoteIdOrderByFechaLaborDesc(Long loteId);
    
    // Buscar labores por usuario
    List<Labor> findByUsuarioIdOrderByFechaLaborDesc(Long usuarioId);
    
    // Buscar labores por estado
    List<Labor> findByEstadoOrderByFechaLaborDesc(String estado);
    
    // Buscar labores planificadas
    List<Labor> findByEstadoOrderByFechaLaborAsc(String estado);
    
    // Buscar labores por rango de fechas
    @Query("SELECT l FROM Labor l WHERE l.fechaLabor BETWEEN :fechaInicio AND :fechaFin ORDER BY l.fechaLabor DESC")
    List<Labor> findByRangoFechas(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    // Buscar labores próximas a ejecutar (próximos 7 días)
    @Query("SELECT l FROM Labor l WHERE l.fechaLabor BETWEEN :hoy AND :fechaLimite AND l.estado = 'PLANIFICADA' ORDER BY l.fechaLabor ASC")
    List<Labor> findLaboresProximas(@Param("hoy") LocalDate hoy, @Param("fechaLimite") LocalDate fechaLimite);
    
    // Buscar labores vencidas
    @Query("SELECT l FROM Labor l WHERE l.fechaLabor < :hoy AND l.estado = 'PLANIFICADA' ORDER BY l.fechaLabor ASC")
    List<Labor> findLaboresVencidas(@Param("hoy") LocalDate hoy);
    
    // Contar labores por estado
    @Query("SELECT COUNT(l) FROM Labor l WHERE l.estado = :estado")
    Long countByEstado(@Param("estado") String estado);
    
    // Contar labores planificadas para hoy
    @Query("SELECT COUNT(l) FROM Labor l WHERE l.fechaLabor = :hoy AND l.estado = 'PLANIFICADA'")
    Long countLaboresHoy(@Param("hoy") LocalDate hoy);
    
    // Buscar labores por usuario y rango de fechas
    List<Labor> findByUsuarioIdAndFechaLaborBetween(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    
    // Buscar labores por lote y rango de fechas
    List<Labor> findByLoteIdAndFechaLaborBetween(Long loteId, LocalDate fechaInicio, LocalDate fechaFin);
}
