package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CultivoControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_cultivo_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Soja Test " + sufijo,
                "tipo", "Grano",
                "variedad", "DM 53i54",
                "estado", "ACTIVO"
        );

        MvcResult creado = postJson("/api/v1/cultivos", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Soja Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/v1/cultivos/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Soja Test " + sufijo));

        Map<String, Object> actualizar = Map.of(
                "nombre", "Soja Actualizada " + sufijo,
                "tipo", "Grano",
                "variedad", "DM 53i54",
                "estado", "ACTIVO"
        );
        putJson("/api/v1/cultivos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Soja Actualizada " + sufijo));

        deleteJson("/api/v1/cultivos/" + id).andExpect(status().isOk());
        getJson("/api/v1/cultivos/" + id).andExpect(status().isNotFound());
    }

    @Test
    void listarCultivos_ok() throws Exception {
        getJson("/api/v1/cultivos").andExpect(status().isOk());
    }
}
