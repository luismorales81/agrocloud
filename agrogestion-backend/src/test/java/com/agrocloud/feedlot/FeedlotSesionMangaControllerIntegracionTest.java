package com.agrocloud.feedlot;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedlotSesionMangaControllerIntegracionTest extends BaseIntegracionFeedlotCrudTest {

    @Test
    void crearBorradorEnrolamiento() throws Exception {
        Long loteId = crearLoteFeedlotCompletoId();
        Map<String, Object> cuerpo = Map.of(
                "loteId", loteId,
                "tipo", "ENROLAMIENTO",
                "fecha", LocalDate.now().toString()
        );
        postJsonConCampana("/api/feedlot/sesiones-manga", cuerpo)
                .andExpect(status().isCreated());
    }
}
