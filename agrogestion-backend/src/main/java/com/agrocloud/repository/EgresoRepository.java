package com.agrocloud.repository;

import com.agrocloud.model.entity.Egreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Egreso.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Repository
public interface EgresoRepository extends JpaRepository<Egreso, Long> {

    /**
     * Busca egresos por usuario y rango de fechas.
     */
    List<Egreso> findByUserIdAndFechaBetweenOrderByFechaDesc(
            Long userId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca egresos por lote y rango de fechas.
     */
    List<Egreso> findByLoteIdAndFechaBetweenOrderByFechaDesc(
            Long loteId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca egresos por tipo de egreso.
     */
    List<Egreso> findByTipoAndUserIdOrderByFechaDesc(
            Egreso.TipoEgreso tipo, Long userId);

    /**
     * Calcula el total de egresos por usuario y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.user.id = :usuarioId AND e.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorUsuarioYFecha(
            @Param("usuarioId") Long usuarioId, 
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de egresos por lote y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.lote.id = :loteId AND e.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorLoteYFecha(
            @Param("loteId") Long loteId, 
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de egresos por tipo y usuario.
     */
    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.tipo = :tipoEgreso AND e.user.id = :usuarioId AND e.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorTipoYUsuario(
            @Param("tipoEgreso") Egreso.TipoEgreso tipoEgreso,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);



    // ========================================
    // MÉTODOS PARA EL DASHBOARD
    // ========================================

    /**
     * Contar egresos por usuario
     */
    long countByUserId(Long userId);

    /**
     * Sumar todos los egresos (para ADMIN)
     */
    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e")
    BigDecimal sumAllEgresos();

    /**
     * Sumar egresos por usuario
     */
    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.user.id = :usuarioId")
    BigDecimal sumEgresosByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Métodos para eliminación lógica
    List<Egreso> findByUserIdAndActivoTrue(Long userId);
    long countByUserIdAndActivoTrue(Long userId);
    List<Egreso> findByActivoTrue();
    
    // Método faltante para los tests
    List<Egreso> findByUsuarioId(Long usuarioId);
}
