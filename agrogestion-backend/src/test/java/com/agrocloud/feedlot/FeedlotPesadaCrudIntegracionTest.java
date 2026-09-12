package com.agrocloud.feedlot;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedlotPesadaCrudIntegracionTest extends BaseIntegracionFeedlotCrudTest {

    @Test
    void cicloCrud_pesadaFeedlot_ok() throws Exception {
        Long loteId = crearLoteFeedlotCompletoId();

        Map<String, Object> crear = Map.of(
                "fecha", LocalDate.now().toString(),
                "pesoPromedioKg", 320.5,
                "cabezasMuestreadas", 10,
                "observaciones", "Pesada test " + sufijo
        );

        MvcResult creado = postJsonConCampana("/api/feedlot/lotes/" + loteId + "/pesadas", crear)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pesoPromedioKg").value(320.5))
                .andReturn();
        Long pesadaId = extraerId(creado);

        getJsonConCampana("/api/feedlot/lotes/" + loteId + "/pesadas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pesadaId));

        Map<String, Object> actualizar = Map.of(
                "fecha", LocalDate.now().toString(),
                "pesoPromedioKg", 335.0,
                "cabezasMuestreadas", 12,
                "observaciones", "Pesada actualizada " + sufijo
        );
        putJsonConCampana("/api/feedlot/lotes/" + loteId + "/pesadas/" + pesadaId, actualizar)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesoPromedioKg").value(335.0));

        deleteJsonConCampana("/api/feedlot/lotes/" + loteId + "/pesadas/" + pesadaId)
                .andExpect(status().isNoContent());
    }
}
