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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para las transiciones de estados del ciclo de vida de los lotes.
 * Prueba los métodos auxiliares como puedeSembrar(), puedeCosechar(), esCultivoActivo().
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest
class EstadoLoteTest extends BaseTest {

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
    }

    @Test
    void testTransicionCompletaCicloVida() {
        // Arrange
        Plot lote = crearLote("Lote Ciclo Completo", EstadoLote.DISPONIBLE);
        Plot loteGuardado = plotRepository.save(lote);
        entityManager.flush();

        // Act & Assert - Transición completa del ciclo de vida
        // 1. DISPONIBLE -> PREPARADO
        loteGuardado.setEstado(EstadoLote.PREPARADO);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Preparación del suelo");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.PREPARADO, loteGuardado.getEstado());

        // 2. PREPARADO -> SEMBRADO
        loteGuardado.setEstado(EstadoLote.SEMBRADO);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Siembra realizada");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.SEMBRADO, loteGuardado.getEstado());

        // 3. SEMBRADO -> EN_CRECIMIENTO
        loteGuardado.setEstado(EstadoLote.EN_CRECIMIENTO);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Cultivo en crecimiento vegetativo");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.EN_CRECIMIENTO, loteGuardado.getEstado());

        // 4. EN_CRECIMIENTO -> EN_FLORACION
        loteGuardado.setEstado(EstadoLote.EN_FLORACION);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Cultivo en floración");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.EN_FLORACION, loteGuardado.getEstado());

        // 5. EN_FLORACION -> EN_FRUTIFICACION
        loteGuardado.setEstado(EstadoLote.EN_FRUTIFICACION);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Cultivo en fructificación");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.EN_FRUTIFICACION, loteGuardado.getEstado());

        // 6. EN_FRUTIFICACION -> LISTO_PARA_COSECHA
        loteGuardado.setEstado(EstadoLote.LISTO_PARA_COSECHA);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Cultivo listo para cosecha");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.LISTO_PARA_COSECHA, loteGuardado.getEstado());

        // 7. LISTO_PARA_COSECHA -> EN_COSECHA
        loteGuardado.setEstado(EstadoLote.EN_COSECHA);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Cosecha en progreso");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.EN_COSECHA, loteGuardado.getEstado());

        // 8. EN_COSECHA -> COSECHADO
        loteGuardado.setEstado(EstadoLote.COSECHADO);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Cosecha completada");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.COSECHADO, loteGuardado.getEstado());

        // 9. COSECHADO -> EN_DESCANSO
        loteGuardado.setEstado(EstadoLote.EN_DESCANSO);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Lote en descanso");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.EN_DESCANSO, loteGuardado.getEstado());

        // 10. EN_DESCANSO -> EN_PREPARACION
        loteGuardado.setEstado(EstadoLote.EN_PREPARACION);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Preparando para nuevo ciclo");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.EN_PREPARACION, loteGuardado.getEstado());

        // 11. EN_PREPARACION -> DISPONIBLE (nuevo ciclo)
        loteGuardado.setEstado(EstadoLote.DISPONIBLE);
        loteGuardado.setFechaUltimoCambioEstado(LocalDateTime.now());
        loteGuardado.setMotivoCambioEstado("Listo para nuevo ciclo");
        plotRepository.save(loteGuardado);
        entityManager.flush();
        assertEquals(EstadoLote.DISPONIBLE, loteGuardado.getEstado());
    }

    @Test
    void testMetodoPuedeSembrar() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.DISPONIBLE.puedeSembrar(), "DISPONIBLE debe permitir siembra");
        assertTrue(EstadoLote.PREPARADO.puedeSembrar(), "PREPARADO debe permitir siembra");
        assertTrue(EstadoLote.EN_PREPARACION.puedeSembrar(), "EN_PREPARACION debe permitir siembra");
        
        assertFalse(EstadoLote.SEMBRADO.puedeSembrar(), "SEMBRADO no debe permitir siembra");
        assertFalse(EstadoLote.EN_CRECIMIENTO.puedeSembrar(), "EN_CRECIMIENTO no debe permitir siembra");
        assertFalse(EstadoLote.EN_FLORACION.puedeSembrar(), "EN_FLORACION no debe permitir siembra");
        assertFalse(EstadoLote.EN_FRUTIFICACION.puedeSembrar(), "EN_FRUTIFICACION no debe permitir siembra");
        assertFalse(EstadoLote.LISTO_PARA_COSECHA.puedeSembrar(), "LISTO_PARA_COSECHA no debe permitir siembra");
        assertFalse(EstadoLote.EN_COSECHA.puedeSembrar(), "EN_COSECHA no debe permitir siembra");
        assertFalse(EstadoLote.COSECHADO.puedeSembrar(), "COSECHADO no debe permitir siembra");
        assertFalse(EstadoLote.EN_DESCANSO.puedeSembrar(), "EN_DESCANSO no debe permitir siembra");
        assertFalse(EstadoLote.ENFERMO.puedeSembrar(), "ENFERMO no debe permitir siembra");
        assertFalse(EstadoLote.ABANDONADO.puedeSembrar(), "ABANDONADO no debe permitir siembra");
    }

    @Test
    void testMetodoPuedeCosechar() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.LISTO_PARA_COSECHA.puedeCosechar(), "LISTO_PARA_COSECHA debe permitir cosecha");
        
        assertFalse(EstadoLote.DISPONIBLE.puedeCosechar(), "DISPONIBLE no debe permitir cosecha");
        assertFalse(EstadoLote.PREPARADO.puedeCosechar(), "PREPARADO no debe permitir cosecha");
        assertFalse(EstadoLote.SEMBRADO.puedeCosechar(), "SEMBRADO no debe permitir cosecha");
        assertFalse(EstadoLote.EN_CRECIMIENTO.puedeCosechar(), "EN_CRECIMIENTO no debe permitir cosecha");
        assertFalse(EstadoLote.EN_FLORACION.puedeCosechar(), "EN_FLORACION no debe permitir cosecha");
        assertFalse(EstadoLote.EN_FRUTIFICACION.puedeCosechar(), "EN_FRUTIFICACION no debe permitir cosecha");
        assertFalse(EstadoLote.EN_COSECHA.puedeCosechar(), "EN_COSECHA no debe permitir cosecha");
        assertFalse(EstadoLote.COSECHADO.puedeCosechar(), "COSECHADO no debe permitir cosecha");
        assertFalse(EstadoLote.EN_DESCANSO.puedeCosechar(), "EN_DESCANSO no debe permitir cosecha");
        assertFalse(EstadoLote.EN_PREPARACION.puedeCosechar(), "EN_PREPARACION no debe permitir cosecha");
        assertFalse(EstadoLote.ENFERMO.puedeCosechar(), "ENFERMO no debe permitir cosecha");
        assertFalse(EstadoLote.ABANDONADO.puedeCosechar(), "ABANDONADO no debe permitir cosecha");
    }

    @Test
    void testMetodoEsCultivoActivo() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.SEMBRADO.esCultivoActivo(), "SEMBRADO debe ser cultivo activo");
        assertTrue(EstadoLote.EN_CRECIMIENTO.esCultivoActivo(), "EN_CRECIMIENTO debe ser cultivo activo");
        assertTrue(EstadoLote.EN_FLORACION.esCultivoActivo(), "EN_FLORACION debe ser cultivo activo");
        assertTrue(EstadoLote.EN_FRUTIFICACION.esCultivoActivo(), "EN_FRUTIFICACION debe ser cultivo activo");
        
        assertFalse(EstadoLote.DISPONIBLE.esCultivoActivo(), "DISPONIBLE no debe ser cultivo activo");
        assertFalse(EstadoLote.PREPARADO.esCultivoActivo(), "PREPARADO no debe ser cultivo activo");
        assertFalse(EstadoLote.LISTO_PARA_COSECHA.esCultivoActivo(), "LISTO_PARA_COSECHA no debe ser cultivo activo");
        assertFalse(EstadoLote.EN_COSECHA.esCultivoActivo(), "EN_COSECHA no debe ser cultivo activo");
        assertFalse(EstadoLote.COSECHADO.esCultivoActivo(), "COSECHADO no debe ser cultivo activo");
        assertFalse(EstadoLote.EN_DESCANSO.esCultivoActivo(), "EN_DESCANSO no debe ser cultivo activo");
        assertFalse(EstadoLote.EN_PREPARACION.esCultivoActivo(), "EN_PREPARACION no debe ser cultivo activo");
        assertFalse(EstadoLote.ENFERMO.esCultivoActivo(), "ENFERMO no debe ser cultivo activo");
        assertFalse(EstadoLote.ABANDONADO.esCultivoActivo(), "ABANDONADO no debe ser cultivo activo");
    }

    @Test
    void testMetodoEsDescanso() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.EN_DESCANSO.esDescanso(), "EN_DESCANSO debe ser descanso");
        assertTrue(EstadoLote.EN_PREPARACION.esDescanso(), "EN_PREPARACION debe ser descanso");
        
        assertFalse(EstadoLote.DISPONIBLE.esDescanso(), "DISPONIBLE no debe ser descanso");
        assertFalse(EstadoLote.PREPARADO.esDescanso(), "PREPARADO no debe ser descanso");
        assertFalse(EstadoLote.SEMBRADO.esDescanso(), "SEMBRADO no debe ser descanso");
        assertFalse(EstadoLote.EN_CRECIMIENTO.esDescanso(), "EN_CRECIMIENTO no debe ser descanso");
        assertFalse(EstadoLote.EN_FLORACION.esDescanso(), "EN_FLORACION no debe ser descanso");
        assertFalse(EstadoLote.EN_FRUTIFICACION.esDescanso(), "EN_FRUTIFICACION no debe ser descanso");
        assertFalse(EstadoLote.LISTO_PARA_COSECHA.esDescanso(), "LISTO_PARA_COSECHA no debe ser descanso");
        assertFalse(EstadoLote.EN_COSECHA.esDescanso(), "EN_COSECHA no debe ser descanso");
        assertFalse(EstadoLote.COSECHADO.esDescanso(), "COSECHADO no debe ser descanso");
        assertFalse(EstadoLote.ENFERMO.esDescanso(), "ENFERMO no debe ser descanso");
        assertFalse(EstadoLote.ABANDONADO.esDescanso(), "ABANDONADO no debe ser descanso");
    }

    @Test
    void testMetodoRequiereAtencion() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.ENFERMO.requiereAtencion(), "ENFERMO debe requerir atención");
        assertTrue(EstadoLote.ABANDONADO.requiereAtencion(), "ABANDONADO debe requerir atención");
        
        assertFalse(EstadoLote.DISPONIBLE.requiereAtencion(), "DISPONIBLE no debe requerir atención");
        assertFalse(EstadoLote.PREPARADO.requiereAtencion(), "PREPARADO no debe requerir atención");
        assertFalse(EstadoLote.SEMBRADO.requiereAtencion(), "SEMBRADO no debe requerir atención");
        assertFalse(EstadoLote.EN_CRECIMIENTO.requiereAtencion(), "EN_CRECIMIENTO no debe requerir atención");
        assertFalse(EstadoLote.EN_FLORACION.requiereAtencion(), "EN_FLORACION no debe requerir atención");
        assertFalse(EstadoLote.EN_FRUTIFICACION.requiereAtencion(), "EN_FRUTIFICACION no debe requerir atención");
        assertFalse(EstadoLote.LISTO_PARA_COSECHA.requiereAtencion(), "LISTO_PARA_COSECHA no debe requerir atención");
        assertFalse(EstadoLote.EN_COSECHA.requiereAtencion(), "EN_COSECHA no debe requerir atención");
        assertFalse(EstadoLote.COSECHADO.requiereAtencion(), "COSECHADO no debe requerir atención");
        assertFalse(EstadoLote.EN_DESCANSO.requiereAtencion(), "EN_DESCANSO no debe requerir atención");
        assertFalse(EstadoLote.EN_PREPARACION.requiereAtencion(), "EN_PREPARACION no debe requerir atención");
    }

    @Test
    void testColoresEstados() {
        // Arrange & Act & Assert
        assertEquals("green", EstadoLote.DISPONIBLE.getColor());
        assertEquals("green", EstadoLote.PREPARADO.getColor());
        assertEquals("blue", EstadoLote.SEMBRADO.getColor());
        assertEquals("blue", EstadoLote.EN_CRECIMIENTO.getColor());
        assertEquals("blue", EstadoLote.EN_FLORACION.getColor());
        assertEquals("blue", EstadoLote.EN_FRUTIFICACION.getColor());
        assertEquals("orange", EstadoLote.LISTO_PARA_COSECHA.getColor());
        assertEquals("purple", EstadoLote.EN_COSECHA.getColor());
        assertEquals("purple", EstadoLote.COSECHADO.getColor());
        assertEquals("gray", EstadoLote.EN_DESCANSO.getColor());
        assertEquals("gray", EstadoLote.EN_PREPARACION.getColor());
        assertEquals("red", EstadoLote.ENFERMO.getColor());
        assertEquals("black", EstadoLote.ABANDONADO.getColor());
    }

    @Test
    void testConsultarLotesPorEstado() {
        // Arrange
        Plot loteDisponible = crearLote("Lote Disponible", EstadoLote.DISPONIBLE);
        Plot loteSembrado = crearLote("Lote Sembrado", EstadoLote.SEMBRADO);
        Plot loteEnCrecimiento = crearLote("Lote En Crecimiento", EstadoLote.EN_CRECIMIENTO);
        Plot loteListoCosecha = crearLote("Lote Listo Cosecha", EstadoLote.LISTO_PARA_COSECHA);
        Plot loteEnfermo = crearLote("Lote Enfermo", EstadoLote.ENFERMO);

        plotRepository.save(loteDisponible);
        plotRepository.save(loteSembrado);
        plotRepository.save(loteEnCrecimiento);
        plotRepository.save(loteListoCosecha);
        plotRepository.save(loteEnfermo);
        entityManager.flush();

        // Act
        List<Plot> lotesDisponibles = plotRepository.findByEstado(EstadoLote.DISPONIBLE);
        List<Plot> lotesSembrados = plotRepository.findByEstado(EstadoLote.SEMBRADO);
        List<Plot> lotesEnCrecimiento = plotRepository.findByEstado(EstadoLote.EN_CRECIMIENTO);
        List<Plot> lotesListosCosecha = plotRepository.findByEstado(EstadoLote.LISTO_PARA_COSECHA);
        List<Plot> lotesEnfermos = plotRepository.findByEstado(EstadoLote.ENFERMO);

        // Assert
        assertEquals(1, lotesDisponibles.size());
        assertEquals("Lote Disponible", lotesDisponibles.get(0).getNombre());

        assertEquals(1, lotesSembrados.size());
        assertEquals("Lote Sembrado", lotesSembrados.get(0).getNombre());

        assertEquals(1, lotesEnCrecimiento.size());
        assertEquals("Lote En Crecimiento", lotesEnCrecimiento.get(0).getNombre());

        assertEquals(1, lotesListosCosecha.size());
        assertEquals("Lote Listo Cosecha", lotesListosCosecha.get(0).getNombre());

        assertEquals(1, lotesEnfermos.size());
        assertEquals("Lote Enfermo", lotesEnfermos.get(0).getNombre());
    }

    @Test
    void testTransicionEstadoConHistorial() {
        // Arrange
        Plot lote = crearLote("Lote Historial", EstadoLote.DISPONIBLE);
        Plot loteGuardado = plotRepository.save(lote);
        entityManager.flush();

        // Act - Simular transiciones con historial
        LocalDateTime fecha1 = LocalDateTime.now().minusDays(5);
        loteGuardado.setEstado(EstadoLote.PREPARADO);
        loteGuardado.setFechaUltimoCambioEstado(fecha1);
        loteGuardado.setMotivoCambioEstado("Preparación del suelo completada");
        plotRepository.save(loteGuardado);

        LocalDateTime fecha2 = LocalDateTime.now().minusDays(3);
        loteGuardado.setEstado(EstadoLote.SEMBRADO);
        loteGuardado.setFechaUltimoCambioEstado(fecha2);
        loteGuardado.setMotivoCambioEstado("Siembra de soja realizada");
        plotRepository.save(loteGuardado);

        LocalDateTime fecha3 = LocalDateTime.now().minusDays(1);
        loteGuardado.setEstado(EstadoLote.EN_CRECIMIENTO);
        loteGuardado.setFechaUltimoCambioEstado(fecha3);
        loteGuardado.setMotivoCambioEstado("Cultivo en crecimiento vegetativo");
        plotRepository.save(loteGuardado);
        entityManager.flush();

        // Assert
        assertEquals(EstadoLote.EN_CRECIMIENTO, loteGuardado.getEstado());
        assertEquals(fecha3, loteGuardado.getFechaUltimoCambioEstado());
        assertEquals("Cultivo en crecimiento vegetativo", loteGuardado.getMotivoCambioEstado());
    }

    private Plot crearLote(String nombre, EstadoLote estado) {
        Plot lote = new Plot();
        lote.setNombre(nombre);
        lote.setDescripcion("Lote de prueba para " + nombre);
        lote.setAreaHectareas(new BigDecimal("25.50"));
        lote.setEstado(estado);
        lote.setTipoSuelo("Franco");
        lote.setCampo(campoTest);
        lote.setUsuario(usuarioTest);
        lote.setActivo(true);
        return lote;
    }
}
