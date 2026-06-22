package com.agrocloud.feedlot;

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
import com.agrocloud.feedlot.model.entity.FeedlotCategoria;
import com.agrocloud.feedlot.model.entity.FeedlotCorral;
import com.agrocloud.feedlot.model.entity.FeedlotEstablecimiento;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.enums.FeedlotCorralEstado;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.model.enums.FeedlotTipoTenencia;
import com.agrocloud.feedlot.repository.FeedlotCategoriaRepository;
import com.agrocloud.feedlot.repository.FeedlotCorralRepository;
import com.agrocloud.feedlot.repository.FeedlotEstablecimientoRepository;
import com.agrocloud.feedlot.repository.FeedlotLoteRepository;
import com.agrocloud.model.enums.EstadoCampana;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class FeedlotControllerIntegracionTest {

    private static final String MODULO_FEEDLOT = "FEEDLOT";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private CompanyModuleRepository companyModuleRepository;
    @Autowired private CampanaRepository campanaRepository;
    @Autowired private FeedlotEstablecimientoRepository establecimientoRepository;
    @Autowired private FeedlotCorralRepository corralRepository;
    @Autowired private FeedlotCategoriaRepository categoriaRepository;
    @Autowired private FeedlotLoteRepository loteRepository;

    private User usuario;
    private Empresa empresa;
    private Module moduloFeedlot;
    private FeedlotLote lote;
    private Campana campana;

    @BeforeEach
    void setUp() {
        String sufijo = String.valueOf(System.currentTimeMillis());
        usuario = new User();
        usuario.setUsername("feedlot-test-" + sufijo);
        usuario.setEmail("feedlot-" + sufijo + "@test.com");
        usuario.setPassword("password123");
        usuario.setFirstName("Test");
        usuario.setLastName("Feedlot");
        usuario.setActivo(true);
        usuario = userRepository.save(usuario);

        empresa = new Empresa();
        empresa.setNombre("Empresa Test Feedlot");
        empresa.setCuit("30-" + sufijo.substring(Math.max(0, sufijo.length() - 8)) + "-8");
        empresa = empresaRepository.save(empresa);

        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa(usuario, empresa, com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR);
        usuarioEmpresaRepository.saveAndFlush(usuarioEmpresa);

        moduloFeedlot = moduleRepository.findByCode(MODULO_FEEDLOT).orElseGet(() ->
                moduleRepository.save(new Module("Feedlot", MODULO_FEEDLOT, "Módulo feedlot")));

        campana = new Campana();
        campana.setEmpresaId(empresa.getId());
        campana.setCodigo("2025-26");
        campana.setNombre("Campaña test feedlot");
        campana.setFechaInicio(LocalDate.of(2025, 10, 1));
        campana.setFechaFin(LocalDate.of(2026, 9, 30));
        campana.setEstado(EstadoCampana.ACTIVA);
        campana.setEsDefault(true);
        campana = campanaRepository.save(campana);

        FeedlotEstablecimiento establecimiento = new FeedlotEstablecimiento();
        establecimiento.setEmpresaId(empresa.getId());
        establecimiento.setNombre("Establecimiento Test");
        establecimiento.setActivo(true);
        establecimiento = establecimientoRepository.save(establecimiento);

        FeedlotCorral corral = new FeedlotCorral();
        corral.setEstablecimiento(establecimiento);
        corral.setNombre("Corral 1");
        corral.setEstado(FeedlotCorralEstado.OCUPADO);
        corral.setActivo(true);
        corral = corralRepository.save(corral);

        FeedlotCategoria categoria = new FeedlotCategoria();
        categoria.setEmpresaId(empresa.getId());
        categoria.setNombre("Novillo");
        categoria.setActivo(true);
        categoria = categoriaRepository.save(categoria);

        lote = new FeedlotLote();
        lote.setEmpresaId(empresa.getId());
        lote.setCorral(corral);
        lote.setCampanaId(campana.getId());
        lote.setNombre("Lote Integración");
        lote.setCategoria(categoria);
        lote.setTipoTenencia(FeedlotTipoTenencia.PROPIO);
        lote.setFechaIngreso(LocalDate.of(2026, 1, 15));
        lote.setCabezasInicial(50);
        lote.setCabezasActuales(50);
        lote.setPesoPromedioIngresoKg(new BigDecimal("280"));
        lote.setEstado(FeedlotLoteEstado.ACTIVO);
        lote = loteRepository.save(lote);

        autenticarUsuario();
    }

    private void autenticarUsuario() {
        UserDetails detalles = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getEmail())
                .password("password123")
                .roles("USER")
                .build();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                detalles, null, detalles.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    void postConCampanaCerrada_rechazaEscritura() throws Exception {
        habilitarModuloFeedlot();

        campana.setEstado(EstadoCampana.CERRADA);
        campana.setEsDefault(true);
        campana = campanaRepository.save(campana);

        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "fecha", "2026-03-01",
                "pesoPromedioKg", 320,
                "cabezasMuestreadas", 10));

        mockMvc.perform(post("/api/feedlot/lotes/{id}/pesadas", lote.getId())
                        .header("X-Company-Id", empresa.getId())
                        .header("X-Campaign-Id", campana.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La campaña está cerrada y no admite modificaciones"));
    }

    @Test
    void getSinModuloFeedlot_rechazaAcceso() throws Exception {
        mockMvc.perform(get("/api/feedlot/lotes")
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error").value("MODULE_NOT_ENABLED"));
    }

    @Test
    void getConModuloFeedlot_permiteLectura() throws Exception {
        habilitarModuloFeedlot();

        mockMvc.perform(get("/api/feedlot/lotes")
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isOk());
    }

    private void habilitarModuloFeedlot() {
        if (!companyModuleRepository.existsByCompanyIdAndModuleIdAndEnabledTrue(empresa.getId(), moduloFeedlot.getId())) {
            companyModuleRepository.save(new CompanyModule(empresa, moduloFeedlot, true));
        }
    }
}
