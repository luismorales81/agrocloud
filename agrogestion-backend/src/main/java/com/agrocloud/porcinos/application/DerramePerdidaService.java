package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.porcinos.domain.DerramePerdida;
import com.agrocloud.porcinos.domain.DiaAlimentacion;
import com.agrocloud.porcinos.domain.MovimientoStockPorcino;
import com.agrocloud.porcinos.infrastructure.DerramePerdidaRepository;
import com.agrocloud.porcinos.infrastructure.DiaAlimentacionRepository;
import com.agrocloud.porcinos.infrastructure.MovimientoStockPorcinoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar derrames, pérdidas y accidentes de insumos
 * 
 * REGLAS:
 * - Impactan stock INMEDIATAMENTE (no permite negativo)
 * - Se registran como movimientos de stock independientes
 * - Requieren motivo y observaciones obligatorias
 * - Se pueden asociar a un día pero NO modifican el consumo del día
 * 
 * INVENTARIO: Insumo e InsumoCompuesto se descuentan vía core.inventory (InventoryService).
 * GRANO_PROPIO se actualiza aquí (InventarioGrano) hasta que core.inventory lo soporte.
 */
@Service
public class DerramePerdidaService {

    @Autowired
    private DerramePerdidaRepository derrameRepository;

    @Autowired
    private MovimientoStockPorcinoRepository movimientoStockRepository;

    @Autowired
    private DiaAlimentacionRepository diaAlimentacionRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    @Qualifier("insumoCompuestoRepositoryInventario")
        private InsumoCompuestoRepository insumoCompuestoRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    @Qualifier("campanaContextServiceCore")
    private com.agrocloud.core.application.CampanaContextService campanaContextService;

    @Autowired
    private CultivoRepository cultivoRepository;

