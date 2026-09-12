package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EgresoCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_egreso_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "concepto", "Egreso test " + sufijo,
                "tipo", "OTROS",
                "monto", 8500.75,
                "fecha", LocalDate.now().toString(),
                "estado", "REGISTRADO"
        );

        MvcResult creado = postJson("/api/v1/egresos", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.concepto").value("Egreso test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/v1/egresos/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepto").value("Egreso test " + sufijo));

        Map<String, Object> actualizar = Map.of(
                "concepto", "Egreso actualizado " + sufijo,
                "tipo", "OTROS",
                "monto", 9000.00,
                "fecha", LocalDate.now().toString(),
                "estado", "CONFIRMADO"
        );
        putJson("/api/v1/egresos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepto").value("Egreso actualizado " + sufijo));

        deleteJson("/api/v1/egresos/" + id).andExpect(status().isNoContent());
        getJson("/api/v1/egresos/" + id).andExpect(status().isNotFound());
    }
}
