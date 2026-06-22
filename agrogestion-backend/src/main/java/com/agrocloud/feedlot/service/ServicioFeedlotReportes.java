package com.agrocloud.feedlot.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.application.ExcelExportService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotCloseoutRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotCurvaPesoRespuesta;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotPesada;
import com.agrocloud.feedlot.repository.FeedlotLoteRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServicioFeedlotReportes {

    private static final int DIAS_PROYECCION_DEFAULT = 30;
    private static final BigDecimal PESO_OBJETIVO_DEFAULT = new BigDecimal("520");

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final FeedlotLoteRepository loteRepository;
    private final FeedlotPesadaRepository pesadaRepository;
    private final ServicioFeedlotCloseout servicioCloseout;
    private final ExcelExportService excelExportService;

    public ServicioFeedlotReportes(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            FeedlotLoteRepository loteRepository,
            FeedlotPesadaRepository pesadaRepository,
            ServicioFeedlotCloseout servicioCloseout,
            @Qualifier("excelExportServiceCore") ExcelExportService excelExportService) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.loteRepository = loteRepository;
        this.pesadaRepository = pesadaRepository;
        this.servicioCloseout = servicioCloseout;
        this.excelExportService = excelExportService;
    }

    @Transactional(readOnly = true)
    public FeedlotCurvaPesoRespuesta curvaPeso(Long loteId, BigDecimal pesoObjetivoKg, Integer diasProyeccion) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = loteRepository.buscarPorIdYEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Lote feedlot no encontrado"));

        BigDecimal objetivo = pesoObjetivoKg != null ? pesoObjetivoKg : PESO_OBJETIVO_DEFAULT;
        int diasProj = diasProyeccion != null && diasProyeccion > 0 ? diasProyeccion : DIAS_PROYECCION_DEFAULT;

        List<FeedlotPesada> pesadas = pesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId);
        pesadas = new ArrayList<>(pesadas);
        pesadas.sort(Comparator.comparing(FeedlotPesada::getFecha));

        FeedlotCurvaPesoRespuesta resp = new FeedlotCurvaPesoRespuesta();
        resp.setLoteId(loteId);
        resp.setLoteNombre(lote.getNombre());
        resp.setPesoIngresoKg(lote.getPesoPromedioIngresoKg());
        resp.setPesoObjetivoKg(objetivo);

        resp.getSerie().add(new FeedlotCurvaPesoRespuesta.PuntoPeso(
                lote.getFechaIngreso(), lote.getPesoPromedioIngresoKg(), false));
        for (FeedlotPesada p : pesadas) {
            resp.getSerie().add(new FeedlotCurvaPesoRespuesta.PuntoPeso(p.getFecha(), p.getPesoPromedioKg(), false));
        }

        BigDecimal pesoActual = lote.getPesoPromedioIngresoKg();
        LocalDate fechaActual = lote.getFechaIngreso();
        if (!pesadas.isEmpty()) {
            FeedlotPesada ultima = pesadas.get(pesadas.size() - 1);
            pesoActual = ultima.getPesoPromedioKg();
            fechaActual = ultima.getFecha();
        }

        long diasTotales = Math.max(1, ChronoUnit.DAYS.between(lote.getFechaIngreso(), fechaActual));
        BigDecimal gmd = pesoActual.subtract(lote.getPesoPromedioIngresoKg())
                .divide(BigDecimal.valueOf(diasTotales), 4, RoundingMode.HALF_UP);
        resp.setGmd(gmd);

        if (gmd.compareTo(BigDecimal.ZERO) > 0 && pesoActual.compareTo(objetivo) < 0) {
            long diasHastaObjetivo = pesoActual.subtract(objetivo).abs()
                    .divide(gmd, 0, RoundingMode.CEILING).longValue();
            resp.setFechaProyeccionObjetivo(fechaActual.plusDays(diasHastaObjetivo));
        }

        if (gmd.compareTo(BigDecimal.ZERO) > 0) {
            for (int i = 1; i <= diasProj; i++) {
                LocalDate fecha = fechaActual.plusDays(i);
                BigDecimal peso = pesoActual.add(gmd.multiply(BigDecimal.valueOf(i)))
                        .setScale(2, RoundingMode.HALF_UP);
                resp.getProyeccion().add(new FeedlotCurvaPesoRespuesta.PuntoPeso(fecha, peso, true));
            }
        }
        return resp;
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcelPeriodoActivo() throws IOException {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
        List<FeedlotLote> lotes = loteRepository.listarPorEmpresaIdYCampanaId(empresaId, campanaId);

        List<Map<String, Object>> filas = new ArrayList<>();
        for (FeedlotLote lote : lotes) {
            FeedlotCloseoutRespuesta c = servicioCloseout.construirCloseout(lote, empresaId);
            Map<String, Object> fila = new HashMap<>();
            fila.put("Lote", lote.getNombre());
            fila.put("Estado", lote.getEstado().name());
            fila.put("Cabezas actuales", lote.getCabezasActuales());
            fila.put("Días en feedlot", c.getDiasEnFeedlot());
            fila.put("GMD (kg/día)", c.getGmd());
            fila.put("Conversión alimenticia", c.getConversionAlimenticia());
            fila.put("Mortalidad %", c.getMortalidadPct());
            fila.put("Alimento total (kg)", c.getTotalAlimentoKg());
            fila.put("Margen", c.getMargen());
            fila.put("Breakeven $/kg", c.getBreakevenKg());
            filas.add(fila);
        }

        List<String> columnas = List.of(
                "Lote", "Estado", "Cabezas actuales", "Días en feedlot", "GMD (kg/día)",
                "Conversión alimenticia", "Mortalidad %", "Alimento total (kg)", "Margen", "Breakeven $/kg");

        ExcelExportService.HojaExcel hoja = new ExcelExportService.HojaExcel(
                "Lotes período activo",
                "Comparativa lotes feedlot — campaña activa",
                columnas,
                filas);

        return excelExportService.generarExcel("Reporte Feedlot", List.of(hoja));
    }
}
