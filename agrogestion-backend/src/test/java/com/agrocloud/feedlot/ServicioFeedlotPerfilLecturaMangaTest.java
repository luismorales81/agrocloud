package com.agrocloud.feedlot;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.feedlot.model.dto.FeedlotAliasColumnasPerfil;
import com.agrocloud.feedlot.model.dto.FeedlotPerfilLecturaMangaSolicitud;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.repository.FeedlotPerfilLecturaMangaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotImportacionManga;
import com.agrocloud.feedlot.service.ServicioFeedlotPerfilLecturaManga;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotPerfilLecturaMangaTest {

    @Mock private ServicioSeguridadContexto seguridad;
    @Mock private FeedlotPerfilLecturaMangaRepository repositorio;
    @Mock private ServicioFeedlotImportacionManga importacion;

    private ServicioFeedlotPerfilLecturaManga servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioFeedlotPerfilLecturaManga(seguridad, repositorio, importacion, new ObjectMapper());
    }

    @Test
    void noGuardaPesadaSinPeso() {
        FeedlotPerfilLecturaMangaSolicitud s = new FeedlotPerfilLecturaMangaSolicitud();
        s.setNombre("Cabezal");
        s.setTipo(FeedlotTipoSesionManga.PESADA);
        FeedlotAliasColumnasPerfil a = new FeedlotAliasColumnasPerfil();
        a.setEid(List.of("EID"));
        s.setAlias(a);
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(s));
    }

    @Test
    void noGuardaSinIdentidad() {
        FeedlotPerfilLecturaMangaSolicitud s = new FeedlotPerfilLecturaMangaSolicitud();
        s.setNombre("Scan");
        s.setTipo(FeedlotTipoSesionManga.RECUENTO);
        s.setAlias(new FeedlotAliasColumnasPerfil());
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(s));
    }
}
