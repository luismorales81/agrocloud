package com.agrocloud.trazabilidad.api;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRepository;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.LaborInsumo;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.cultivos.infrastructure.LaborInsumoRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import com.agrocloud.trazabilidad.domain.TrazabilidadReglaCertificacion;
import com.agrocloud.trazabilidad.infrastructure.TrazabilidadCertificacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class TrazabilidadComercialControllerIntegracionTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired private FieldRepository fieldRepository;
    @Autowired private PlotRepository plotRepository;
    @Autowired private LaborRepository laborRepository;
    @Autowired private LaborInsumoRepository laborInsumoRepository;
    @Autowired private InsumoRepository insumoRepository;
    @Autowired private TrazabilidadCertificacionRepository trazabilidadCertificacionRepository;

    private User usuario;
    private Empresa empresa;
    private Plot lote;

    @BeforeEach
    void setUp() {
        // Usuario usado por controller cuando no hay filtros de seguridad: test@test.com
        usuario = new User();
        usuario.setUsername("test-traz-" + System.currentTimeMillis());
        usuario.setEmail("test@test.com");
        usuario.setPassword("password123");
        usuario.setFirstName("Test");
        usuario.setLastName("Usuario");
        usuario.setActivo(true);
        usuario = userRepository.save(usuario);

        empresa = new Empresa();
        empresa.setNombre("Empresa Test Trazabilidad");
        empresa.setCuit("30-99999999-9");
        empresa = empresaRepository.save(empresa);

        UsuarioEmpresa ue = new UsuarioEmpresa(usuario, empresa, com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR);
        usuarioEmpresaRepository.save(ue);

        Field campo = new Field();
        campo.setNombre("Campo Test");
        campo.setUbicacion("Test");
        campo.setAreaHectareas(BigDecimal.TEN);
        campo.setUser(usuario);
        campo.setEmpresa(empresa);
        campo.setActivo(true);
        campo.setEstado("ACTIVO");
        campo = fieldRepository.save(campo);

        lote = new Plot();
        lote.setNombre("Lote Test");
        lote.setAreaHectareas(BigDecimal.ONE);
        lote.setCampo(campo);
        lote.setUser(usuario);
        lote.setActivo(true);
        lote = plotRepository.save(lote);
    }

    @Test
    void post_reporte_rechaza_si_hay_insumo_prohibido() throws Exception {
        crearCertificacionLibreAgroquimicosProhibiendo("HERBICIDA");

        Insumo herbicida = new Insumo();
        herbicida.setNombre("Herbicida X");
        herbicida.setTipo(Insumo.TipoInsumo.HERBICIDA);
        herbicida.setUnidadMedida("L");
        herbicida.setPrecioUnitario(new BigDecimal("10"));
        herbicida.setStockActual(new BigDecimal("100"));
        herbicida.setStockMinimo(new BigDecimal("1"));
        herbicida.setUser(usuario);
        herbicida.setEmpresa(empresa);
        herbicida.setActivo(true);
        herbicida = insumoRepository.save(herbicida);

        Labor labor = new Labor();
        labor.setTipoLabor(Labor.TipoLabor.CONTROL_MALEZAS);
        labor.setFechaInicio(LocalDate.now().minusDays(1));
        labor.setUsuario(usuario);
        labor.setLote(lote);
        labor.setActivo(true);
        labor = laborRepository.save(labor);

        LaborInsumo li = new LaborInsumo();
        li.setLabor(labor);
        li.setInsumo(herbicida);
        li.setCantidadUsada(new BigDecimal("1"));
        li.setCantidadPlanificada(new BigDecimal("1"));
        li.setCostoUnitario(new BigDecimal("10"));
        li.setCostoTotal(new BigDecimal("10"));
        laborInsumoRepository.save(li);

        mockMvc.perform(post("/api/trazabilidad/reportes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entidadTipo\":\"LOTE\",\"entidadId\":" + lote.getId() + ",\"certificacion\":\"LIBRE_AGROQUIMICOS\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("CERTIFICACION_RECHAZADA"))
            .andExpect(jsonPath("$.incidencias").isArray());
    }

    @Test
    void post_reporte_ok_y_get_pdf_devuelve_pdf() throws Exception {
        crearCertificacionLibreAgroquimicosProhibiendo("HERBICIDA");

        Insumo semilla = new Insumo();
        semilla.setNombre("Semilla Soja");
        semilla.setTipo(Insumo.TipoInsumo.SEMILLA);
        semilla.setUnidadMedida("kg");
        semilla.setPrecioUnitario(new BigDecimal("1"));
        semilla.setStockActual(new BigDecimal("100"));
        semilla.setStockMinimo(new BigDecimal("1"));
        semilla.setUser(usuario);
        semilla.setEmpresa(empresa);
        semilla.setActivo(true);
        semilla = insumoRepository.save(semilla);

        Labor labor = new Labor();
        labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);
        labor.setFechaInicio(LocalDate.now().minusDays(3));
        labor.setUsuario(usuario);
        labor.setLote(lote);
        labor.setActivo(true);
        labor = laborRepository.save(labor);

        LaborInsumo li = new LaborInsumo();
        li.setLabor(labor);
        li.setInsumo(semilla);
        li.setCantidadUsada(new BigDecimal("50"));
        li.setCantidadPlanificada(new BigDecimal("50"));
        li.setCostoUnitario(new BigDecimal("1"));
        li.setCostoTotal(new BigDecimal("50"));
        laborInsumoRepository.save(li);

        String cuerpoCreado = mockMvc.perform(post("/api/trazabilidad/reportes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entidadTipo\":\"LOTE\",\"entidadId\":" + lote.getId() + ",\"certificacion\":\"LIBRE_AGROQUIMICOS\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultado").value("VALIDO"))
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(cuerpoCreado).get("id").asLong();
        assertThat(id).isPositive();

        mockMvc.perform(get("/api/trazabilidad/reportes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id))
            .andExpect(jsonPath("$[0].certificacionCodigo").value("LIBRE_AGROQUIMICOS"));

        byte[] pdf = mockMvc.perform(get("/api/trazabilidad/reportes/" + id + "/pdf"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn().getResponse().getContentAsByteArray();
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(100);
    }

    @Test
    void post_expediente_lote_ok_y_get_pdf() throws Exception {
        String cuerpoCreado = mockMvc.perform(post("/api/trazabilidad/expedientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entidadTipo\":\"LOTE\",\"entidadId\":" + lote.getId() + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultado").value("VALIDO"))
            .andExpect(jsonPath("$.certificacion").value("EXPEDIENTE_CICLO_VIDA"))
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(cuerpoCreado).get("id").asLong();
        byte[] pdf = mockMvc.perform(get("/api/trazabilidad/reportes/" + id + "/pdf"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn().getResponse().getContentAsByteArray();
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(100);
    }

    @Test
    void put_admin_regla_actualiza_parametros_json() throws Exception {
        crearCertificacionLibreAgroquimicosProhibiendo("HERBICIDA");

        TrazabilidadCertificacion c = trazabilidadCertificacionRepository.findByCodigoConReglas("LIBRE_AGROQUIMICOS")
                .orElseThrow();
        long idRegla = c.getReglas().stream().findFirst().orElseThrow().getId();
        assertThat(idRegla).isPositive();

        mockMvc.perform(put("/api/admin/trazabilidad/reglas/" + idRegla)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parametrosJson\":\"{\\\"tiposInsumoProhibidos\\\":[\\\"FERTILIZANTE\\\"]}\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Parametros actualizados"));

        TrazabilidadCertificacion c2 = trazabilidadCertificacionRepository.findByCodigoConReglas("LIBRE_AGROQUIMICOS")
                .orElseThrow();
        String parametros = c2.getReglas().stream().filter(r -> r.getId().equals(idRegla)).findFirst().orElseThrow().getParametrosJson();
        assertThat(parametros).contains("FERTILIZANTE");
    }

    @Test
    void put_admin_regla_rechaza_json_invalido() throws Exception {
        crearCertificacionLibreAgroquimicosProhibiendo("HERBICIDA");

        TrazabilidadCertificacion c = trazabilidadCertificacionRepository.findByCodigoConReglas("LIBRE_AGROQUIMICOS")
                .orElseThrow();
        long idRegla = c.getReglas().stream().findFirst().orElseThrow().getId();

        mockMvc.perform(put("/api/admin/trazabilidad/reglas/" + idRegla)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parametrosJson\":\"{\\\"tiposInsumoProhibidos\\\":[}\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Argumento inválido"));
    }

    private void crearCertificacionLibreAgroquimicosProhibiendo(String tipo) {
        // Limpieza simple por aislamiento transaccional; si ya existe, reusar.
        TrazabilidadCertificacion c = trazabilidadCertificacionRepository.findByCodigoAndActivaTrue("LIBRE_AGROQUIMICOS")
                .orElseGet(() -> {
                    TrazabilidadCertificacion nueva = new TrazabilidadCertificacion();
                    nueva.setCodigo("LIBRE_AGROQUIMICOS");
                    nueva.setDescripcion("Libre de agroquimicos (test)");
                    nueva.setActiva(true);
                    return trazabilidadCertificacionRepository.save(nueva);
                });

        c.getReglas().clear();
        TrazabilidadReglaCertificacion r = new TrazabilidadReglaCertificacion();
        r.setCertificacion(c);
        r.setTipoRegla("NINGUNO_TIPOS_INSUMO");
        r.setParametrosJson("{\"tiposInsumoProhibidos\":[\"" + tipo + "\"]}");
        r.setOrdenEjecucion(0);
        r.setActiva(true);
        c.getReglas().add(r);
        trazabilidadCertificacionRepository.save(c);
    }
}

