package com.agrocloud.integration;

import com.agrocloud.BaseEntityTest;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Pruebas de integración para el controlador de autenticación.
 * Valida endpoints de login, registro y gestión de contraseñas.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
class AuthControllerIntegrationTest extends BaseEntityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User usuarioTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Para soporte de Java 8 time
        
        // Crear usuario de prueba
        usuarioTest = new User();
        usuarioTest.setNombreUsuario("testuser_" + System.currentTimeMillis());
        usuarioTest.setEmail("test_" + System.currentTimeMillis() + "@test.com");
        usuarioTest.setPassword(passwordEncoder.encode("password123"));
        usuarioTest.setNombre("Usuario");
        usuarioTest.setApellido("Test");
        usuarioTest.setActivo(true);
        userRepository.save(usuarioTest);
    }

    @Test
    void testLogin_UsuarioValido_DeberiaRetornar200() throws Exception {
        // Given
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", usuarioTest.getEmail());
        loginData.put("password", "password123");

        String jsonContent = objectMapper.writeValueAsString(loginData);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.email").value(usuarioTest.getEmail()))
                .andExpect(jsonPath("$.user.name").value("Usuario Test"));
    }

    @Test
    void testLogin_UsuarioInvalido_DeberiaRetornar401() throws Exception {
        // Given
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "usuario@inexistente.com");
        loginData.put("password", "password123");

        String jsonContent = objectMapper.writeValueAsString(loginData);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_PasswordIncorrecta_DeberiaRetornar401() throws Exception {
        // Given
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", usuarioTest.getEmail());
        loginData.put("password", "passwordIncorrecta");

        String jsonContent = objectMapper.writeValueAsString(loginData);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_DatosFaltantes_DeberiaRetornar400() throws Exception {
        // Given - Datos incompletos
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", usuarioTest.getEmail());
        // Password faltante

        String jsonContent = objectMapper.writeValueAsString(loginData);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_UsuarioInactivo_DeberiaRetornar403() throws Exception {
        // Given - Usuario inactivo
        usuarioTest.setActivo(false);
        userRepository.save(usuarioTest);

        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", usuarioTest.getEmail());
        loginData.put("password", "password123");

        String jsonContent = objectMapper.writeValueAsString(loginData);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testRequestPasswordReset_EmailValido_DeberiaRetornar200() throws Exception {
        // Given
        Map<String, String> resetData = new HashMap<>();
        resetData.put("email", usuarioTest.getEmail());

        String jsonContent = objectMapper.writeValueAsString(resetData);

        // When & Then
        mockMvc.perform(post("/api/auth/request-password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRequestPasswordReset_EmailInvalido_DeberiaRetornar404() throws Exception {
        // Given
        Map<String, String> resetData = new HashMap<>();
        resetData.put("email", "usuario@inexistente.com");

        String jsonContent = objectMapper.writeValueAsString(resetData);

        // When & Then
        mockMvc.perform(post("/api/auth/request-password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testChangePassword_UsuarioAutenticado_DeberiaRetornar200() throws Exception {
        // Given - Primero hacer login para obtener token
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", usuarioTest.getEmail());
        loginData.put("password", "password123");

        String loginJson = objectMapper.writeValueAsString(loginData);

        // Obtener token
        String token = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer token del JSON (simplificado para el test)
        String authToken = "Bearer " + extractTokenFromResponse(token);

        // Datos para cambio de contraseña
        Map<String, String> changePasswordData = new HashMap<>();
        changePasswordData.put("currentPassword", "password123");
        changePasswordData.put("newPassword", "newPassword123");
        changePasswordData.put("confirmPassword", "newPassword123");

        String changePasswordJson = objectMapper.writeValueAsString(changePasswordData);

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testChangePassword_PasswordActualIncorrecta_DeberiaRetornar400() throws Exception {
        // Given - Primero hacer login para obtener token
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", usuarioTest.getEmail());
        loginData.put("password", "password123");

        String loginJson = objectMapper.writeValueAsString(loginData);

        // Obtener token
        String token = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String authToken = "Bearer " + extractTokenFromResponse(token);

        // Datos para cambio de contraseña con password actual incorrecta
        Map<String, String> changePasswordData = new HashMap<>();
        changePasswordData.put("currentPassword", "passwordIncorrecta");
        changePasswordData.put("newPassword", "newPassword123");
        changePasswordData.put("confirmPassword", "newPassword123");

        String changePasswordJson = objectMapper.writeValueAsString(changePasswordData);

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePassword_PasswordsNoCoinciden_DeberiaRetornar400() throws Exception {
        // Given - Primero hacer login para obtener token
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", usuarioTest.getEmail());
        loginData.put("password", "password123");

        String loginJson = objectMapper.writeValueAsString(loginData);

        // Obtener token
        String token = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String authToken = "Bearer " + extractTokenFromResponse(token);

        // Datos para cambio de contraseña con passwords que no coinciden
        Map<String, String> changePasswordData = new HashMap<>();
        changePasswordData.put("currentPassword", "password123");
        changePasswordData.put("newPassword", "newPassword123");
        changePasswordData.put("confirmPassword", "differentPassword123");

        String changePasswordJson = objectMapper.writeValueAsString(changePasswordData);

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePassword_SinToken_DeberiaRetornar401() throws Exception {
        // Given
        Map<String, String> changePasswordData = new HashMap<>();
        changePasswordData.put("currentPassword", "password123");
        changePasswordData.put("newPassword", "newPassword123");
        changePasswordData.put("confirmPassword", "newPassword123");

        String changePasswordJson = objectMapper.writeValueAsString(changePasswordData);

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testTestEndpoint_DeberiaRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/test"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("AuthController funcionando correctamente"));
    }

    @Test
    void testGenerateHash_DeberiaRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/generate-hash")
                .param("password", "testPassword123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hash generado:")));
    }

    // Método auxiliar para extraer token del JSON de respuesta
    private String extractTokenFromResponse(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            return (String) responseMap.get("token");
        } catch (Exception e) {
            return "mock-token-for-test";
        }
    }
}
