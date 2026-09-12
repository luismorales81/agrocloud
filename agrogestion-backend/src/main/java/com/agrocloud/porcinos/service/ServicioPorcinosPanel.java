package com.agrocloud.porcinos.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.porcinos.model.dto.PorcinosPanelResumen;
import com.agrocloud.porcinos.model.entity.PorcinosLote;
import com.agrocloud.porcinos.model.enums.PorcinosGestacionEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.model.enums.PorcinosMadreEstado;
import com.agrocloud.porcinos.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ServicioPorcinosPanel {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final PorcinosLoteRepository loteRepository;
    private final PorcinosMuerteRepository muerteRepository;
    private final PorcinosConsumoRepository consumoRepository;
    private final PorcinosMadreRepository madreRepository;
    private final PorcinosGestacionRepository gestacionRepository;

    public ServicioPorcinosPanel(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            PorcinosLoteRepository loteRepository,
            PorcinosMuerteRepository muerteRepository,
            PorcinosConsumoRepository consumoRepository,
            PorcinosMadreRepository madreRepository,
            PorcinosGestacionRepository gestacionRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.loteRepository = loteRepository;
        this.muerteRepository = muerteRepository;
        this.consumoRepository = consumoRepository;
        this.madreRepository = madreRepository;
        this.gestacionRepository = gestacionRepository;
    }

    @Transactional(readOnly = true)
    public PorcinosPanelResumen resumenPeriodoActivo() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);

        Long cabezas = loteRepository.sumarCabezasActualesPorEmpresaCampanaYEstado(
                empresaId, campanaId, PorcinosLoteEstado.ACTIVO);
        if (cabezas == null) {
            cabezas = 0L;
        }

        List<PorcinosLote> lotesActivos = loteRepository.listarPorEmpresaIdYCampanaId(empresaId, campanaId).stream()
                .filter(l -> l.getEstado() == PorcinosLoteEstado.ACTIVO)
                .toList();

        BigDecimal mortalidadSum = BigDecimal.ZERO;
        for (PorcinosLote lote : lotesActivos) {
            Integer muertes = muerteRepository.sumarCabezasPorLoteId(lote.getId(), empresaId);
            if (lote.getCabezasInicial() != null && lote.getCabezasInicial() > 0 && muertes != null) {
                mortalidadSum = mortalidadSum.add(BigDecimal.valueOf(muertes)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(lote.getCabezasInicial()), 4, RoundingMode.HALF_UP));
            }
        }

        BigDecimal consumoTotal = consumoRepository.sumarCantidadKgPorEmpresaIdYCampanaId(empresaId, campanaId);
        if (consumoTotal == null) {
            consumoTotal = BigDecimal.ZERO;
        }

        PorcinosPanelResumen panel = new PorcinosPanelResumen();
        panel.setCampanaId(campanaId);
        panel.setCabezasEnLotes(cabezas);
        panel.setLotesActivos((long) lotesActivos.size());
        panel.setMadresActivas(madreRepository.listarPorEmpresaId(empresaId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo())).count());
        panel.setGestacionesEnCurso(gestacionRepository.listarPorEmpresaId(empresaId).stream()
                .filter(g -> g.getEstado() == PorcinosGestacionEstado.EN_CURSO && Boolean.TRUE.equals(g.getActivo()))
                .count());
        panel.setMadresEnLactancia(madreRepository.listarPorEmpresaIdYEstado(empresaId, PorcinosMadreEstado.LACTANCIA).stream()
                .count());
        panel.setMortalidadPctPromedio(!lotesActivos.isEmpty()
                ? mortalidadSum.divide(BigDecimal.valueOf(lotesActivos.size()), 4, RoundingMode.HALF_UP) : null);
        panel.setConsumoTotalKg(consumoTotal);
        return panel;
    }
}