    /**
     * Registrar derrame/pérdida
     * Descuenta stock INMEDIATAMENTE (NO permite negativo)
     */
    @Transactional
    public DerramePerdida registrarDerrame(DerramePerdida derrameData, User usuario) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId());
        if (empresaOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Validar campos obligatorios
        if (derrameData.getMotivo() == null || derrameData.getMotivo().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        if (derrameData.getObservaciones() == null || derrameData.getObservaciones().trim().isEmpty()) {
            throw new IllegalArgumentException("Las observaciones son obligatorias");
        }
        if (derrameData.getCantidad() == null || derrameData.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (derrameData.getFecha() == null) {
            derrameData.setFecha(LocalDate.now());
        }

        derrameData.setEmpresa(empresaOpt.get());
        derrameData.setUsuario(usuario);

        // Asociar a día si se proporciona (solo referencia, no modifica consumo)
        if (derrameData.getDiaAlimentacion() != null && derrameData.getDiaAlimentacion().getId() != null) {
            Optional<DiaAlimentacion> diaOpt = diaAlimentacionRepository
                .findById(derrameData.getDiaAlimentacion().getId());
            if (diaOpt.isPresent()) {
                derrameData.setDiaAlimentacion(diaOpt.get());
            }
        }

        // Descontar stock INMEDIATAMENTE (NO permite negativo)
        BigDecimal stockAnterior;
        BigDecimal stockPosterior;

        if (derrameData.getTipoInsumo() == DerramePerdida.TipoInsumo.INSUMO && derrameData.getInsumo() != null) {
            Long empresaId = empresaOpt.get().getId();
            Long insumoId = derrameData.getInsumo().getId();
            BigDecimal cantidad = derrameData.getCantidad();
            stockAnterior = inventoryService.consultarStock(empresaId, insumoId);
            if (stockAnterior == null) throw new IllegalArgumentException("Insumo no encontrado");
            if (stockAnterior.compareTo(cantidad) < 0) {
                throw new IllegalArgumentException(
                    String.format("Stock insuficiente para registrar derrame. Disponible: %.2f, Requerido: %.2f",
                        stockAnterior, cantidad));
            }
            InventoryResult r = inventoryService.consumir(empresaId, insumoId, cantidad,
                    InventoryOrigin.PORCINOS, null, usuario.getId());
            if (!r.exito()) throw new IllegalArgumentException(r.mensaje());
            stockPosterior = r.stockRestante();
            inventoryService.consultarProducto(empresaId, insumoId).ifPresent(info -> {
                derrameData.setNombreInsumo(info.nombre());
                derrameData.setUnidadMedida(info.unidadMedida());
            });
            Insumo ref = new Insumo();
            ref.setId(insumoId);
            derrameData.setInsumo(ref);
            derrameData.setStockAnterior(stockAnterior);
            derrameData.setStockPosterior(stockPosterior);

        } else if (derrameData.getTipoInsumo() == DerramePerdida.TipoInsumo.GRANO_PROPIO && derrameData.getCultivoId() != null) {
            // GRANO_PROPIO: descuento vía core.inventory (FIFO)
            Long empresaId = empresaOpt.get().getId();
            Long cultivoId = derrameData.getCultivoId();
            stockAnterior = inventoryService.consultarStockGrano(empresaId, cultivoId);
            if (stockAnterior.compareTo(derrameData.getCantidad()) < 0) {
                throw new IllegalArgumentException(
                    String.format("Stock insuficiente para registrar derrame de grano propio. Disponible: %.2f kg, Requerido: %.2f kg",
                        stockAnterior, derrameData.getCantidad()));
            }
            InventoryResult r = inventoryService.consumirGrano(empresaId, cultivoId, derrameData.getCantidad(),
                    InventoryOrigin.PORCINOS, null, usuario.getId());
            if (!r.exito()) throw new IllegalArgumentException(r.mensaje());
            stockPosterior = r.stockRestante();
            derrameData.setStockAnterior(stockAnterior);
            derrameData.setStockPosterior(stockPosterior);
            inventoryService.consultarNombreCultivo(empresaId, cultivoId).ifPresent(derrameData::setNombreInsumo);
            derrameData.setUnidadMedida("kg");

        } else if (derrameData.getTipoInsumo() == DerramePerdida.TipoInsumo.INSUMO_COMPUESTO && derrameData.getInsumoCompuesto() != null) {
            // INSUMO_COMPUESTO: descuento vía core.inventory (InventoryService)
            Long empresaId = empresaOpt.get().getId();
            Long insumoCompuestoId = derrameData.getInsumoCompuesto().getId();
            stockAnterior = inventoryService.consultarStockInsumoCompuesto(empresaId, insumoCompuestoId);
            if (stockAnterior == null) {
                throw new IllegalArgumentException("Insumo compuesto no encontrado");
            }
            if (stockAnterior.compareTo(derrameData.getCantidad()) < 0) {
                throw new IllegalArgumentException(
                    String.format("Stock insuficiente para registrar derrame. Disponible: %.2f, Requerido: %.2f",
                        stockAnterior, derrameData.getCantidad()));
            }
            InventoryResult r = inventoryService.consumirInsumoCompuesto(empresaId, insumoCompuestoId,
                    derrameData.getCantidad(), InventoryOrigin.PORCINOS, null, usuario.getId());
            if (!r.exito()) throw new IllegalArgumentException(r.mensaje());
            stockPosterior = r.stockRestante();
            inventoryService.consultarProductoInsumoCompuesto(empresaId, insumoCompuestoId).ifPresent(info -> {
                derrameData.setNombreInsumo(info.nombre());
                derrameData.setUnidadMedida(info.unidadMedida());
            });
            InsumoCompuesto ref = insumoCompuestoRepository.findById(insumoCompuestoId)
                    .orElseThrow(() -> new IllegalArgumentException("Insumo compuesto no encontrado"));
            derrameData.setInsumoCompuesto(ref);
            derrameData.setStockAnterior(stockAnterior);
            derrameData.setStockPosterior(stockPosterior);

        } else {
            throw new IllegalArgumentException("Debe especificar un insumo válido (insumo, cultivo o insumo compuesto)");
        }

        // Guardar derrame
        derrameData.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaOpt.get().getId()));
        DerramePerdida derrameGuardado = derrameRepository.save(derrameData);

        // Registrar movimiento de stock (tipo DERRAME, NO permite negativo)
        // Usar setters para establecer el tipo correcto (los setters ya manejan la conversión)
        MovimientoStockPorcino movimiento = new MovimientoStockPorcino();
        movimiento.setEmpresa(empresaOpt.get());
        movimiento.setTipoMovimiento(MovimientoStockPorcino.TipoMovimiento.DERRAME);
        movimiento.setFechaMovimiento(derrameData.getFecha());
        movimiento.setCantidad(derrameData.getCantidad());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockPosterior(stockPosterior);
        movimiento.setPermiteNegativo(false); // NO permite negativo para derrames
        
        movimiento.setInsumo(derrameData.getInsumo());
        movimiento.setCultivoId(derrameData.getCultivoId());
        movimiento.setInsumoCompuesto(derrameData.getInsumoCompuesto());
        
        // Si no se pudo determinar el tipo mediante los setters, determinarlo manualmente
        if (movimiento.getTipoInsumo() == null) {
            if (derrameData.getTipoInsumo() == DerramePerdida.TipoInsumo.INSUMO) {
                movimiento.setTipoInsumo(MovimientoStockPorcino.TipoInsumo.INSUMO);
            } else if (derrameData.getTipoInsumo() == DerramePerdida.TipoInsumo.GRANO_PROPIO) {
                movimiento.setTipoInsumo(MovimientoStockPorcino.TipoInsumo.GRANO_PROPIO);
            } else if (derrameData.getTipoInsumo() == DerramePerdida.TipoInsumo.INSUMO_COMPUESTO) {
                movimiento.setTipoInsumo(MovimientoStockPorcino.TipoInsumo.INSUMO_COMPUESTO);
            } else {
                throw new IllegalArgumentException("No se pudo determinar el tipo de insumo");
            }
        }

        movimiento.setDerrame(derrameGuardado);
        movimiento.setUsuario(usuario);
        movimiento.setMotivo(derrameData.getMotivo());
        movimiento.setObservaciones(derrameData.getObservaciones());
        movimiento.setLoteId(derrameData.getLoteId());

        if (derrameData.getDiaAlimentacion() != null) {
            movimiento.setDiaAlimentacion(derrameData.getDiaAlimentacion());
        }

        movimientoStockRepository.save(movimiento);

        return derrameGuardado;
    }

    /**
     * Obtener derrames por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<DerramePerdida> obtenerDerrames(LocalDate fechaDesde, LocalDate fechaHasta, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            return List.of();
        }

        if (fechaDesde == null) {
            fechaDesde = LocalDate.now().minusMonths(1);
        }
        if (fechaHasta == null) {
            fechaHasta = LocalDate.now();
        }

        return derrameRepository.findByEmpresaAndFechaBetweenOrderByFechaDesc(
            empresaOpt.get(), fechaDesde, fechaHasta);
    }

    /**
     * Obtener derrames por día (referencia)
     */
    @Transactional(readOnly = true)
    public List<DerramePerdida> obtenerDerramesPorDia(LocalDate fecha, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            return List.of();
        }

        Optional<DiaAlimentacion> diaOpt = diaAlimentacionRepository
            .findByEmpresaAndFecha(empresaOpt.get(), fecha);

        if (diaOpt.isEmpty()) {
            return List.of();
        }

        List<DerramePerdida> derrames = derrameRepository.findByDiaAlimentacionOrderByFechaDesc(diaOpt.get());

        // Poblar nombres e información adicional
        for (DerramePerdida derrame : derrames) {
            if (derrame.getInsumo() != null) {
                derrame.setNombreInsumo(derrame.getInsumo().getNombre());
                derrame.setUnidadMedida(derrame.getInsumo().getUnidadMedida());
            } else if (derrame.getCultivoId() != null) {
                derrame.setNombreInsumo(cultivoRepository.findById(derrame.getCultivoId()).map(c -> c.getNombre()).orElse(null));
                derrame.setUnidadMedida("kg");
            } else if (derrame.getInsumoCompuesto() != null) {
                derrame.setNombreInsumo(derrame.getInsumoCompuesto().getNombre());
                derrame.setUnidadMedida(derrame.getInsumoCompuesto().getUnidadMedida());
            }
        }

        return derrames;
    }
}
