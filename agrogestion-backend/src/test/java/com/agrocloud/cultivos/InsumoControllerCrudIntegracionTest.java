package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InsumoControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_insumo_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Fertilizante Test " + sufijo,
                "tipo", "FERTILIZANTE",
                "unidadMedida", "kg",
                "precioUnitario", 150.0,
                "stockMinimo", 10,
                "stockActual", 100,
                "activo", true
        );

        MvcResult creado = postJson("/api/insumos", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Fertilizante Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/insumos/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FERTILIZANTE"));

        Map<String, Object> actualizar = Map.of(
                "nombre", "Fertilizante Actualizado " + sufijo,
                "tipo", "FERTILIZANTE",
                "unidadMedida", "kg",
                "precioUnitario", 180.0,
                "stockMinimo", 10,
                "stockActual", 90,
                "activo", true
        );
        putJson("/api/insumos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioUnitario").value(180.0));

        deleteJson("/api/insumos/" + id).andExpect(status().isNoContent());
        getJson("/api/insumos/" + id).andExpect(status().isNotFound());
    }
}
