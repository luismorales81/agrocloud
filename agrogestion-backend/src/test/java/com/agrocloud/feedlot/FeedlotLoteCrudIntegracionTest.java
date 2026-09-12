package com.agrocloud.feedlot;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedlotLoteCrudIntegracionTest extends BaseIntegracionFeedlotCrudTest {

    @Test
    void cicloCrud_loteFeedlot_ok() throws Exception {
        Long establecimientoId = crearEstablecimientoFeedlotId();
        Long corralId = crearCorralDisponibleId(establecimientoId);
        Long categoriaId = crearCategoriaFeedlotId();

        MvcResult creado = postJsonConCampana("/api/feedlot/lotes", Map.of(
                "corralId", corralId,
                "nombre", "Lote FL " + sufijo,
                "categoriaId", categoriaId,
                "tipoTenencia", "PROPIO",
                "fechaIngreso", "2026-03-01",
                "cabezasInicial", 40,
                "pesoPromedioIngresoKg", 275.0
        ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Lote FL " + sufijo))
                .andReturn();
        Long loteId = extraerId(creado);

        getJson("/api/feedlot/lotes/" + loteId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lote FL " + sufijo));

        putJsonConCampana("/api/feedlot/lotes/" + loteId, Map.of(
                "corralId", corralId,
                "nombre", "Lote FL Actualizado " + sufijo,
                "categoriaId", categoriaId,
                "tipoTenencia", "PROPIO",
                "fechaIngreso", "2026-03-01",
                "cabezasInicial", 40,
                "pesoPromedioIngresoKg", 280.0
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lote FL Actualizado " + sufijo));
    }
}
