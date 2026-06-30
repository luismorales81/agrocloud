package com.agrocloud.feedlot;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotConsumoTeoricoRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotDietaFaseSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotDieta;
import com.agrocloud.feedlot.model.entity.FeedlotDietaFase;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.FeedlotConsumoRepository;
import com.agrocloud.feedlot.repository.FeedlotDietaFaseRepository;
import com.agrocloud.feedlot.repository.FeedlotDietaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotDietas;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotDietasTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long LOTE_ID = 10L;
    private static final Long DIETA_ID = 5L;

    @Mock private ServicioSeguridadContexto servicioSeguridadContexto;
    @Mock private ServicioFeedlotLotes servicioLotes;
    @Mock private FeedlotDietaRepository dietaRepository;
    @Mock private FeedlotDietaFaseRepository faseRepository;
    @Mock private FeedlotConsumoRepository consumoRepository;

    @InjectMocks
    private ServicioFeedlotDietas servicio;

    private FeedlotLote lote;
    private FeedlotDieta dieta;

    @BeforeEach
    void setUp() {
        when(servicioSeguridadContexto.obtenerEmpresaIdActual()).thenReturn(EMPRESA_ID);

        dieta = new FeedlotDieta();
        dieta.setId(DIETA_ID);
        dieta.setEmpresaId(EMPRESA_ID);
        dieta.setNombre("Dieta Engorde");
        dieta.setActivo(true);

        FeedlotDietaFase faseAdaptacion = new FeedlotDietaFase();
        faseAdaptacion.setId(1L);
        faseAdaptacion.setDieta(dieta);
        faseAdaptacion.setNombreFase("Adaptación");
        faseAdaptacion.setDiasDesdeIngreso(0);
        faseAdaptacion.setKgMsCabezaDia(new BigDecimal("8.5"));

        FeedlotDietaFase faseTerminacion = new FeedlotDietaFase();
        faseTerminacion.setId(2L);
        faseTerminacion.setDieta(dieta);
        faseTerminacion.setNombreFase("Terminación");
        faseTerminacion.setDiasDesdeIngreso(30);
        faseTerminacion.setKgMsCabezaDia(new BigDecimal("12.0"));

        dieta.setFases(new ArrayList<>(List.of(faseAdaptacion, faseTerminacion)));

        lote = new FeedlotLote();
        lote.setId(LOTE_ID);
        lote.setEmpresaId(EMPRESA_ID);
        lote.setEstado(FeedlotLoteEstado.ACTIVO);
        lote.setFechaIngreso(LocalDate.of(2026, 1, 1));
        lote.setCabezasActuales(50);
        lote.setDieta(dieta);
    }

    @Test
    void calcularConsumoTeorico_usaFaseSegunDiasDesdeIngreso() {
        when(servicioLotes.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(lote);
        when(dietaRepository.buscarPorIdYEmpresaId(DIETA_ID, EMPRESA_ID)).thenReturn(Optional.of(dieta));
        when(consumoRepository.sumarMateriaSecaKgPorLoteIdEmpresaIdYFecha(anyLong(), anyLong(), any()))
                .thenReturn(null);
        when(consumoRepository.sumarCantidadKgPorLoteIdEmpresaIdYFecha(anyLong(), anyLong(), any()))
                .thenReturn(BigDecimal.ZERO);

        LocalDate desde = LocalDate.of(2026, 1, 31);
        LocalDate hasta = LocalDate.of(2026, 2, 1);

        FeedlotConsumoTeoricoRespuesta resp = servicio.calcularConsumoTeorico(LOTE_ID, desde, hasta);

        assertEquals(DIETA_ID, resp.getDietaId());
        assertEquals(2, resp.getDias().size());
        assertEquals("Terminación", resp.getDias().get(0).getFaseNombre());
        assertEquals(new BigDecimal("600.000"), resp.getDias().get(0).getKgMsTeorico());
    }

    @Test
    void calcularConsumoTeorico_sinDieta_devuelveRespuestaVacia() {
        lote.setDieta(null);
        when(servicioLotes.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(lote);

        FeedlotConsumoTeoricoRespuesta resp = servicio.calcularConsumoTeorico(LOTE_ID, null, null);

        assertEquals(LOTE_ID, resp.getLoteId());
        assertNull(resp.getDietaId());
        assertTrue(resp.getDias().isEmpty());
    }

    @Test
    void crearFase_sinNombre_lanzaExcepcion() {
        when(dietaRepository.buscarPorIdYEmpresaId(DIETA_ID, EMPRESA_ID)).thenReturn(Optional.of(dieta));

        FeedlotDietaFaseSolicitud solicitud = new FeedlotDietaFaseSolicitud();
        solicitud.setKgMsCabezaDia(new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> servicio.crearFase(DIETA_ID, solicitud));
    }

    @Test
    void calcularConsumoTeorico_faseAdaptacionAlInicio() {
        when(servicioLotes.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(lote);
        when(dietaRepository.buscarPorIdYEmpresaId(DIETA_ID, EMPRESA_ID)).thenReturn(Optional.of(dieta));
        when(consumoRepository.sumarMateriaSecaKgPorLoteIdEmpresaIdYFecha(anyLong(), anyLong(), any()))
                .thenReturn(null);
        when(consumoRepository.sumarCantidadKgPorLoteIdEmpresaIdYFecha(anyLong(), anyLong(), any()))
                .thenReturn(BigDecimal.ZERO);

        LocalDate dia = LocalDate.of(2026, 1, 10);
        FeedlotConsumoTeoricoRespuesta resp = servicio.calcularConsumoTeorico(LOTE_ID, dia, dia);

        assertEquals("Adaptación", resp.getDias().get(0).getFaseNombre());
        assertEquals(new BigDecimal("425.000"), resp.getDias().get(0).getKgMsTeorico());
    }
}
