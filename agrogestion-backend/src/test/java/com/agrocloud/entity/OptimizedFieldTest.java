package com.agrocloud.entity;

import com.agrocloud.BaseEntityTest;
import com.agrocloud.model.entity.*;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests optimizados para la entidad Field (Campos).
 * Versión optimizada con mejor rendimiento y configuración simplificada.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
class OptimizedFieldTest extends BaseEntityTest {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    private User usuarioTest;
    private Empresa empresaTest;
    private Field campoTest;

    @BeforeEach
    void setUp() {
        // Crear datos de prueba de forma optimizada
        usuarioTest = crearUsuarioTest();
        empresaTest = crearEmpresaTest();
        campoTest = crearCampoTest();
    }

    private User crearUsuarioTest() {
        User usuario = new User();
        usuario.setNombreUsuario("testuser_" + System.currentTimeMillis());
        usuario.setEmail("test_" + System.currentTimeMillis() + "@test.com");
        usuario.setPassword("password123");
        usuario.setNombre("Usuario");
        usuario.setApellido("Test");
        usuario.setActivo(true);
        return userRepository.save(usuario);
    }

    private Empresa crearEmpresaTest() {
        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa Test");
        empresa.setCuit("20-" + (System.currentTimeMillis() % 100000000) + "-9");
        empresa.setEmailContacto("test_" + System.currentTimeMillis() + "@empresa.com");
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        empresa.setActivo(true);
        empresa.setCreadoPor(usuarioTest);
        return empresaRepository.save(empresa);
    }

    private Field crearCampoTest() {
        Field campo = new Field();
        campo.setNombre("Campo Test");
        campo.setUbicacion("Zona Test");
        campo.setAreaHectareas(new BigDecimal("100.50"));
        campo.setTipoSuelo("Franco");
        campo.setDescripcion("Campo de prueba para tests");
        campo.setUsuario(usuarioTest);
        campo.setEmpresa(empresaTest);
        campo.setActivo(true);
        return campo;
    }

    @Test
    void testCrearCampo() {
        // Act
        Field campoGuardado = fieldRepository.save(campoTest);

        // Assert
        assertNotNull(campoGuardado.getId());
        assertEquals("Campo Test", campoGuardado.getNombre());
        assertEquals(new BigDecimal("100.50"), campoGuardado.getAreaHectareas());
        assertEquals("Franco", campoGuardado.getTipoSuelo());
        assertEquals(usuarioTest.getId(), campoGuardado.getUsuario().getId());
        assertEquals(empresaTest.getId(), campoGuardado.getEmpresa().getId());
        assertTrue(campoGuardado.getActivo());
    }

    @Test
    void testConsultarCampoPorId() {
        // Arrange
        Field campoGuardado = fieldRepository.save(campoTest);

        // Act
        Optional<Field> campoEncontrado = fieldRepository.findById(campoGuardado.getId());

        // Assert
        assertTrue(campoEncontrado.isPresent());
        assertEquals(campoGuardado.getId(), campoEncontrado.get().getId());
        assertEquals("Campo Test", campoEncontrado.get().getNombre());
    }

    @Test
    void testConsultarCamposPorUsuario() {
        // Arrange
        fieldRepository.save(campoTest);

        // Act
        List<Field> camposUsuario = fieldRepository.findByUserId(usuarioTest.getId());

        // Assert
        assertFalse(camposUsuario.isEmpty());
        assertEquals(1, camposUsuario.size());
        assertEquals("Campo Test", camposUsuario.get(0).getNombre());
    }

    @Test
    void testConsultarCamposActivos() {
        // Arrange
        fieldRepository.save(campoTest);

        // Act
        List<Field> camposActivos = fieldRepository.findByUserIdAndActivoTrue(usuarioTest.getId());

        // Assert
        assertFalse(camposActivos.isEmpty());
        assertEquals(1, camposActivos.size());
        assertTrue(camposActivos.get(0).getActivo());
    }

    @Test
    void testEditarCampo() {
        // Arrange
        Field campoGuardado = fieldRepository.save(campoTest);
        campoGuardado.setNombre("Campo Editado");
        campoGuardado.setAreaHectareas(new BigDecimal("150.75"));

        // Act
        Field campoEditado = fieldRepository.save(campoGuardado);

        // Assert
        assertEquals("Campo Editado", campoEditado.getNombre());
        assertEquals(new BigDecimal("150.75"), campoEditado.getAreaHectareas());
    }

    @Test
    void testBuscarCamposPorNombre() {
        // Arrange
        fieldRepository.save(campoTest);

        // Act
        List<Field> camposEncontrados = fieldRepository.findByNombreContainingIgnoreCase("Test");

        // Assert
        assertFalse(camposEncontrados.isEmpty());
        assertTrue(camposEncontrados.stream().anyMatch(c -> c.getNombre().contains("Test")));
    }

    @Test
    void testBuscarCamposPorTipoSuelo() {
        // Arrange
        fieldRepository.save(campoTest);

        // Act
        List<Field> camposEncontrados = fieldRepository.findByTipoSuelo("Franco");

        // Assert
        assertFalse(camposEncontrados.isEmpty());
        assertTrue(camposEncontrados.stream().anyMatch(c -> "Franco".equals(c.getTipoSuelo())));
    }

    @Test
    void testCalcularAreaTotalPorUsuario() {
        // Arrange
        fieldRepository.save(campoTest);
        
        // Crear un segundo campo para el mismo usuario
        Field campo2 = new Field();
        campo2.setNombre("Campo Test 2");
        campo2.setUbicacion("Zona Test 2");
        campo2.setAreaHectareas(new BigDecimal("50.25"));
        campo2.setTipoSuelo("Arcilloso");
        campo2.setDescripcion("Segundo campo de prueba");
        campo2.setUsuario(usuarioTest);
        campo2.setEmpresa(empresaTest);
        campo2.setActivo(true);
        fieldRepository.save(campo2);

        // Act
        List<Field> camposUsuario = fieldRepository.findByUserId(usuarioTest.getId());
        BigDecimal areaTotal = camposUsuario.stream()
                .map(Field::getAreaHectareas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(2, camposUsuario.size());
        assertEquals(new BigDecimal("150.75"), areaTotal);
    }

    @Test
    void testValidarNombreObligatorio() {
        // Arrange
        campoTest.setNombre(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            fieldRepository.save(campoTest);
        });
    }

    @Test
    void testValidarSuperficiePositiva() {
        // Arrange
        campoTest.setAreaHectareas(new BigDecimal("-10.0"));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            fieldRepository.save(campoTest);
        });
    }
}
