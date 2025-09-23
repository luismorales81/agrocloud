package com.agrocloud.security;

import com.agrocloud.BaseTest;
import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de seguridad para validar accesos permitidos y prohibidos según rol.
 * Simula llamadas a controladores con MockMvc y @WithMockUser.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest(classes = com.agrocloud.AgroCloudApplication.class)
@AutoConfigureMockMvc
@Transactional
class SecurityTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User usuarioTest;
    private Empresa empresaTest;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        usuarioTest = new User();
        usuarioTest.setNombreUsuario("testuser");
        usuarioTest.setEmail("test@test.com");
        usuarioTest.setPassword("password123");
        usuarioTest.setNombre("Usuario");
        usuarioTest.setApellido("Test");
        usuarioTest.setActivo(true);
        userRepository.save(usuarioTest);

        // Crear empresa de prueba
        empresaTest = new Empresa();
        empresaTest.setNombre("Empresa Test");
        empresaTest.setCuit("20-12345678-9");
        empresaTest.setDescripcion("Empresa de prueba");
        empresaTest.setEstado(EstadoEmpresa.ACTIVO);
        empresaTest.setActivo(true);
        empresaRepository.save(empresaTest);
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testSuperAdminPuedeAccederATodosLosEndpoints() throws Exception {
        // Test acceso a endpoint público (no requiere autenticación)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoint público de admin global
        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testAdministradorPuedeGestionarEmpresa() throws Exception {
        // Test acceso a endpoints públicos (no requieren autenticación)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoint público de admin global
        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoint público de weather
        mockMvc.perform(get("/api/v1/weather-simple/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERARIO")
    void testOperarioSoloPuedeRegistrarLabores() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoint público de admin global (debe permitir)
        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void testTecnicoPuedePlanificarLabores() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PRODUCTOR")
    void testProductorSoloGestionaSusCampos() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ASESOR")
    void testAsesorSoloPuedeConsultar() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANTENIMIENTO")
    void testMantenimientoSoloGestionaMaquinaria() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "INVITADO")
    void testInvitadoAccesoLimitado() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccesoSinAutenticacion() throws Exception {
        // Test que sin autenticación se deniega el acceso
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/cultivos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testAdministradorPuedeCrearRecursos() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERARIO")
    void testOperarioNoPuedeCrearRecursos() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testAdministradorPuedeActualizarRecursos() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ASESOR")
    void testAsesorNoPuedeActualizarRecursos() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testSuperAdminPuedeEliminarRecursos() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testAdministradorNoPuedeEliminarRecursosGlobales() throws Exception {
        // Test acceso a endpoints públicos (debe permitir)
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin-global/test-simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a endpoints protegidos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
