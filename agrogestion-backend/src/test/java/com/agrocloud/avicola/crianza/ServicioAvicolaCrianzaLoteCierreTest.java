package com.agrocloud.avicola.crianza;

import com.agrocloud.avicola.crianza.application.ServicioAvicolaCrianzaLote;
import com.agrocloud.avicola.crianza.model.dto.AvicolaCrianzaCierreLoteSolicitud;
import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.avicola.crianza.model.enums.AvicolaModuloOrigen;
import com.agrocloud.avicola.crianza.repository.AvicolaEstablecimientoRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaLoteRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaRazaRepository;
import com.agrocloud.core.application.CampanaContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioAvicolaCrianzaLoteCierreTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long LOTE_ID = 10L;

    @Mock
    private AvicolaLoteRepository loteRepository;
    @Mock
    private AvicolaEstablecimientoRepository establecimientoRepository;
    @Mock
    private AvicolaRazaRepository razaRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CampanaContextService campanaContextService;

    @InjectMocks
    private ServicioAvicolaCrianzaLote servicio;

    private AvicolaLote lote;

    @BeforeEach
    void setUp() {
        lote = new AvicolaLote();
        lote.setId(LOTE_ID);
        lote.setEmpresaId(EMPRESA_ID);
        lote.setEstado(AvicolaLoteEstado.ACTIVO);
        lote.setCantidadAnimales(0);
        lote.setNombre("Lote test");
    }

    private void mockLoteEnRepo() {
        when(loteRepository.buscarPorIdYEmpresaIdYModulo(LOTE_ID, EMPRESA_ID, AvicolaModuloOrigen.AVICOLA_CRIANZA))
                .thenReturn(Optional.of(lote));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void cerrarLote_sinAves_cierraDirecto() {
        mockLoteEnRepo();

        servicio.cerrarLote(EMPRESA_ID, LOTE_ID, null);

        assertEquals(AvicolaLoteEstado.CERRADO, lote.getEstado());
        assertNotNull(lote.getFechaSalida());
    }

    @Test
    void cerrarLote_conAvesSinConfirmacion_falla() {
        lote.setCantidadAnimales(50);
        when(loteRepository.buscarPorIdYEmpresaIdYModulo(LOTE_ID, EMPRESA_ID, AvicolaModuloOrigen.AVICOLA_CRIANZA))
                .thenReturn(Optional.of(lote));

        assertThrows(IllegalStateException.class, () -> servicio.cerrarLote(EMPRESA_ID, LOTE_ID, null));
        verify(loteRepository, never()).save(any());
    }

    @Test
    void cerrarLote_conAvesYConfirmacion_cierra() {
        lote.setCantidadAnimales(50);
        mockLoteEnRepo();

        AvicolaCrianzaCierreLoteSolicitud solicitud = new AvicolaCrianzaCierreLoteSolicitud();
        solicitud.setConfirmarConAvesPendientes(true);

        servicio.cerrarLote(EMPRESA_ID, LOTE_ID, solicitud);

        assertEquals(AvicolaLoteEstado.CERRADO, lote.getEstado());
        verify(loteRepository).save(eq(lote));
    }
}
