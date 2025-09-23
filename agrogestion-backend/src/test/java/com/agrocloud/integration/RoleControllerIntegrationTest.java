package com.agrocloud.integration;

import com.agrocloud.BaseEntityTest;
import com.agrocloud.model.entity.Role;
import com.agrocloud.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Pruebas de integración para el controlador de roles.
 * Valida endpoints REST para gestión de roles y permisos.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
class RoleControllerIntegrationTest extends BaseEntityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RoleRepository roleRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Role roleTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Para soporte de Java 8 time
        
        // Crear rol de prueba
        roleTest = new Role();
        roleTest.setNombre("TEST_ROLE");
        roleTest.setDescripcion("Rol de prueba para tests de integración");
        roleRepository.save(roleTest);
    }

    @Test
    void testObtenerTodosLosRoles_DeberiaRetornar200() throws Exception {
        // Given - Crear roles adicionales
        Role role1 = new Role();
        role1.setNombre("ROLE_1");
        role1.setDescripcion("Rol 1");
        roleRepository.save(role1);

        Role role2 = new Role();
        role2.setNombre("ROLE_2");
        role2.setDescripcion("Rol 2");
        roleRepository.save(role2);

        // When & Then
        mockMvc.perform(get("/api/roles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3))) // Al menos 3 roles: TEST_ROLE, ROLE_1, ROLE_2
                .andExpect(jsonPath("$[0].nombre").exists())
                .andExpect(jsonPath("$[1].nombre").exists())
                .andExpect(jsonPath("$[2].nombre").exists());
    }

    @Test
    void testObtenerRolPorId_Existente_DeberiaRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roles/{id}", roleTest.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(roleTest.getId()))
                .andExpect(jsonPath("$.nombre").value("TEST_ROLE"))
                .andExpect(jsonPath("$.descripcion").value("Rol de prueba para tests de integración"));
    }

    @Test
    void testObtenerRolPorId_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;

        // When & Then
        mockMvc.perform(get("/api/roles/{id}", idNoExistente))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerRolPorNombre_Existente_DeberiaRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roles/name/{name}", "TEST_ROLE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("TEST_ROLE"))
                .andExpect(jsonPath("$.descripcion").value("Rol de prueba para tests de integración"));
    }

    @Test
    void testObtenerRolPorNombre_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        String nombreNoExistente = "ROLE_NO_EXISTENTE";

        // When & Then
        mockMvc.perform(get("/api/roles/name/{name}", nombreNoExistente))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearRol_DeberiaRetornar201() throws Exception {
        // Given
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("name", "NUEVO_ROLE");
        roleData.put("description", "Nuevo rol creado desde test de integración");

        String jsonContent = objectMapper.writeValueAsString(roleData);

        // When & Then
        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("NUEVO_ROLE"))
                .andExpect(jsonPath("$.descripcion").value("Nuevo rol creado desde test de integración"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCrearRol_DatosInvalidos_DeberiaRetornar400() throws Exception {
        // Given - Rol con datos inválidos
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("nombre", ""); // Nombre vacío - inválido
        roleData.put("descripcion", "Descripción válida");

        String jsonContent = objectMapper.writeValueAsString(roleData);

        // When & Then
        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCrearRol_NombreDuplicado_DeberiaRetornar409() throws Exception {
        // Given - Rol con nombre duplicado
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("name", "TEST_ROLE"); // Nombre ya existente
        roleData.put("description", "Descripción duplicada");

        String jsonContent = objectMapper.writeValueAsString(roleData);

        // When & Then
        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void testActualizarRol_Existente_DeberiaRetornar200() throws Exception {
        // Given
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("name", "TEST_ROLE_ACTUALIZADO");
        roleData.put("description", "Descripción actualizada desde test");

        String jsonContent = objectMapper.writeValueAsString(roleData);

        // When & Then
        mockMvc.perform(put("/api/roles/{id}", roleTest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(roleTest.getId()))
                .andExpect(jsonPath("$.nombre").value("TEST_ROLE_ACTUALIZADO"))
                .andExpect(jsonPath("$.descripcion").value("Descripción actualizada desde test"));
    }

    @Test
    void testActualizarRol_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("nombre", "ROLE_ACTUALIZADO");
        roleData.put("descripcion", "Descripción actualizada");

        String jsonContent = objectMapper.writeValueAsString(roleData);

        // When & Then
        mockMvc.perform(put("/api/roles/{id}", idNoExistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarRol_Existente_DeberiaRetornar204() throws Exception {
        // Given - Crear un rol específico para eliminar
        Role roleAEliminar = new Role();
        roleAEliminar.setNombre("ROLE_A_ELIMINAR");
        roleAEliminar.setDescripcion("Rol que será eliminado");
        Role roleGuardado = roleRepository.save(roleAEliminar);

        // When & Then
        mockMvc.perform(delete("/api/roles/{id}", roleGuardado.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verificar que el rol fue eliminado
        mockMvc.perform(get("/api/roles/{id}", roleGuardado.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarRol_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;

        // When & Then
        mockMvc.perform(delete("/api/roles/{id}", idNoExistente))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerPermisosDisponibles_DeberiaRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roles/permissions/available"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testObtenerPermisosPorRol_Existente_DeberiaRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roles/{id}/permissions", roleTest.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testObtenerPermisosPorRol_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;

        // When & Then
        mockMvc.perform(get("/api/roles/{id}/permissions", idNoExistente))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testAsignarPermisosARol_Existente_DeberiaRetornar200() throws Exception {
        // Given
        String[] permisos = {"READ", "WRITE", "DELETE"};
        Map<String, Object> requestBody = Map.of("permissions", permisos);
        String jsonContent = objectMapper.writeValueAsString(requestBody);

        // When & Then
        mockMvc.perform(put("/api/roles/{id}/permissions", roleTest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testAsignarPermisosARol_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;
        String[] permisos = {"READ", "WRITE"};
        Map<String, Object> requestBody = Map.of("permissions", permisos);
        String jsonContent = objectMapper.writeValueAsString(requestBody);

        // When & Then
        mockMvc.perform(put("/api/roles/{id}/permissions", idNoExistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerEstadisticasRoles_DeberiaRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roles/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalRoles").exists())
                .andExpect(jsonPath("$.usersPerRole").exists());
    }
}
