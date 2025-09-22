package com.agrocloud.service;

import com.agrocloud.model.entity.*;
import com.agrocloud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para la gestión de inventario de insumos.
 * Maneja el control de stock y auditoría de movimientos.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service
@Transactional
public class InventarioService {
    
    @Autowired
    private InsumoRepository insumoRepository;
    
    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;
    
    @Autowired
    private LaborInsumoRepository laborInsumoRepository;
    
    /**
     * Actualiza el inventario cuando se crea o edita una labor.
     * 
     * @param laborId ID de la labor
     * @param insumosNuevos Lista de insumos nuevos
     * @param insumosAnteriores Lista de insumos anteriores (null si es creación)
     * @param usuario Usuario que realiza la operación
     */
    public void actualizarInventarioLabor(Long laborId, List<LaborInsumo> insumosNuevos, 
                                         List<LaborInsumo> insumosAnteriores, User usuario) {
        
        // 1. Restaurar inventario de insumos anteriores (si es edición)
        if (insumosAnteriores != null && !insumosAnteriores.isEmpty()) {
            for (LaborInsumo insumoAnterior : insumosAnteriores) {
                restaurarInventario(insumoAnterior, usuario, "Restauración por edición de labor");
            }
        }
        
        // 2. Descontar inventario de insumos nuevos
        for (LaborInsumo insumoNuevo : insumosNuevos) {
            descontarInventario(insumoNuevo, usuario, "Uso en labor");
        }
    }
    
    /**
     * Descuenta cantidad del inventario de un insumo.
     * 
     * @param laborInsumo Insumo de la labor
     * @param usuario Usuario que realiza la operación
     * @param motivo Motivo del movimiento
     */
    private void descontarInventario(LaborInsumo laborInsumo, User usuario, String motivo) {
        Insumo insumo = insumoRepository.findById(laborInsumo.getInsumo().getId())
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + laborInsumo.getInsumo().getId()));
        
        // Verificar stock disponible
        if (insumo.getStockActual().compareTo(laborInsumo.getCantidadUsada()) < 0) {
            throw new InsufficientStockException(
                "Stock insuficiente para " + insumo.getNombre() + 
                ". Disponible: " + insumo.getStockActual() + 
                ", Requerido: " + laborInsumo.getCantidadUsada()
            );
        }
        
        // Actualizar stock
        insumo.setStockActual(insumo.getStockActual().subtract(laborInsumo.getCantidadUsada()));
        insumoRepository.save(insumo);
        
        // Registrar movimiento
        registrarMovimiento(insumo, laborInsumo.getLabor(), 
                          MovimientoInventario.TipoMovimiento.SALIDA, 
                          laborInsumo.getCantidadUsada(), 
                          motivo + ": " + laborInsumo.getLabor().getDescripcion(),
                          usuario);
    }
    
    /**
     * Restaura cantidad al inventario de un insumo.
     * 
     * @param laborInsumo Insumo de la labor
     * @param usuario Usuario que realiza la operación
     * @param motivo Motivo del movimiento
     */
    private void restaurarInventario(LaborInsumo laborInsumo, User usuario, String motivo) {
        Insumo insumo = insumoRepository.findById(laborInsumo.getInsumo().getId())
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + laborInsumo.getInsumo().getId()));
        
        // Restaurar stock
        insumo.setStockActual(insumo.getStockActual().add(laborInsumo.getCantidadUsada()));
        insumoRepository.save(insumo);
        
        // Registrar movimiento
        registrarMovimiento(insumo, laborInsumo.getLabor(), 
                          MovimientoInventario.TipoMovimiento.ENTRADA, 
                          laborInsumo.getCantidadUsada(), 
                          motivo + ": " + laborInsumo.getLabor().getDescripcion(),
                          usuario);
    }
    
    /**
     * Registra un movimiento en el inventario.
     * 
     * @param insumo Insumo afectado
     * @param labor Labor relacionada (puede ser null)
     * @param tipoMovimiento Tipo de movimiento
     * @param cantidad Cantidad del movimiento
     * @param motivo Motivo del movimiento
     * @param usuario Usuario que realiza la operación
     */
    private void registrarMovimiento(Insumo insumo, Labor labor, 
                                   MovimientoInventario.TipoMovimiento tipoMovimiento,
                                   BigDecimal cantidad, String motivo, User usuario) {
        
        MovimientoInventario movimiento = new MovimientoInventario(
            insumo, labor, tipoMovimiento, cantidad, motivo, usuario
        );
        
        movimientoInventarioRepository.save(movimiento);
    }
    
    /**
     * Obtiene el historial de movimientos de un insumo.
     * 
     * @param insumoId ID del insumo
     * @return Lista de movimientos
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventario> obtenerHistorialInsumo(Long insumoId) {
        return movimientoInventarioRepository.findByInsumoId(insumoId);
    }
    
    /**
     * Obtiene el historial de movimientos de una labor.
     * 
     * @param laborId ID de la labor
     * @return Lista de movimientos
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventario> obtenerHistorialLabor(Long laborId) {
        return movimientoInventarioRepository.findByLaborId(laborId);
    }
    
    /**
     * Calcula el saldo actual de un insumo basado en movimientos.
     * 
     * @param insumoId ID del insumo
     * @return Saldo calculado
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoInsumo(Long insumoId) {
        return movimientoInventarioRepository.calcularSaldoByInsumoId(insumoId);
    }
    
    /**
     * Verifica si hay stock suficiente para una cantidad.
     * 
     * @param insumoId ID del insumo
     * @param cantidadRequerida Cantidad requerida
     * @return true si hay stock suficiente
     */
    @Transactional(readOnly = true)
    public boolean verificarStockSuficiente(Long insumoId, BigDecimal cantidadRequerida) {
        Insumo insumo = insumoRepository.findById(insumoId)
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
        
        return insumo.getStockActual().compareTo(cantidadRequerida) >= 0;
    }
    
    /**
     * Excepción personalizada para stock insuficiente.
     */
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
