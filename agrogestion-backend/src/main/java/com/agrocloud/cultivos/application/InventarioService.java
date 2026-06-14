package com.agrocloud.cultivos.application;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.inventory.domain.MovimientoInventarioDTO;
import com.agrocloud.cultivos.domain.LaborInsumo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de inventario para el módulo Cultivos.
 * Delega todo (lectura y escritura) en core.inventory (InventoryService).
 * No accede a repositorios JPA de inventario.
 */
@Service("inventarioServiceCultivos")
@Transactional
public class InventarioService {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Actualiza el inventario cuando se crea o edita una labor.
     */
    public void actualizarInventarioLabor(Long laborId, List<LaborInsumo> insumosNuevos,
                                         List<LaborInsumo> insumosAnteriores, User usuario) {
        Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
        if (empresaId == null) throw new IllegalStateException("Usuario sin empresa para actualizar inventario");
        Long usuarioId = usuario.getId();

        if (insumosAnteriores != null && !insumosAnteriores.isEmpty()) {
            for (LaborInsumo li : insumosAnteriores) {
                InventoryResult r = inventoryService.reponer(empresaId, li.getInsumo().getId(),
                        li.getCantidadUsada(), InventoryOrigin.CULTIVOS, laborId, usuarioId);
                if (!r.exito()) throw new InsufficientStockException(r.mensaje());
            }
        }
        for (LaborInsumo li : insumosNuevos) {
            InventoryResult r = inventoryService.consumir(empresaId, li.getInsumo().getId(),
                    li.getCantidadUsada(), InventoryOrigin.CULTIVOS, laborId, usuarioId);
            if (!r.exito()) throw new InsufficientStockException(r.mensaje());
        }
    }

    /**
     * Restaura el inventario de una labor eliminada o cancelada.
     */
    public void restaurarInventarioLabor(List<LaborInsumo> insumosLabor, User usuario, String motivo) {
        if (insumosLabor == null || insumosLabor.isEmpty()) return;
        Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
        if (empresaId == null) throw new IllegalStateException("Usuario sin empresa");
        Long laborId = insumosLabor.get(0).getLabor() != null ? insumosLabor.get(0).getLabor().getId() : null;
        Long usuarioId = usuario.getId();
        for (LaborInsumo li : insumosLabor) {
            InventoryResult r = inventoryService.reponer(empresaId, li.getInsumo().getId(),
                    li.getCantidadUsada(), InventoryOrigin.CULTIVOS, laborId, usuarioId);
            if (!r.exito()) throw new InsufficientStockException(r.mensaje());
        }
    }

    /**
     * Historial de movimientos de un insumo. Requiere empresaId (obtener con inventoryService.obtenerEmpresaIdDeProducto(insumoId) si aplica).
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> obtenerHistorialAgroquimico(Long empresaId, Long insumoId) {
        return inventoryService.listarMovimientosPorInsumo(empresaId, insumoId);
    }

    /**
     * Historial de movimientos asociados a una labor.
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> obtenerHistorialLabor(Long laborId) {
        return inventoryService.listarMovimientosPorLaborCultivos(laborId);
    }

    /**
     * Saldo calculado por movimientos de un insumo.
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoAgroquimico(Long empresaId, Long insumoId) {
        BigDecimal saldo = inventoryService.calcularSaldoPorInsumo(empresaId, insumoId);
        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    /**
     * Verifica si hay stock suficiente (obtiene empresa del producto internamente).
     */
    @Transactional(readOnly = true)
    public boolean verificarStockSuficiente(Long insumoId, BigDecimal cantidadRequerida) {
        if (cantidadRequerida == null) return false;
        return inventoryService.obtenerEmpresaIdDeProducto(insumoId)
                .map(empresaId -> inventoryService.hayStockSuficiente(empresaId, insumoId, cantidadRequerida))
                .orElse(false);
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
