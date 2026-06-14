package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.Plot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("laborRepositoryCultivos")
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
    
    // Métodos faltantes para los tests
    List<Labor> findByEstado(Labor.EstadoLabor estado);
    List<Labor> findByLoteId(Long loteId);
    List<Labor> findByUsuarioId(Long usuarioId);
    List<Labor> findByFechaInicio(LocalDate fechaInicio);
    List<Labor> findByTipoLabor(Labor.TipoLabor tipoLabor);
    
    // Métodos para transiciones automáticas de estado
    List<Labor> findByLoteAndTipoLaborAndEstado(Plot lote, Labor.TipoLabor tipoLabor, Labor.EstadoLabor estado);
    List<Labor> findByLoteAndTipoLaborInAndEstado(Plot lote, List<Labor.TipoLabor> tiposLabor, Labor.EstadoLabor estado);

    long countByTipoLaborAndActivoTrue(Labor.TipoLabor tipoLabor);

    long countByTipoLaborAndEstadoAndActivoTrue(Labor.TipoLabor tipoLabor, Labor.EstadoLabor estado);

    @Query("SELECT DISTINCT l FROM Labor l LEFT JOIN FETCH l.lote LEFT JOIN FETCH l.usuario WHERE l.lote.id IN :loteIds ORDER BY l.fechaInicio DESC")
    List<Labor> findByLoteIdInWithFetch(@Param("loteIds") List<Long> loteIds);

    @Query("SELECT l FROM Labor l WHERE l.lote.id = :loteId AND l.tipoLabor = :tipo AND l.activo = true " +
           "AND l.id <> :excluirId AND l.fechaInicio <= :fechaFinMax " +
           "AND COALESCE(l.fechaFin, l.fechaInicio) >= :fechaInicioMin")
    List<Labor> findLaboresActivasSolapadas(@Param("loteId") Long loteId,
                                            @Param("tipo") Labor.TipoLabor tipo,
                                            @Param("fechaInicioMin") LocalDate fechaInicioMin,
                                            @Param("fechaFinMax") LocalDate fechaFinMax,
                                            @Param("excluirId") Long excluirId);

    @Query("SELECT l.id FROM Labor l WHERE l.lote.id IN :loteIds AND l.activo = true ORDER BY l.fechaInicio DESC")
    Page<Long> findLaborIdsByLoteIdIn(@Param("loteIds") List<Long> loteIds, Pageable pageable);

    @Query(value = """
            SELECT l.id FROM Labor l
            WHERE l.lote.id IN :loteIds AND l.activo = true
            AND (:loteId IS NULL OR l.lote.id = :loteId)
            AND (:estado IS NULL OR l.estado = :estado)
            AND (:soloVencidas = false OR (l.estado = com.agrocloud.cultivos.domain.Labor.EstadoLabor.PLANIFICADA AND l.fechaInicio < CURRENT_DATE))
            AND (:busqueda IS NULL OR :busqueda = '' OR LOWER(COALESCE(l.responsable, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                 OR LOWER(l.lote.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                 OR LOWER(CAST(l.tipoLabor AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            ORDER BY l.fechaInicio DESC
            """,
            countQuery = """
            SELECT COUNT(l.id) FROM Labor l
            WHERE l.lote.id IN :loteIds AND l.activo = true
            AND (:loteId IS NULL OR l.lote.id = :loteId)
            AND (:estado IS NULL OR l.estado = :estado)
            AND (:soloVencidas = false OR (l.estado = com.agrocloud.cultivos.domain.Labor.EstadoLabor.PLANIFICADA AND l.fechaInicio < CURRENT_DATE))
            AND (:busqueda IS NULL OR :busqueda = '' OR LOWER(COALESCE(l.responsable, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                 OR LOWER(l.lote.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                 OR LOWER(CAST(l.tipoLabor AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<Long> findLaborIdsFiltradasByLoteIdIn(
            @Param("loteIds") List<Long> loteIds,
            @Param("loteId") Long loteId,
            @Param("estado") Labor.EstadoLabor estado,
            @Param("soloVencidas") boolean soloVencidas,
            @Param("busqueda") String busqueda,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT l FROM Labor l LEFT JOIN FETCH l.lote LEFT JOIN FETCH l.usuario
            WHERE l.lote.id IN :loteIds AND l.activo = true
            AND (:loteId IS NULL OR l.lote.id = :loteId)
            AND (:estado IS NULL OR l.estado = :estado)
            AND (:soloVencidas = false OR (l.estado = com.agrocloud.cultivos.domain.Labor.EstadoLabor.PLANIFICADA AND l.fechaInicio < CURRENT_DATE))
            AND (:fechaDesde IS NULL OR l.fechaInicio >= :fechaDesde)
            AND (:fechaHasta IS NULL OR l.fechaInicio <= :fechaHasta)
            AND (:busqueda IS NULL OR :busqueda = '' OR LOWER(COALESCE(l.responsable, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                 OR LOWER(l.lote.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                 OR LOWER(CAST(l.tipoLabor AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            ORDER BY l.fechaInicio DESC
            """)
    List<Labor> findActivasFiltradasByLoteIdIn(
            @Param("loteIds") List<Long> loteIds,
            @Param("loteId") Long loteId,
            @Param("estado") Labor.EstadoLabor estado,
            @Param("soloVencidas") boolean soloVencidas,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("busqueda") String busqueda);

    @Query("SELECT DISTINCT l FROM Labor l LEFT JOIN FETCH l.lote LEFT JOIN FETCH l.usuario " +
           "WHERE l.lote.id IN :loteIds AND l.activo = true " +
           "AND l.fechaInicio BETWEEN :desde AND :hasta " +
           "AND l.estado IN (com.agrocloud.cultivos.domain.Labor.EstadoLabor.PLANIFICADA, " +
           "com.agrocloud.cultivos.domain.Labor.EstadoLabor.COMPLETADA) " +
           "ORDER BY l.fechaInicio DESC")
    List<Labor> findActivasCalendarioByLoteIdInAndFechaInicioBetween(
            @Param("loteIds") List<Long> loteIds,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT DISTINCT l FROM Labor l LEFT JOIN FETCH l.lote LEFT JOIN FETCH l.usuario LEFT JOIN FETCH l.cultivo WHERE l.id IN :ids")
    List<Labor> findByIdInWithFetch(@Param("ids") List<Long> ids);
}
