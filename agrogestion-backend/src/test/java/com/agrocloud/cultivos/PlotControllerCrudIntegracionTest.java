package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlotControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_lote_ok() throws Exception {
        Long campoId = crearCampoYRetornarId();

        Map<String, Object> crear = Map.of(
                "nombre", "Lote Test " + sufijo,
                "descripcion", "Lote de integración",
                "areaHectareas", 25.5,
                "activo", true,
                "campoId", campoId
        );

        MvcResult creado = postJson("/api/v1/lotes", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lote Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/v1/lotes/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lote Test " + sufijo));

        Map<String, Object> actualizar = Map.of(
                "nombre", "Lote Actualizado " + sufijo,
                "descripcion", "Actualizado",
                "areaHectareas", 30.0,
                "activo", true,
                "campoId", campoId
        );
        putJson("/api/v1/lotes/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lote Actualizado " + sufijo));

        deleteJson("/api/v1/lotes/" + id).andExpect(status().isOk());
    }

    @Test
    void listarLotes_ok() throws Exception {
        getJson("/api/v1/lotes").andExpect(status().isOk());
    }
}
