package com.agrocloud.admin;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CampanaCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_campana_crearActivarCerrar_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "codigo", "CAMP-" + sufijo.substring(Math.max(0, sufijo.length() - 6)),
                "nombre", "Campaña nueva " + sufijo,
                "fechaInicio", "2026-10-01",
                "fechaFin", "2027-09-30"
        );

        MvcResult creado = postJson("/api/v1/campanas", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Campaña nueva " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/v1/campanas")
                .andExpect(status().isOk());

        postJson("/api/v1/campanas/" + id + "/activar", Map.of())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        postJson("/api/v1/campanas/" + id + "/cerrar", Map.of())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"));
    }
}
