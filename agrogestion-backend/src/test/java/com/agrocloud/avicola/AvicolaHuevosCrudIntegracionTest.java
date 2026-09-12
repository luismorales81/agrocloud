package com.agrocloud.avicola;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvicolaHuevosCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("AVICOLA_HUEVOS");
    }

    @Test
    void cicloCrud_establecimiento_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Huevos Test " + sufijo,
                "ubicacion", "Crespo",
                "activo", true
        );

        MvcResult creado = postJson("/api/avicola-huevos/establecimientos", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Huevos Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/avicola-huevos/establecimientos")
                .andExpect(status().isOk());

        Map<String, Object> actualizar = Map.of(
                "nombre", "Huevos Actualizado " + sufijo,
                "ubicacion", "Paraná",
                "activo", true
        );
        putJson("/api/avicola-huevos/establecimientos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Huevos Actualizado " + sufijo));
    }
}
