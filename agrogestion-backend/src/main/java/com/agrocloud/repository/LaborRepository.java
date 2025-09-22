package com.agrocloud.repository;

import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.Plot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LaborRepository extends JpaRepository<Labor, Long> {
    
    // Buscar labores por lote
    List<Labor> findByLoteIdOrderByFechaInicioDesc(Long loteId);
    
    // Buscar labores por usuario
    List<Labor> findByUsuarioIdOrderByFechaInicioDesc(Long usuarioId);
    
    // Buscar labores por estado
    List<Labor> findByEstadoOrderByFechaInicioDesc(String estado);
    
    // Buscar labores planificadas
    List<Labor> findByEstadoOrderByFechaInicioAsc(String estado);
    
    // Buscar labores por rango de fechas
    @Query("SELECT l FROM Labor l WHERE l.fechaInicio BETWEEN :fechaInicio AND :fechaFin ORDER BY l.fechaInicio DESC")
    List<Labor> findByRangoFechas(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    // Buscar labores próximas a ejecutar (próximos 7 días)
    @Query("SELECT l FROM Labor l WHERE l.fechaInicio BETWEEN :hoy AND :fechaLimite AND l.estado = 'PLANIFICADA' ORDER BY l.fechaInicio ASC")
    List<Labor> findLaboresProximas(@Param("hoy") LocalDate hoy, @Param("fechaLimite") LocalDate fechaLimite);
    
    // Buscar labores vencidas
    @Query("SELECT l FROM Labor l WHERE l.fechaInicio < :hoy AND l.estado = 'PLANIFICADA' ORDER BY l.fechaInicio ASC")
    List<Labor> findLaboresVencidas(@Param("hoy") LocalDate hoy);
    
    // Contar labores por estado
    @Query("SELECT COUNT(l) FROM Labor l WHERE l.estado = :estado")
    Long countByEstado(@Param("estado") String estado);
    
    // Contar labores planificadas para hoy
    @Query("SELECT COUNT(l) FROM Labor l WHERE l.fechaInicio = :hoy AND l.estado = 'PLANIFICADA'")
    Long countLaboresHoy(@Param("hoy") LocalDate hoy);
    
    // Buscar labores por usuario y rango de fechas
    List<Labor> findByUsuarioIdAndFechaInicioBetween(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    
    // Buscar labores por lote y rango de fechas
    List<Labor> findByLoteIdAndFechaInicioBetween(Long loteId, LocalDate fechaInicio, LocalDate fechaFin);

    // ========================================
    // MÉTODOS PARA EL DASHBOARD
    // ========================================

    /**
     * Contar labores por usuario
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Contar labores activas por usuario
     */
    long countByUsuarioIdAndActivoTrue(Long usuarioId);

    /**
     * Buscar labores activas por usuario
     */
    List<Labor> findByUsuarioIdAndActivoTrue(Long usuarioId);

    /**
     * Buscar labores activas por lote
     */
    List<Labor> findByLoteIdAndActivoTrue(Long loteId);

    /**
     * Buscar todas las labores activas
     */
    List<Labor> findByActivoTrue();
    
    /**
     * Buscar labores por lista de IDs de lotes
     */
    List<Labor> findByLoteIdInOrderByFechaInicioDesc(List<Long> loteIds);
    
    /**
     * Contar labores por lista de IDs de lotes
     */
    long countByLoteIdIn(List<Long> loteIds);

    // Métodos adicionales para eliminación lógica
    List<Labor> findByActivoFalse();
    List<Labor> findByUsuarioIdAndActivoFalse(Long usuarioId);
    
    // Buscar labores por lote y tipo de labor
    List<Labor> findByLoteAndTipoLaborOrderByFechaInicioDesc(Plot lote, Labor.TipoLabor tipoLabor);
}
