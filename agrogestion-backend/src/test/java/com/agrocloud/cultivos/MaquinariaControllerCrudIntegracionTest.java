package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MaquinariaControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_maquinaria_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Tractor Test " + sufijo,
                "marca", "John Deere",
                "modelo", "5075E",
                "tipo", "TRACTOR",
                "estado", "DISPONIBLE",
                "activo", true
        );

        MvcResult creado = postJson("/api/maquinaria", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tractor Test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/maquinaria/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("John Deere"));

        Map<String, Object> actualizar = Map.of(
                "nombre", "Tractor Actualizado " + sufijo,
                "marca", "Case IH",
                "modelo", "Farmall",
                "tipo", "TRACTOR",
                "estado", "DISPONIBLE",
                "activo", true
        );
        putJson("/api/maquinaria/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Case IH"));

        deleteJson("/api/maquinaria/" + id).andExpect(status().isNoContent());
        // Baja lógica: el recurso sigue existiendo pero inactivo
        getJson("/api/maquinaria/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
