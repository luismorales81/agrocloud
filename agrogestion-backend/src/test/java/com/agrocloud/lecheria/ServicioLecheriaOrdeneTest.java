package com.agrocloud.lecheria;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.lecheria.model.dto.LecheriaRegistroOrdeneRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaRegistroOrdeneSolicitud;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaLactancia;
import com.agrocloud.lecheria.model.entity.LecheriaRegistroOrdene;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaTurnoOrdene;
import com.agrocloud.lecheria.repository.LecheriaLactanciaRepository;
import com.agrocloud.lecheria.repository.LecheriaRegistroOrdeneRepository;
import com.agrocloud.lecheria.service.ServicioLecheriaAnimales;
import com.agrocloud.lecheria.service.ServicioLecheriaOrdene;
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
class ServicioLecheriaOrdeneTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long ANIMAL_ID = 10L;

    @Mock private ServicioSeguridadContexto servicioSeguridadContexto;
    @Mock private ServicioLecheriaAnimales servicioAnimales;
    @Mock private LecheriaLactanciaRepository lactanciaRepository;
    @Mock private LecheriaRegistroOrdeneRepository ordeneRepository;

    @InjectMocks
    private ServicioLecheriaOrdene servicio;

    private LecheriaAnimal animal;
    private LecheriaLactancia lactancia;

    @BeforeEach
    void setUp() {
        when(servicioSeguridadContexto.obtenerEmpresaIdActual()).thenReturn(EMPRESA_ID);
        animal = new LecheriaAnimal();
        animal.setId(ANIMAL_ID);
        animal.setEmpresaId(EMPRESA_ID);
        animal.setActivo(true);
        animal.setEstado(LecheriaEstadoAnimal.LACTANDO);
        lactancia = new LecheriaLactancia();
        lactancia.setId(5L);
        lactancia.setAnimal(animal);
        lactancia.setActiva(true);
        lactancia.setFechaParto(LocalDate.now().minusDays(30));
    }

    @Test
    void registrarOrdene_animalLactando_ok() {
        when(servicioAnimales.obtenerEntidadAnimal(EMPRESA_ID, ANIMAL_ID)).thenReturn(animal);
        when(lactanciaRepository.buscarActivaPorAnimalId(ANIMAL_ID)).thenReturn(Optional.of(lactancia));
        when(ordeneRepository.existsByAnimalIdAndFechaAndTurno(any(), any(), any())).thenReturn(false);
        when(ordeneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LecheriaRegistroOrdeneSolicitud solicitud = new LecheriaRegistroOrdeneSolicitud();
        solicitud.setFecha(LocalDate.now());
        solicitud.setTurno(LecheriaTurnoOrdene.AM);
        solicitud.setLitros(new BigDecimal("15.5"));

        LecheriaRegistroOrdeneRespuesta resp = servicio.registrarOrdene(ANIMAL_ID, solicitud);
        assertNotNull(resp);
        verify(ordeneRepository).save(any(LecheriaRegistroOrdene.class));
    }

    @Test
    void registrarOrdene_animalNoLactando_falla() {
        animal.setEstado(LecheriaEstadoAnimal.SECA);
        when(servicioAnimales.obtenerEntidadAnimal(EMPRESA_ID, ANIMAL_ID)).thenReturn(animal);

        LecheriaRegistroOrdeneSolicitud solicitud = new LecheriaRegistroOrdeneSolicitud();
        solicitud.setFecha(LocalDate.now());
        solicitud.setTurno(LecheriaTurnoOrdene.PM);
        solicitud.setLitros(new BigDecimal("10"));

        assertThrows(IllegalStateException.class, () -> servicio.registrarOrdene(ANIMAL_ID, solicitud));
    }

    @Test
    void registrarOrdene_turnoDuplicado_falla() {
        when(servicioAnimales.obtenerEntidadAnimal(EMPRESA_ID, ANIMAL_ID)).thenReturn(animal);
        when(ordeneRepository.existsByAnimalIdAndFechaAndTurno(ANIMAL_ID, LocalDate.now(), LecheriaTurnoOrdene.AM))
                .thenReturn(true);

        LecheriaRegistroOrdeneSolicitud solicitud = new LecheriaRegistroOrdeneSolicitud();
        solicitud.setFecha(LocalDate.now());
        solicitud.setTurno(LecheriaTurnoOrdene.AM);
        solicitud.setLitros(new BigDecimal("12"));

        assertThrows(ResourceConflictException.class, () -> servicio.registrarOrdene(ANIMAL_ID, solicitud));
    }
}
