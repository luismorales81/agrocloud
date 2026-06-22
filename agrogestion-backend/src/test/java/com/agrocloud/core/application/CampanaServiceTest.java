package com.agrocloud.core.application;

import com.agrocloud.core.domain.Campana;
import com.agrocloud.core.infrastructure.CampanaRepository;
import com.agrocloud.dto.CrearCampanaRequest;
import com.agrocloud.exception.BadRequestException;
import com.agrocloud.model.enums.EstadoCampana;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampanaServiceTest {

    @Mock
    private CampanaRepository campanaRepository;

    @InjectMocks
    private CampanaService campanaService;

    @Test
    void validarCampanaEditable_lanzaSiCerrada() {
        Campana campana = new Campana();
        campana.setEstado(EstadoCampana.CERRADA);
        assertThrows(BadRequestException.class, () -> campanaService.validarCampanaEditable(campana));
    }

    @Test
    void crear_rechazaCodigoDuplicado() {
        when(campanaRepository.existsByEmpresaIdAndCodigo(1L, "2025-26")).thenReturn(true);
        CrearCampanaRequest req = new CrearCampanaRequest();
        req.setCodigo("2025-26");
        req.setNombre("Test");
        req.setFechaInicio(LocalDate.of(2025, 10, 1));
        req.setFechaFin(LocalDate.of(2026, 9, 30));
        assertThrows(BadRequestException.class, () -> campanaService.crear(1L, req));
    }

    @Test
    void activar_cierraLaActivaAnterior() {
        Campana actual = new Campana();
        actual.setId(1L);
        actual.setEmpresaId(10L);
        actual.setEstado(EstadoCampana.ACTIVA);
        Campana nueva = new Campana();
        nueva.setId(2L);
        nueva.setEmpresaId(10L);
        nueva.setEstado(EstadoCampana.BORRADOR);
        when(campanaRepository.findById(2L)).thenReturn(Optional.of(nueva));
        when(campanaRepository.findByEmpresaIdAndEstado(10L, EstadoCampana.ACTIVA)).thenReturn(Optional.of(actual));
        when(campanaRepository.save(any(Campana.class))).thenAnswer(inv -> inv.getArgument(0));
        campanaService.activar(10L, 2L);
        verify(campanaRepository, atLeastOnce()).save(any(Campana.class));
    }
}
