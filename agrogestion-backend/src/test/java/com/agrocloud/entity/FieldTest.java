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
 * Prueba la creación, edición y consulta de campos con validación de coordenadas y superficies.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
class FieldTest extends BaseEntityTest {

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
        empresaTest.setEmailContacto("test@empresa.com");
        empresaTest.setEstado(EstadoEmpresa.ACTIVO);
        empresaTest.setActivo(true);
        empresaTest.setCreadoPor(usuarioTest);
        empresaRepository.save(empresaTest);

        // Crear campo de prueba
        campoTest = new Field();
        campoTest.setNombre("Campo Test");
        campoTest.setUbicacion("Zona Test");
        campoTest.setAreaHectareas(new BigDecimal("100.50"));
        campoTest.setTipoSuelo("Franco");
        campoTest.setDescripcion("Campo de prueba para tests");
        campoTest.setUsuario(usuarioTest);
        campoTest.setEmpresa(empresaTest);
        campoTest.setActivo(true);
    }

    @Test
    void testCrearCampo() {
        // Act
        Field campoGuardado = fieldRepository.save(campoTest);

        // Assert
        assertNotNull(campoGuardado.getId());
        assertEquals("Campo Test", campoGuardado.getNombre());
        assertEquals("Zona Test", campoGuardado.getUbicacion());
        assertEquals(new BigDecimal("100.50"), campoGuardado.getAreaHectareas());
        assertEquals("Franco", campoGuardado.getTipoSuelo());
        assertEquals("Campo de prueba para tests", campoGuardado.getDescripcion());
        assertTrue(campoGuardado.getActivo());
        assertEquals(usuarioTest.getId(), campoGuardado.getUsuario().getId());
    }

    @Test
    void testEditarCampo() {
        // Arrange
        Field campoGuardado = fieldRepository.save(campoTest);

        // Act
        campoGuardado.setNombre("Campo Test Modificado");
        campoGuardado.setAreaHectareas(new BigDecimal("120.75"));
        campoGuardado.setTipoSuelo("Franco-arcilloso");
        Field campoModificado = fieldRepository.save(campoGuardado);

        // Assert
        assertEquals("Campo Test Modificado", campoModificado.getNombre());
        assertEquals(new BigDecimal("120.75"), campoModificado.getAreaHectareas());
        assertEquals("Franco-arcilloso", campoModificado.getTipoSuelo());
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
        
        // Crear segundo campo para el mismo usuario
        Field campo2 = new Field();
        campo2.setNombre("Campo Test 2");
        campo2.setUbicacion("Zona Test 2");
        campo2.setAreaHectareas(new BigDecimal("75.25"));
        campo2.setTipoSuelo("Arcilloso");
        campo2.setDescripcion("Segundo campo de prueba");
        campo2.setUsuario(usuarioTest);
        campo2.setEmpresa(empresaTest);
        campo2.setActivo(true);
        fieldRepository.save(campo2);

        // Act
        List<Field> camposUsuario = fieldRepository.findByUserId(usuarioTest.getId());

        // Assert
        assertEquals(2, camposUsuario.size());
        assertTrue(camposUsuario.stream().anyMatch(c -> c.getNombre().equals("Campo Test")));
        assertTrue(camposUsuario.stream().anyMatch(c -> c.getNombre().equals("Campo Test 2")));
    }

    @Test
    void testConsultarCamposActivos() {
        // Arrange
        fieldRepository.save(campoTest);
        
        // Crear campo inactivo
        Field campoInactivo = new Field();
        campoInactivo.setNombre("Campo Inactivo");
        campoInactivo.setUbicacion("Zona Test");
        campoInactivo.setAreaHectareas(new BigDecimal("50.00"));
        campoInactivo.setTipoSuelo("Franco");
        campoInactivo.setDescripcion("Campo inactivo");
        campoInactivo.setUsuario(usuarioTest);
        campoInactivo.setEmpresa(empresaTest);
        campoInactivo.setActivo(false);
        fieldRepository.save(campoInactivo);

        // Act
        List<Field> camposActivos = fieldRepository.findByUserIdAndActivoTrue(usuarioTest.getId());

        // Assert
        assertEquals(1, camposActivos.size());
        assertEquals("Campo Test", camposActivos.get(0).getNombre());
        assertTrue(camposActivos.get(0).getActivo());
    }

    @Test
    void testValidarSuperficiePositiva() {
        // Arrange
        campoTest.setAreaHectareas(new BigDecimal("0")); // Superficie cero

        // Act & Assert
        assertThrows(Exception.class, () -> {
            fieldRepository.save(campoTest);
        });
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
    void testBuscarCamposPorNombre() {
        // Arrange
        fieldRepository.save(campoTest);

        // Act
        List<Field> camposEncontrados = fieldRepository.findByNombreContainingIgnoreCase("Test");

        // Assert
        assertEquals(1, camposEncontrados.size());
        assertEquals("Campo Test", camposEncontrados.get(0).getNombre());
    }

    @Test
    void testBuscarCamposPorTipoSuelo() {
        // Arrange
        fieldRepository.save(campoTest);

        // Act
        List<Field> camposFranco = fieldRepository.findByTipoSuelo("Franco");

        // Assert
        assertEquals(1, camposFranco.size());
        assertEquals("Franco", camposFranco.get(0).getTipoSuelo());
    }

    @Test
    void testCalcularAreaTotalPorUsuario() {
        // Arrange
        fieldRepository.save(campoTest);
        
        Field campo2 = new Field();
        campo2.setNombre("Campo Test 2");
        campo2.setUbicacion("Zona Test 2");
        campo2.setAreaHectareas(new BigDecimal("75.25"));
        campo2.setTipoSuelo("Franco");
        campo2.setDescripcion("Segundo campo");
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
        assertEquals(new BigDecimal("175.75"), areaTotal);
    }
}
