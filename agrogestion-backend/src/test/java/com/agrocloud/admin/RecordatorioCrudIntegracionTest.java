package com.agrocloud.admin;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecordatorioCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_recordatorio_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "titulo", "Recordatorio " + sufijo,
                "descripcion", "Verificar silo",
                "fecha", "2026-04-01",
                "tipo", "GENERAL",
                "completado", false
        );

        MvcResult creado = postJson("/api/recordatorios", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Recordatorio " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/recordatorios/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Verificar silo"));

        Map<String, Object> actualizar = Map.of(
                "titulo", "Recordatorio actualizado " + sufijo,
                "descripcion", "Verificar tolva",
                "fecha", "2026-04-02",
                "tipo", "GENERAL",
                "completado", false
        );
        putJson("/api/recordatorios/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Recordatorio actualizado " + sufijo));

        deleteJson("/api/recordatorios/" + id).andExpect(status().isNoContent());
        // Baja lógica: no se espera 404
        getJson("/api/recordatorios/" + id).andExpect(status().isOk());
    }
}
