package com.agrocloud.feedlot.service;

import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotCloseoutRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotResumenRespuesta;
import com.agrocloud.feedlot.model.entity.FeedlotConsumo;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotMuerte;
import com.agrocloud.feedlot.model.entity.FeedlotPesada;
import com.agrocloud.feedlot.model.entity.FeedlotVenta;
import com.agrocloud.feedlot.model.enums.FeedlotMetodoCloseout;
import com.agrocloud.feedlot.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ServicioFeedlotCloseout {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioFeedlotLotes servicioLotes;
    private final ServicioFeedlotConfiguracion servicioConfiguracion;
    private final FeedlotPesadaRepository pesadaRepository;
    private final FeedlotConsumoRepository consumoRepository;
    private final FeedlotMuerteRepository muerteRepository;
    private final FeedlotVentaRepository ventaRepository;
    private final InsumoRepository insumoRepository;

    public ServicioFeedlotCloseout(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioFeedlotLotes servicioLotes,
            ServicioFeedlotConfiguracion servicioConfiguracion,
            FeedlotPesadaRepository pesadaRepository,
            FeedlotConsumoRepository consumoRepository,
            FeedlotMuerteRepository muerteRepository,
            FeedlotVentaRepository ventaRepository,
            @Qualifier("insumoRepositoryInventario") InsumoRepository insumoRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.servicioConfiguracion = servicioConfiguracion;
        this.pesadaRepository = pesadaRepository;
        this.consumoRepository = consumoRepository;
        this.muerteRepository = muerteRepository;
        this.ventaRepository = ventaRepository;
        this.insumoRepository = insumoRepository;
    }

    @Transactional(readOnly = true)
    public FeedlotCloseoutRespuesta calcularCloseout(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return construirCloseout(lote, empresaId);
    }

    @Transactional(readOnly = true)
    public FeedlotResumenRespuesta calcularResumen(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        FeedlotCloseoutRespuesta closeout = construirCloseout(lote, empresaId);

        FeedlotResumenRespuesta r = new FeedlotResumenRespuesta();
        r.setLoteId(loteId);
        r.setCabezasActuales(lote.getCabezasActuales());
        r.setCabezasInicial(lote.getCabezasInicial());
        r.setDiasEnFeedlot(closeout.getDiasEnFeedlot());
        r.setPesoActualKg(closeout.getPesoActualKg());
        r.setGmd(closeout.getGmd());
        r.setMortalidadPct(closeout.getMortalidadPct());
        r.setTotalAlimentoKg(closeout.getTotalAlimentoKg());
        r.setConversionAlimenticia(closeout.getConversionAlimenticia());
        if (closeout.getHeadDays() != null && closeout.getHeadDays().compareTo(BigDecimal.ZERO) > 0) {
            r.setConsumoCabDia(closeout.getTotalAlimentoKg()
                    .divide(closeout.getHeadDays(), 4, RoundingMode.HALF_UP));
        }
        return r;
    }

    public FeedlotCloseoutRespuesta construirCloseout(FeedlotLote lote, Long empresaId) {
        FeedlotMetodoCloseout metodo = servicioConfiguracion.resolverMetodoCloseout(empresaId);
        LocalDate fechaReferencia = lote.getFechaCierre() != null ? lote.getFechaCierre() : LocalDate.now();
        long dias = Math.max(1, ChronoUnit.DAYS.between(lote.getFechaIngreso(), fechaReferencia));

        BigDecimal pesoIngreso = lote.getPesoPromedioIngresoKg();
        BigDecimal pesoActual = pesoIngreso;
        List<FeedlotPesada> pesadas = pesadaRepository.listarPorLoteIdYEmpresaId(lote.getId(), empresaId);
        if (!pesadas.isEmpty() && pesadas.get(0).getPesoPromedioKg() != null) {
            pesoActual = pesadas.get(0).getPesoPromedioKg();
        }

        BigDecimal gmd = pesoActual.subtract(pesoIngreso)
                .divide(BigDecimal.valueOf(dias), 4, RoundingMode.HALF_UP);

        Integer totalMuertes = muerteRepository.sumarCabezasPorLoteIdYEmpresaId(lote.getId(), empresaId);
        if (totalMuertes == null) {
            totalMuertes = 0;
        }
        int cabezasInicial = lote.getCabezasInicial() != null ? lote.getCabezasInicial() : 0;
        BigDecimal mortalidadPct = cabezasInicial > 0
                ? BigDecimal.valueOf(totalMuertes * 100.0 / cabezasInicial).setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalAlimentoKg = consumoRepository.sumarCantidadKgPorLoteIdYEmpresaId(lote.getId(), empresaId);
        if (totalAlimentoKg == null) {
            totalAlimentoKg = BigDecimal.ZERO;
        }
        BigDecimal totalAlimentoMsKg = consumoRepository.sumarMateriaSecaKgPorLoteIdYEmpresaId(lote.getId(), empresaId);
        if (totalAlimentoMsKg == null) {
            totalAlimentoMsKg = BigDecimal.ZERO;
        }

        int cabezasParaGanancia = metodo == FeedlotMetodoCloseout.DEADS_OUT
                ? Math.max(0, cabezasInicial - totalMuertes)
                : cabezasInicial;
        BigDecimal kgGanados = pesoActual.subtract(pesoIngreso)
                .multiply(BigDecimal.valueOf(cabezasParaGanancia));

        BigDecimal conversion = null;
        if (kgGanados.compareTo(BigDecimal.ZERO) > 0) {
            conversion = totalAlimentoKg.divide(kgGanados, 4, RoundingMode.HALF_UP);
        }

        int cabezasActuales = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        BigDecimal headDays = calcularHeadDaysPorIntervalos(lote, empresaId, fechaReferencia);

        BigDecimal costoCompra = null;
        if (lote.getPrecioCompraKg() != null && pesoIngreso != null) {
            costoCompra = lote.getPrecioCompraKg()
                    .multiply(pesoIngreso)
                    .multiply(BigDecimal.valueOf(cabezasInicial));
        }

        BigDecimal costoAlimento = calcularCostoAlimento(lote.getId(), empresaId);
        BigDecimal costoHoteleria = null;
        if (lote.getCostoHoteleriaDia() != null) {
            costoHoteleria = lote.getCostoHoteleriaDia().multiply(headDays);
        }

        BigDecimal costoAcumulado = BigDecimal.ZERO;
        if (costoCompra != null) {
            costoAcumulado = costoAcumulado.add(costoCompra);
        }
        if (costoAlimento != null) {
            costoAcumulado = costoAcumulado.add(costoAlimento);
        }
        if (costoHoteleria != null) {
            costoAcumulado = costoAcumulado.add(costoHoteleria);
        }

        BigDecimal ingresosVentas = ventaRepository.sumarTotalPorLoteIdYEmpresaId(lote.getId(), empresaId);
        if (ingresosVentas == null) {
            ingresosVentas = BigDecimal.ZERO;
        }
        BigDecimal margen = ingresosVentas.subtract(costoAcumulado);

        BigDecimal breakevenKg = null;
        Integer cabezasVendidas = ventaRepository.sumarCabezasVendidasPorLoteIdYEmpresaId(lote.getId(), empresaId);
        if (cabezasVendidas != null && cabezasVendidas > 0 && pesoActual.compareTo(BigDecimal.ZERO) > 0
                && costoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal denominador = BigDecimal.valueOf(cabezasVendidas).multiply(pesoActual);
            breakevenKg = costoAcumulado.divide(denominador, 4, RoundingMode.HALF_UP);
        }

        FeedlotCloseoutRespuesta resp = new FeedlotCloseoutRespuesta();
        resp.setLoteId(lote.getId());
        resp.setLoteNombre(lote.getNombre());
        resp.setFechaIngreso(lote.getFechaIngreso());
        resp.setFechaReferencia(fechaReferencia);
        resp.setDiasEnFeedlot(dias);
        resp.setCabezasInicial(cabezasInicial);
        resp.setCabezasActuales(cabezasActuales);
        resp.setTotalMuertes(totalMuertes);
        resp.setMortalidadPct(mortalidadPct);
        resp.setPesoIngresoKg(pesoIngreso);
        resp.setPesoActualKg(pesoActual);
        resp.setGmd(gmd);
        resp.setKgGanados(kgGanados);
        resp.setTotalAlimentoKg(totalAlimentoKg);
        resp.setTotalAlimentoMsKg(totalAlimentoMsKg);
        resp.setConversionAlimenticia(conversion);
        resp.setHeadDays(headDays);
        resp.setCostoCompra(costoCompra);
        resp.setCostoAlimento(costoAlimento);
        resp.setCostoHoteleria(costoHoteleria);
        resp.setCostoAcumulado(costoAcumulado);
        resp.setIngresosVentas(ingresosVentas);
        resp.setMargen(margen);
        resp.setBreakevenKg(breakevenKg);
        resp.setMetodoCloseout(metodo.name());
        return resp;
    }

    private BigDecimal calcularHeadDaysPorIntervalos(FeedlotLote lote, Long empresaId, LocalDate fechaFin) {
        List<CambioPlantel> cambios = new ArrayList<>();
        for (FeedlotMuerte m : muerteRepository.listarPorLoteIdYEmpresaId(lote.getId(), empresaId)) {
            cambios.add(new CambioPlantel(m.getFecha(), -m.getCabezas()));
        }
        for (FeedlotVenta v : ventaRepository.listarPorLoteIdYEmpresaId(lote.getId(), empresaId)) {
            cambios.add(new CambioPlantel(v.getFecha(), -v.getCabezas()));
        }
        cambios.sort(Comparator.comparing(CambioPlantel::fecha).thenComparing(CambioPlantel::delta));

        int cabezas = lote.getCabezasInicial() != null ? lote.getCabezasInicial() : 0;
        LocalDate inicioIntervalo = lote.getFechaIngreso();
        BigDecimal total = BigDecimal.ZERO;

        for (CambioPlantel cambio : cambios) {
            if (!cambio.fecha.isBefore(inicioIntervalo) && cambio.fecha.isBefore(fechaFin.plusDays(1))) {
                long diasIntervalo = ChronoUnit.DAYS.between(inicioIntervalo, cambio.fecha);
                if (diasIntervalo > 0) {
                    total = total.add(BigDecimal.valueOf((long) cabezas * diasIntervalo));
                }
                inicioIntervalo = cambio.fecha;
            }
            cabezas = Math.max(0, cabezas + cambio.delta);
        }
        long diasFinal = ChronoUnit.DAYS.between(inicioIntervalo, fechaFin);
        if (diasFinal > 0) {
            total = total.add(BigDecimal.valueOf((long) cabezas * diasFinal));
        }
        return total.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularCostoAlimento(Long loteId, Long empresaId) {
        List<FeedlotConsumo> consumos = consumoRepository.listarPorLoteIdYEmpresaId(loteId, empresaId);
        BigDecimal total = BigDecimal.ZERO;
        boolean hayPrecio = false;
        for (FeedlotConsumo c : consumos) {
            Insumo insumo = insumoRepository.findById(c.getInsumoId()).orElse(null);
            if (insumo != null && insumo.getPrecioUnitario() != null) {
                total = total.add(insumo.getPrecioUnitario().multiply(c.getCantidadKg()));
                hayPrecio = true;
            }
        }
        return hayPrecio ? total : null;
    }

    private record CambioPlantel(LocalDate fecha, int delta) {
    }
}
