package com.agrocloud.feedlot;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.dto.FeedlotCierreLoteSolicitud;
import com.agrocloud.feedlot.model.dto.FeedlotLoteRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotLoteSolicitud;
import com.agrocloud.feedlot.model.entity.*;
import com.agrocloud.feedlot.model.enums.FeedlotCorralEstado;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.repository.*;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotLotesTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long CAMPANA_ID = 10L;
    private static final Long CORRAL_ID = 5L;

    @Mock
    private ServicioSeguridadContexto servicioSeguridadContexto;
    @Mock
    private CampanaContextService campanaContextService;
    @Mock
    private FeedlotLoteRepository loteRepository;
    @Mock
    private FeedlotCorralRepository corralRepository;
    @Mock
    private FeedlotCategoriaRepository categoriaRepository;
    @Mock
    private FeedlotRazaRepository razaRepository;
    @Mock
    private FeedlotProveedorOrigenRepository proveedorRepository;
    @Mock
    private FeedlotDietaRepository dietaRepository;
    @Mock
    private FeedlotAjustePlantelRepository ajustePlantelRepository;

    @InjectMocks
    private ServicioFeedlotLotes servicioLotes;

    private FeedlotCorral corralDisponible;
    private FeedlotCategoria categoria;

    @BeforeEach
    void setUp() {
        when(servicioSeguridadContexto.obtenerEmpresaIdActual()).thenReturn(EMPRESA_ID);

        FeedlotEstablecimiento est = new FeedlotEstablecimiento();
        est.setId(100L);
        est.setEmpresaId(EMPRESA_ID);
        est.setNombre("Establecimiento Test");

        corralDisponible = new FeedlotCorral();
        corralDisponible.setId(CORRAL_ID);
        corralDisponible.setEstablecimiento(est);
        corralDisponible.setNombre("Corral A");
        corralDisponible.setEstado(FeedlotCorralEstado.DISPONIBLE);
        corralDisponible.setActivo(true);

        categoria = new FeedlotCategoria();
        categoria.setId(2L);
        categoria.setEmpresaId(EMPRESA_ID);
        categoria.setNombre("Novillo");
    }

    private FeedlotLoteSolicitud solicitudAltaBasica() {
        FeedlotLoteSolicitud s = new FeedlotLoteSolicitud();
        s.setCorralId(CORRAL_ID);
        s.setNombre("Lote Test Mar 2026");
        s.setCategoriaId(2L);
        s.setFechaIngreso(LocalDate.of(2026, 3, 15));
        s.setCabezasInicial(120);
        s.setPesoPromedioIngresoKg(new BigDecimal("280.50"));
        return s;
    }

    @Test
    void crearLote_ocupaCorral() {
        when(campanaContextService.resolverCampanaIdActiva(EMPRESA_ID)).thenReturn(CAMPANA_ID);
        when(corralRepository.buscarPorIdYEmpresaId(CORRAL_ID, EMPRESA_ID))
                .thenReturn(Optional.of(corralDisponible));
        when(loteRepository.existsByCorralIdAndEstado(CORRAL_ID, FeedlotLoteEstado.ACTIVO)).thenReturn(false);
        when(categoriaRepository.buscarPorIdYEmpresaId(2L, EMPRESA_ID)).thenReturn(Optional.of(categoria));
        when(corralRepository.save(any(FeedlotCorral.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loteRepository.save(any(FeedlotLote.class))).thenAnswer(inv -> {
            FeedlotLote l = inv.getArgument(0);
            l.setId(99L);
            return l;
        });

        FeedlotLoteRespuesta resp = servicioLotes.crearLote(solicitudAltaBasica());

        assertNotNull(resp);
        assertEquals(FeedlotLoteEstado.ACTIVO, resp.getEstado());
        assertEquals(120, resp.getCabezasActuales());
        assertEquals(CAMPANA_ID, resp.getCampanaId());
        verify(corralRepository).save(argThat(c -> c.getEstado() == FeedlotCorralEstado.OCUPADO));
    }

    @Test
    void crearLote_rechazaSegundoLoteEnCorralOcupado() {
        corralDisponible.setEstado(FeedlotCorralEstado.OCUPADO);
        when(corralRepository.buscarPorIdYEmpresaId(CORRAL_ID, EMPRESA_ID))
                .thenReturn(Optional.of(corralDisponible));

        assertThrows(ResourceConflictException.class, () -> servicioLotes.crearLote(solicitudAltaBasica()));
        verify(loteRepository, never()).save(any());
    }

    @Test
    void crearLote_rechazaSiExisteLoteActivoEnCorral() {
        when(corralRepository.buscarPorIdYEmpresaId(CORRAL_ID, EMPRESA_ID))
                .thenReturn(Optional.of(corralDisponible));
        when(loteRepository.existsByCorralIdAndEstado(CORRAL_ID, FeedlotLoteEstado.ACTIVO)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> servicioLotes.crearLote(solicitudAltaBasica()));
        verify(loteRepository, never()).save(any());
    }

    @Test
    void cerrarLote_liberaCorral() {
        FeedlotLote lote = new FeedlotLote();
        lote.setId(99L);
        lote.setEmpresaId(EMPRESA_ID);
        lote.setCorral(corralDisponible);
        lote.setCampanaId(CAMPANA_ID);
        lote.setNombre("Lote cerrar");
        lote.setCategoria(categoria);
        lote.setFechaIngreso(LocalDate.of(2026, 1, 1));
        lote.setCabezasInicial(50);
        lote.setCabezasActuales(0);
        lote.setPesoPromedioIngresoKg(new BigDecimal("250"));
        lote.setEstado(FeedlotLoteEstado.ACTIVO);
        corralDisponible.setEstado(FeedlotCorralEstado.OCUPADO);

        when(loteRepository.buscarPorIdYEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(FeedlotLote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(corralRepository.save(any(FeedlotCorral.class))).thenAnswer(inv -> inv.getArgument(0));

        FeedlotLoteRespuesta resp = servicioLotes.cerrarLote(99L, new FeedlotCierreLoteSolicitud());

        assertEquals(FeedlotLoteEstado.CERRADO, resp.getEstado());
        assertNotNull(resp.getFechaCierre());
        verify(corralRepository).save(argThat(c -> c.getEstado() == FeedlotCorralEstado.DISPONIBLE));
    }

    @Test
    void cerrarLote_conCabezasRequiereConfirmacion() {
        FeedlotLote lote = new FeedlotLote();
        lote.setId(99L);
        lote.setEmpresaId(EMPRESA_ID);
        lote.setCorral(corralDisponible);
        lote.setCabezasActuales(10);
        lote.setEstado(FeedlotLoteEstado.ACTIVO);

        when(loteRepository.buscarPorIdYEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.of(lote));

        assertThrows(IllegalStateException.class, () -> servicioLotes.cerrarLote(99L, null));
    }

    @Test
    void cerrarLote_conCabezasYConfirmacionCierra() {
        FeedlotLote lote = new FeedlotLote();
        lote.setId(99L);
        lote.setEmpresaId(EMPRESA_ID);
        lote.setCorral(corralDisponible);
        lote.setCabezasActuales(10);
        lote.setEstado(FeedlotLoteEstado.ACTIVO);
        corralDisponible.setEstado(FeedlotCorralEstado.OCUPADO);

        when(loteRepository.buscarPorIdYEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(FeedlotLote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(corralRepository.save(any(FeedlotCorral.class))).thenAnswer(inv -> inv.getArgument(0));

        FeedlotCierreLoteSolicitud confirmacion = new FeedlotCierreLoteSolicitud();
        confirmacion.setConfirmarConCabezas(true);

        FeedlotLoteRespuesta resp = servicioLotes.cerrarLote(99L, confirmacion);
        assertEquals(FeedlotLoteEstado.CERRADO, resp.getEstado());
    }
}
