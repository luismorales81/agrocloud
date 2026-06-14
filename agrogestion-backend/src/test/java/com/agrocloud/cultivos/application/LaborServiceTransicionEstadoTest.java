package com.agrocloud.cultivos.application;

import com.agrocloud.dto.ActualizarLaborParcialRequest;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de la maquina de estados de Labor (spec SDD).
 * Valida transiciones, invariantes I1 a I5 y que PUT y PATCH usen la misma logica.
 */
@ExtendWith(MockitoExtension.class)
class LaborServiceTransicionEstadoTest {

    @Mock
    private LaborRepository laborRepository;

    @Mock
    private PlotRepository plotRepository;

    @Mock
    private TransicionEstadoService transicionEstadoService;

    @InjectMocks
    private LaborService laborService;

    private User usuario;
    private Labor laborPlanificada;
    private Labor laborCompletada;
    private Labor laborCancelada;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);
        usuario.setEmail("test@test.com");

        laborPlanificada = new Labor();
        laborPlanificada.setId(10L);
        laborPlanificada.setTipoLabor(Labor.TipoLabor.SIEMBRA);
        laborPlanificada.setEstado(Labor.EstadoLabor.PLANIFICADA);
        laborPlanificada.setFechaInicio(LocalDate.now().minusDays(1));
        laborPlanificada.setActivo(true);
        laborPlanificada.setLote(null);

        laborCompletada = new Labor();
        laborCompletada.setId(20L);
        laborCompletada.setTipoLabor(Labor.TipoLabor.SIEMBRA);
        laborCompletada.setEstado(Labor.EstadoLabor.COMPLETADA);
        laborCompletada.setFechaInicio(LocalDate.now().minusDays(1));
        laborCompletada.setFechaRealizacion(LocalDate.now());
        laborCompletada.setActivo(true);
        laborCompletada.setLote(null);

        laborCancelada = new Labor();
        laborCancelada.setId(30L);
        laborCancelada.setTipoLabor(Labor.TipoLabor.SIEMBRA);
        laborCancelada.setEstado(Labor.EstadoLabor.CANCELADA);
        laborCancelada.setFechaInicio(LocalDate.now().plusDays(2));
        laborCancelada.setActivo(true);
        laborCancelada.setLote(null);
    }

    @Test
    @DisplayName("PATCH: PLANIFICADA a COMPLETADA asigna fechaRealizacion")
    void patch_planificada_a_completada_setea_fecha_realizacion() {
        Mockito.when(laborRepository.findById(10L)).thenReturn(Optional.of(laborPlanificada));
        Mockito.when(laborRepository.save(Mockito.any(Labor.class))).thenAnswer(i -> i.getArgument(0));

        ActualizarLaborParcialRequest request = new ActualizarLaborParcialRequest();
        request.setEstado("COMPLETADA");
        request.setFechaRealizacion(null);

        Labor resultado = laborService.actualizarParcialLabor(10L, request, usuario);

        assertThat(resultado.getEstado()).isEqualTo(Labor.EstadoLabor.COMPLETADA);
        assertThat(resultado.getFechaRealizacion()).isNotNull();
    }

    @Test
    @DisplayName("PATCH: COMPLETADA a PLANIFICADA lanza IllegalStateException")
    void patch_completada_a_planificada_lanza_excepcion() {
        Mockito.when(laborRepository.findById(20L)).thenReturn(Optional.of(laborCompletada));

        ActualizarLaborParcialRequest request = new ActualizarLaborParcialRequest();
        request.setEstado("PLANIFICADA");

        assertThatThrownBy(() -> laborService.actualizarParcialLabor(20L, request, usuario))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Transición no permitida");
    }

    @Test
    @DisplayName("PATCH: fechaRealizacion anterior a fechaInicio lanza IllegalStateException")
    void patch_fecha_realizacion_anterior_a_fecha_inicio_lanza_excepcion() {
        Mockito.when(laborRepository.findById(10L)).thenReturn(Optional.of(laborPlanificada));

        ActualizarLaborParcialRequest request = new ActualizarLaborParcialRequest();
        request.setEstado("COMPLETADA");
        request.setFechaRealizacion(LocalDate.now().minusDays(10));
        laborPlanificada.setFechaInicio(LocalDate.now().plusDays(5));

        assertThatThrownBy(() -> laborService.actualizarParcialLabor(10L, request, usuario))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("fechaRealizacion no puede ser anterior a fechaInicio");
    }

    @Test
    @DisplayName("PATCH: reprogramar cuando estado != PLANIFICADA lanza excepcion")
    void patch_reprogramar_sin_estar_planificada_lanza_excepcion() {
        Mockito.when(laborRepository.findById(20L)).thenReturn(Optional.of(laborCompletada));

        ActualizarLaborParcialRequest request = new ActualizarLaborParcialRequest();
        request.setFechaPlanificada(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> laborService.actualizarParcialLabor(20L, request, usuario))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Solo se puede reprogramar una labor en estado PLANIFICADA");
    }

    @Test
    @DisplayName("PUT: cambio de estado PLANIFICADA a COMPLETADA setea fechaRealizacion (misma logica que PATCH)")
    void put_cambio_estado_a_completada_setea_fecha_realizacion() {
        Mockito.when(laborRepository.findById(10L)).thenReturn(Optional.of(laborPlanificada));
        Mockito.when(laborRepository.save(Mockito.any(Labor.class))).thenAnswer(i -> i.getArgument(0));

        Labor laborData = new Labor();
        laborData.setTipoLabor(laborPlanificada.getTipoLabor());
        laborData.setDescripcion("Desc");
        laborData.setFechaInicio(laborPlanificada.getFechaInicio());
        laborData.setFechaFin(null);
        laborData.setCostoTotal(null);
        laborData.setObservaciones(null);
        laborData.setLote(null);
        laborData.setEstado(Labor.EstadoLabor.COMPLETADA);
        laborData.setFechaRealizacion(null);

        Labor resultado = laborService.actualizarLabor(10L, laborData, usuario);

        assertThat(resultado.getEstado()).isEqualTo(Labor.EstadoLabor.COMPLETADA);
        assertThat(resultado.getFechaRealizacion()).isNotNull();
    }

    @Test
    @DisplayName("PUT: cambio de estado COMPLETADA a PLANIFICADA lanza IllegalStateException")
    void put_completada_a_planificada_lanza_excepcion() {
        Mockito.when(laborRepository.findById(20L)).thenReturn(Optional.of(laborCompletada));

        Labor laborData = new Labor();
        laborData.setTipoLabor(laborCompletada.getTipoLabor());
        laborData.setDescripcion(laborCompletada.getDescripcion());
        laborData.setFechaInicio(laborCompletada.getFechaInicio());
        laborData.setFechaFin(laborCompletada.getFechaFin());
        laborData.setCostoTotal(laborCompletada.getCostoTotal());
        laborData.setObservaciones(laborCompletada.getObservaciones());
        laborData.setLote(null);
        laborData.setEstado(Labor.EstadoLabor.PLANIFICADA);

        assertThatThrownBy(() -> laborService.actualizarLabor(20L, laborData, usuario))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Transición no permitida");
    }

    @Test
    @DisplayName("PUT: cambiar fechaInicio cuando estado != PLANIFICADA lanza excepcion")
    void put_cambiar_fecha_inicio_sin_estar_planificada_lanza_excepcion() {
        Mockito.when(laborRepository.findById(20L)).thenReturn(Optional.of(laborCompletada));

        Labor laborData = new Labor();
        laborData.setTipoLabor(laborCompletada.getTipoLabor());
        laborData.setDescripcion(laborCompletada.getDescripcion());
        laborData.setFechaInicio(LocalDate.now().plusDays(10));
        laborData.setFechaFin(laborCompletada.getFechaFin());
        laborData.setCostoTotal(laborCompletada.getCostoTotal());
        laborData.setObservaciones(laborCompletada.getObservaciones());
        laborData.setLote(null);
        laborData.setEstado(Labor.EstadoLabor.COMPLETADA);
        laborData.setFechaRealizacion(laborCompletada.getFechaRealizacion());

        assertThatThrownBy(() -> laborService.actualizarLabor(20L, laborData, usuario))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Solo se puede reprogramar una labor en estado PLANIFICADA");
    }
}
