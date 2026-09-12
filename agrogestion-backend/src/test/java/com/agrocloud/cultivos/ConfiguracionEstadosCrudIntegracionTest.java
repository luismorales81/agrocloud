package com.agrocloud.cultivos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConfiguracionEstadosCrudIntegracionTest extends BaseIntegracionCrudTest {

    private static final String BASE = "/api/v1/configuracion-estados";

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("CULTIVOS");
    }

    @Test
    void cicloCrud_tipoCultivoYEstado_ok() throws Exception {
        String nombreTipo = "TipoTest-" + sufijo;
        Map<String, Object> crearTipo = Map.of(
                "nombre", nombreTipo,
                "descripcion", "Tipo de cultivo para test CRUD",
                "esPlantilla", false,
                "activo", true
        );

        MvcResult tipoCreado = postJson(BASE + "/tipos-cultivo", crearTipo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombreTipo))
                .andReturn();
        Long tipoId = extraerId(tipoCreado);

        getJson(BASE + "/tipos-cultivo/" + tipoId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombreTipo));

        Map<String, Object> actualizarTipo = Map.of(
                "nombre", nombreTipo,
                "descripcion", "Descripción actualizada",
                "esPlantilla", false,
                "activo", true
        );
        putJson(BASE + "/tipos-cultivo/" + tipoId, actualizarTipo)
                .andExpect(status().isOk());

        String nombreEstado = "EstadoTest-" + sufijo;
        Map<String, Object> crearEstado = Map.of(
                "nombre", nombreEstado,
                "descripcion", "Estado inicial de prueba",
                "orden", 1,
                "esEstadoInicial", true,
                "esEstadoFinal", false,
                "activo", true
        );

        MvcResult estadoCreado = mockMvc.perform(
                        conContexto(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .post(BASE + "/estados?tipoCultivoId=" + tipoId)
                        ).content(objectMapper.writeValueAsString(crearEstado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombreEstado))
                .andReturn();
        Long estadoId = extraerId(estadoCreado);

        getJson(BASE + "/estados?tipoCultivoId=" + tipoId)
                .andExpect(status().isOk());

        Map<String, Object> actualizarEstado = Map.of(
                "nombre", nombreEstado,
                "descripcion", "Estado actualizado",
                "orden", 1,
                "esEstadoInicial", true,
                "esEstadoFinal", false,
                "activo", true
        );
        putJson(BASE + "/estados/" + estadoId, actualizarEstado)
                .andExpect(status().isOk());

        deleteJson(BASE + "/estados/" + estadoId).andExpect(status().isOk());
        deleteJson(BASE + "/tipos-cultivo/" + tipoId).andExpect(status().isOk());
    }

    @Test
    void listarTiposCultivo_ok() throws Exception {
        getJson(BASE + "/tipos-cultivo").andExpect(status().isOk());
    }

    @Test
    void descargarPlantillaExcel_ok() throws Exception {
        mockMvc.perform(conContexto(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get(BASE + "/plantilla-excel")))
                .andExpect(status().isOk());
    }
}
