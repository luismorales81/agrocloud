package com.agrocloud.lecheria;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.lecheria.model.dto.LecheriaPanelRespuesta;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.repository.*;
import com.agrocloud.lecheria.service.ServicioLecheriaPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioLecheriaPanelTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long CAMPANA_ID = 100L;

    @Mock private ServicioSeguridadContexto servicioSeguridadContexto;
    @Mock private CampanaContextService campanaContextService;
    @Mock private LecheriaAnimalRepository animalRepository;
    @Mock private LecheriaRegistroOrdeneRepository ordeneRepository;
    @Mock private LecheriaEventoReproductivoRepository eventoReproductivoRepository;
    @Mock private LecheriaEventoSanitarioRepository eventoSanitarioRepository;
    @Mock private LecheriaLactanciaRepository lactanciaRepository;
    @Mock private LecheriaParametroEspecieRepository parametroEspecieRepository;

    @InjectMocks
    private ServicioLecheriaPanel servicio;

    @BeforeEach
    void setUp() {
        when(servicioSeguridadContexto.obtenerEmpresaIdActual()).thenReturn(EMPRESA_ID);
        when(campanaContextService.resolverCampanaIdActiva(EMPRESA_ID)).thenReturn(CAMPANA_ID);
        when(ordeneRepository.sumarLitrosPorEmpresaYFechas(any(), any(), any())).thenReturn(new BigDecimal("500"));
        when(animalRepository.contarPorEmpresaIdYEstado(EMPRESA_ID, LecheriaEstadoAnimal.LACTANDO)).thenReturn(10L);
        when(animalRepository.contarPorEmpresaIdYEstado(EMPRESA_ID, LecheriaEstadoAnimal.SECA)).thenReturn(5L);
        when(animalRepository.contarPorEmpresaIdYEstado(EMPRESA_ID, LecheriaEstadoAnimal.PRENADA)).thenReturn(3L);
        when(ordeneRepository.listarPorEmpresaId(EMPRESA_ID)).thenReturn(Collections.emptyList());
        when(animalRepository.listarPorEmpresaId(EMPRESA_ID)).thenReturn(Collections.emptyList());
        when(eventoSanitarioRepository.listarConRetiroVigente(any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    void resumenPeriodoActivo_incluyeKpis() {
        LecheriaPanelRespuesta panel = servicio.resumenPeriodoActivo();
        assertNotNull(panel);
        assertEquals(CAMPANA_ID, panel.getCampanaId());
        assertEquals(new BigDecimal("500"), panel.getLitrosTotalesPeriodo());
        assertEquals(10L, panel.getAnimalesLactando());
        assertEquals(5L, panel.getAnimalesSecas());
        assertEquals(3L, panel.getAnimalesPrenadas());
        assertNotNull(panel.getAcciones());
    }
}
