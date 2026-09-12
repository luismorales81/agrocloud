package com.agrocloud.avicola;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvicolaPonedorasCrudIntegracionTest extends BaseIntegracionCrudTest {

    private static final String BASE_GALPONES = "/api/avicola-ponedoras/galpones";

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("AVICOLA_CRIANZA");
        habilitarModulo("AVICOLA_HUEVOS");
        habilitarModulo("AVICOLA_PONEDORAS");
    }

    @Test
    void cicloCrud_galpon_ok() throws Exception {
        MvcResult estCreado = postJson("/api/avicola-crianza/establecimientos", Map.of(
                "nombre", "Est Ponedoras " + sufijo,
                "ubicacion", "Test",
                "activo", true
        ))
                .andExpect(status().isOk())
                .andReturn();
        Long establecimientoId = extraerId(estCreado);

        Map<String, Object> crearGalpon = Map.of(
                "establecimientoId", establecimientoId,
                "nombre", "Galpón " + sufijo,
                "raza", "Hy-Line",
                "fechaIngreso", LocalDate.of(2025, 6, 1).toString(),
                "cantidadInicial", 500
        );

        MvcResult galponCreado = postJsonConCampana(BASE_GALPONES, crearGalpon)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Galpón " + sufijo))
                .andReturn();
        Long galponId = extraerId(galponCreado);

        getJson(BASE_GALPONES + "/" + galponId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadInicial").value(500));

        Map<String, Object> actualizar = Map.of(
                "establecimientoId", establecimientoId,
                "nombre", "Galpón Actualizado " + sufijo,
                "raza", "Lohmann",
                "fechaIngreso", LocalDate.of(2025, 6, 1).toString(),
                "cantidadAves", 480,
                "estado", "ACTIVO",
                "observaciones", "Actualizado en test CRUD"
        );
        putJsonConCampana(BASE_GALPONES + "/" + galponId, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Galpón Actualizado " + sufijo))
                .andExpect(jsonPath("$.cantidadAves").value(480));
    }

    @Test
    void listarGalpones_conModulo_ok() throws Exception {
        getJson(BASE_GALPONES).andExpect(status().isOk());
    }
}
