package com.agrocloud.porcinos;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración CRUD para catálogos configurables del módulo porcinos.
 * La API expone crear (POST), listar (GET) y eliminar (DELETE); no hay PUT.
 */
class CatalogosPorcinoCrudIntegracionTest extends BaseIntegracionCrudTest {

  private static final String BASE = "/api/v1/porcinos/catalogos";

  @Override
  protected void configurarContextoAdicional() {
    habilitarModulo("PORCINOS");
  }

  @Test
  void listarLotesPorcinos_ok() throws Exception {
    getJson(BASE + "/lotes").andExpect(status().isOk());
  }

  @Test
  void cicloCrud_razaPorcino_ok() throws Exception {
    Long id = crearCatalogo(BASE + "/razas", Map.of(
        "nombre", "Raza Test " + sufijo,
        "tipo", "MADRE",
        "descripcion", "Raza para test CRUD",
        "activo", true
    ), "nombre", "Raza Test " + sufijo);

    getJson(BASE + "/razas/tipo/MADRE")
        .andExpect(status().isOk());

    eliminarCatalogo(BASE + "/razas", id);
  }

  @Test
  void cicloCrud_tipoServicio_ok() throws Exception {
    Long id = crearCatalogo(BASE + "/tipos-servicio", Map.of(
        "nombre", "Servicio Test " + sufijo,
        "tipo", "IA_TRADICIONAL",
        "descripcion", "Tipo servicio test",
        "activo", true
    ), "nombre", "Servicio Test " + sufijo);

    eliminarCatalogo(BASE + "/tipos-servicio", id);
  }

  @Test
  void cicloCrud_causaMortalidad_ok() throws Exception {
    Long id = crearCatalogo(BASE + "/causas-mortalidad", Map.of(
        "nombre", "Causa Test " + sufijo,
        "etapa", "GENERAL",
        "descripcion", "Causa mortalidad test",
        "activo", true
    ), "nombre", "Causa Test " + sufijo);

    eliminarCatalogo(BASE + "/causas-mortalidad", id);
  }

  @Test
  void cicloCrud_motivoBaja_ok() throws Exception {
    Long id = crearCatalogo(BASE + "/motivos-baja", Map.of(
        "nombre", "Motivo Baja " + sufijo,
        "tipo", "VENTA",
        "descripcion", "Motivo baja test",
        "activo", true
    ), "nombre", "Motivo Baja " + sufijo);

    eliminarCatalogo(BASE + "/motivos-baja", id);
  }

  @Test
  void cicloCrud_esquemaSanitario_ok() throws Exception {
    Long id = crearCatalogo(BASE + "/esquemas-sanitarios", Map.of(
        "nombre", "Esquema Test " + sufijo,
        "tipo", "VACUNA",
        "producto", "Vacuna test",
        "dosis", "2ml",
        "frecuenciaDias", 30,
        "activo", true
    ), "nombre", "Esquema Test " + sufijo);

    eliminarCatalogo(BASE + "/esquemas-sanitarios", id);
  }

  @Test
  void cicloCrud_tipoParto_ok() throws Exception {
    Long id = crearCatalogo(BASE + "/tipos-parto", Map.of(
        "nombre", "Parto Test " + sufijo,
        "descripcion", "Tipo parto test",
        "requiereIntervencion", false,
        "activo", true
    ), "nombre", "Parto Test " + sufijo);

    eliminarCatalogo(BASE + "/tipos-parto", id);
  }

  @Test
  void cicloCrud_tipoEventoSanitario_ok() throws Exception {
    Long id = crearCatalogo(BASE + "/tipos-evento-sanitario", Map.of(
        "nombre", "Evento Sanitario " + sufijo,
        "categoria", "VACUNACION",
        "descripcion", "Tipo evento test",
        "requiereFechaRetiro", false,
        "requiereLoteMedicamento", false,
        "activo", true
    ), "nombre", "Evento Sanitario " + sufijo);

    getJson(BASE + "/tipos-evento-sanitario/categoria/VACUNACION")
        .andExpect(status().isOk());

    eliminarCatalogo(BASE + "/tipos-evento-sanitario", id);
  }

  @Test
  void cicloCrud_ubicacionInterna_jerarquia_ok() throws Exception {
    Long galponId = crearCatalogo(BASE + "/ubicaciones-internas", Map.of(
        "nombre", "Galpón " + sufijo,
        "nivel", "GALPON",
        "tipoUbicacion", "GENERAL",
        "capacidadMaxima", 200,
        "activo", true
    ), "nombre", "Galpón " + sufijo);

    getJson(BASE + "/ubicaciones-internas/nivel/GALPON")
        .andExpect(status().isOk());

    Map<String, Object> sala = new HashMap<>();
    sala.put("nombre", "Sala " + sufijo);
    sala.put("nivel", "SALA");
    sala.put("ubicacionPadreId", galponId);
    sala.put("tipoUbicacion", "RECRIA");
    sala.put("capacidadMaxima", 80);
    sala.put("activo", true);

    Long salaId = crearCatalogo(BASE + "/ubicaciones-internas", sala, "nombre", "Sala " + sufijo);

    getJson(BASE + "/ubicaciones-internas/padre/" + galponId)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(salaId));

    eliminarCatalogo(BASE + "/ubicaciones-internas", salaId);
    eliminarCatalogo(BASE + "/ubicaciones-internas", galponId);
  }

  /** POST + GET listado; devuelve id creado. */
  private Long crearCatalogo(String ruta, Map<String, Object> cuerpo, String campoJson, Object valorEsperado)
      throws Exception {
    MvcResult creado = postJson(ruta, cuerpo)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$." + campoJson).value(valorEsperado))
        .andReturn();
    Long id = extraerId(creado);
    getJson(ruta).andExpect(status().isOk());
    return id;
  }

  private void eliminarCatalogo(String rutaBase, Long id) throws Exception {
    deleteJson(rutaBase + "/" + id).andExpect(status().isOk());
  }
}
