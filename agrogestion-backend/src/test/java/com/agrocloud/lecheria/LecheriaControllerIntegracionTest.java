package com.agrocloud.lecheria;

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
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaLactancia;
import com.agrocloud.lecheria.model.entity.LecheriaRegistroOrdene;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import com.agrocloud.lecheria.model.enums.LecheriaSexoAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaTurnoOrdene;
import com.agrocloud.lecheria.repository.LecheriaAnimalRepository;
import com.agrocloud.lecheria.repository.LecheriaLactanciaRepository;
import com.agrocloud.lecheria.repository.LecheriaRegistroOrdeneRepository;
import com.agrocloud.model.enums.EstadoCampana;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LecheriaControllerIntegracionTest {

    private static final String MODULO_LECHERIA = "LECHERIA";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private CompanyModuleRepository companyModuleRepository;
    @Autowired private CampanaRepository campanaRepository;
    @Autowired private LecheriaAnimalRepository animalRepository;
    @Autowired private LecheriaLactanciaRepository lactanciaRepository;
    @Autowired private LecheriaRegistroOrdeneRepository ordeneRepository;

    private User usuario;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        String sufijo = String.valueOf(System.currentTimeMillis());
        usuario = new User();
        usuario.setUsername("lecheria-test-" + sufijo);
        usuario.setEmail("lecheria-" + sufijo + "@test.com");
        usuario.setPassword("password123");
        usuario.setFirstName("Test");
        usuario.setLastName("Lecheria");
        usuario.setActivo(true);
        usuario = userRepository.save(usuario);

        empresa = new Empresa();
        empresa.setNombre("Empresa Test Lechería");
        empresa.setCuit("30-" + sufijo.substring(Math.max(0, sufijo.length() - 8)) + "-9");
        empresa = empresaRepository.save(empresa);

        usuarioEmpresaRepository.saveAndFlush(
                new UsuarioEmpresa(usuario, empresa, com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR));

        Campana campana = new Campana();
        campana.setEmpresaId(empresa.getId());
        campana.setCodigo("2025-26");
        campana.setNombre("Campaña test lechería");
        campana.setFechaInicio(LocalDate.of(2025, 10, 1));
        campana.setFechaFin(LocalDate.of(2026, 9, 30));
        campana.setEstado(EstadoCampana.ACTIVA);
        campana.setEsDefault(true);
        campanaRepository.save(campana);

        autenticarUsuario();
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

    @Test
    void panelResumen_sinModulo_rechazaAcceso() throws Exception {
        Module modulo = moduleRepository.findByCode(MODULO_LECHERIA).orElseGet(() ->
                moduleRepository.save(new Module("Lechería", MODULO_LECHERIA, "Módulo lechería")));

        mockMvc.perform(get("/api/lecheria/panel/resumen")
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error").value("MODULE_NOT_ENABLED"));
    }

    @Test
    void panelResumen_conModuloYDatosDemo_respondeOk() throws Exception {
        habilitarModuloLecheria();
        cargarDatosMinimosPanel();

        mockMvc.perform(get("/api/lecheria/panel/resumen")
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalesLactando").value(1))
                .andExpect(jsonPath("$.litrosTotalesPeriodo").exists());
    }

    private void habilitarModuloLecheria() {
        Module modulo = moduleRepository.findByCode(MODULO_LECHERIA).orElseGet(() ->
                moduleRepository.save(new Module("Lechería", MODULO_LECHERIA, "Módulo lechería")));
        if (!companyModuleRepository.existsByCompanyIdAndModuleIdAndEnabledTrue(empresa.getId(), modulo.getId())) {
            companyModuleRepository.save(new CompanyModule(empresa, modulo, true));
        }
    }

    private void cargarDatosMinimosPanel() {
        LecheriaAnimal animal = new LecheriaAnimal();
        animal.setEmpresaId(empresa.getId());
        animal.setIdentificacion("TEST-001");
        animal.setEspecie(LecheriaEspecie.BOVINO);
        animal.setSexo(LecheriaSexoAnimal.HEMBRA);
        animal.setEstado(LecheriaEstadoAnimal.LACTANDO);
        animal.setFechaIngreso(LocalDate.now().minusYears(2));
        animal.setActivo(true);
        animal = animalRepository.save(animal);

        LecheriaLactancia lactancia = new LecheriaLactancia();
        lactancia.setAnimal(animal);
        lactancia.setEmpresaId(empresa.getId());
        lactancia.setNumeroLactancia(1);
        lactancia.setFechaParto(LocalDate.now().minusDays(30));
        lactancia.setActiva(true);
        lactancia = lactanciaRepository.save(lactancia);

        LecheriaRegistroOrdene ordene = new LecheriaRegistroOrdene();
        ordene.setAnimal(animal);
        ordene.setLactancia(lactancia);
        ordene.setEmpresaId(empresa.getId());
        ordene.setFecha(LocalDate.now());
        ordene.setTurno(LecheriaTurnoOrdene.AM);
        ordene.setLitros(new BigDecimal("15.500"));
        ordene.setRcs(120_000L);
        ordeneRepository.save(ordene);
    }
}
