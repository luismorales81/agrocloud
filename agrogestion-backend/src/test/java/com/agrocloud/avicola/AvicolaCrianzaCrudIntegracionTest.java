package com.agrocloud.avicola;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvicolaCrianzaCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("AVICOLA_CRIANZA");
    }

    @Test
    void cicloCrud_establecimiento_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Avícola Crianza " + sufijo,
                "ubicacion", "Rafaela",
                "activo", true
        );

        MvcResult creado = postJson("/api/avicola-crianza/establecimientos", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Avícola Crianza " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/avicola-crianza/establecimientos")
                .andExpect(status().isOk());

        Map<String, Object> actualizar = Map.of(
                "nombre", "Avícola Actualizada " + sufijo,
                "ubicacion", "Reconquista",
                "activo", true
        );
        putJson("/api/avicola-crianza/establecimientos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Avícola Actualizada " + sufijo));
    }
}
