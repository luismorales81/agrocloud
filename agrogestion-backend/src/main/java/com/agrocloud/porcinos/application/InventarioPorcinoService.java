package com.agrocloud.porcinos.application;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.inventory.domain.ComponenteInsumoCompuesto;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.porcinos.domain.EventoSanitario;
import com.agrocloud.core.inventory.infrastructure.ComponenteInsumoCompuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de inventario para el módulo Porcinos.
 * Delega todo el control de stock en core.inventory (InventoryService).
 */
@Service
public class InventarioPorcinoService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    @Qualifier("componenteInsumoCompuestoRepositoryInventario")
        private ComponenteInsumoCompuestoRepository componenteRepository;

    @Transactional
    public void descontarStockEventoSanitario(Long insumoId, BigDecimal cantidad,
                                              EventoSanitario eventoSanitario, User usuario) {
        if (insumoId == null) return;
        Long empresaId = eventoSanitario.getEmpresa() != null ? eventoSanitario.getEmpresa().getId() : null;
        if (empresaId == null) empresaId = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId()).map(e -> e.getId()).orElse(null);
        if (empresaId == null) throw new IllegalArgumentException("No se pudo determinar la empresa");
        Long refId = eventoSanitario.getId();
        InventoryResult r = inventoryService.consumir(empresaId, insumoId, cantidad, InventoryOrigin.PORCINOS, refId, usuario.getId());
        if (!r.exito()) throw new IllegalArgumentException(r.mensaje());
    }

    @Transactional
    public void restaurarStockEventoSanitario(Long insumoId, BigDecimal cantidad,
                                              EventoSanitario eventoSanitario, User usuario) {
        if (insumoId == null) return;
        Long empresaId = eventoSanitario.getEmpresa() != null ? eventoSanitario.getEmpresa().getId() : null;
        if (empresaId == null) empresaId = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId()).map(e -> e.getId()).orElse(null);
        if (empresaId == null) return;
        inventoryService.reponer(empresaId, insumoId, cantidad, InventoryOrigin.PORCINOS, eventoSanitario.getId(), usuario.getId());
    }

    /**
     * Valida stock disponible. Requiere empresaId para delegar en core.inventory.
     */
    @Transactional(readOnly = true)
    public void validarStockDisponible(Long empresaId, Long insumoId, BigDecimal cantidad) {
        if (insumoId == null) return;
        if (empresaId == null) throw new IllegalArgumentException("Empresa es obligatoria");
        if (!inventoryService.hayStockSuficiente(empresaId, insumoId, cantidad)) {
            throw new IllegalArgumentException("Stock insuficiente. Requerido: " + cantidad);
        }
    }

    @Transactional
    public void descontarIngredientesReceta(InsumoCompuesto insumoCompuesto, BigDecimal cantidadPreparada, User usuario) {
        if (insumoCompuesto == null || insumoCompuesto.getId() == null) {
            throw new IllegalArgumentException("La receta es obligatoria");
        }
        if (cantidadPreparada == null || cantidadPreparada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad preparada debe ser mayor a cero");
        }
        Long empresaId = insumoCompuesto.getEmpresa() != null ? insumoCompuesto.getEmpresa().getId() : null;
        if (empresaId == null) {
            empresaId = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId()).map(e -> e.getId()).orElse(null);
        }
        if (empresaId == null) throw new IllegalArgumentException("No se pudo determinar la empresa");

        List<ComponenteInsumoCompuesto> componentes = componenteRepository.findByInsumoCompuesto(insumoCompuesto);
        if (componentes.isEmpty()) throw new IllegalArgumentException("La receta no tiene ingredientes configurados");

        for (ComponenteInsumoCompuesto componente : componentes) {
            BigDecimal cantidadNecesaria = componente.calcularCantidadNecesaria(cantidadPreparada);
            if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO &&
                    componente.getInsumo() != null && componente.getInsumo().getId() != null) {
                validarStockDisponible(empresaId, componente.getInsumo().getId(), cantidadNecesaria);
            } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO_COMPUESTO &&
                    componente.getInsumoCompuestoPadre() != null) {
                descontarIngredientesReceta(componente.getInsumoCompuestoPadre(), cantidadNecesaria, usuario);
            }
        }

        Long refId = insumoCompuesto.getId();
        Long usuarioId = usuario.getId();
        for (ComponenteInsumoCompuesto componente : componentes) {
            BigDecimal cantidadNecesaria = componente.calcularCantidadNecesaria(cantidadPreparada);
            if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO &&
                    componente.getInsumo() != null && componente.getInsumo().getId() != null) {
                InventoryResult r = inventoryService.consumir(empresaId, componente.getInsumo().getId(),
                        cantidadNecesaria, InventoryOrigin.PORCINOS, refId, usuarioId);
                if (!r.exito()) throw new IllegalArgumentException(r.mensaje());
            }
        }
    }
}

