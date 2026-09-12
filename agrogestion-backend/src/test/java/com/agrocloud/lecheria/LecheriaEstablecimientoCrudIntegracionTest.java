package com.agrocloud.lecheria;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LecheriaEstablecimientoCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("LECHERIA");
    }

    @Test
    void cicloCrud_establecimiento_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Tambo Test " + sufijo,
                "ubicacion", "Santa Fe",
                "capacidadAnimales", 200,
                "activo", true
        );

        MvcResult creado = postJson("/api/lecheria/establecimientos", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Tambo Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/lecheria/establecimientos")
                .andExpect(status().isOk());

        Map<String, Object> actualizar = Map.of(
                "nombre", "Tambo Actualizado " + sufijo,
                "ubicacion", "Venado Tuerto",
                "capacidadAnimales", 250,
                "activo", true
        );
        putJson("/api/lecheria/establecimientos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tambo Actualizado " + sufijo));
    }
}
