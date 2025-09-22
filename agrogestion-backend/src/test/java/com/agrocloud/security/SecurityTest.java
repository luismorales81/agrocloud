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
        empresaTest.setDescripcion("Empresa de prueba");
        empresaTest.setEstado(EstadoEmpresa.ACTIVO);
        empresaTest.setActivo(true);
        empresaRepository.save(empresaTest);
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testSuperAdminPuedeAccederATodosLosEndpoints() throws Exception {
        // Test acceso a gestión de empresas
        mockMvc.perform(get("/api/empresas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a gestión de usuarios
        mockMvc.perform(get("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a roles
        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testAdministradorPuedeGestionarEmpresa() throws Exception {
        // Test acceso a campos
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a lotes
        mockMvc.perform(get("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a cultivos
        mockMvc.perform(get("/api/cultivos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a insumos
        mockMvc.perform(get("/api/insumos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a labores
        mockMvc.perform(get("/api/labores")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a maquinaria
        mockMvc.perform(get("/api/maquinaria")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERARIO")
    void testOperarioSoloPuedeRegistrarLabores() throws Exception {
        // Test acceso a labores (debe permitir)
        mockMvc.perform(get("/api/labores")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a campos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Test acceso a lotes (debe denegar)
        mockMvc.perform(get("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Test acceso a cultivos (debe denegar)
        mockMvc.perform(get("/api/cultivos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void testTecnicoPuedePlanificarLabores() throws Exception {
        // Test acceso a labores (debe permitir)
        mockMvc.perform(get("/api/labores")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a insumos (debe permitir para planificación)
        mockMvc.perform(get("/api/insumos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a campos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PRODUCTOR")
    void testProductorSoloGestionaSusCampos() throws Exception {
        // Test acceso a campos (debe permitir solo sus campos)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a lotes (debe permitir solo sus lotes)
        mockMvc.perform(get("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a reportes (debe permitir)
        mockMvc.perform(get("/api/reportes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a usuarios (debe denegar)
        mockMvc.perform(get("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ASESOR")
    void testAsesorSoloPuedeConsultar() throws Exception {
        // Test acceso de solo lectura a campos
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso de solo lectura a lotes
        mockMvc.perform(get("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso de solo lectura a cultivos
        mockMvc.perform(get("/api/cultivos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test que no puede crear campos (POST debe denegar)
        mockMvc.perform(post("/api/campos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Campo Test\",\"areaHectareas\":100.50}"))
                .andExpect(status().isForbidden());

        // Test que no puede crear lotes (POST debe denegar)
        mockMvc.perform(post("/api/lotes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Lote Test\",\"areaHectareas\":25.50}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANTENIMIENTO")
    void testMantenimientoSoloGestionaMaquinaria() throws Exception {
        // Test acceso a maquinaria (debe permitir)
        mockMvc.perform(get("/api/maquinaria")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test acceso a campos (debe denegar)
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Test acceso a lotes (debe denegar)
        mockMvc.perform(get("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Test acceso a cultivos (debe denegar)
        mockMvc.perform(get("/api/cultivos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INVITADO")
    void testInvitadoAccesoLimitado() throws Exception {
        // Test acceso limitado a información básica
        mockMvc.perform(get("/api/campos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Test que no puede crear nada
        mockMvc.perform(post("/api/campos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Campo Test\"}"))
                .andExpect(status().isForbidden());
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
        // Test crear campo
        mockMvc.perform(post("/api/campos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Campo Test\",\"areaHectareas\":100.50,\"tipoSuelo\":\"Franco\"}"))
                .andExpect(status().isCreated());

        // Test crear lote
        mockMvc.perform(post("/api/lotes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Lote Test\",\"areaHectareas\":25.50,\"estado\":\"DISPONIBLE\"}"))
                .andExpect(status().isCreated());

        // Test crear cultivo
        mockMvc.perform(post("/api/cultivos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Soja\",\"tipo\":\"Oleaginosa\",\"variedad\":\"DM 53i53\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OPERARIO")
    void testOperarioNoPuedeCrearRecursos() throws Exception {
        // Test que no puede crear campos
        mockMvc.perform(post("/api/campos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Campo Test\",\"areaHectareas\":100.50}"))
                .andExpect(status().isForbidden());

        // Test que no puede crear lotes
        mockMvc.perform(post("/api/lotes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Lote Test\",\"areaHectareas\":25.50}"))
                .andExpect(status().isForbidden());

        // Test que no puede crear cultivos
        mockMvc.perform(post("/api/cultivos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Soja\",\"tipo\":\"Oleaginosa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testAdministradorPuedeActualizarRecursos() throws Exception {
        // Test actualizar campo
        mockMvc.perform(put("/api/campos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Campo Actualizado\",\"areaHectareas\":120.75}"))
                .andExpect(status().isOk());

        // Test actualizar lote
        mockMvc.perform(put("/api/lotes/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Lote Actualizado\",\"estado\":\"PREPARADO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ASESOR")
    void testAsesorNoPuedeActualizarRecursos() throws Exception {
        // Test que no puede actualizar campos
        mockMvc.perform(put("/api/campos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Campo Actualizado\"}"))
                .andExpect(status().isForbidden());

        // Test que no puede actualizar lotes
        mockMvc.perform(put("/api/lotes/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Lote Actualizado\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testSuperAdminPuedeEliminarRecursos() throws Exception {
        // Test eliminar campo
        mockMvc.perform(delete("/api/campos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Test eliminar lote
        mockMvc.perform(delete("/api/lotes/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testAdministradorNoPuedeEliminarRecursosGlobales() throws Exception {
        // Test que no puede eliminar empresas (solo SUPERADMIN)
        mockMvc.perform(delete("/api/empresas/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Test que no puede eliminar usuarios globales (solo SUPERADMIN)
        mockMvc.perform(delete("/api/usuarios/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
