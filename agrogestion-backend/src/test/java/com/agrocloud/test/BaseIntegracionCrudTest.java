package com.agrocloud.test;

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
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base para tests de integración CRUD vía MockMvc.
 * Crea usuario, empresa y autenticación por test (sufijo único).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public abstract class BaseIntegracionCrudTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected EmpresaRepository empresaRepository;
    @Autowired protected UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired protected ModuleRepository moduleRepository;
    @Autowired protected CompanyModuleRepository companyModuleRepository;
    @Autowired protected CampanaRepository campanaRepository;

    protected User usuario;
    protected Empresa empresa;
    protected Campana campana;
    protected String sufijo;

    @BeforeEach
    void baseSetUp() {
        sufijo = String.valueOf(System.nanoTime());
        usuario = crearUsuario("crud-test-" + sufijo, "crud-" + sufijo + "@test.com");
        empresa = crearEmpresa("Empresa CRUD " + sufijo, "30-" + sufijo.substring(Math.max(0, sufijo.length() - 8)) + "-9");
        vincularUsuarioEmpresa(usuario, empresa, RolEmpresa.ADMINISTRADOR);
        campana = crearCampanaActiva(empresa.getId());
        autenticarUsuario(usuario);
        configurarContextoAdicional();
    }

    /** Hook para subclases: habilitar módulos, datos previos, etc. */
    protected void configurarContextoAdicional() {
        // por defecto vacío
    }

    protected String generarSufijo() {
        return sufijo;
    }

    protected User crearUsuario(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("password123");
        u.setFirstName("Test");
        u.setLastName("CRUD");
        u.setActivo(true);
        return userRepository.save(u);
    }

    protected Empresa crearEmpresa(String nombre, String cuit) {
        Empresa e = new Empresa();
        e.setNombre(nombre);
        e.setCuit(cuit);
        return empresaRepository.save(e);
    }

    protected void vincularUsuarioEmpresa(User user, Empresa emp, RolEmpresa rol) {
        UsuarioEmpresa ue = new UsuarioEmpresa(user, emp, rol);
        ue.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.saveAndFlush(ue);
    }

    protected Campana crearCampanaActiva(Long empresaId) {
        Campana c = new Campana();
        c.setEmpresaId(empresaId);
        c.setCodigo("TEST-" + sufijo.substring(Math.max(0, sufijo.length() - 6)));
        c.setNombre("Campaña test " + sufijo);
        c.setFechaInicio(LocalDate.of(2025, 10, 1));
        c.setFechaFin(LocalDate.of(2026, 9, 30));
        c.setEstado(EstadoCampana.ACTIVA);
        c.setEsDefault(true);
        return campanaRepository.save(c);
    }

    protected void habilitarModulo(String codigoModulo) {
        Module modulo = moduleRepository.findByCode(codigoModulo).orElseGet(() ->
                moduleRepository.save(new Module(codigoModulo, codigoModulo, "Módulo test " + codigoModulo)));
        if (!companyModuleRepository.existsByCompanyIdAndModuleIdAndEnabledTrue(empresa.getId(), modulo.getId())) {
            companyModuleRepository.save(new CompanyModule(empresa, modulo, true));
        }
    }

    protected void autenticarUsuario(User user) {
        UserDetails detalles = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("password123")
                .roles("USER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(detalles, null, detalles.getAuthorities()));
    }

    protected MockHttpServletRequestBuilder conContexto(MockHttpServletRequestBuilder builder) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return builder
                .with(securityContext(SecurityContextHolder.getContext()))
                .with(authentication(auth))
                .header("X-Company-Id", empresa.getId())
                .contentType(MediaType.APPLICATION_JSON);
    }

    protected MockHttpServletRequestBuilder conContextoYCampana(MockHttpServletRequestBuilder builder) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return builder
                .with(securityContext(SecurityContextHolder.getContext()))
                .with(authentication(auth))
                .header("X-Company-Id", empresa.getId())
                .header("X-Campaign-Id", campana.getId())
                .contentType(MediaType.APPLICATION_JSON);
    }

    protected ResultActions getJson(String ruta) throws Exception {
        return mockMvc.perform(conContexto(get(ruta)));
    }

    protected ResultActions postJson(String ruta, Object cuerpo) throws Exception {
        return mockMvc.perform(conContexto(post(ruta)).content(objectMapper.writeValueAsString(cuerpo)));
    }

    protected ResultActions putJson(String ruta, Object cuerpo) throws Exception {
        return mockMvc.perform(conContexto(put(ruta)).content(objectMapper.writeValueAsString(cuerpo)));
    }

    protected ResultActions deleteJson(String ruta) throws Exception {
        return mockMvc.perform(conContexto(delete(ruta)));
    }

    protected ResultActions getJsonConCampana(String ruta) throws Exception {
        return mockMvc.perform(conContextoYCampana(get(ruta)));
    }

    protected ResultActions postJsonConCampana(String ruta, Object cuerpo) throws Exception {
        return mockMvc.perform(conContextoYCampana(post(ruta)).content(objectMapper.writeValueAsString(cuerpo)));
    }

    protected ResultActions putJsonConCampana(String ruta, Object cuerpo) throws Exception {
        return mockMvc.perform(conContextoYCampana(put(ruta)).content(objectMapper.writeValueAsString(cuerpo)));
    }

    protected ResultActions deleteJsonConCampana(String ruta) throws Exception {
        return mockMvc.perform(conContextoYCampana(delete(ruta)));
    }

    protected Long crearCampoYRetornarId() throws Exception {
        Map<String, Object> crear = Map.of(
                "nombre", "Campo Fixture " + sufijo,
                "ubicacion", "Test",
                "areaHectareas", 100.0,
                "estado", "ACTIVO",
                "activo", true
        );
        MvcResult resultado = postJson("/api/campos", crear)
                .andExpect(status().isCreated())
                .andReturn();
        return extraerId(resultado);
    }

    protected Long crearLoteCultivoYRetornarId() throws Exception {
        Long campoId = crearCampoYRetornarId();
        Map<String, Object> crear = Map.of(
                "nombre", "Lote Fixture " + sufijo,
                "descripcion", "Lote para tests CRUD",
                "areaHectareas", 20.0,
                "activo", true,
                "campoId", campoId
        );
        MvcResult resultado = postJson("/api/v1/lotes", crear)
                .andExpect(status().isOk())
                .andReturn();
        return extraerId(resultado);
    }

    protected Long extraerId(MvcResult resultado) throws Exception {
        JsonNode nodo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        if (nodo.has("id")) {
            return nodo.get("id").asLong();
        }
        throw new IllegalStateException("Respuesta sin campo id: " + resultado.getResponse().getContentAsString());
    }
}
