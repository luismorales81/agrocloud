package com.agrocloud.avicola.crianza;

import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaLote;
import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaOperaciones;
import com.agrocloud.avicola.crianza.model.dto.AvicolaMuerteSolicitud;
import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.avicola.crianza.repository.AvicolaConsumoRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaEventoSanitarioRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaMuerteRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaPesadaRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaVentaRepository;
import com.agrocloud.core.inventory.application.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioAvicolaCrianzaOperacionesTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long LOTE_ID = 10L;

    @Mock
    private ServicioAvicolaCrianzaLote servicioLote;
    @Mock
    private AvicolaPesadaRepository pesadaRepository;
    @Mock
    private AvicolaMuerteRepository muerteRepository;
    @Mock
    private AvicolaVentaRepository ventaRepository;
    @Mock
    private AvicolaConsumoRepository consumoRepository;
    @Mock
    private AvicolaEventoSanitarioRepository eventoSanitarioRepository;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ServicioAvicolaCrianzaOperaciones servicio;

    private AvicolaLote loteActivo;

    @BeforeEach
    void setUp() {
        loteActivo = new AvicolaLote();
        loteActivo.setId(LOTE_ID);
        loteActivo.setEmpresaId(EMPRESA_ID);
        loteActivo.setEstado(AvicolaLoteEstado.ACTIVO);
        loteActivo.setCantidadAnimales(1000);
    }

    @Test
    void registrarMuerte_noDescuentaPlantel() {
        when(servicioLote.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(loteActivo);
        when(muerteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AvicolaMuerteSolicitud solicitud = new AvicolaMuerteSolicitud();
        solicitud.setFecha(LocalDate.now());
        solicitud.setCantidad(5);

        servicio.registrarMuerte(EMPRESA_ID, LOTE_ID, solicitud);

        verify(servicioLote, never()).guardarLote(any());
        assertEquals(1000, loteActivo.getCantidadAnimales());
    }

    @Test
    void registrarMuerte_loteCerrado_falla() {
        loteActivo.setEstado(AvicolaLoteEstado.CERRADO);
        when(servicioLote.obtenerEntidadLote(EMPRESA_ID, LOTE_ID)).thenReturn(loteActivo);

        AvicolaMuerteSolicitud solicitud = new AvicolaMuerteSolicitud();
        solicitud.setFecha(LocalDate.now());
        solicitud.setCantidad(1);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrarMuerte(EMPRESA_ID, LOTE_ID, solicitud));
    }
}
