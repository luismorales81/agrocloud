package com.agrocloud.integration;

import com.agrocloud.BaseEntityTest;
import com.agrocloud.model.entity.*;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.agrocloud.config.TestSecurityConfig;
import com.agrocloud.config.MockUserService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Pruebas de integración para el controlador de insumos.
 * Valida endpoints REST completos con MockMvc.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Import({TestSecurityConfig.class, MockUserService.class})
class InsumoControllerIntegrationTest extends BaseEntityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User usuarioTest;
    private Empresa empresaTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Para soporte de Java 8 time
        
        // Crear usuario de prueba
        usuarioTest = new User();
        usuarioTest.setNombreUsuario("testuser_" + System.currentTimeMillis());
        usuarioTest.setEmail("test_" + System.currentTimeMillis() + "@test.com");
        usuarioTest.setPassword("password123");
        usuarioTest.setNombre("Usuario");
        usuarioTest.setApellido("Test");
        usuarioTest.setActivo(true);
        userRepository.save(usuarioTest);

        // Crear empresa de prueba
        empresaTest = new Empresa();
        empresaTest.setNombre("Empresa Test");
        empresaTest.setCuit("20-" + (System.currentTimeMillis() % 100000000) + "-9");
        empresaTest.setEmailContacto("test_empresa@test.com");
        empresaTest.setEstado(EstadoEmpresa.ACTIVO);
        empresaTest.setActivo(true);
        empresaTest.setCreadoPor(usuarioTest);
        empresaRepository.save(empresaTest);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void testCrearInsumo_DeberiaRetornar201() throws Exception {
        // Given
        Insumo insumoData = new Insumo();
        insumoData.setNombre("Fertilizante Test");
        insumoData.setDescripcion("Fertilizante de prueba para tests de integración");
        insumoData.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        insumoData.setUnidadMedida("kg");
        insumoData.setPrecioUnitario(new BigDecimal("150.00"));
        insumoData.setStockMinimo(new BigDecimal("10.0"));
        insumoData.setStockActual(new BigDecimal("50.0"));
        insumoData.setProveedor("Proveedor Test");
        insumoData.setFechaVencimiento(LocalDate.now().plusMonths(6));
        insumoData.setActivo(true);
        insumoData.setUsuario(usuarioTest);
        insumoData.setEmpresa(empresaTest);

        String jsonContent = objectMapper.writeValueAsString(insumoData);

        // When & Then
        mockMvc.perform(post("/api/insumos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Fertilizante Test"))
                .andExpect(jsonPath("$.descripcion").value("Fertilizante de prueba para tests de integración"))
                .andExpect(jsonPath("$.tipo").value("FERTILIZANTE"))
                .andExpect(jsonPath("$.unidadMedida").value("kg"))
                .andExpect(jsonPath("$.precioUnitario").value(150.00))
                .andExpect(jsonPath("$.stockMinimo").value(10.0))
                .andExpect(jsonPath("$.stockActual").value(50.0))
                .andExpect(jsonPath("$.proveedor").value("Proveedor Test"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCrearInsumo_DatosInvalidos_DeberiaRetornar400() throws Exception {
        // Given - Insumo con datos inválidos
        Insumo insumoData = new Insumo();
        insumoData.setNombre(""); // Nombre vacío - inválido
        insumoData.setTipo(null); // Tipo nulo - inválido
        insumoData.setUnidadMedida(""); // Unidad vacía - inválida
        insumoData.setPrecioUnitario(new BigDecimal("-10")); // Precio negativo - inválido

        String jsonContent = objectMapper.writeValueAsString(insumoData);

        // When & Then
        mockMvc.perform(post("/api/insumos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testObtenerInsumos_DeberiaRetornar200() throws Exception {
        // Given - Crear insumos de prueba
        Insumo insumo1 = new Insumo();
        insumo1.setNombre("Fertilizante 1");
        insumo1.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        insumo1.setUnidadMedida("kg");
        insumo1.setPrecioUnitario(new BigDecimal("100.00"));
        insumo1.setStockMinimo(new BigDecimal("5.0"));
        insumo1.setStockActual(new BigDecimal("20.0"));
        insumo1.setUsuario(usuarioTest);
        insumo1.setEmpresa(empresaTest);
        insumoRepository.save(insumo1);

        Insumo insumo2 = new Insumo();
        insumo2.setNombre("Herbicida 1");
        insumo2.setTipo(Insumo.TipoInsumo.HERBICIDA);
        insumo2.setUnidadMedida("L");
        insumo2.setPrecioUnitario(new BigDecimal("200.00"));
        insumo2.setStockMinimo(new BigDecimal("2.0"));
        insumo2.setStockActual(new BigDecimal("10.0"));
        insumo2.setUsuario(usuarioTest);
        insumo2.setEmpresa(empresaTest);
        insumoRepository.save(insumo2);

        // When & Then
        mockMvc.perform(get("/api/insumos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").exists())
                .andExpect(jsonPath("$[1].nombre").exists());
    }

    @Test
    void testObtenerInsumoPorId_Existente_DeberiaRetornar200() throws Exception {
        // Given
        Insumo insumo = new Insumo();
        insumo.setNombre("Insumo Test");
        insumo.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        insumo.setUnidadMedida("kg");
        insumo.setPrecioUnitario(new BigDecimal("150.00"));
        insumo.setStockMinimo(new BigDecimal("10.0"));
        insumo.setStockActual(new BigDecimal("50.0"));
        insumo.setUsuario(usuarioTest);
        insumo.setEmpresa(empresaTest);
        Insumo insumoGuardado = insumoRepository.save(insumo);

        // When & Then
        mockMvc.perform(get("/api/insumos/{id}", insumoGuardado.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(insumoGuardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Insumo Test"))
                .andExpect(jsonPath("$.tipo").value("FERTILIZANTE"))
                .andExpect(jsonPath("$.precioUnitario").value(150.00));
    }

    @Test
    void testObtenerInsumoPorId_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;

        // When & Then
        mockMvc.perform(get("/api/insumos/{id}", idNoExistente))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarInsumo_Existente_DeberiaRetornar200() throws Exception {
        // Given
        Insumo insumo = new Insumo();
        insumo.setNombre("Insumo Original");
        insumo.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        insumo.setUnidadMedida("kg");
        insumo.setPrecioUnitario(new BigDecimal("100.00"));
        insumo.setStockMinimo(new BigDecimal("5.0"));
        insumo.setStockActual(new BigDecimal("20.0"));
        insumo.setUsuario(usuarioTest);
        insumo.setEmpresa(empresaTest);
        Insumo insumoGuardado = insumoRepository.save(insumo);

        // Datos actualizados
        insumo.setNombre("Insumo Actualizado");
        insumo.setDescripcion("Descripción actualizada");
        insumo.setPrecioUnitario(new BigDecimal("200.00"));
        insumo.setStockActual(new BigDecimal("30.0"));

        String jsonContent = objectMapper.writeValueAsString(insumo);

        // When & Then
        mockMvc.perform(put("/api/insumos/{id}", insumoGuardado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(insumoGuardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Insumo Actualizado"))
                .andExpect(jsonPath("$.descripcion").value("Descripción actualizada"))
                .andExpect(jsonPath("$.precioUnitario").value(200.00))
                .andExpect(jsonPath("$.stockActual").value(30.0));
    }

    @Test
    void testActualizarInsumo_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;
        Insumo insumoData = new Insumo();
        insumoData.setNombre("Insumo Test");
        insumoData.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        insumoData.setUnidadMedida("kg");
        insumoData.setPrecioUnitario(new BigDecimal("100.00"));
        insumoData.setStockMinimo(new BigDecimal("5.0"));
        insumoData.setStockActual(new BigDecimal("20.0"));

        String jsonContent = objectMapper.writeValueAsString(insumoData);

        // When & Then
        mockMvc.perform(put("/api/insumos/{id}", idNoExistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarInsumo_Existente_DeberiaRetornar204() throws Exception {
        // Given
        Insumo insumo = new Insumo();
        insumo.setNombre("Insumo a Eliminar");
        insumo.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        insumo.setUnidadMedida("kg");
        insumo.setPrecioUnitario(new BigDecimal("100.00"));
        insumo.setStockMinimo(new BigDecimal("5.0"));
        insumo.setStockActual(new BigDecimal("20.0"));
        insumo.setUsuario(usuarioTest);
        insumo.setEmpresa(empresaTest);
        Insumo insumoGuardado = insumoRepository.save(insumo);

        // When & Then
        mockMvc.perform(delete("/api/insumos/{id}", insumoGuardado.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verificar que el insumo fue eliminado
        mockMvc.perform(get("/api/insumos/{id}", insumoGuardado.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarInsumo_NoExistente_DeberiaRetornar404() throws Exception {
        // Given
        Long idNoExistente = 99999L;

        // When & Then
        mockMvc.perform(delete("/api/insumos/{id}", idNoExistente))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerInsumosPorTipo_DeberiaRetornar200() throws Exception {
        // Given - Crear insumos de diferentes tipos
        Insumo fertilizante = new Insumo();
        fertilizante.setNombre("Fertilizante Test");
        fertilizante.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        fertilizante.setUnidadMedida("kg");
        fertilizante.setPrecioUnitario(new BigDecimal("100.00"));
        fertilizante.setStockMinimo(new BigDecimal("5.0"));
        fertilizante.setStockActual(new BigDecimal("20.0"));
        fertilizante.setUsuario(usuarioTest);
        fertilizante.setEmpresa(empresaTest);
        insumoRepository.save(fertilizante);

        Insumo herbicida = new Insumo();
        herbicida.setNombre("Herbicida Test");
        herbicida.setTipo(Insumo.TipoInsumo.HERBICIDA);
        herbicida.setUnidadMedida("L");
        herbicida.setPrecioUnitario(new BigDecimal("200.00"));
        herbicida.setStockMinimo(new BigDecimal("2.0"));
        herbicida.setStockActual(new BigDecimal("10.0"));
        herbicida.setUsuario(usuarioTest);
        herbicida.setEmpresa(empresaTest);
        insumoRepository.save(herbicida);

        // When & Then - Filtrar por tipo fertilizante
        mockMvc.perform(get("/api/insumos")
                .param("tipo", "FERTILIZANTE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tipo").value("FERTILIZANTE"));
    }

    @Test
    void testObtenerInsumosStockBajo_DeberiaRetornar200() throws Exception {
        // Given - Crear insumo con stock bajo
        Insumo insumoStockBajo = new Insumo();
        insumoStockBajo.setNombre("Insumo Stock Bajo");
        insumoStockBajo.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        insumoStockBajo.setUnidadMedida("kg");
        insumoStockBajo.setPrecioUnitario(new BigDecimal("100.00"));
        insumoStockBajo.setStockMinimo(new BigDecimal("10.0"));
        insumoStockBajo.setStockActual(new BigDecimal("5.0")); // Stock por debajo del mínimo
        insumoStockBajo.setUsuario(usuarioTest);
        insumoStockBajo.setEmpresa(empresaTest);
        insumoRepository.save(insumoStockBajo);

        // When & Then
        mockMvc.perform(get("/api/insumos/stock-bajo"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Insumo Stock Bajo"))
                .andExpect(jsonPath("$[0].stockActual").value(5.0))
                .andExpect(jsonPath("$[0].stockMinimo").value(10.0));
    }
}
