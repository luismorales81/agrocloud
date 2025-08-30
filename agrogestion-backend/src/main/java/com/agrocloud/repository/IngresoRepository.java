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
    List<Ingreso> findByUsuarioIdOrderByFechaIngresoDesc(Long usuarioId);

    /**
     * Busca ingresos por lote.
     */
    List<Ingreso> findByLoteIdOrderByFechaIngresoDesc(Long loteId);

    /**
     * Busca ingresos por tipo.
     */
    List<Ingreso> findByTipoIngresoOrderByFechaIngresoDesc(Ingreso.TipoIngreso tipoIngreso);

    /**
     * Busca ingresos por rango de fechas.
     */
    List<Ingreso> findByFechaIngresoBetweenOrderByFechaIngresoDesc(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca ingresos por usuario y rango de fechas.
     */
    List<Ingreso> findByUsuarioIdAndFechaIngresoBetweenOrderByFechaIngresoDesc(
            Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Calcula el total de ingresos por usuario y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.usuario.id = :usuarioId AND i.fechaIngreso BETWEEN :fechaInicio AND :fechaFin AND i.estado = 'CONFIRMADO'")
    BigDecimal calcularTotalIngresosPorUsuarioYFecha(@Param("usuarioId") Long usuarioId, 
                                                     @Param("fechaInicio") LocalDate fechaInicio, 
                                                     @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de ingresos por lote y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.lote.id = :loteId AND i.fechaIngreso BETWEEN :fechaInicio AND :fechaFin AND i.estado = 'CONFIRMADO'")
    BigDecimal calcularTotalIngresosPorLoteYFecha(@Param("loteId") Long loteId, 
                                                  @Param("fechaInicio") LocalDate fechaInicio, 
                                                  @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de ingresos por tipo y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.tipoIngreso = :tipoIngreso AND i.fechaIngreso BETWEEN :fechaInicio AND :fechaFin AND i.estado = 'CONFIRMADO'")
    BigDecimal calcularTotalIngresosPorTipoYFecha(@Param("tipoIngreso") Ingreso.TipoIngreso tipoIngreso, 
                                                  @Param("fechaInicio") LocalDate fechaInicio, 
                                                  @Param("fechaFin") LocalDate fechaFin);

    /**
     * Obtiene estadísticas de ingresos por mes para un usuario.
     */
    @Query("SELECT YEAR(i.fechaIngreso) as año, MONTH(i.fechaIngreso) as mes, SUM(i.monto) as total " +
           "FROM Ingreso i WHERE i.usuario.id = :usuarioId AND i.estado = 'CONFIRMADO' " +
           "GROUP BY YEAR(i.fechaIngreso), MONTH(i.fechaIngreso) " +
           "ORDER BY año DESC, mes DESC")
    List<Object[]> obtenerEstadisticasIngresosPorMes(@Param("usuarioId") Long usuarioId);
}
