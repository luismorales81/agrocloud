package com.agrocloud.feedlot;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedlotEstablecimientoCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("FEEDLOT");
    }

    @Test
    void cicloCrud_establecimiento_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Feedlot Test " + sufijo,
                "ubicacion", "Pergamino",
                "capacidadTotalCabezas", 500,
                "activo", true
        );

        MvcResult creado = postJson("/api/feedlot/establecimientos", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Feedlot Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/feedlot/establecimientos")
                .andExpect(status().isOk());

        Map<String, Object> actualizar = Map.of(
                "nombre", "Feedlot Actualizado " + sufijo,
                "ubicacion", "Junín",
                "capacidadTotalCabezas", 600,
                "activo", true
        );
        putJson("/api/feedlot/establecimientos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Feedlot Actualizado " + sufijo));
    }
}
