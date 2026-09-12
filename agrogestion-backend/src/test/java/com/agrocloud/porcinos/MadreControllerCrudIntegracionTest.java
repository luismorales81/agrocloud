package com.agrocloud.porcinos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MadreControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("PORCINOS");
    }

    @Test
    void cicloCrud_madre_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "identificacion", "MADRE-" + sufijo,
                "fechaNacimiento", LocalDate.of(2023, 1, 15).toString(),
                "cantidadTetas", 14,
                "origen", "EXTERNA",
                "fechaIngresoGranja", LocalDate.of(2024, 6, 1).toString(),
                "numeroPartos", 0
        );

        MvcResult creado = postJson("/api/v1/porcinos/madres", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacion").value("MADRE-" + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/v1/porcinos/madres/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacion").value("MADRE-" + sufijo));

        Map<String, Object> actualizar = Map.of(
                "identificacion", "MADRE-" + sufijo,
                "fechaNacimiento", LocalDate.of(2023, 1, 15).toString(),
                "cantidadTetas", 16,
                "origen", "EXTERNA",
                "fechaIngresoGranja", LocalDate.of(2024, 6, 1).toString(),
                "numeroPartos", 1,
                "observaciones", "Actualizada en test"
        );
        putJson("/api/v1/porcinos/madres/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadTetas").value(16));

        deleteJson("/api/v1/porcinos/madres/" + id).andExpect(status().isNoContent());
    }

    @Test
    void listarMadres_ok() throws Exception {
        getJson("/api/v1/porcinos/madres").andExpect(status().isOk());
    }
}
