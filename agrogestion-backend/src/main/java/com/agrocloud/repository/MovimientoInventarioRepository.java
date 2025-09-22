package com.agrocloud.repository;

import com.agrocloud.model.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad MovimientoInventario.
 * Proporciona métodos para acceder a los datos de movimientos de inventario.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    
    /**
     * Busca todos los movimientos de un insumo específico.
     * 
     * @param insumoId ID del insumo
     * @return Lista de movimientos del insumo
     */
    List<MovimientoInventario> findByInsumoId(Long insumoId);
    
    /**
     * Busca todos los movimientos de una labor específica.
     * 
     * @param laborId ID de la labor
     * @return Lista de movimientos de la labor
     */
    List<MovimientoInventario> findByLaborId(Long laborId);
    
    /**
     * Busca todos los movimientos de un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de movimientos del usuario
     */
    List<MovimientoInventario> findByUsuarioId(Long usuarioId);
    
    /**
     * Busca movimientos por tipo de movimiento.
     * 
     * @param tipoMovimiento Tipo de movimiento
     * @return Lista de movimientos del tipo especificado
     */
    List<MovimientoInventario> findByTipoMovimiento(MovimientoInventario.TipoMovimiento tipoMovimiento);
    
    /**
     * Busca movimientos en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de movimientos en el rango de fechas
     */
    List<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca movimientos de un insumo en un rango de fechas.
     * 
     * @param insumoId ID del insumo
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de movimientos del insumo en el rango de fechas
     */
    List<MovimientoInventario> findByInsumoIdAndFechaMovimientoBetween(Long insumoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Cuenta el total de movimientos de un insumo.
     * 
     * @param insumoId ID del insumo
     * @return Número total de movimientos
     */
    long countByInsumoId(Long insumoId);
    
    /**
     * Busca los últimos movimientos de un insumo.
     * 
     * @param insumoId ID del insumo
     * @param limit Número máximo de movimientos a retornar
     * @return Lista de los últimos movimientos
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE m.insumo.id = :insumoId ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findUltimosMovimientosByInsumoId(@Param("insumoId") Long insumoId, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Calcula el saldo actual de un insumo basado en sus movimientos.
     * 
     * @param insumoId ID del insumo
     * @return Saldo calculado (entradas - salidas)
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN m.tipoMovimiento = 'ENTRADA' THEN m.cantidad ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN m.tipoMovimiento = 'SALIDA' THEN m.cantidad ELSE 0 END), 0) " +
           "FROM MovimientoInventario m WHERE m.insumo.id = :insumoId")
    java.math.BigDecimal calcularSaldoByInsumoId(@Param("insumoId") Long insumoId);
}
