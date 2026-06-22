package com.agrocloud.feedlot;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.application.ExcelExportService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotCloseoutRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotCurvaPesoRespuesta;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotPesada;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.FeedlotLoteRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotCloseout;
import com.agrocloud.feedlot.service.ServicioFeedlotReportes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotReportesTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long CAMPANA_ID = 3L;
    private static final Long LOTE_ID = 10L;

    @Mock private ServicioSeguridadContexto servicioSeguridadContexto;
    @Mock private CampanaContextService campanaContextService;
    @Mock private FeedlotLoteRepository loteRepository;
    @Mock private FeedlotPesadaRepository pesadaRepository;
    @Mock private ServicioFeedlotCloseout servicioCloseout;
    @Mock private ExcelExportService excelExportService;

    @InjectMocks
    private ServicioFeedlotReportes servicio;

    private FeedlotLote lote;

    @BeforeEach
    void setUp() {
        when(servicioSeguridadContexto.obtenerEmpresaIdActual()).thenReturn(EMPRESA_ID);

        lote = new FeedlotLote();
        lote.setId(LOTE_ID);
        lote.setEmpresaId(EMPRESA_ID);
        lote.setNombre("Lote Test");
        lote.setEstado(FeedlotLoteEstado.ACTIVO);
        lote.setFechaIngreso(LocalDate.of(2026, 1, 1));
        lote.setPesoPromedioIngresoKg(new BigDecimal("280"));
        lote.setCabezasActuales(50);
    }

    @Test
    void curvaPeso_incluyeIngresoPesadasYGmdPositivo() {
        when(loteRepository.buscarPorIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(Optional.of(lote));

        FeedlotPesada p1 = new FeedlotPesada();
        p1.setFecha(LocalDate.of(2026, 2, 1));
        p1.setPesoPromedioKg(new BigDecimal("310"));

        FeedlotPesada p2 = new FeedlotPesada();
        p2.setFecha(LocalDate.of(2026, 3, 1));
        p2.setPesoPromedioKg(new BigDecimal("335"));

        when(pesadaRepository.listarPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(List.of(p1, p2));

        FeedlotCurvaPesoRespuesta resp = servicio.curvaPeso(LOTE_ID, new BigDecimal("520"), 30);

        assertEquals(LOTE_ID, resp.getLoteId());
        assertEquals(3, resp.getSerie().size());
        assertNotNull(resp.getGmd());
        assertTrue(resp.getGmd().compareTo(BigDecimal.ZERO) > 0);
        assertFalse(resp.getProyeccion().isEmpty());
        assertTrue(resp.getProyeccion().get(0).isProyectado());
    }

    @Test
    void exportarExcelPeriodoActivo_generaBytes() throws Exception {
        when(campanaContextService.resolverCampanaIdActiva(EMPRESA_ID)).thenReturn(CAMPANA_ID);
        when(loteRepository.listarPorEmpresaIdYCampanaId(EMPRESA_ID, CAMPANA_ID)).thenReturn(List.of(lote));

        FeedlotCloseoutRespuesta closeout = new FeedlotCloseoutRespuesta();
        closeout.setDiasEnFeedlot(90L);
        closeout.setGmd(new BigDecimal("1.2"));
        closeout.setConversionAlimenticia(new BigDecimal("6.5"));
        closeout.setMortalidadPct(new BigDecimal("2.5"));
        closeout.setTotalAlimentoKg(new BigDecimal("5000"));
        when(servicioCloseout.construirCloseout(eq(lote), eq(EMPRESA_ID))).thenReturn(closeout);
        when(excelExportService.generarExcel(anyString(), anyList())).thenReturn(new byte[] {1, 2, 3});

        byte[] resultado = servicio.exportarExcelPeriodoActivo();

        assertNotNull(resultado);
        assertEquals(3, resultado.length);
        verify(excelExportService).generarExcel(eq("Reporte Feedlot"), anyList());
    }
}
