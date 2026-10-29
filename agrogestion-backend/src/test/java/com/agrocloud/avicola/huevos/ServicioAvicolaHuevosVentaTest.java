package com.agrocloud.avicola.huevos;

import com.agrocloud.avicola.huevos.application.ServicioAvicolaHuevosLote;
import com.agrocloud.avicola.huevos.application.ServicioAvicolaHuevosOperaciones;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevoVentaSolicitud;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoLote;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoVenta;
import com.agrocloud.avicola.huevos.model.enums.AvicolaHuevoLoteEstado;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoConsumoRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoEventoSanitarioRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoProduccionDiariaRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoVentaRepository;
import com.agrocloud.core.application.ServicioLibroEmpresa;
import com.agrocloud.core.domain.Ingreso;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.model.enums.ModuloSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioAvicolaHuevosVentaTest {

    @Mock private ServicioAvicolaHuevosLote servicioLote;
    @Mock private AvicolaHuevoProduccionDiariaRepository produccionRepository;
    @Mock private AvicolaHuevoConsumoRepository consumoRepository;
    @Mock private AvicolaHuevoEventoSanitarioRepository eventoSanitarioRepository;
    @Mock private InventoryService inventoryService;
    @Mock private InsumoRepository insumoRepository;
    @Mock private AvicolaHuevoVentaRepository ventaRepository;
    @Mock private ServicioLibroEmpresa libroEmpresa;

    @InjectMocks
    private ServicioAvicolaHuevosOperaciones servicio;

    private AvicolaHuevoLote lote;

    @BeforeEach
    void preparar() {
        lote = new AvicolaHuevoLote();
        lote.setId(7L);
        lote.setEmpresaId(1L);
        lote.setNombre("Galpón norte");
        lote.setEstado(AvicolaHuevoLoteEstado.ACTIVO);
        lote.setCampanaId(3L);
    }

    @Test
    void registrarVenta_creaIngresoCuandoHayImporte() {
        when(servicioLote.obtenerEntidadLote(1L, 7L)).thenReturn(lote);
        when(produccionRepository.sumarHuevosComercializables(7L, 1L)).thenReturn(100L);
        when(ventaRepository.sumarCantidadVendida(7L, 1L)).thenReturn(10L);
        when(ventaRepository.save(any())).thenAnswer(inv -> {
            AvicolaHuevoVenta venta = inv.getArgument(0);
            if (venta.getId() == null) {
                venta.setId(20L);
            }
            return venta;
        });
        when(libroEmpresa.asegurarIngresoDeVenta(
                isNull(),
                eq(ModuloSistema.AVICOLA_HUEVOS),
                eq(Ingreso.TipoIngreso.VENTA_HUEVOS),
                eq(new BigDecimal("250.00")),
                eq(LocalDate.of(2026, 10, 28)),
                eq("Venta de huevos lote Galpón norte"),
                eq("Acopio"),
                eq(3L),
                eq(ServicioLibroEmpresa.REF_VENTA_HUEVOS),
                eq(20L))).thenReturn(88L);

        AvicolaHuevoVentaSolicitud solicitud = new AvicolaHuevoVentaSolicitud();
        solicitud.setFecha(LocalDate.of(2026, 10, 28));
        solicitud.setCantidad(25);
        solicitud.setPrecioUnitario(new BigDecimal("10"));
        solicitud.setComprador("Acopio");

        var respuesta = servicio.registrarVenta(1L, 7L, solicitud);

        assertEquals(88L, respuesta.getIngresoId());
        assertEquals(new BigDecimal("250.00"), respuesta.getTotal());
        assertEquals(25, respuesta.getCantidad());
    }

    @Test
    void registrarVenta_rechazaSiSuperaLoProducidoSinRotos() {
        when(servicioLote.obtenerEntidadLote(1L, 7L)).thenReturn(lote);
        when(produccionRepository.sumarHuevosComercializables(7L, 1L)).thenReturn(5L);
        when(ventaRepository.sumarCantidadVendida(7L, 1L)).thenReturn(4L);

        AvicolaHuevoVentaSolicitud solicitud = new AvicolaHuevoVentaSolicitud();
        solicitud.setFecha(LocalDate.of(2026, 10, 28));
        solicitud.setCantidad(2);

        assertThrows(ResourceConflictException.class, () -> servicio.registrarVenta(1L, 7L, solicitud));
        verify(ventaRepository, never()).save(any());
        verify(libroEmpresa, never()).asegurarIngresoDeVenta(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
