package com.agrocloud.entity;

import com.agrocloud.BaseTest;
import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.repository.FieldRepository;
import com.agrocloud.repository.PlotRepository;
import com.agrocloud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la entidad Plot (Lotes).
 * Prueba la creación, edición y consulta de lotes con validación de superficies y estados.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest
class PlotTest extends BaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    private User usuarioTest;
    private Field campoTest;
    private Plot loteTest;

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
        entityManager.persistAndFlush(usuarioTest);

        // Crear campo de prueba
        campoTest = new Field();
        campoTest.setNombre("Campo Test");
        campoTest.setUbicacion("Zona Test");
        campoTest.setAreaHectareas(new BigDecimal("100.50"));
        campoTest.setTipoSuelo("Franco");
        campoTest.setDescripcion("Campo de prueba");
        campoTest.setUsuario(usuarioTest);
        campoTest.setActivo(true);
        entityManager.persistAndFlush(campoTest);

        // Crear lote de prueba
        loteTest = new Plot();
        loteTest.setNombre("Lote Test");
        loteTest.setDescripcion("Lote de prueba para tests");
        loteTest.setAreaHectareas(new BigDecimal("25.50"));
        loteTest.setEstado(EstadoLote.DISPONIBLE);
        loteTest.setTipoSuelo("Franco");
        loteTest.setCampo(campoTest);
        loteTest.setUsuario(usuarioTest);
        loteTest.setActivo(true);
    }

    @Test
    void testCrearLote() {
        // Act
        Plot loteGuardado = plotRepository.save(loteTest);
        entityManager.flush();

        // Assert
        assertNotNull(loteGuardado.getId());
        assertEquals("Lote Test", loteGuardado.getNombre());
        assertEquals("Lote de prueba para tests", loteGuardado.getDescripcion());
        assertEquals(new BigDecimal("25.50"), loteGuardado.getAreaHectareas());
        assertEquals(EstadoLote.DISPONIBLE, loteGuardado.getEstado());
        assertEquals("Franco", loteGuardado.getTipoSuelo());
        assertTrue(loteGuardado.getActivo());
        assertEquals(campoTest.getId(), loteGuardado.getCampo().getId());
        assertEquals(usuarioTest.getId(), loteGuardado.getUsuario().getId());
    }

    @Test
    void testEditarLote() {
        // Arrange
        Plot loteGuardado = plotRepository.save(loteTest);
        entityManager.flush();

        // Act
        loteGuardado.setNombre("Lote Test Modificado");
        loteGuardado.setAreaHectareas(new BigDecimal("30.75"));
        loteGuardado.setEstado(EstadoLote.PREPARADO);
        loteGuardado.setTipoSuelo("Franco-arcilloso");
        Plot loteModificado = plotRepository.save(loteGuardado);
        entityManager.flush();

        // Assert
        assertEquals("Lote Test Modificado", loteModificado.getNombre());
        assertEquals(new BigDecimal("30.75"), loteModificado.getAreaHectareas());
        assertEquals(EstadoLote.PREPARADO, loteModificado.getEstado());
        assertEquals("Franco-arcilloso", loteModificado.getTipoSuelo());
    }

    @Test
    void testConsultarLotePorId() {
        // Arrange
        Plot loteGuardado = plotRepository.save(loteTest);
        entityManager.flush();

        // Act
        Optional<Plot> loteEncontrado = plotRepository.findById(loteGuardado.getId());

        // Assert
        assertTrue(loteEncontrado.isPresent());
        assertEquals(loteGuardado.getId(), loteEncontrado.get().getId());
        assertEquals("Lote Test", loteEncontrado.get().getNombre());
    }

    @Test
    void testConsultarLotesPorCampo() {
        // Arrange
        plotRepository.save(loteTest);
        
        // Crear segundo lote para el mismo campo
        Plot lote2 = new Plot();
        lote2.setNombre("Lote Test 2");
        lote2.setDescripcion("Segundo lote de prueba");
        lote2.setAreaHectareas(new BigDecimal("20.25"));
        lote2.setEstado(EstadoLote.DISPONIBLE);
        lote2.setTipoSuelo("Franco");
        lote2.setCampo(campoTest);
        lote2.setUsuario(usuarioTest);
        lote2.setActivo(true);
        plotRepository.save(lote2);
        entityManager.flush();

        // Act
        List<Plot> lotesCampo = plotRepository.findByCampoId(campoTest.getId());

        // Assert
        assertEquals(2, lotesCampo.size());
        assertTrue(lotesCampo.stream().anyMatch(l -> l.getNombre().equals("Lote Test")));
        assertTrue(lotesCampo.stream().anyMatch(l -> l.getNombre().equals("Lote Test 2")));
    }

    @Test
    void testConsultarLotesPorEstado() {
        // Arrange
        plotRepository.save(loteTest);
        
        // Crear lote con estado diferente
        Plot loteSembrado = new Plot();
        loteSembrado.setNombre("Lote Sembrado");
        loteSembrado.setDescripcion("Lote con cultivo sembrado");
        loteSembrado.setAreaHectareas(new BigDecimal("15.00"));
        loteSembrado.setEstado(EstadoLote.SEMBRADO);
        loteSembrado.setTipoSuelo("Franco");
        loteSembrado.setCampo(campoTest);
        loteSembrado.setUsuario(usuarioTest);
        loteSembrado.setActivo(true);
        plotRepository.save(loteSembrado);
        entityManager.flush();

        // Act
        List<Plot> lotesDisponibles = plotRepository.findByEstado(EstadoLote.DISPONIBLE);
        List<Plot> lotesSembrados = plotRepository.findByEstado(EstadoLote.SEMBRADO);

        // Assert
        assertEquals(1, lotesDisponibles.size());
        assertEquals("Lote Test", lotesDisponibles.get(0).getNombre());
        
        assertEquals(1, lotesSembrados.size());
        assertEquals("Lote Sembrado", lotesSembrados.get(0).getNombre());
    }

    @Test
    void testCambiarEstadoLote() {
        // Arrange
        Plot loteGuardado = plotRepository.save(loteTest);
        entityManager.flush();

        // Act
        loteGuardado.setEstado(EstadoLote.PREPARADO);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Preparación para siembra");
        Plot loteModificado = plotRepository.save(loteGuardado);
        entityManager.flush();

        // Assert
        assertEquals(EstadoLote.PREPARADO, loteModificado.getEstado());
        assertNotNull(loteModificado.getFechaUltimoCambioEstado());
        assertEquals("Preparación para siembra", loteModificado.getMotivoCambioEstado());
    }

    @Test
    void testValidarSuperficiePositiva() {
        // Arrange
        loteTest.setAreaHectareas(new BigDecimal("-10.00")); // Superficie negativa

        // Act & Assert
        assertThrows(Exception.class, () -> {
            plotRepository.save(loteTest);
            entityManager.flush();
        });
    }

    @Test
    void testValidarNombreObligatorio() {
        // Arrange
        loteTest.setNombre(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            plotRepository.save(loteTest);
            entityManager.flush();
        });
    }

    @Test
    void testBuscarLotesPorNombre() {
        // Arrange
        plotRepository.save(loteTest);
        entityManager.flush();

        // Act
        List<Plot> lotesEncontrados = plotRepository.findByNombreContainingIgnoreCase("Test");

        // Assert
        assertEquals(1, lotesEncontrados.size());
        assertEquals("Lote Test", lotesEncontrados.get(0).getNombre());
    }

    @Test
    void testCalcularAreaTotalPorCampo() {
        // Arrange
        plotRepository.save(loteTest);
        
        Plot lote2 = new Plot();
        lote2.setNombre("Lote Test 2");
        lote2.setDescripcion("Segundo lote");
        lote2.setAreaHectareas(new BigDecimal("20.25"));
        lote2.setEstado(EstadoLote.DISPONIBLE);
        lote2.setTipoSuelo("Franco");
        lote2.setCampo(campoTest);
        lote2.setUsuario(usuarioTest);
        lote2.setActivo(true);
        plotRepository.save(lote2);
        entityManager.flush();

        // Act
        List<Plot> lotesCampo = plotRepository.findByCampoId(campoTest.getId());
        BigDecimal areaTotal = lotesCampo.stream()
                .map(Plot::getAreaHectareas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(new BigDecimal("45.75"), areaTotal);
    }

    @Test
    void testConsultarLotesActivos() {
        // Arrange
        plotRepository.save(loteTest);
        
        // Crear lote inactivo
        Plot loteInactivo = new Plot();
        loteInactivo.setNombre("Lote Inactivo");
        loteInactivo.setDescripcion("Lote inactivo");
        loteInactivo.setAreaHectareas(new BigDecimal("10.00"));
        loteInactivo.setEstado(EstadoLote.DISPONIBLE);
        loteInactivo.setTipoSuelo("Franco");
        loteInactivo.setCampo(campoTest);
        loteInactivo.setUsuario(usuarioTest);
        loteInactivo.setActivo(false);
        plotRepository.save(loteInactivo);
        entityManager.flush();

        // Act
        List<Plot> lotesActivos = plotRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId());

        // Assert
        assertEquals(1, lotesActivos.size());
        assertEquals("Lote Test", lotesActivos.get(0).getNombre());
        assertTrue(lotesActivos.get(0).getActivo());
    }
}
