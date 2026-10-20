package com.agrocloud.feedlot;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.entity.FeedlotAnimal;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotEstadoAnimal;
import com.agrocloud.feedlot.model.enums.FeedlotResultadoFilaManga;
import com.agrocloud.feedlot.repository.FeedlotAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotEventoSanitarioRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import com.agrocloud.feedlot.service.manga.ConfirmacionEnrolamiento;
import com.agrocloud.feedlot.service.manga.ContextoConfirmacionManga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotSesionMangaEnrolamientoTest {

    @Mock private FeedlotAnimalRepository animalRepository;
    @Mock private FeedlotSesionMangaFilaRepository filaRepository;
    @Mock private FeedlotPesadaAnimalRepository pesadaAnimalRepository;
    @Mock private FeedlotEventoSanitarioRepository eventoSanitarioRepository;
    @Mock private ServicioFeedlotLotes servicioLotes;
    @Mock private ServicioSeguridadContexto ignorado;

    private ConfirmacionEnrolamiento confirmacion;
    private FeedlotLote lote;
    private FeedlotSesionManga sesion;

    @BeforeEach
    void setUp() {
        confirmacion = new ConfirmacionEnrolamiento(
                animalRepository, filaRepository, pesadaAnimalRepository, eventoSanitarioRepository, servicioLotes);
        lote = new FeedlotLote();
        lote.setId(10L);
        lote.setCabezasActuales(2);
        sesion = new FeedlotSesionManga();
        sesion.setId(1L);
        sesion.setEmpresaId(1L);
        sesion.setLote(lote);
        sesion.setFecha(LocalDate.of(2026, 9, 12));
        sesion.setAjustarPlantel(false);
    }

    @Test
    void excesoSinAjusteLanzaConflicto() {
        FeedlotSesionMangaFila fila = new FeedlotSesionMangaFila();
        fila.setEidNormalizado("000000000000001");
        when(animalRepository.save(any())).thenAnswer(inv -> {
            FeedlotAnimal a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });
        when(animalRepository.contarPorLoteEstado(10L, 1L, FeedlotEstadoAnimal.ACTIVO)).thenReturn(3L);

        assertThrows(ResourceConflictException.class,
                () -> confirmacion.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(new User())));
    }

    @Test
    void excesoConAjusteActualizaPlantel() {
        sesion.setAjustarPlantel(true);
        FeedlotSesionMangaFila fila = new FeedlotSesionMangaFila();
        fila.setEidNormalizado("000000000000001");
        when(animalRepository.save(any())).thenAnswer(inv -> {
            FeedlotAnimal a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });
        when(animalRepository.contarPorLoteEstado(10L, 1L, FeedlotEstadoAnimal.ACTIVO)).thenReturn(3L);
        User usuario = new User();
        usuario.setId(7L);

        confirmacion.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(usuario));

        verify(servicioLotes).registrarAjustePlantel(eq(10L), any(), eq(usuario));
    }
}
