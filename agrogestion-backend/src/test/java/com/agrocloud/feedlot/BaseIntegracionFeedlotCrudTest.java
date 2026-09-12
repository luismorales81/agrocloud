package com.agrocloud.feedlot;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base para tests CRUD de feedlot con fixtures de establecimiento, corral y catálogos.
 */
public abstract class BaseIntegracionFeedlotCrudTest extends BaseIntegracionCrudTest {

    @Override
    protected void configurarContextoAdicional() {
        habilitarModulo("FEEDLOT");
    }

    protected Long crearEstablecimientoFeedlotId() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Feedlot Fixture " + sufijo,
                "ubicacion", "Test",
                "capacidadTotalCabezas", 300,
                "activo", true
        );
        MvcResult resultado = postJson("/api/feedlot/establecimientos", crear)
                .andExpect(status().isCreated())
                .andReturn();
        return extraerId(resultado);
    }

    protected Long crearCorralDisponibleId(Long establecimientoId) throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Corral " + sufijo,
                "capacidadCabezas", 120,
                "estado", "DISPONIBLE",
                "activo", true
        );
        MvcResult resultado = postJson("/api/feedlot/establecimientos/" + establecimientoId + "/corrales", crear)
                .andExpect(status().isCreated())
                .andReturn();
        return extraerId(resultado);
    }

    protected Long crearCategoriaFeedlotId() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Categoría " + sufijo,
                "activo", true
        );
        MvcResult resultado = postJson("/api/feedlot/categorias", crear)
                .andExpect(status().isCreated())
                .andReturn();
        return extraerId(resultado);
    }

    protected Long crearDietaFeedlotId() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Dieta " + sufijo,
                "activo", true
        );
        MvcResult resultado = postJson("/api/feedlot/dietas", crear)
                .andExpect(status().isCreated())
                .andReturn();
        return extraerId(resultado);
    }

    protected Long crearLoteFeedlotId(Long corralId, Long categoriaId) throws Exception {
        Map<String, Object> crear = Map.of(
                "corralId", corralId,
                "nombre", "Lote Feedlot " + sufijo,
                "categoriaId", categoriaId,
                "tipoTenencia", "PROPIO",
                "fechaIngreso", LocalDate.now().toString(),
                "cabezasInicial", 50,
                "pesoPromedioIngresoKg", 280.0
        );
        MvcResult resultado = postJsonConCampana("/api/feedlot/lotes", crear)
                .andExpect(status().isCreated())
                .andReturn();
        return extraerId(resultado);
    }

    /** Fixture completo: establecimiento → corral → categoría → lote feedlot. */
    protected Long crearLoteFeedlotCompletoId() throws Exception {
        Long establecimientoId = crearEstablecimientoFeedlotId();
        Long corralId = crearCorralDisponibleId(establecimientoId);
        Long categoriaId = crearCategoriaFeedlotId();
        return crearLoteFeedlotId(corralId, categoriaId);
    }

    protected Long crearInsumoAlimentoId() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Alimento Feedlot " + sufijo,
                "tipo", "OTROS",
                "unidadMedida", "kg",
                "precioUnitario", 80.0,
                "stockMinimo", 10,
                "stockActual", 5000,
                "activo", true
        );
        MvcResult resultado = postJson("/api/insumos", crear)
                .andExpect(status().isCreated())
                .andReturn();
        return extraerId(resultado);
    }
}
