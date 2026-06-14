package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.porcinos.domain.ConsumoAlimento;
import com.agrocloud.porcinos.domain.StockAlimento;
import com.agrocloud.porcinos.infrastructure.ConsumoAlimentoRepository;
import com.agrocloud.porcinos.infrastructure.StockAlimentoRepository;
import com.agrocloud.core.inventory.infrastructure.InventarioGranoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar consumos de alimento
 * Incluye validaciones de stock y automatismos para descontar inventario
 */
@Service
public class ConsumoAlimentoService {

    @Autowired
    private ConsumoAlimentoRepository consumoAlimentoRepository;

    @Autowired
    private StockAlimentoRepository stockAlimentoRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private EmpresaContextService empresaContextService;

    /**
     * Registrar consumo de alimento con validaciones de stock
     */
    @Transactional
    public ConsumoAlimento registrarConsumo(ConsumoAlimento consumoData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Validar cantidad
        if (consumoData.getCantidadKg() == null || consumoData.getCantidadKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        // VALIDAR STOCK antes de registrar el consumo
        validarYDescontarStock(consumoData, empresaActiva.get());

        consumoData.setEmpresa(empresaActiva.get());
        consumoData.setUsuario(user);
        consumoData.setActivo(true);

        // Si no tiene fecha, usar la fecha actual
        if (consumoData.getFecha() == null) {
            consumoData.setFecha(LocalDate.now());
        }

        ConsumoAlimento consumoGuardado = consumoAlimentoRepository.save(consumoData);

        // AUTOMATISMO: Restar stock automáticamente (ya se hizo en validarYDescontarStock)
        // pero lo confirmamos aquí

        return consumoGuardado;
    }

    /**
     * Validar stock disponible y descontarlo
     */
    private void validarYDescontarStock(ConsumoAlimento consumo, Empresa empresa) {
        BigDecimal cantidadRequerida = consumo.getCantidadKg();

        if (consumo.getTipoAlimento() == ConsumoAlimento.TipoAlimento.GRANO_PROPIO) {
            if (consumo.getCultivoRelacionadoId() != null) {
                // GRANO_PROPIO: validar y descontar vía core.inventory (FIFO)
                Long empresaId = empresa.getId();
                Long cultivoId = consumo.getCultivoRelacionadoId();
                InventoryResult r = inventoryService.consumirGrano(empresaId, cultivoId, cantidadRequerida,
                        InventoryOrigin.PORCINOS, null, null);
                if (!r.exito()) {
                    throw new IllegalArgumentException(r.mensaje() != null ? r.mensaje() : "Stock insuficiente de grano propio");
                }
            } else {
                // Buscar por nombre en StockAlimento
                Optional<StockAlimento> stockOpt = stockAlimentoRepository
                    .findByNombreAndEmpresa(consumo.getObservaciones() != null ? 
                        consumo.getObservaciones() : "Grano Propio", empresa);
                
                if (stockOpt.isPresent()) {
                    StockAlimento stock = stockOpt.get();
                    if (stock.getCantidadDisponible().compareTo(cantidadRequerida) < 0) {
                        throw new IllegalArgumentException(
                            String.format("Stock insuficiente. Disponible: %.2f kg, Requerido: %.2f kg",
                                stock.getCantidadDisponible(), cantidadRequerida));
                    }
                    stock.setCantidadDisponible(stock.getCantidadDisponible().subtract(cantidadRequerida));
                    stockAlimentoRepository.save(stock);
                } else {
                    // Si no hay stock registrado, permitir el consumo pero registrar advertencia
                    // En producción esto debería ser más estricto
                }
            }
        } else if (consumo.getTipoAlimento() == ConsumoAlimento.TipoAlimento.BALANCEADO) {
            // Validar stock de balanceado (StockAlimento con tipo INSUMO)
            // Buscar stock de balanceado por nombre o insumo
            // Por ahora permitimos el consumo sin validación estricta de stock
            // En producción se debería tener un control de stock de balanceados
        }
    }

    public List<ConsumoAlimento> obtenerConsumos(User user, LocalDate fechaDesde, LocalDate fechaHasta) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        if (fechaDesde != null && fechaHasta != null) {
            return consumoAlimentoRepository.findByEmpresaAndRangoFechas(empresaActiva.get(), fechaDesde, fechaHasta);
        }

        return consumoAlimentoRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    public BigDecimal obtenerConsumoTotalPorCategoria(User user, ConsumoAlimento.CategoriaAlimento categoria, 
                                                      LocalDate fechaDesde, LocalDate fechaHasta) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = consumoAlimentoRepository.sumCantidadByCategoriaAndRangoFechas(
            empresaActiva.get(), categoria, fechaDesde, fechaHasta);
        return total != null ? total : BigDecimal.ZERO;
    }
}
