package com.agrocloud.feedlot;

import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotCloseoutRespuesta;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.model.enums.FeedlotMetodoCloseout;
import com.agrocloud.feedlot.repository.*;
import com.agrocloud.feedlot.service.ServicioFeedlotCloseout;
import com.agrocloud.feedlot.service.ServicioFeedlotConfiguracion;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotCloseoutTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long LOTE_ID = 20L;

    @Mock private ServicioSeguridadContexto servicioSeguridadContexto;
    @Mock private ServicioFeedlotLotes servicioLotes;
    @Mock private ServicioFeedlotConfiguracion servicioConfiguracion;
    @Mock private FeedlotPesadaRepository pesadaRepository;
    @Mock private FeedlotConsumoRepository consumoRepository;
    @Mock private FeedlotMuerteRepository muerteRepository;
    @Mock private FeedlotVentaRepository ventaRepository;
    @Mock private InsumoRepository insumoRepository;

    @InjectMocks
    private ServicioFeedlotCloseout servicio;

    private FeedlotLote lote;

    @BeforeEach
    void setUp() {
        when(servicioSeguridadContexto.obtenerEmpresaIdActual()).thenReturn(EMPRESA_ID);
        lote = new FeedlotLote();
        lote.setId(LOTE_ID);
        lote.setNombre("Lote Demo");
        lote.setEmpresaId(EMPRESA_ID);
        lote.setEstado(FeedlotLoteEstado.ACTIVO);
        lote.setCabezasInicial(50);
        lote.setCabezasActuales(48);
        lote.setFechaIngreso(LocalDate.now().minusDays(100));
        lote.setPesoPromedioIngresoKg(new BigDecimal("280"));
        when(servicioLotes.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(lote);
        when(servicioConfiguracion.resolverMetodoCloseout(EMPRESA_ID)).thenReturn(FeedlotMetodoCloseout.DEADS_IN);
        when(pesadaRepository.listarPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(List.of());
        when(muerteRepository.listarPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(List.of());
        when(ventaRepository.listarPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(List.of());
        when(consumoRepository.sumarCantidadKgPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(new BigDecimal("5000"));
        when(consumoRepository.sumarMateriaSecaKgPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(BigDecimal.ZERO);
        when(muerteRepository.sumarCabezasPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(2);
        when(ventaRepository.sumarTotalPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(null);
        when(ventaRepository.sumarCabezasVendidasPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(0);
        when(consumoRepository.listarPorLoteIdYEmpresaId(LOTE_ID, EMPRESA_ID)).thenReturn(List.of());
    }

    @Test
    void calcularCloseout_incluyeMortalidadYGmd() {
        FeedlotCloseoutRespuesta r = servicio.calcularCloseout(LOTE_ID);
        assertNotNull(r);
        assertEquals(LOTE_ID, r.getLoteId());
        assertEquals(2, r.getTotalMuertes());
        assertEquals("DEADS_IN", r.getMetodoCloseout());
        assertTrue(r.getMortalidadPct().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(r.getGmd());
    }
}
