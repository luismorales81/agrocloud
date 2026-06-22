package com.agrocloud.feedlot;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotMuerteSolicitud;
import com.agrocloud.feedlot.model.dto.FeedlotVentaSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.model.enums.FeedlotTipoVenta;
import com.agrocloud.feedlot.repository.*;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import com.agrocloud.feedlot.service.ServicioFeedlotOperaciones;
import com.agrocloud.core.inventory.application.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotOperacionesTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long LOTE_ID = 10L;

    @Mock private ServicioSeguridadContexto servicioSeguridadContexto;
    @Mock private ServicioFeedlotLotes servicioLotes;
    @Mock private FeedlotPesadaRepository pesadaRepository;
    @Mock private FeedlotConsumoRepository consumoRepository;
    @Mock private FeedlotMuerteRepository muerteRepository;
    @Mock private FeedlotVentaRepository ventaRepository;
    @Mock private FeedlotEventoSanitarioRepository eventoSanitarioRepository;
    @Mock private FeedlotMotivoMuerteRepository motivoMuerteRepository;
    @Mock private InventoryService inventoryService;

    @InjectMocks
    private ServicioFeedlotOperaciones servicio;

    private FeedlotLote loteActivo;

    @BeforeEach
    void setUp() {
        when(servicioSeguridadContexto.obtenerEmpresaIdActual()).thenReturn(EMPRESA_ID);
        loteActivo = new FeedlotLote();
        loteActivo.setId(LOTE_ID);
        loteActivo.setEmpresaId(EMPRESA_ID);
        loteActivo.setEstado(FeedlotLoteEstado.ACTIVO);
        loteActivo.setCabezasInicial(100);
        loteActivo.setCabezasActuales(100);
        loteActivo.setFechaIngreso(LocalDate.of(2026, 1, 1));
        loteActivo.setCampanaId(5L);
        when(servicioLotes.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(loteActivo);
    }

    @Test
    void registrarMuerte_descuentaCabezas() {
        when(muerteRepository.save(any())).thenAnswer(inv -> {
            var m = inv.getArgument(0, com.agrocloud.feedlot.model.entity.FeedlotMuerte.class);
            m.setId(1L);
            return m;
        });
        when(servicioLotes.guardarLote(any())).thenAnswer(inv -> inv.getArgument(0));

        FeedlotMuerteSolicitud s = new FeedlotMuerteSolicitud();
        s.setFecha(LocalDate.of(2026, 2, 1));
        s.setCabezas(3);

        servicio.registrarMuerte(LOTE_ID, s);

        assertEquals(97, loteActivo.getCabezasActuales());
        verify(servicioLotes).guardarLote(loteActivo);
    }

    @Test
    void registrarVenta_totalCierraLote() {
        when(ventaRepository.save(any())).thenAnswer(inv -> {
            var v = inv.getArgument(0, com.agrocloud.feedlot.model.entity.FeedlotVenta.class);
            v.setId(1L);
            return v;
        });

        FeedlotVentaSolicitud s = new FeedlotVentaSolicitud();
        s.setFecha(LocalDate.of(2026, 3, 1));
        s.setTipo(FeedlotTipoVenta.FAENA);
        s.setCabezas(100);

        servicio.registrarVenta(LOTE_ID, s);

        verify(servicioLotes).cerrarLoteYLiberarCorral(loteActivo);
    }

    @Test
    void registrarVenta_parcialNoCierra() {
        when(ventaRepository.save(any())).thenAnswer(inv -> {
            var v = inv.getArgument(0, com.agrocloud.feedlot.model.entity.FeedlotVenta.class);
            v.setId(1L);
            return v;
        });
        when(servicioLotes.guardarLote(any())).thenAnswer(inv -> inv.getArgument(0));

        FeedlotVentaSolicitud s = new FeedlotVentaSolicitud();
        s.setFecha(LocalDate.of(2026, 3, 1));
        s.setTipo(FeedlotTipoVenta.VENTA_EN_PIE);
        s.setCabezas(40);

        servicio.registrarVenta(LOTE_ID, s);

        assertEquals(60, loteActivo.getCabezasActuales());
        verify(servicioLotes).guardarLote(loteActivo);
        verify(servicioLotes, never()).cerrarLoteYLiberarCorral(any());
    }
}
