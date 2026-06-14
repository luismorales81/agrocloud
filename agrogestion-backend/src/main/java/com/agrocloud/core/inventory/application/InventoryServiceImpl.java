package com.agrocloud.core.inventory.application;

import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.inventory.domain.MovimientoInventarioDTO;
import com.agrocloud.core.inventory.domain.ProductoInfo;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.inventory.domain.InventarioGrano;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.core.inventory.domain.MovimientoInventario;
import com.agrocloud.model.enums.TipoMovimiento;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.inventory.infrastructure.InventarioGranoRepository;
import com.agrocloud.core.inventory.infrastructure.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del contrato de inventario.
 * Única clase que modifica stock y persiste movimientos; usada por Cultivos y Porcinos.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InsumoRepository insumoRepository;
    private final InsumoCompuestoRepository insumoCompuestoRepository;
    private final InventarioGranoRepository inventarioGranoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final UserRepository userRepository;

    public InventoryServiceImpl(
            @Qualifier("insumoRepositoryInventario") InsumoRepository insumoRepository,
            @Qualifier("insumoCompuestoRepositoryInventario") InsumoCompuestoRepository insumoCompuestoRepository,
            @Qualifier("inventarioGranoRepositoryInventario") InventarioGranoRepository inventarioGranoRepository,
            @Qualifier("movimientoInventarioRepositoryInventario") MovimientoInventarioRepository movimientoInventarioRepository,
            @Qualifier("userRepositoryCore") UserRepository userRepository) {
        this.insumoRepository = insumoRepository;
        this.insumoCompuestoRepository = insumoCompuestoRepository;
        this.inventarioGranoRepository = inventarioGranoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public InventoryResult consumir(Long empresaId, Long productoId, BigDecimal cantidad,
                                    InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        Optional<Insumo> opt = insumoRepository.findById(productoId);
        if (opt.isEmpty()) {
            return InventoryResult.error("Producto no encontrado con ID: " + productoId);
        }
        Insumo insumo = opt.get();
        if (insumo.getEmpresa() == null || !insumo.getEmpresa().getId().equals(empresaId)) {
            return InventoryResult.error("El producto no pertenece a la empresa");
        }
        BigDecimal actual = insumo.getStockActual() != null ? insumo.getStockActual() : BigDecimal.ZERO;
        if (actual.compareTo(cantidad) < 0) {
            return InventoryResult.error(
                    String.format("Stock insuficiente. Disponible: %s, Requerido: %s", actual, cantidad));
        }
        BigDecimal nuevoStock = actual.subtract(cantidad);
        insumo.setStockActual(nuevoStock);
        insumoRepository.save(insumo);
        registrarMovimiento(insumo, TipoMovimiento.SALIDA, cantidad, origen, referenciaId, usuarioId,
                "Consumo - " + origen.name() + (referenciaId != null ? " ref: " + referenciaId : ""));
        return InventoryResult.ok(nuevoStock);
    }

    @Override
    @Transactional
    public InventoryResult reponer(Long empresaId, Long productoId, BigDecimal cantidad,
                                   InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        Optional<Insumo> opt = insumoRepository.findById(productoId);
        if (opt.isEmpty()) {
            return InventoryResult.error("Producto no encontrado con ID: " + productoId);
        }
        Insumo insumo = opt.get();
        if (insumo.getEmpresa() == null || !insumo.getEmpresa().getId().equals(empresaId)) {
            return InventoryResult.error("El producto no pertenece a la empresa");
        }
        BigDecimal actual = insumo.getStockActual() != null ? insumo.getStockActual() : BigDecimal.ZERO;
        BigDecimal nuevoStock = actual.add(cantidad);
        insumo.setStockActual(nuevoStock);
        insumoRepository.save(insumo);
        registrarMovimiento(insumo, TipoMovimiento.ENTRADA, cantidad, origen, referenciaId, usuarioId,
                "Reposición - " + origen.name() + (referenciaId != null ? " ref: " + referenciaId : ""));
        return InventoryResult.ok(nuevoStock);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarStock(Long empresaId, Long productoId) {
        Optional<Insumo> opt = insumoRepository.findById(productoId);
        if (opt.isEmpty()) return null;
        Insumo insumo = opt.get();
        if (insumo.getEmpresa() == null || !insumo.getEmpresa().getId().equals(empresaId)) {
            return null;
        }
        return insumo.getStockActual() != null ? insumo.getStockActual() : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayStockSuficiente(Long empresaId, Long productoId, BigDecimal cantidad) {
        BigDecimal stock = consultarStock(empresaId, productoId);
        return stock != null && cantidad != null && stock.compareTo(cantidad) >= 0;
    }

    @Override
    @Transactional
    public InventoryResult consumirPermitiendoNegativo(Long empresaId, Long productoId, BigDecimal cantidad,
                                                        InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        Optional<Insumo> opt = insumoRepository.findById(productoId);
        if (opt.isEmpty()) {
            return InventoryResult.error("Producto no encontrado con ID: " + productoId);
        }
        Insumo insumo = opt.get();
        if (insumo.getEmpresa() == null || !insumo.getEmpresa().getId().equals(empresaId)) {
            return InventoryResult.error("El producto no pertenece a la empresa");
        }
        BigDecimal actual = insumo.getStockActual() != null ? insumo.getStockActual() : BigDecimal.ZERO;
        BigDecimal nuevoStock = actual.subtract(cantidad);
        insumo.setStockActual(nuevoStock);
        insumoRepository.save(insumo);
        registrarMovimiento(insumo, TipoMovimiento.SALIDA, cantidad, origen, referenciaId, usuarioId,
                "Consumo (permite negativo) - " + (origen != null ? origen.name() : "") + (referenciaId != null ? " ref: " + referenciaId : ""));
        return InventoryResult.ok(nuevoStock);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoInfo> consultarProducto(Long empresaId, Long productoId) {
        Optional<Insumo> opt = insumoRepository.findById(productoId);
        if (opt.isEmpty()) return Optional.empty();
        Insumo i = opt.get();
        if (i.getEmpresa() == null || !i.getEmpresa().getId().equals(empresaId)) return Optional.empty();
        return Optional.of(new ProductoInfo(i.getNombre(), i.getUnidadMedida()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> obtenerEmpresaIdDeProducto(Long productoId) {
        return insumoRepository.findById(productoId)
                .filter(i -> i.getEmpresa() != null)
                .map(i -> i.getEmpresa().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> listarMovimientosPorInsumo(Long empresaId, Long productoId) {
        Optional<Insumo> opt = insumoRepository.findById(productoId);
        if (opt.isEmpty()) return List.of();
        if (opt.get().getEmpresa() == null || !opt.get().getEmpresa().getId().equals(empresaId)) return List.of();
        return movimientoInventarioRepository.findByInsumoIdOrderByFechaMovimientoDesc(productoId).stream()
                .map(this::aDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> listarMovimientosPorLaborCultivos(Long laborId) {
        return movimientoInventarioRepository.findByCultivosYLaborId(laborId).stream()
                .map(this::aDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoPorInsumo(Long empresaId, Long productoId) {
        Optional<Insumo> opt = insumoRepository.findById(productoId);
        if (opt.isEmpty()) return null;
        if (opt.get().getEmpresa() == null || !opt.get().getEmpresa().getId().equals(empresaId)) return null;
        return movimientoInventarioRepository.calcularSaldoByInsumoId(productoId);
    }

    // ----- Insumo compuesto -----

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarStockInsumoCompuesto(Long empresaId, Long insumoCompuestoId) {
        Optional<InsumoCompuesto> opt = insumoCompuestoRepository.findById(insumoCompuestoId);
        if (opt.isEmpty()) return null;
        InsumoCompuesto ic = opt.get();
        if (ic.getEmpresa() == null || !ic.getEmpresa().getId().equals(empresaId)) return null;
        return ic.getStockActual() != null ? ic.getStockActual() : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public InventoryResult consumirInsumoCompuesto(Long empresaId, Long insumoCompuestoId, BigDecimal cantidad,
                                                   InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        Optional<InsumoCompuesto> opt = insumoCompuestoRepository.findById(insumoCompuestoId);
        if (opt.isEmpty()) {
            return InventoryResult.error("Insumo compuesto no encontrado con ID: " + insumoCompuestoId);
        }
        InsumoCompuesto ic = opt.get();
        if (ic.getEmpresa() == null || !ic.getEmpresa().getId().equals(empresaId)) {
            return InventoryResult.error("El insumo compuesto no pertenece a la empresa");
        }
        BigDecimal actual = ic.getStockActual() != null ? ic.getStockActual() : BigDecimal.ZERO;
        if (actual.compareTo(cantidad) < 0) {
            return InventoryResult.error(
                    String.format("Stock insuficiente. Disponible: %s, Requerido: %s", actual, cantidad));
        }
        BigDecimal nuevoStock = actual.subtract(cantidad);
        ic.setStockActual(nuevoStock);
        insumoCompuestoRepository.save(ic);
        return InventoryResult.ok(nuevoStock);
    }

    @Override
    @Transactional
    public InventoryResult consumirInsumoCompuestoPermitiendoNegativo(Long empresaId, Long insumoCompuestoId,
                                                                       BigDecimal cantidad, InventoryOrigin origen,
                                                                       Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        Optional<InsumoCompuesto> opt = insumoCompuestoRepository.findById(insumoCompuestoId);
        if (opt.isEmpty()) {
            return InventoryResult.error("Insumo compuesto no encontrado con ID: " + insumoCompuestoId);
        }
        InsumoCompuesto ic = opt.get();
        if (ic.getEmpresa() == null || !ic.getEmpresa().getId().equals(empresaId)) {
            return InventoryResult.error("El insumo compuesto no pertenece a la empresa");
        }
        BigDecimal actual = ic.getStockActual() != null ? ic.getStockActual() : BigDecimal.ZERO;
        BigDecimal nuevoStock = actual.subtract(cantidad);
        ic.setStockActual(nuevoStock);
        insumoCompuestoRepository.save(ic);
        return InventoryResult.ok(nuevoStock);
    }

    @Override
    @Transactional
    public InventoryResult reponerInsumoCompuesto(Long empresaId, Long insumoCompuestoId, BigDecimal cantidad,
                                                  InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        Optional<InsumoCompuesto> opt = insumoCompuestoRepository.findById(insumoCompuestoId);
        if (opt.isEmpty()) {
            return InventoryResult.error("Insumo compuesto no encontrado con ID: " + insumoCompuestoId);
        }
        InsumoCompuesto ic = opt.get();
        if (ic.getEmpresa() == null || !ic.getEmpresa().getId().equals(empresaId)) {
            return InventoryResult.error("El insumo compuesto no pertenece a la empresa");
        }
        BigDecimal actual = ic.getStockActual() != null ? ic.getStockActual() : BigDecimal.ZERO;
        BigDecimal nuevoStock = actual.add(cantidad);
        ic.setStockActual(nuevoStock);
        insumoCompuestoRepository.save(ic);
        return InventoryResult.ok(nuevoStock);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayStockSuficienteInsumoCompuesto(Long empresaId, Long insumoCompuestoId, BigDecimal cantidad) {
        BigDecimal stock = consultarStockInsumoCompuesto(empresaId, insumoCompuestoId);
        return stock != null && cantidad != null && stock.compareTo(cantidad) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoInfo> consultarProductoInsumoCompuesto(Long empresaId, Long insumoCompuestoId) {
        Optional<InsumoCompuesto> opt = insumoCompuestoRepository.findById(insumoCompuestoId);
        if (opt.isEmpty()) return Optional.empty();
        InsumoCompuesto ic = opt.get();
        if (ic.getEmpresa() == null || !ic.getEmpresa().getId().equals(empresaId)) return Optional.empty();
        return Optional.of(new ProductoInfo(ic.getNombre(), ic.getUnidadMedida()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> obtenerEmpresaIdDeInsumoCompuesto(Long insumoCompuestoId) {
        return insumoCompuestoRepository.findById(insumoCompuestoId)
                .filter(ic -> ic.getEmpresa() != null)
                .map(ic -> ic.getEmpresa().getId());
    }

    // ----- Grano propio (FIFO por cultivo) -----

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarStockGrano(Long empresaId, Long cultivoId) {
        List<InventarioGrano> inventarios = inventarioGranoRepository
                .findByCultivoIdOrderByFechaIngresoDesc(cultivoId);
        return inventarios.stream()
                .filter(inv -> "DISPONIBLE".equals(inv.getEstado()))
                .filter(inv -> inv.getCantidadDisponible() != null && inv.getCantidadDisponible().compareTo(BigDecimal.ZERO) > 0)
                .map(InventarioGrano::getCantidadDisponible)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public InventoryResult consumirGrano(Long empresaId, Long cultivoId, BigDecimal cantidad,
                                         InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        BigDecimal stockTotal = consultarStockGrano(empresaId, cultivoId);
        if (stockTotal.compareTo(cantidad) < 0) {
            return InventoryResult.error(
                    String.format("Stock insuficiente de grano. Disponible: %s, Requerido: %s", stockTotal, cantidad));
        }
        return aplicarConsumoGranoFIFO(empresaId, cultivoId, cantidad, origen, referenciaId, usuarioId);
    }

    @Override
    @Transactional
    public InventoryResult consumirGranoPermitiendoNegativo(Long empresaId, Long cultivoId, BigDecimal cantidad,
                                                            InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        return aplicarConsumoGranoFIFO(empresaId, cultivoId, cantidad, origen, referenciaId, usuarioId);
    }

    private InventoryResult aplicarConsumoGranoFIFO(Long empresaId, Long cultivoId, BigDecimal cantidad,
                                                     InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        List<InventarioGrano> inventarios = inventarioGranoRepository
                .findByCultivoIdOrderByFechaIngresoDesc(cultivoId);
        List<InventarioGrano> disponibles = inventarios.stream()
                .filter(inv -> "DISPONIBLE".equals(inv.getEstado()))
                .filter(inv -> inv.getCantidadDisponible() != null && inv.getCantidadDisponible().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        BigDecimal cantidadRestante = cantidad;
        for (InventarioGrano inv : disponibles) {
            if (cantidadRestante.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal aDescontar = cantidadRestante.min(inv.getCantidadDisponible());
            BigDecimal nuevo = inv.getCantidadDisponible().subtract(aDescontar);
            inv.setCantidadDisponible(nuevo);
            if (inv.getCantidadDisponible().compareTo(BigDecimal.ZERO) == 0) {
                inv.setEstado("AGOTADO");
            }
            inventarioGranoRepository.save(inv);
            cantidadRestante = cantidadRestante.subtract(aDescontar);
        }
        BigDecimal stockFinal = consultarStockGrano(empresaId, cultivoId);
        return InventoryResult.ok(stockFinal);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayStockSuficienteGrano(Long empresaId, Long cultivoId, BigDecimal cantidad) {
        BigDecimal stock = consultarStockGrano(empresaId, cultivoId);
        return cantidad != null && stock.compareTo(cantidad) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> consultarNombreCultivo(Long empresaId, Long cultivoId) {
        List<InventarioGrano> inventarios = inventarioGranoRepository
                .findByCultivoIdOrderByFechaIngresoDesc(cultivoId);
        return inventarios.stream()
                .map(InventarioGrano::getCultivo)
                .filter(c -> c != null)
                .map(c -> c.getNombre())
                .findFirst();
    }

    @Override
    @Transactional
    public InventoryResult consumirGranoPorInventarioId(Long empresaId, Long inventarioId, BigDecimal cantidad,
                                                         InventoryOrigin origen, Long referenciaId, Long usuarioId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryResult.error("La cantidad debe ser mayor que cero");
        }
        Optional<InventarioGrano> opt = inventarioGranoRepository.findById(inventarioId);
        if (opt.isEmpty()) {
            return InventoryResult.error("Inventario de grano no encontrado con ID: " + inventarioId);
        }
        InventarioGrano inv = opt.get();
        if (inv.getCultivo() == null || inv.getCultivo().getEmpresa() == null
                || !inv.getCultivo().getEmpresa().getId().equals(empresaId)) {
            return InventoryResult.error("El inventario no pertenece a la empresa");
        }
        BigDecimal actual = inv.getCantidadDisponible() != null ? inv.getCantidadDisponible() : BigDecimal.ZERO;
        if (actual.compareTo(cantidad) < 0) {
            return InventoryResult.error(
                    String.format("Stock insuficiente. Disponible: %s, Requerido: %s", actual, cantidad));
        }
        BigDecimal nuevo = actual.subtract(cantidad);
        inv.setCantidadDisponible(nuevo);
        if (nuevo.compareTo(BigDecimal.ZERO) == 0) {
            inv.setEstado("AGOTADO");
        }
        inventarioGranoRepository.save(inv);
        return InventoryResult.ok(nuevo);
    }

    private MovimientoInventarioDTO aDTO(MovimientoInventario m) {
        return new MovimientoInventarioDTO(
                m.getId(),
                m.getFechaMovimiento(),
                m.getTipoMovimiento() != null ? m.getTipoMovimiento().name() : null,
                m.getCantidad(),
                m.getMotivo(),
                m.getOrigen(),
                m.getReferenciaId(),
                m.getInsumo() != null ? m.getInsumo().getId() : null
        );
    }

    private void registrarMovimiento(Insumo insumo, TipoMovimiento tipo, BigDecimal cantidad,
                                     InventoryOrigin origen, Long referenciaId, Long usuarioId, String motivo) {
        MovimientoInventario m = new MovimientoInventario();
        m.setInsumo(insumo);
        m.setTipoMovimiento(tipo);
        m.setCantidad(cantidad);
        m.setFechaMovimiento(LocalDateTime.now());
        m.setMotivo(motivo);
        m.setOrigen(origen != null ? origen.name() : null);
        m.setReferenciaId(referenciaId);
        if (usuarioId != null) {
            userRepository.findById(usuarioId).ifPresent(m::setUsuario);
        }
        // saveAndFlush para forzar el INSERT inmediato: si hay fallo de esquema, FK o constraint,
        // la excepción SQL real se verá aquí en vez del AssertionFailure posterior
        movimientoInventarioRepository.saveAndFlush(m);
    }
}
