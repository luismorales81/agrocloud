package com.agrocloud.feedlot;

import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.FeedlotConsumoActualizarSolicitud;
import com.agrocloud.feedlot.model.dto.FeedlotConsumoSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotConsumo;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.*;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import com.agrocloud.feedlot.service.ServicioFeedlotOperaciones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotOperacionesConsumoTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long USUARIO_ID = 99L;
    private static final Long LOTE_ID = 10L;
    private static final Long INSUMO_ID = 5L;
    private static final Long CONSUMO_ID = 50L;

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
        when(servicioSeguridadContexto.obtenerUsuarioIdActual()).thenReturn(USUARIO_ID);
        loteActivo = new FeedlotLote();
        loteActivo.setId(LOTE_ID);
        loteActivo.setEmpresaId(EMPRESA_ID);
        loteActivo.setEstado(FeedlotLoteEstado.ACTIVO);
        loteActivo.setCampanaId(3L);
        when(servicioLotes.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(loteActivo);
    }

    @Test
    void registrarConsumo_egresaInventario() {
        when(consumoRepository.saveAndFlush(any())).thenAnswer(inv -> {
            FeedlotConsumo c = inv.getArgument(0);
            c.setId(CONSUMO_ID);
            return c;
        });
        when(inventoryService.consumir(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("100"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID))
                .thenReturn(InventoryResult.ok(new BigDecimal("400")));

        FeedlotConsumoSolicitud solicitud = new FeedlotConsumoSolicitud();
        solicitud.setInsumoId(INSUMO_ID);
        solicitud.setFecha(LocalDate.of(2026, 2, 1));
        solicitud.setCantidadKg(new BigDecimal("100"));

        var respuesta = servicio.registrarConsumo(LOTE_ID, solicitud);

        assertNotNull(respuesta);
        assertEquals(CONSUMO_ID, respuesta.getId());
        verify(inventoryService).consumir(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("100"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID);
    }

    @Test
    void registrarConsumo_stockInsuficiente_lanzaExcepcion() {
        when(consumoRepository.saveAndFlush(any())).thenAnswer(inv -> {
            FeedlotConsumo c = inv.getArgument(0);
            c.setId(CONSUMO_ID);
            return c;
        });
        when(inventoryService.consumir(anyLong(), anyLong(), any(), any(), anyLong(), anyLong()))
                .thenReturn(InventoryResult.error("Stock insuficiente"));

        FeedlotConsumoSolicitud solicitud = new FeedlotConsumoSolicitud();
        solicitud.setInsumoId(INSUMO_ID);
        solicitud.setFecha(LocalDate.of(2026, 2, 1));
        solicitud.setCantidadKg(new BigDecimal("100"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class, () -> servicio.registrarConsumo(LOTE_ID, solicitud));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void actualizarConsumo_aumentoCantidad_consumeDelta() {
        FeedlotConsumo consumo = consumoExistente(new BigDecimal("80"));
        when(consumoRepository.buscarPorIdYEmpresaId(CONSUMO_ID, EMPRESA_ID)).thenReturn(Optional.of(consumo));
        when(inventoryService.consumir(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("20"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID))
                .thenReturn(InventoryResult.ok(new BigDecimal("380")));
        when(consumoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FeedlotConsumoActualizarSolicitud solicitud = new FeedlotConsumoActualizarSolicitud();
        solicitud.setFecha(LocalDate.of(2026, 2, 2));
        solicitud.setCantidadKg(new BigDecimal("100"));

        servicio.actualizarConsumo(LOTE_ID, CONSUMO_ID, solicitud);

        verify(inventoryService).consumir(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("20"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID);
        verify(inventoryService, never()).reponer(anyLong(), anyLong(), any(), any(), anyLong(), anyLong());
    }

    @Test
    void actualizarConsumo_disminuyeCantidad_repone() {
        FeedlotConsumo consumo = consumoExistente(new BigDecimal("100"));
        when(consumoRepository.buscarPorIdYEmpresaId(CONSUMO_ID, EMPRESA_ID)).thenReturn(Optional.of(consumo));
        when(inventoryService.reponer(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("30"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID))
                .thenReturn(InventoryResult.ok(new BigDecimal("430")));
        when(consumoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FeedlotConsumoActualizarSolicitud solicitud = new FeedlotConsumoActualizarSolicitud();
        solicitud.setFecha(LocalDate.of(2026, 2, 2));
        solicitud.setCantidadKg(new BigDecimal("70"));

        servicio.actualizarConsumo(LOTE_ID, CONSUMO_ID, solicitud);

        verify(inventoryService).reponer(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("30"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID);
    }

    @Test
    void eliminarConsumo_reponeInventario() {
        FeedlotConsumo consumo = consumoExistente(new BigDecimal("100"));
        when(consumoRepository.buscarPorIdYEmpresaId(CONSUMO_ID, EMPRESA_ID)).thenReturn(Optional.of(consumo));
        when(inventoryService.reponer(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("100"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID))
                .thenReturn(InventoryResult.ok(new BigDecimal("500")));

        servicio.eliminarConsumo(LOTE_ID, CONSUMO_ID);

        verify(inventoryService).reponer(
                EMPRESA_ID, INSUMO_ID, new BigDecimal("100"), InventoryOrigin.FEEDLOT, CONSUMO_ID, USUARIO_ID);
        verify(consumoRepository).delete(consumo);
    }

    @Test
    void eliminarConsumo_fallaReversion_noElimina() {
        FeedlotConsumo consumo = consumoExistente(new BigDecimal("100"));
        when(consumoRepository.buscarPorIdYEmpresaId(CONSUMO_ID, EMPRESA_ID)).thenReturn(Optional.of(consumo));
        when(inventoryService.reponer(anyLong(), anyLong(), any(), any(), anyLong(), anyLong()))
                .thenReturn(InventoryResult.error("Error al revertir"));

        assertThrows(IllegalStateException.class, () -> servicio.eliminarConsumo(LOTE_ID, CONSUMO_ID));
        verify(consumoRepository, never()).delete(any());
    }

    @Test
    void eliminarConsumo_noEncontrado_lanzaResourceNotFound() {
        when(consumoRepository.buscarPorIdYEmpresaId(CONSUMO_ID, EMPRESA_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> servicio.eliminarConsumo(LOTE_ID, CONSUMO_ID));
    }

    private FeedlotConsumo consumoExistente(BigDecimal cantidadKg) {
        FeedlotConsumo consumo = new FeedlotConsumo();
        consumo.setId(CONSUMO_ID);
        consumo.setLote(loteActivo);
        consumo.setEmpresaId(EMPRESA_ID);
        consumo.setInsumoId(INSUMO_ID);
        consumo.setCantidadKg(cantidadKg);
        consumo.setFecha(LocalDate.of(2026, 2, 1));
        return consumo;
    }
}
