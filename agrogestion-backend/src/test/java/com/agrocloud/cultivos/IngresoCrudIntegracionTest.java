package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IngresoCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_ingreso_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "concepto", "Ingreso test " + sufijo,
                "tipoIngreso", "OTROS_INGRESOS",
                "monto", 15000.50,
                "fecha", LocalDate.now().toString(),
                "estado", "REGISTRADO"
        );

        MvcResult creado = postJson("/api/v1/ingresos", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepto").value("Ingreso test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/v1/ingresos/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepto").value("Ingreso test " + sufijo));

        Map<String, Object> actualizar = Map.of(
                "concepto", "Ingreso actualizado " + sufijo,
                "tipoIngreso", "OTROS_INGRESOS",
                "monto", 20000.00,
                "fecha", LocalDate.now().toString(),
                "estado", "CONFIRMADO"
        );
        putJson("/api/v1/ingresos/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepto").value("Ingreso actualizado " + sufijo));

        deleteJson("/api/v1/ingresos/" + id).andExpect(status().isNoContent());
        getJson("/api/v1/ingresos/" + id).andExpect(status().isNotFound());
    }
}
