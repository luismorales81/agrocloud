package com.agrocloud.feedlot;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedlotConsumoCrudIntegracionTest extends BaseIntegracionFeedlotCrudTest {

    @Test
    void cicloCrud_consumoFeedlot_ok() throws Exception {
        Long loteId = crearLoteFeedlotCompletoId();
        Long insumoId = crearInsumoAlimentoId();

        Map<String, Object> crear = Map.of(
                "insumoId", insumoId,
                "fecha", LocalDate.now().toString(),
                "cantidadKg", 50.0,
                "materiaSecaPct", 88.0,
                "observaciones", "Consumo test " + sufijo
        );

        MvcResult creado = postJsonConCampana("/api/feedlot/lotes/" + loteId + "/consumos", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadKg").value(50.0))
                .andReturn();
        Long consumoId = extraerId(creado);

        getJsonConCampana("/api/feedlot/lotes/" + loteId + "/consumos")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(consumoId));

        Map<String, Object> actualizar = Map.of(
                "fecha", LocalDate.now().toString(),
                "cantidadKg", 45.0,
                "materiaSecaPct", 90.0,
                "observaciones", "Consumo actualizado " + sufijo
        );
        putJsonConCampana("/api/feedlot/lotes/" + loteId + "/consumos/" + consumoId, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadKg").value(45.0));

        deleteJsonConCampana("/api/feedlot/lotes/" + loteId + "/consumos/" + consumoId)
                .andExpect(status().isNoContent());
    }
}
