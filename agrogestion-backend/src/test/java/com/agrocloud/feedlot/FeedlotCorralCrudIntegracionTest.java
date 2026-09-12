package com.agrocloud.feedlot;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedlotCorralCrudIntegracionTest extends BaseIntegracionFeedlotCrudTest {

    @Test
    void cicloCrud_corral_ok() throws Exception {
        Long establecimientoId = crearEstablecimientoFeedlotId();

        Map<String, Object> crear = Map.of(
                "nombre", "Corral Test " + sufijo,
                "capacidadCabezas", 80,
                "estado", "DISPONIBLE",
                "activo", true
        );

        MvcResult creado = postJson("/api/feedlot/establecimientos/" + establecimientoId + "/corrales", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Corral Test " + sufijo))
                .andReturn();
        Long corralId = extraerId(creado);

        getJson("/api/feedlot/establecimientos/" + establecimientoId + "/corrales")
                .andExpect(status().isOk());

        Map<String, Object> actualizar = Map.of(
                "nombre", "Corral Actualizado " + sufijo,
                "capacidadCabezas", 100,
                "estado", "DISPONIBLE",
                "activo", true
        );
        putJson("/api/feedlot/establecimientos/" + establecimientoId + "/corrales/" + corralId, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Corral Actualizado " + sufijo));
    }
}
