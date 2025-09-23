package com.agrocloud.repository;

import com.agrocloud.model.entity.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Ingreso.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Long> {

    /**
     * Busca ingresos por usuario.
     */
    List<Ingreso> findByUserIdOrderByFechaDesc(Long userId);

    /**
     * Busca ingresos por lote.
     */
    List<Ingreso> findByLoteIdOrderByFechaDesc(Long loteId);

    /**
     * Busca ingresos por tipo.
     */
    List<Ingreso> findByTipoIngresoOrderByFechaDesc(Ingreso.TipoIngreso tipoIngreso);

    /**
     * Busca ingresos por rango de fechas.
     */
    List<Ingreso> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca ingresos por usuario y rango de fechas.
     */
    List<Ingreso> findByUserIdAndFechaBetweenOrderByFechaDesc(
            Long userId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Calcula el total de ingresos por usuario y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.user.id = :usuarioId AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosPorUsuarioYFecha(@Param("usuarioId") Long usuarioId, 
                                                     @Param("fechaInicio") LocalDate fechaInicio, 
                                                     @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de ingresos por lote y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.lote.id = :loteId AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosPorLoteYFecha(@Param("loteId") Long loteId, 
                                                  @Param("fechaInicio") LocalDate fechaInicio, 
                                                  @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de ingresos por tipo y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.tipoIngreso = :tipoIngreso AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosPorTipoYFecha(@Param("tipoIngreso") Ingreso.TipoIngreso tipoIngreso, 
                                                  @Param("fechaInicio") LocalDate fechaInicio, 
                                                  @Param("fechaFin") LocalDate fechaFin);

    /**
     * Obtiene estadísticas de ingresos por mes para un usuario.
     */
    @Query("SELECT YEAR(i.fecha) as año, MONTH(i.fecha) as mes, SUM(i.monto) as total " +
           "FROM Ingreso i WHERE i.user.id = :usuarioId " +
           "GROUP BY YEAR(i.fecha), MONTH(i.fecha) " +
           "ORDER BY año DESC, mes DESC")
    List<Object[]> obtenerEstadisticasIngresosPorMes(@Param("usuarioId") Long usuarioId);

    // ========================================
    // MÉTODOS PARA EL DASHBOARD
    // ========================================

    /**
     * Contar ingresos por usuario
     */
    long countByUserId(Long userId);

    /**
     * Sumar todos los ingresos (para ADMIN)
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i")
    BigDecimal sumAllIngresos();

    /**
     * Sumar ingresos por usuario
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.user.id = :usuarioId")
    BigDecimal sumIngresosByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Métodos para eliminación lógica
    List<Ingreso> findByUserIdAndActivoTrue(Long userId);
    long countByUserIdAndActivoTrue(Long userId);
    List<Ingreso> findByActivoTrue();
    
    // Método faltante para los tests
    List<Ingreso> findByUserId(Long userId);
    
}
