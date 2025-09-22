package com.agrocloud.entity;

import com.agrocloud.BaseTest;
import com.agrocloud.model.entity.*;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la entidad Labor.
 * Prueba la gestión de labores: SIEMBRA, FERTILIZACION, RIEGO, COSECHA, etc.
 * con estados PLANIFICADA, EN_PROGRESO, COMPLETADA, CANCELADA.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@DataJpaTest
class LaboresTest extends BaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MaquinariaRepository maquinariaRepository;

    private User usuarioTest;
    private Field campoTest;
    private Plot loteTest;
    private Insumo insumoTest;
    private Maquinaria maquinariaTest;

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
        loteTest.setDescripcion("Lote de prueba");
        loteTest.setAreaHectareas(new BigDecimal("25.50"));
        loteTest.setEstado(EstadoLote.DISPONIBLE);
        loteTest.setTipoSuelo("Franco");
        loteTest.setCampo(campoTest);
        loteTest.setUsuario(usuarioTest);
        loteTest.setActivo(true);
        entityManager.persistAndFlush(loteTest);

        // Crear insumo de prueba
        insumoTest = new Insumo();
        insumoTest.setNombre("Semilla Soja DM 53i53");
        insumoTest.setTipo("Semilla");
        insumoTest.setDescripcion("Semilla de soja");
        insumoTest.setUnidadMedida("kg");
        insumoTest.setPrecioUnitario(new BigDecimal("15.50"));
        insumoTest.setStockDisponible(new BigDecimal("1000.00"));
        insumoTest.setStockMinimo(new BigDecimal("100.00"));
        insumoTest.setProveedor("Semillas del Norte");
        insumoTest.setUsuario(usuarioTest);
        insumoTest.setActivo(true);
        entityManager.persistAndFlush(insumoTest);

        // Crear maquinaria de prueba
        maquinariaTest = new Maquinaria();
        maquinariaTest.setNombre("Tractor John Deere");
        maquinariaTest.setTipo("Tractor");
        maquinariaTest.setMarca("John Deere");
        maquinariaTest.setModelo("6120R");
        maquinariaTest.setAño(2020);
        maquinariaTest.setDescripcion("Tractor de 120 HP");
        maquinariaTest.setEstado(Maquinaria.EstadoMaquinaria.DISPONIBLE);
        maquinariaTest.setCostoPorHora(new BigDecimal("25.00"));
        maquinariaTest.setUsuario(usuarioTest);
        maquinariaTest.setActivo(true);
        entityManager.persistAndFlush(maquinariaTest);
    }

    @Test
    void testCrearLaborSiembra() {
        // Arrange
        Labor labor = new Labor();
        labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);
        labor.setFechaInicio(LocalDate.now());
        labor.setDescripcion("Siembra de soja en lote A1");
        labor.setCostoTotal(new BigDecimal("1250.00"));
        labor.setEstado(Labor.EstadoLabor.PLANIFICADA);
        labor.setObservaciones("Siembra con sembradora neumática");
        labor.setResponsable("Carlos Operario");
        labor.setLote(loteTest);
        labor.setUsuario(usuarioTest);
        labor.setActivo(true);

        // Act
        Labor laborGuardada = laborRepository.save(labor);
        entityManager.flush();

        // Assert
        assertNotNull(laborGuardada.getId());
        assertEquals(Labor.TipoLabor.SIEMBRA, laborGuardada.getTipoLabor());
        assertEquals(LocalDate.now(), laborGuardada.getFechaInicio());
        assertEquals("Siembra de soja en lote A1", laborGuardada.getDescripcion());
        assertEquals(new BigDecimal("1250.00"), laborGuardada.getCostoTotal());
        assertEquals(Labor.EstadoLabor.PLANIFICADA, laborGuardada.getEstado());
        assertEquals("Siembra con sembradora neumática", laborGuardada.getObservaciones());
        assertEquals("Carlos Operario", laborGuardada.getResponsable());
        assertEquals(loteTest.getId(), laborGuardada.getLote().getId());
        assertEquals(usuarioTest.getId(), laborGuardada.getUsuario().getId());
        assertTrue(laborGuardada.getActivo());
    }

    @Test
    void testCrearLaborFertilizacion() {
        // Arrange
        Labor labor = new Labor();
        labor.setTipoLabor(Labor.TipoLabor.FERTILIZACION);
        labor.setFechaInicio(LocalDate.now().plusDays(15));
        labor.setDescripcion("Fertilización nitrogenada");
        labor.setCostoTotal(new BigDecimal("850.00"));
        labor.setEstado(Labor.EstadoLabor.PLANIFICADA);
        labor.setObservaciones("Aplicación de urea");
        labor.setResponsable("María Técnica");
        labor.setLote(loteTest);
        labor.setUsuario(usuarioTest);
        labor.setActivo(true);

        // Act
        Labor laborGuardada = laborRepository.save(labor);
        entityManager.flush();

        // Assert
        assertEquals(Labor.TipoLabor.FERTILIZACION, laborGuardada.getTipoLabor());
        assertEquals("Fertilización nitrogenada", laborGuardada.getDescripcion());
        assertEquals("María Técnica", laborGuardada.getResponsable());
    }

    @Test
    void testCrearLaborRiego() {
        // Arrange
        Labor labor = new Labor();
        labor.setTipoLabor(Labor.TipoLabor.RIEGO);
        labor.setFechaInicio(LocalDate.now().plusDays(30));
        labor.setDescripcion("Riego complementario");
        labor.setCostoTotal(new BigDecimal("300.00"));
        labor.setEstado(Labor.EstadoLabor.PLANIFICADA);
        labor.setObservaciones("Riego por aspersión");
        labor.setResponsable("Carlos Operario");
        labor.setLote(loteTest);
        labor.setUsuario(usuarioTest);
        labor.setActivo(true);

        // Act
        Labor laborGuardada = laborRepository.save(labor);
        entityManager.flush();

        // Assert
        assertEquals(Labor.TipoLabor.RIEGO, laborGuardada.getTipoLabor());
        assertEquals("Riego complementario", laborGuardada.getDescripcion());
        assertEquals("Riego por aspersión", laborGuardada.getObservaciones());
    }

    @Test
    void testCrearLaborCosecha() {
        // Arrange
        Labor labor = new Labor();
        labor.setTipoLabor(Labor.TipoLabor.COSECHA);
        labor.setFechaInicio(LocalDate.now().plusDays(120));
        labor.setFechaFin(LocalDate.now().plusDays(122));
        labor.setDescripcion("Cosecha de soja");
        labor.setCostoTotal(new BigDecimal("2100.00"));
        labor.setEstado(Labor.EstadoLabor.PLANIFICADA);
        labor.setObservaciones("Cosecha programada para mayo");
        labor.setResponsable("Carlos Operario");
        labor.setLote(loteTest);
        labor.setUsuario(usuarioTest);
        labor.setActivo(true);

        // Act
        Labor laborGuardada = laborRepository.save(labor);
        entityManager.flush();

        // Assert
        assertEquals(Labor.TipoLabor.COSECHA, laborGuardada.getTipoLabor());
        assertEquals(LocalDate.now().plusDays(120), laborGuardada.getFechaInicio());
        assertEquals(LocalDate.now().plusDays(122), laborGuardada.getFechaFin());
        assertEquals("Cosecha de soja", laborGuardada.getDescripcion());
    }

    @Test
    void testTransicionEstadosLabor() {
        // Arrange
        Labor labor = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.PLANIFICADA);
        Labor laborGuardada = laborRepository.save(labor);
        entityManager.flush();

        // Act & Assert - Transición de estados
        // 1. PLANIFICADA -> EN_PROGRESO
        laborGuardada.setEstado(Labor.EstadoLabor.EN_PROGRESO);
        Labor laborEnProgreso = laborRepository.save(laborGuardada);
        entityManager.flush();
        assertEquals(Labor.EstadoLabor.EN_PROGRESO, laborEnProgreso.getEstado());

        // 2. EN_PROGRESO -> COMPLETADA
        laborEnProgreso.setEstado(Labor.EstadoLabor.COMPLETADA);
        laborEnProgreso.setFechaFin(LocalDate.now());
        Labor laborCompletada = laborRepository.save(laborEnProgreso);
        entityManager.flush();
        assertEquals(Labor.EstadoLabor.COMPLETADA, laborCompletada.getEstado());
        assertNotNull(laborCompletada.getFechaFin());
    }

    @Test
    void testCancelarLabor() {
        // Arrange
        Labor labor = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.PLANIFICADA);
        Labor laborGuardada = laborRepository.save(labor);
        entityManager.flush();

        // Act
        laborGuardada.setEstado(Labor.EstadoLabor.CANCELADA);
        laborGuardada.setObservaciones("Cancelada por condiciones climáticas adversas");
        Labor laborCancelada = laborRepository.save(laborGuardada);
        entityManager.flush();

        // Assert
        assertEquals(Labor.EstadoLabor.CANCELADA, laborCancelada.getEstado());
        assertEquals("Cancelada por condiciones climáticas adversas", laborCancelada.getObservaciones());
    }

    @Test
    void testConsultarLaboresPorTipo() {
        // Arrange
        Labor siembra = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.PLANIFICADA);
        Labor fertilizacion = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.PLANIFICADA);
        Labor riego = crearLabor(Labor.TipoLabor.RIEGO, Labor.EstadoLabor.PLANIFICADA);

        laborRepository.save(siembra);
        laborRepository.save(fertilizacion);
        laborRepository.save(riego);
        entityManager.flush();

        // Act
        List<Labor> laboresSiembra = laborRepository.findByTipoLabor(Labor.TipoLabor.SIEMBRA);
        List<Labor> laboresFertilizacion = laborRepository.findByTipoLabor(Labor.TipoLabor.FERTILIZACION);
        List<Labor> laboresRiego = laborRepository.findByTipoLabor(Labor.TipoLabor.RIEGO);

        // Assert
        assertEquals(1, laboresSiembra.size());
        assertEquals(Labor.TipoLabor.SIEMBRA, laboresSiembra.get(0).getTipoLabor());

        assertEquals(1, laboresFertilizacion.size());
        assertEquals(Labor.TipoLabor.FERTILIZACION, laboresFertilizacion.get(0).getTipoLabor());

        assertEquals(1, laboresRiego.size());
        assertEquals(Labor.TipoLabor.RIEGO, laboresRiego.get(0).getTipoLabor());
    }

    @Test
    void testConsultarLaboresPorEstado() {
        // Arrange
        Labor planificada = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.PLANIFICADA);
        Labor enProgreso = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.EN_PROGRESO);
        Labor completada = crearLabor(Labor.TipoLabor.RIEGO, Labor.EstadoLabor.COMPLETADA);
        Labor cancelada = crearLabor(Labor.TipoLabor.COSECHA, Labor.EstadoLabor.CANCELADA);

        laborRepository.save(planificada);
        laborRepository.save(enProgreso);
        laborRepository.save(completada);
        laborRepository.save(cancelada);
        entityManager.flush();

        // Act
        List<Labor> laboresPlanificadas = laborRepository.findByEstado(Labor.EstadoLabor.PLANIFICADA);
        List<Labor> laboresEnProgreso = laborRepository.findByEstado(Labor.EstadoLabor.EN_PROGRESO);
        List<Labor> laboresCompletadas = laborRepository.findByEstado(Labor.EstadoLabor.COMPLETADA);
        List<Labor> laboresCanceladas = laborRepository.findByEstado(Labor.EstadoLabor.CANCELADA);

        // Assert
        assertEquals(1, laboresPlanificadas.size());
        assertEquals(Labor.EstadoLabor.PLANIFICADA, laboresPlanificadas.get(0).getEstado());

        assertEquals(1, laboresEnProgreso.size());
        assertEquals(Labor.EstadoLabor.EN_PROGRESO, laboresEnProgreso.get(0).getEstado());

        assertEquals(1, laboresCompletadas.size());
        assertEquals(Labor.EstadoLabor.COMPLETADA, laboresCompletadas.get(0).getEstado());

        assertEquals(1, laboresCanceladas.size());
        assertEquals(Labor.EstadoLabor.CANCELADA, laboresCanceladas.get(0).getEstado());
    }

    @Test
    void testConsultarLaboresPorLote() {
        // Arrange
        Labor labor1 = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.PLANIFICADA);
        Labor labor2 = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.PLANIFICADA);
        Labor labor3 = crearLabor(Labor.TipoLabor.RIEGO, Labor.EstadoLabor.PLANIFICADA);

        laborRepository.save(labor1);
        laborRepository.save(labor2);
        laborRepository.save(labor3);
        entityManager.flush();

        // Act
        List<Labor> laboresLote = laborRepository.findByLoteId(loteTest.getId());

        // Assert
        assertEquals(3, laboresLote.size());
        assertTrue(laboresLote.stream().allMatch(l -> l.getLote().getId().equals(loteTest.getId())));
    }

    @Test
    void testConsultarLaboresPorUsuario() {
        // Arrange
        Labor labor1 = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.PLANIFICADA);
        Labor labor2 = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.PLANIFICADA);

        laborRepository.save(labor1);
        laborRepository.save(labor2);
        entityManager.flush();

        // Act
        List<Labor> laboresUsuario = laborRepository.findByUsuarioId(usuarioTest.getId());

        // Assert
        assertEquals(2, laboresUsuario.size());
        assertTrue(laboresUsuario.stream().allMatch(l -> l.getUsuario().getId().equals(usuarioTest.getId())));
    }

    @Test
    void testConsultarLaboresPorFecha() {
        // Arrange
        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaAyer = LocalDate.now().minusDays(1);
        LocalDate fechaManana = LocalDate.now().plusDays(1);

        Labor laborAyer = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.COMPLETADA);
        laborAyer.setFechaInicio(fechaAyer);

        Labor laborHoy = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.EN_PROGRESO);
        laborHoy.setFechaInicio(fechaHoy);

        Labor laborManana = crearLabor(Labor.TipoLabor.RIEGO, Labor.EstadoLabor.PLANIFICADA);
        laborManana.setFechaInicio(fechaManana);

        laborRepository.save(laborAyer);
        laborRepository.save(laborHoy);
        laborRepository.save(laborManana);
        entityManager.flush();

        // Act
        List<Labor> laboresHoy = laborRepository.findByFechaInicio(fechaHoy);
        List<Labor> laboresManana = laborRepository.findByFechaInicio(fechaManana);

        // Assert
        assertEquals(1, laboresHoy.size());
        assertEquals(fechaHoy, laboresHoy.get(0).getFechaInicio());

        assertEquals(1, laboresManana.size());
        assertEquals(fechaManana, laboresManana.get(0).getFechaInicio());
    }

    @Test
    void testCalcularCostoTotalLabores() {
        // Arrange
        Labor labor1 = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.COMPLETADA);
        labor1.setCostoTotal(new BigDecimal("1250.00"));

        Labor labor2 = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.COMPLETADA);
        labor2.setCostoTotal(new BigDecimal("850.00"));

        Labor labor3 = crearLabor(Labor.TipoLabor.RIEGO, Labor.EstadoLabor.COMPLETADA);
        labor3.setCostoTotal(new BigDecimal("300.00"));

        laborRepository.save(labor1);
        laborRepository.save(labor2);
        laborRepository.save(labor3);
        entityManager.flush();

        // Act
        List<Labor> laboresCompletadas = laborRepository.findByEstado(Labor.EstadoLabor.COMPLETADA);
        BigDecimal costoTotal = laboresCompletadas.stream()
                .map(Labor::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(3, laboresCompletadas.size());
        assertEquals(new BigDecimal("2400.00"), costoTotal);
    }

    @Test
    void testConsultarLaboresActivas() {
        // Arrange
        Labor laborActiva = crearLabor(Labor.TipoLabor.SIEMBRA, Labor.EstadoLabor.PLANIFICADA);
        laborActiva.setActivo(true);

        Labor laborInactiva = crearLabor(Labor.TipoLabor.FERTILIZACION, Labor.EstadoLabor.PLANIFICADA);
        laborInactiva.setActivo(false);

        laborRepository.save(laborActiva);
        laborRepository.save(laborInactiva);
        entityManager.flush();

        // Act
        List<Labor> laboresActivas = laborRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId());

        // Assert
        assertEquals(1, laboresActivas.size());
        assertEquals("Siembra de soja en lote A1", laboresActivas.get(0).getDescripcion());
        assertTrue(laboresActivas.get(0).getActivo());
    }

    @Test
    void testValidarDatosObligatorios() {
        // Arrange
        Labor laborSinTipo = new Labor();
        laborSinTipo.setFechaInicio(LocalDate.now());
        laborSinTipo.setUsuario(usuarioTest);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            laborRepository.save(laborSinTipo);
            entityManager.flush();
        });
    }

    private Labor crearLabor(Labor.TipoLabor tipoLabor, Labor.EstadoLabor estado) {
        Labor labor = new Labor();
        labor.setTipoLabor(tipoLabor);
        labor.setFechaInicio(LocalDate.now());
        labor.setDescripcion("Siembra de soja en lote A1");
        labor.setCostoTotal(new BigDecimal("1250.00"));
        labor.setEstado(estado);
        labor.setObservaciones("Observaciones de prueba");
        labor.setResponsable("Responsable Test");
        labor.setLote(loteTest);
        labor.setUsuario(usuarioTest);
        labor.setActivo(true);
        return labor;
    }
}
