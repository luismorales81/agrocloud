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
    List<Egreso> findByUsuarioIdAndFechaEgresoBetweenOrderByFechaEgresoDesc(
            Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca egresos por lote y rango de fechas.
     */
    List<Egreso> findByLoteIdAndFechaEgresoBetweenOrderByFechaEgresoDesc(
            Long loteId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca egresos por tipo de egreso.
     */
    List<Egreso> findByTipoEgresoAndUsuarioIdOrderByFechaEgresoDesc(
            Egreso.TipoEgreso tipoEgreso, Long usuarioId);

    /**
     * Busca egresos por insumo.
     */
    List<Egreso> findByInsumoIdOrderByFechaEgresoDesc(Long insumoId);

    /**
     * Calcula el total de egresos por usuario y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Egreso e WHERE e.usuario.id = :usuarioId AND e.fechaEgreso BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorUsuarioYFecha(
            @Param("usuarioId") Long usuarioId, 
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de egresos por lote y rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Egreso e WHERE e.lote.id = :loteId AND e.fechaEgreso BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorLoteYFecha(
            @Param("loteId") Long loteId, 
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de egresos por tipo y usuario.
     */
    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Egreso e WHERE e.tipoEgreso = :tipoEgreso AND e.usuario.id = :usuarioId AND e.fechaEgreso BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorTipoYUsuario(
            @Param("tipoEgreso") Egreso.TipoEgreso tipoEgreso,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca egresos por estado.
     */
    List<Egreso> findByEstadoAndUsuarioIdOrderByFechaEgresoDesc(
            Egreso.EstadoEgreso estado, Long usuarioId);

    /**
     * Busca egresos por proveedor.
     */
    List<Egreso> findByProveedorContainingIgnoreCaseAndUsuarioIdOrderByFechaEgresoDesc(
            String proveedor, Long usuarioId);
}
