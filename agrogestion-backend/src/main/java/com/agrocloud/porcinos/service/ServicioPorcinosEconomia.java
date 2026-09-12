package com.agrocloud.porcinos.service;

import com.agrocloud.core.inventory.domain.MovimientoInventario;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.inventory.infrastructure.MovimientoInventarioRepository;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.model.enums.TipoMovimiento;
import com.agrocloud.porcinos.model.dto.PorcinosResumenEconomicoRespuesta;
import com.agrocloud.porcinos.model.entity.PorcinosConsumo;
import com.agrocloud.porcinos.model.entity.PorcinosEventoSanitario;
import com.agrocloud.porcinos.model.entity.PorcinosLote;
import com.agrocloud.porcinos.repository.PorcinosConsumoRepository;
import com.agrocloud.porcinos.repository.PorcinosEventoSanitarioRepository;
import com.agrocloud.porcinos.repository.PorcinosVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ServicioPorcinosEconomia {

    private static final String ORIGEN_INVENTARIO = "PORCINOS";

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioPorcinosLotes servicioLotes;
    private final PorcinosConsumoRepository consumoRepository;
    private final PorcinosEventoSanitarioRepository eventoSanitarioRepository;
    private final PorcinosVentaRepository ventaRepository;
    private final InsumoRepository insumoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public ServicioPorcinosEconomia(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioPorcinosLotes servicioLotes,
            PorcinosConsumoRepository consumoRepository,
            PorcinosEventoSanitarioRepository eventoSanitarioRepository,
            PorcinosVentaRepository ventaRepository,
            InsumoRepository insumoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.consumoRepository = consumoRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.ventaRepository = ventaRepository;
        this.insumoRepository = insumoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Transactional(readOnly = true)
    public PorcinosResumenEconomicoRespuesta resumenEconomicoLote(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        PorcinosLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);

        List<PorcinosConsumo> consumos = consumoRepository.listarPorLoteIdYEmpresaId(loteId, empresaId);
        BigDecimal totalAlimentoKg = BigDecimal.ZERO;
        BigDecimal costoAlimento = BigDecimal.ZERO;
        boolean hayPrecioAlimento = false;
        for (PorcinosConsumo c : consumos) {
            totalAlimentoKg = totalAlimentoKg.add(c.getCantidadKg());
            var insumo = insumoRepository.findById(c.getInsumoId()).orElse(null);
            if (insumo != null && insumo.getPrecioUnitario() != null) {
                costoAlimento = costoAlimento.add(insumo.getPrecioUnitario().multiply(c.getCantidadKg()));
                hayPrecioAlimento = true;
            }
        }

        List<PorcinosEventoSanitario> eventos =
                eventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresaId);
        List<Long> refsEventos = eventos.stream().map(PorcinosEventoSanitario::getId).toList();
        BigDecimal costoSanidad = calcularCostoMovimientos(refsEventos);

        BigDecimal ingresos = ventaRepository.sumarTotalPorLoteIdYEmpresaId(loteId, empresaId);
        if (ingresos == null) {
            ingresos = BigDecimal.ZERO;
        }

        BigDecimal costoAcumulado = BigDecimal.ZERO;
        if (hayPrecioAlimento) {
            costoAcumulado = costoAcumulado.add(costoAlimento);
        }
        if (costoSanidad != null) {
            costoAcumulado = costoAcumulado.add(costoSanidad);
        }

        PorcinosResumenEconomicoRespuesta resp = new PorcinosResumenEconomicoRespuesta();
        resp.setLoteId(lote.getId());
        resp.setLoteNombre(lote.getNombre());
        resp.setTotalAlimentoKg(totalAlimentoKg.setScale(2, RoundingMode.HALF_UP));
        resp.setCostoAlimento(hayPrecioAlimento ? costoAlimento.setScale(2, RoundingMode.HALF_UP) : null);
        resp.setCostoSanidad(costoSanidad != null ? costoSanidad.setScale(2, RoundingMode.HALF_UP) : null);
        resp.setCostoAcumulado(costoAcumulado.setScale(2, RoundingMode.HALF_UP));
        resp.setIngresosVentas(ingresos.setScale(2, RoundingMode.HALF_UP));
        resp.setMargen(ingresos.subtract(costoAcumulado).setScale(2, RoundingMode.HALF_UP));
        return resp;
    }

    private BigDecimal calcularCostoMovimientos(List<Long> referenciaIds) {
        if (referenciaIds.isEmpty()) {
            return null;
        }
        List<MovimientoInventario> movimientos = movimientoInventarioRepository
                .listarSalidasPorOrigenYReferencias(ORIGEN_INVENTARIO, referenciaIds);
        if (movimientos.isEmpty()) {
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (MovimientoInventario m : movimientos) {
            if (m.getTipoMovimiento() != TipoMovimiento.SALIDA) {
                continue;
            }
            var insumo = m.getInsumo();
            if (insumo != null && insumo.getPrecioUnitario() != null) {
                total = total.add(insumo.getPrecioUnitario().multiply(m.getCantidad()));
            }
        }
        return total.compareTo(BigDecimal.ZERO) > 0 ? total : null;
    }
}
