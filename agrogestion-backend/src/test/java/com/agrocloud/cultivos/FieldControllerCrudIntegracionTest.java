package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FieldControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_campo_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Campo Test " + sufijo,
                "ubicacion", "Buenos Aires",
                "areaHectareas", 100.5,
                "estado", "ACTIVO",
                "activo", true
        );

        MvcResult creado = postJson("/api/campos", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Campo Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/campos/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Campo Test " + sufijo));

        Map<String, Object> actualizar = Map.of(
                "nombre", "Campo Actualizado " + sufijo,
                "ubicacion", "Córdoba",
                "areaHectareas", 120.0,
                "estado", "ACTIVO",
                "activo", true
        );
        putJson("/api/campos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Campo Actualizado " + sufijo));

        deleteJson("/api/campos/" + id).andExpect(status().isNoContent());
        getJson("/api/campos/" + id).andExpect(status().isNotFound());
    }

    @Test
    void crearCampo_bodyInvalido_devuelve400() throws Exception {
        postJson("/api/campos", Map.of("nombre", ""))
                .andExpect(status().isBadRequest());
    }
}
