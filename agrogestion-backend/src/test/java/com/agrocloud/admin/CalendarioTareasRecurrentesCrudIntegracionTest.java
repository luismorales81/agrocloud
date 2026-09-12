package com.agrocloud.admin;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendarioTareasRecurrentesCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_tareaRecurrente_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "titulo", "Riego semanal " + sufijo,
                "descripcion", "Verificar aspersores",
                "fechaInicio", LocalDate.now().toString(),
                "tipoRepeticion", "SEMANAL",
                "ambitoCalendario", "GENERAL"
        );

        MvcResult creado = postJson("/api/calendario/tareas-recurrentes", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Riego semanal " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/calendario/tareas-recurrentes")
                .andExpect(status().isOk());

        Map<String, Object> actualizar = Map.of(
                "titulo", "Riego mensual " + sufijo,
                "descripcion", "Actualizado",
                "tipoRepeticion", "MENSUAL"
        );
        putJson("/api/calendario/tareas-recurrentes/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Riego mensual " + sufijo));

        deleteJson("/api/calendario/tareas-recurrentes/" + id).andExpect(status().isNoContent());
    }
}
