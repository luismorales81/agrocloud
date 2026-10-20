package com.agrocloud.feedlot;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.entity.FeedlotAnimal;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotAplicador;
import com.agrocloud.feedlot.model.enums.FeedlotOrigenInsumo;
import com.agrocloud.feedlot.model.enums.FeedlotTipoEventoSanitario;
import com.agrocloud.feedlot.repository.FeedlotEventoSanitarioRepository;
import com.agrocloud.feedlot.service.manga.ConfirmacionSanidad;
import com.agrocloud.feedlot.service.manga.ContextoConfirmacionManga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotSesionMangaPesadaSanidadTest {

    @Mock private FeedlotEventoSanitarioRepository eventoSanitarioRepository;
    @Mock private InventoryService inventoryService;

    private ConfirmacionSanidad confirmacion;
    private FeedlotSesionManga sesion;
    private FeedlotSesionMangaFila fila;

    @BeforeEach
    void setUp() {
        confirmacion = new ConfirmacionSanidad(eventoSanitarioRepository, inventoryService);
        FeedlotLote lote = new FeedlotLote();
        lote.setId(10L);
        FeedlotAnimal animal = new FeedlotAnimal();
        animal.setId(5L);
        fila = new FeedlotSesionMangaFila();
        fila.setAnimal(animal);
        sesion = new FeedlotSesionManga();
        sesion.setId(3L);
        sesion.setEmpresaId(1L);
        sesion.setLote(lote);
        sesion.setFecha(LocalDate.of(2026, 9, 12));
        sesion.setTipoEvento(FeedlotTipoEventoSanitario.VACUNA);
        sesion.setAplicador(FeedlotAplicador.PERSONAL_ESTABLECIMIENTO);
        sesion.setInsumoId(80L);
        sesion.setDosisPorAnimal(new BigDecimal("2"));
    }

    @Test
    void stockEmpresaConsumeUnaVez() {
        sesion.setOrigenInsumo(FeedlotOrigenInsumo.STOCK_EMPRESA);
        when(inventoryService.hayStockSuficiente(1L, 80L, new BigDecimal("2"))).thenReturn(true);
        when(inventoryService.consumir(eq(1L), eq(80L), eq(new BigDecimal("2")), eq(InventoryOrigin.FEEDLOT), eq(3L), any()))
                .thenReturn(InventoryResult.ok(new BigDecimal("10")));
        User u = new User();
        u.setId(1L);

        confirmacion.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(u));

        verify(inventoryService).consumir(eq(1L), eq(80L), eq(new BigDecimal("2")), eq(InventoryOrigin.FEEDLOT), eq(3L), eq(1L));
        verify(eventoSanitarioRepository).save(any());
    }

    @Test
    void veterinarioNoConsumeStock() {
        sesion.setOrigenInsumo(FeedlotOrigenInsumo.PROVEIDO_POR_APLICADOR);
        confirmacion.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(new User()));
        verify(inventoryService, never()).consumir(any(), any(), any(), any(), any(), any());
        verify(eventoSanitarioRepository).save(any());
    }

    @Test
    void anularSanidadReponeStock() {
        sesion.setOrigenInsumo(FeedlotOrigenInsumo.STOCK_EMPRESA);
        sesion.setCantidadTotal(new BigDecimal("2"));
        sesion.setMovimientoInventarioRef(3L);
        sesion.setUsuarioId(1L);
        when(inventoryService.reponer(eq(1L), eq(80L), eq(new BigDecimal("2")), eq(InventoryOrigin.FEEDLOT), eq(3L), eq(1L)))
                .thenReturn(InventoryResult.ok(new BigDecimal("12")));
        confirmacion.revertir(sesion);
        verify(inventoryService).reponer(eq(1L), eq(80L), eq(new BigDecimal("2")), eq(InventoryOrigin.FEEDLOT), eq(3L), eq(1L));
        verify(eventoSanitarioRepository).eliminarPorSesionId(3L);
    }

    @Test
    void stockInsuficienteNoConfirma() {
        sesion.setOrigenInsumo(FeedlotOrigenInsumo.STOCK_EMPRESA);
        when(inventoryService.hayStockSuficiente(1L, 80L, new BigDecimal("2"))).thenReturn(false);
        assertThrows(ResourceConflictException.class,
                () -> confirmacion.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(new User())));
        verify(eventoSanitarioRepository, never()).save(any());
    }

    @Test
    void vacunaStockSinInsumoNoConfirma() {
        sesion.setOrigenInsumo(FeedlotOrigenInsumo.STOCK_EMPRESA);
        sesion.setInsumoId(null);
        assertThrows(IllegalArgumentException.class,
                () -> confirmacion.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(new User())));
        verify(inventoryService, never()).consumir(any(), any(), any(), any(), any(), any());
    }
}
