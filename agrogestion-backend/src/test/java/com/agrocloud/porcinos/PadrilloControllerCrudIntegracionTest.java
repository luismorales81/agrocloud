package com.agrocloud.porcinos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PadrilloControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    private static final String BASE = "/api/v1/porcinos/padrillos";

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("PORCINOS");
    }

    @Test
    void cicloCrud_padrillo_ok() throws Exception {
        Map<String, Object> crear = Map.of(
                "identificacion", "PAD-" + sufijo,
                "fechaNacimiento", LocalDate.of(2022, 3, 10).toString(),
                "origen", "EXTERNA",
                "fechaIngresoGranja", LocalDate.of(2024, 1, 15).toString()
        );

        MvcResult creado = postJson(BASE, crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacion").value("PAD-" + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson(BASE + "/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacion").value("PAD-" + sufijo));

        Map<String, Object> actualizar = Map.of(
                "identificacion", "PAD-" + sufijo,
                "fechaNacimiento", LocalDate.of(2022, 3, 10).toString(),
                "origen", "EXTERNA",
                "fechaIngresoGranja", LocalDate.of(2024, 1, 15).toString(),
                "observaciones", "Actualizado en test CRUD"
        );
        putJson(BASE + "/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observaciones").value("Actualizado en test CRUD"));

        deleteJson(BASE + "/" + id).andExpect(status().isNoContent());
    }

    @Test
    void listarPadrillos_ok() throws Exception {
        getJson(BASE).andExpect(status().isOk());
    }
}
