package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LaborControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void cicloCrud_labor_ok() throws Exception {
        Long loteId = crearLoteCultivoYRetornarId();

        Map<String, Object> crear = Map.of(
                "tipoLabor", "OTROS",
                "descripcion", "Labor test " + sufijo,
                "fechaInicio", LocalDate.now().toString(),
                "estado", "PLANIFICADA",
                "responsable", "Test CRUD",
                "costoTotal", 0,
                "lote", Map.of("id", loteId)
        );

        MvcResult creado = postJson("/api/labores", crear)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Labor test " + sufijo))
                .andReturn();
        Long id = extraerId(creado);

        getJson("/api/labores/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoLabor").value("OTROS"));

        Map<String, Object> actualizar = Map.of(
                "tipoLabor", "OTROS",
                "descripcion", "Labor actualizada " + sufijo,
                "fechaInicio", LocalDate.now().toString(),
                "estado", "PLANIFICADA",
                "costoTotal", 0,
                "activo", true,
                "lote", Map.of("id", loteId)
        );
        putJson("/api/labores/" + id, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Labor actualizada " + sufijo));

        deleteJson("/api/labores/" + id).andExpect(status().isOk());
    }

    @Test
    void listarLabores_ok() throws Exception {
        getJson("/api/labores").andExpect(status().isOk());
    }
}
