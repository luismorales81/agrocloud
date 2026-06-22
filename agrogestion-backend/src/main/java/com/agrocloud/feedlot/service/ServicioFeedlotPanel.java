package com.agrocloud.feedlot.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotAnalisisLotesRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotCloseoutRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotPanelRespuesta;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.FeedlotConsumoRepository;
import com.agrocloud.feedlot.repository.FeedlotLoteRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioFeedlotPanel {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final FeedlotLoteRepository loteRepository;
    private final FeedlotConsumoRepository consumoRepository;
    private final ServicioFeedlotCloseout servicioCloseout;

    public ServicioFeedlotPanel(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            FeedlotLoteRepository loteRepository,
            FeedlotConsumoRepository consumoRepository,
            ServicioFeedlotCloseout servicioCloseout) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.loteRepository = loteRepository;
        this.consumoRepository = consumoRepository;
        this.servicioCloseout = servicioCloseout;
    }

    @Transactional(readOnly = true)
    public FeedlotPanelRespuesta resumenPeriodoActivo() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);

        Long cabezas = loteRepository.sumarCabezasActualesPorEmpresaCampanaYEstado(
                empresaId, campanaId, FeedlotLoteEstado.ACTIVO);
        if (cabezas == null) {
            cabezas = 0L;
        }

        List<FeedlotLote> lotesActivos = loteRepository.listarPorEmpresaIdYCampanaId(empresaId, campanaId).stream()
                .filter(l -> l.getEstado() == FeedlotLoteEstado.ACTIVO)
                .toList();

        BigDecimal gmdSum = BigDecimal.ZERO;
        BigDecimal mortalidadSum = BigDecimal.ZERO;
        int contadorGmd = 0;
        for (FeedlotLote lote : lotesActivos) {
            FeedlotCloseoutRespuesta c = servicioCloseout.construirCloseout(lote, empresaId);
            if (c.getGmd() != null) {
                gmdSum = gmdSum.add(c.getGmd());
                contadorGmd++;
            }
            if (c.getMortalidadPct() != null) {
                mortalidadSum = mortalidadSum.add(c.getMortalidadPct());
            }
        }

        BigDecimal gmdPromedio = contadorGmd > 0
                ? gmdSum.divide(BigDecimal.valueOf(contadorGmd), 4, RoundingMode.HALF_UP) : null;
        BigDecimal mortalidadPromedio = !lotesActivos.isEmpty()
                ? mortalidadSum.divide(BigDecimal.valueOf(lotesActivos.size()), 4, RoundingMode.HALF_UP) : null;

        BigDecimal consumoTotal = consumoRepository.sumarCantidadKgPorEmpresaIdYCampanaId(empresaId, campanaId);
        if (consumoTotal == null) {
            consumoTotal = BigDecimal.ZERO;
        }

        FeedlotPanelRespuesta panel = new FeedlotPanelRespuesta();
        panel.setCampanaId(campanaId);
        panel.setCabezasEnFeed(cabezas);
        panel.setLotesActivos((long) lotesActivos.size());
        panel.setGmdPromedio(gmdPromedio);
        panel.setMortalidadPctPromedio(mortalidadPromedio);
        panel.setConsumoTotalKg(consumoTotal);
        return panel;
    }

    @Transactional(readOnly = true)
    public FeedlotAnalisisLotesRespuesta analisisLotesPeriodoActivo() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
        List<FeedlotLote> lotes = loteRepository.listarPorEmpresaIdYCampanaId(empresaId, campanaId);

        FeedlotAnalisisLotesRespuesta resp = new FeedlotAnalisisLotesRespuesta();
        List<FeedlotAnalisisLotesRespuesta.FeedlotAnalisisLoteItemRespuesta> items = new ArrayList<>();
        for (FeedlotLote lote : lotes) {
            FeedlotCloseoutRespuesta c = servicioCloseout.construirCloseout(lote, empresaId);
            FeedlotAnalisisLotesRespuesta.FeedlotAnalisisLoteItemRespuesta item =
                    new FeedlotAnalisisLotesRespuesta.FeedlotAnalisisLoteItemRespuesta();
            item.setLoteId(lote.getId());
            item.setLoteNombre(lote.getNombre());
            item.setGmd(c.getGmd());
            item.setConversionAlimenticia(c.getConversionAlimenticia());
            item.setMortalidadPct(c.getMortalidadPct());
            item.setMargen(c.getMargen());
            items.add(item);
        }
        resp.setLotes(items);
        return resp;
    }
}
