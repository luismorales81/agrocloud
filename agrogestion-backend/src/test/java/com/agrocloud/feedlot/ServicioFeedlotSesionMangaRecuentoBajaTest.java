package com.agrocloud.feedlot;

import com.agrocloud.core.domain.User;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.dto.FeedlotAccionDesconocidoSolicitud;
import com.agrocloud.feedlot.model.dto.FeedlotConfirmarSesionSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotEventoSanitario;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotAccionDesconocido;
import com.agrocloud.feedlot.model.enums.FeedlotOrigenFilaRecuento;
import com.agrocloud.feedlot.repository.FeedlotEventoSanitarioRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotAnimales;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import com.agrocloud.feedlot.service.manga.ConfirmacionEnrolamiento;
import com.agrocloud.feedlot.service.manga.ConfirmacionRecuento;
import com.agrocloud.feedlot.service.manga.ContextoConfirmacionManga;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotSesionMangaRecuentoBajaTest {

    @Mock private FeedlotEventoSanitarioRepository eventoSanitarioRepository;
    @Mock private ConfirmacionEnrolamiento confirmacionEnrolamiento;
    @Mock private ServicioFeedlotLotes servicioLotes;
    @Mock private FeedlotSesionMangaFilaRepository filaRepository;

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recuentoSinAccionesNoEnrola() {
        ConfirmacionRecuento recuento = new ConfirmacionRecuento(
                confirmacionEnrolamiento, servicioLotes, filaRepository);
        FeedlotSesionManga sesion = new FeedlotSesionManga();
        FeedlotSesionMangaFila fila = new FeedlotSesionMangaFila();
        fila.setId(1L);
        fila.setOrigenRecuento(FeedlotOrigenFilaRecuento.DESCONOCIDO);
        recuento.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(null));
        verifyNoInteractions(confirmacionEnrolamiento);
        verify(filaRepository).saveAll(any());
    }

    @Test
    void operarioNoPuedeEnrolarDesdeRecuento() {
        ConfirmacionRecuento recuento = new ConfirmacionRecuento(
                confirmacionEnrolamiento, servicioLotes, filaRepository);
        FeedlotSesionManga sesion = new FeedlotSesionManga();
        FeedlotLote lote = new FeedlotLote();
        lote.setId(10L);
        sesion.setLote(lote);
        FeedlotSesionMangaFila fila = new FeedlotSesionMangaFila();
        fila.setId(1L);
        fila.setOrigenRecuento(FeedlotOrigenFilaRecuento.DESCONOCIDO);
        FeedlotConfirmarSesionSolicitud solicitud = new FeedlotConfirmarSesionSolicitud();
        FeedlotAccionDesconocidoSolicitud accion = new FeedlotAccionDesconocidoSolicitud();
        accion.setFilaId(1L);
        accion.setAccion(FeedlotAccionDesconocido.ENROLAR_EN_ESTE_LOTE);
        solicitud.setAccionesDesconocidos(List.of(accion));
        User operario = new User();
        operario.setId(3L);

        assertThrows(ResourceConflictException.class,
                () -> recuento.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(operario, solicitud)));
        verify(confirmacionEnrolamiento, never()).enrolarEnLote(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void adminEnrolaDesconocidoEnEsteLote() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "x", List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
        ConfirmacionRecuento recuento = new ConfirmacionRecuento(
                confirmacionEnrolamiento, servicioLotes, filaRepository);
        FeedlotSesionManga sesion = new FeedlotSesionManga();
        FeedlotLote lote = new FeedlotLote();
        lote.setId(10L);
        sesion.setLote(lote);
        FeedlotSesionMangaFila fila = new FeedlotSesionMangaFila();
        fila.setId(1L);
        fila.setOrigenRecuento(FeedlotOrigenFilaRecuento.DESCONOCIDO);
        FeedlotConfirmarSesionSolicitud solicitud = new FeedlotConfirmarSesionSolicitud();
        FeedlotAccionDesconocidoSolicitud accion = new FeedlotAccionDesconocidoSolicitud();
        accion.setFilaId(1L);
        accion.setAccion(FeedlotAccionDesconocido.ENROLAR_EN_ESTE_LOTE);
        solicitud.setAccionesDesconocidos(List.of(accion));
        User admin = new User();
        admin.setId(1L);

        recuento.confirmar(sesion, List.of(fila), new ContextoConfirmacionManga(admin, solicitud));

        verify(confirmacionEnrolamiento).enrolarEnLote(eq(sesion), eq(lote), any(), any(), eq(true));
    }

    @Test
    void retiroVigenteBloqueaFaena() {
        ServicioFeedlotAnimales animales = new ServicioFeedlotAnimales(null, null, null, null, eventoSanitarioRepository);
        FeedlotEventoSanitario e = new FeedlotEventoSanitario();
        e.setFecha(LocalDate.of(2026, 9, 1));
        e.setDiasRetiro(15);
        when(eventoSanitarioRepository.listarPorAnimalId(8L)).thenReturn(List.of(e));
        assertTrue(animales.retiroVigente(8L, LocalDate.of(2026, 9, 12)));
        assertFalse(animales.retiroVigente(8L, LocalDate.of(2026, 9, 20)));
    }
}
