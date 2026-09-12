package com.agrocloud.porcinos;

import com.agrocloud.core.domain.Campana;
import com.agrocloud.core.domain.CompanyModule;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Module;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.core.infrastructure.CampanaRepository;
import com.agrocloud.core.infrastructure.CompanyModuleRepository;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.ModuleRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRepository;
import com.agrocloud.model.enums.EstadoCampana;
import com.agrocloud.porcinos.config.FiltroDeprecacionApiPorcinosLegacy;
import com.agrocloud.porcinos.model.entity.PorcinosEstablecimiento;
import com.agrocloud.porcinos.model.entity.PorcinosGalpon;
import com.agrocloud.porcinos.model.entity.PorcinosMadre;
import com.agrocloud.porcinos.model.entity.PorcinosPadrillo;
import com.agrocloud.porcinos.model.enums.PorcinosGalponEstado;
import com.agrocloud.porcinos.model.enums.PorcinosMadreEstado;
import com.agrocloud.porcinos.repository.PorcinosEstablecimientoRepository;
import com.agrocloud.porcinos.repository.PorcinosGalponRepository;
import com.agrocloud.porcinos.repository.PorcinosMadreRepository;
import com.agrocloud.porcinos.repository.PorcinosPadrilloRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PorcinosControllerIntegracionTest {

    private static final String MODULO_PORCINOS = "PORCINOS";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private CompanyModuleRepository companyModuleRepository;
    @Autowired private CampanaRepository campanaRepository;
    @Autowired private PorcinosEstablecimientoRepository establecimientoRepository;
    @Autowired private PorcinosGalponRepository galponRepository;
    @Autowired private PorcinosMadreRepository madreRepository;
    @Autowired private PorcinosPadrilloRepository padrilloRepository;

    private User usuario;
    private Empresa empresa;
    private Module moduloPorcinos;
    private PorcinosMadre madre;
    private PorcinosPadrillo padrillo;
    private PorcinosGalpon galpon;
    private Campana campana;

    @BeforeEach
    void setUp() {
        String sufijo = String.valueOf(System.currentTimeMillis());
        usuario = new User();
        usuario.setUsername("porcinos-test-" + sufijo);
        usuario.setEmail("porcinos-" + sufijo + "@test.com");
        usuario.setPassword("password123");
        usuario.setFirstName("Test");
        usuario.setLastName("Porcinos");
        usuario.setActivo(true);
        usuario = userRepository.save(usuario);

        empresa = new Empresa();
        empresa.setNombre("Empresa Test Porcinos");
        empresa.setCuit("30-" + sufijo.substring(Math.max(0, sufijo.length() - 8)) + "-9");
        empresa = empresaRepository.save(empresa);

        usuarioEmpresaRepository.saveAndFlush(
                new UsuarioEmpresa(usuario, empresa, com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR));

        moduloPorcinos = moduleRepository.findByCode(MODULO_PORCINOS).orElseGet(() ->
                moduleRepository.save(new Module("Porcinos", MODULO_PORCINOS, "Módulo porcinos")));

        campana = new Campana();
        campana.setEmpresaId(empresa.getId());
        campana.setCodigo("2025-26");
        campana.setNombre("Campaña test porcinos");
        campana.setFechaInicio(LocalDate.of(2025, 10, 1));
        campana.setFechaFin(LocalDate.of(2026, 9, 30));
        campana.setEstado(EstadoCampana.ACTIVA);
        campana.setEsDefault(true);
        campana = campanaRepository.save(campana);

        PorcinosEstablecimiento establecimiento = new PorcinosEstablecimiento();
        establecimiento.setEmpresaId(empresa.getId());
        establecimiento.setNombre("Granja Test");
        establecimiento.setDiasGestacion(114);
        establecimiento.setActivo(true);
        establecimiento = establecimientoRepository.save(establecimiento);

        galpon = new PorcinosGalpon();
        galpon.setEstablecimiento(establecimiento);
        galpon.setNombre("Galpón 1");
        galpon.setEstado(PorcinosGalponEstado.DISPONIBLE);
        galpon.setActivo(true);
        galpon = galponRepository.save(galpon);

        madre = new PorcinosMadre();
        madre.setEmpresaId(empresa.getId());
        madre.setCaravana("M-" + sufijo);
        madre.setGalpon(galpon);
        madre.setEstado(PorcinosMadreEstado.ADULTA);
        madre.setFechaIngreso(LocalDate.of(2024, 1, 1));
        madre.setActivo(true);
        madre = madreRepository.save(madre);

        padrillo = new PorcinosPadrillo();
        padrillo.setEmpresaId(empresa.getId());
        padrillo.setNombre("Padrillo Test");
        padrillo.setActivo(true);
        padrillo = padrilloRepository.save(padrillo);

        autenticarUsuario();
        habilitarModuloPorcinos();
    }

    private void autenticarUsuario() {
        UserDetails detalles = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getEmail())
                .password("password123")
                .roles("USER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(detalles, null, detalles.getAuthorities()));
    }

    private void habilitarModuloPorcinos() {
        if (!companyModuleRepository.existsByCompanyIdAndModuleIdAndEnabledTrue(
                empresa.getId(), moduloPorcinos.getId())) {
            companyModuleRepository.save(new CompanyModule(empresa, moduloPorcinos, true));
        }
    }

    @Test
    void cicloCompleto_reproduccionLoteVenta() throws Exception {
        String cuerpoServicio = objectMapper.writeValueAsString(Map.of(
                "fecha", "2026-01-10",
                "padrilloId", padrillo.getId()));

        MvcResult servicioResult = mockMvc.perform(post("/api/porcinos/madres/{id}/servicios", madre.getId())
                        .header("X-Company-Id", empresa.getId())
                        .header("X-Campaign-Id", campana.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoServicio))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gestacionId").exists())
                .andReturn();

        JsonNode servicioJson = objectMapper.readTree(servicioResult.getResponse().getContentAsString());
        Long gestacionId = servicioJson.get("gestacionId").asLong();

        String cuerpoParto = objectMapper.writeValueAsString(Map.of(
                "fecha", "2026-05-05",
                "nacidosVivos", 12,
                "nacidosMuertos", 0,
                "momificados", 0));

        MvcResult partoResult = mockMvc.perform(post("/api/porcinos/gestaciones/{id}/partos", gestacionId)
                        .header("X-Company-Id", empresa.getId())
                        .header("X-Campaign-Id", campana.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoParto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        Long partoId = objectMapper.readTree(partoResult.getResponse().getContentAsString()).get("id").asLong();

        String cuerpoDestete = objectMapper.writeValueAsString(Map.of(
                "fecha", "2026-06-15",
                "cantidadDestetados", 11,
                "pesoPromedioKg", 6.5,
                "galponId", galpon.getId()));

        MvcResult desteteResult = mockMvc.perform(post("/api/porcinos/partos/{id}/destetes", partoId)
                        .header("X-Company-Id", empresa.getId())
                        .header("X-Campaign-Id", campana.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDestete))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loteId").exists())
                .andReturn();

        Long loteId = objectMapper.readTree(desteteResult.getResponse().getContentAsString()).get("loteId").asLong();
        assertNotNull(loteId);

        String cuerpoVenta = objectMapper.writeValueAsString(Map.of(
                "fecha", "2026-08-01",
                "tipo", "FAENA",
                "cabezas", 11,
                "pesoPromedioKg", 95,
                "precioKg", 2.5));

        mockMvc.perform(post("/api/porcinos/lotes/{id}/ventas", loteId)
                        .header("X-Company-Id", empresa.getId())
                        .header("X-Campaign-Id", campana.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoVenta))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/porcinos/lotes/{id}", loteId)
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cabezasActuales").value(0))
                .andExpect(jsonPath("$.estado").value("CERRADO"));

        mockMvc.perform(get("/api/porcinos/ventas")
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loteId").value(loteId))
                .andExpect(jsonPath("$[0].tipo").value("FAENA"));
    }

    @Test
    void filtroLegacy_devuelve410() throws Exception {
        FiltroDeprecacionApiPorcinosLegacy filtro = new FiltroDeprecacionApiPorcinosLegacy(objectMapper);
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/porcinos/madres");
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        filtro.doFilter(request, response, (req, res) -> { });
        assertEquals(410, response.getStatus());
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals("/api/porcinos", body.get("nuevaRuta").asText());
    }

    @Test
    void getSinModuloPorcinos_rechazaAcceso() throws Exception {
        companyModuleRepository.findByCompanyIdAndModuleId(empresa.getId(), moduloPorcinos.getId())
                .ifPresent(cm -> {
                    cm.setEnabled(false);
                    companyModuleRepository.save(cm);
                });

        mockMvc.perform(get("/api/porcinos/lotes")
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error").value("MODULE_NOT_ENABLED"));
    }
}
