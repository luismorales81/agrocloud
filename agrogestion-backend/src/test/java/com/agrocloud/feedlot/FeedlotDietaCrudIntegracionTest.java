package com.agrocloud.feedlot;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedlotDietaCrudIntegracionTest extends BaseIntegracionFeedlotCrudTest {

    @Test
    void cicloCrud_dieta_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Dieta Test " + sufijo,
                "activo", true
        );

        MvcResult creado = postJson("/api/feedlot/dietas", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Dieta Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/feedlot/dietas/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Dieta Test " + sufijo));

        putJson("/api/feedlot/dietas/" + id, Map.of(
                "nombre", "Dieta Actualizada " + sufijo,
                "activo", true
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Dieta Actualizada " + sufijo));
    }
}
