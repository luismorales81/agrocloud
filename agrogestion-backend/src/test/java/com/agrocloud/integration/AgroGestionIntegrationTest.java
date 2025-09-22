package com.agrocloud.integration;

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración completo del sistema AgroGestion.
 * Prueba el flujo completo desde la creación de campos hasta la cosecha.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest
class AgroGestionIntegrationTest extends BaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MaquinariaRepository maquinariaRepository;

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private CosechaRepository cosechaRepository;

    private User usuarioTest;
    private Field campoTest;
    private Plot loteTest;
    private Cultivo cultivoTest;
    private Insumo semillaTest;
    private Insumo fertilizanteTest;
    private Maquinaria tractorTest;
    private Maquinaria sembradoraTest;

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
        campoTest.setNombre("Campo Norte");
        campoTest.setUbicacion("Zona Norte - Departamento Test");
        campoTest.setAreaHectareas(new BigDecimal("100.50"));
        campoTest.setTipoSuelo("Franco");
        campoTest.setDescripcion("Campo principal para pruebas");
        campoTest.setUsuario(usuarioTest);
        campoTest.setActivo(true);
        entityManager.persistAndFlush(campoTest);

        // Crear lote de prueba
        loteTest = new Plot();
        loteTest.setNombre("Lote A1");
        loteTest.setDescripcion("Lote de prueba para soja");
        loteTest.setAreaHectareas(new BigDecimal("25.50"));
        loteTest.setEstado(EstadoLote.DISPONIBLE);
        loteTest.setTipoSuelo("Franco");
        loteTest.setCampo(campoTest);
        loteTest.setUsuario(usuarioTest);
        loteTest.setActivo(true);
        entityManager.persistAndFlush(loteTest);

        // Crear cultivo de prueba
        cultivoTest = new Cultivo();
        cultivoTest.setNombre("Soja");
        cultivoTest.setTipo("Oleaginosa");
        cultivoTest.setVariedad("DM 53i53");
        cultivoTest.setCicloDias(120);
        cultivoTest.setRendimientoEsperado(new BigDecimal("3500.00"));
        cultivoTest.setUnidadRendimiento("kg/ha");
        cultivoTest.setPrecioPorTonelada(new BigDecimal("450.00"));
        cultivoTest.setDescripcion("Soja de primera calidad");
        cultivoTest.setEstado(Cultivo.EstadoCultivo.ACTIVO);
        cultivoTest.setUsuario(usuarioTest);
        cultivoTest.setActivo(true);
        entityManager.persistAndFlush(cultivoTest);

        // Crear insumos de prueba
        semillaTest = new Insumo();
        semillaTest.setNombre("Semilla Soja DM 53i53");
        semillaTest.setTipo(Insumo.TipoInsumo.SEMILLA);
        semillaTest.setDescripcion("Semilla de soja de primera calidad");
        semillaTest.setUnidadMedida("kg");
        semillaTest.setPrecioUnitario(new BigDecimal("15.50"));
        semillaTest.setStockDisponible(new BigDecimal("1000.00"));
        semillaTest.setStockMinimo(new BigDecimal("100.00"));
        semillaTest.setProveedor("Semillas del Norte");
        semillaTest.setUsuario(usuarioTest);
        semillaTest.setActivo(true);
        entityManager.persistAndFlush(semillaTest);

        fertilizanteTest = new Insumo();
        fertilizanteTest.setNombre("Fertilizante Urea");
        fertilizanteTest.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
        fertilizanteTest.setDescripcion("Fertilizante nitrogenado");
        fertilizanteTest.setUnidadMedida("kg");
        fertilizanteTest.setPrecioUnitario(new BigDecimal("0.85"));
        fertilizanteTest.setStockDisponible(new BigDecimal("5000.00"));
        fertilizanteTest.setStockMinimo(new BigDecimal("500.00"));
        fertilizanteTest.setProveedor("Fertilizantes SA");
        fertilizanteTest.setUsuario(usuarioTest);
        fertilizanteTest.setActivo(true);
        entityManager.persistAndFlush(fertilizanteTest);

        // Crear maquinaria de prueba
        tractorTest = new Maquinaria();
        tractorTest.setNombre("Tractor John Deere");
        tractorTest.setTipo("Tractor");
        tractorTest.setMarca("John Deere");
        tractorTest.setModelo("6120R");
        tractorTest.setAño(2020);
        tractorTest.setDescripcion("Tractor de 120 HP");
        tractorTest.setEstado(Maquinaria.EstadoMaquinaria.DISPONIBLE);
        tractorTest.setCostoPorHora(new BigDecimal("25.00"));
        tractorTest.setUsuario(usuarioTest);
        tractorTest.setActivo(true);
        entityManager.persistAndFlush(tractorTest);

        sembradoraTest = new Maquinaria();
        sembradoraTest.setNombre("Sembradora Neumática");
        sembradoraTest.setTipo("Sembradora");
        sembradoraTest.setMarca("Agrometal");
        sembradoraTest.setModelo("AS-3000");
        sembradoraTest.setAño(2019);
        sembradoraTest.setDescripcion("Sembradora de 24 surcos");
        sembradoraTest.setEstado(Maquinaria.EstadoMaquinaria.DISPONIBLE);
        sembradoraTest.setCostoPorHora(new BigDecimal("15.00"));
        sembradoraTest.setUsuario(usuarioTest);
        sembradoraTest.setActivo(true);
        entityManager.persistAndFlush(sembradoraTest);
    }

    @Test
    void testFlujoCompletoCicloAgricola() {
        // ===== FASE 1: PREPARACIÓN =====
        
        // 1.1. Cambiar estado del lote a PREPARADO
        loteTest.setEstado(EstadoLote.PREPARADO);
        loteTest.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
        loteTest.setMotivoCambioEstado("Preparación del suelo completada");
        Plot lotePreparado = plotRepository.save(loteTest);
        entityManager.flush();

        assertEquals(EstadoLote.PREPARADO, lotePreparado.getEstado());

        // ===== FASE 2: SIEMBRA =====
        
        // 2.1. Crear labor de siembra
        Labor laborSiembra = new Labor();
        laborSiembra.setTipoLabor(Labor.TipoLabor.SIEMBRA);
        laborSiembra.setFechaInicio(LocalDate.now());
        laborSiembra.setDescripcion("Siembra de soja en lote A1");
        laborSiembra.setCostoTotal(new BigDecimal("1250.00"));
        laborSiembra.setEstado(Labor.EstadoLabor.PLANIFICADA);
        laborSiembra.setObservaciones("Siembra con sembradora neumática");
        laborSiembra.setResponsable("Carlos Operario");
        laborSiembra.setLote(loteTest);
        laborSiembra.setUsuario(usuarioTest);
        laborSiembra.setActivo(true);
        Labor siembraGuardada = laborRepository.save(laborSiembra);
        entityManager.flush();

        // 2.2. Ejecutar siembra (cambiar estado a EN_PROGRESO y luego COMPLETADA)
        siembraGuardada.setEstado(Labor.EstadoLabor.EN_PROGRESO);
        laborRepository.save(siembraGuardada);
        
        siembraGuardada.setEstado(Labor.EstadoLabor.COMPLETADA);
        siembraGuardada.setFechaFin(LocalDate.now());
        laborRepository.save(siembraGuardada);
        entityManager.flush();

        // 2.3. Actualizar stock de semillas
        semillaTest.setStockDisponible(semillaTest.getStockDisponible().subtract(new BigDecimal("50.00")));
        insumoRepository.save(semillaTest);
        entityManager.flush();

        // 2.4. Cambiar estado del lote a SEMBRADO
        loteTest.setEstado(EstadoLote.SEMBRADO);
        loteTest.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
        loteTest.setMotivoCambioEstado("Siembra completada");
        plotRepository.save(loteTest);
        entityManager.flush();

        assertEquals(Labor.EstadoLabor.COMPLETADA, siembraGuardada.getEstado());
        assertEquals(new BigDecimal("950.00"), semillaTest.getStockDisponible());
        assertEquals(EstadoLote.SEMBRADO, loteTest.getEstado());

        // ===== FASE 3: FERTILIZACIÓN =====
        
        // 3.1. Crear labor de fertilización
        Labor laborFertilizacion = new Labor();
        laborFertilizacion.setTipoLabor(Labor.TipoLabor.FERTILIZACION);
        laborFertilizacion.setFechaInicio(LocalDate.now().plusDays(15));
        laborFertilizacion.setDescripcion("Fertilización nitrogenada");
        laborFertilizacion.setCostoTotal(new BigDecimal("850.00"));
        laborFertilizacion.setEstado(Labor.EstadoLabor.PLANIFICADA);
        laborFertilizacion.setObservaciones("Aplicación de urea");
        laborFertilizacion.setResponsable("María Técnica");
        laborFertilizacion.setLote(loteTest);
        laborFertilizacion.setUsuario(usuarioTest);
        laborFertilizacion.setActivo(true);
        Labor fertilizacionGuardada = laborRepository.save(laborFertilizacion);
        entityManager.flush();

        // 3.2. Ejecutar fertilización
        fertilizacionGuardada.setEstado(Labor.EstadoLabor.COMPLETADA);
        fertilizacionGuardada.setFechaFin(LocalDate.now().plusDays(15));
        laborRepository.save(fertilizacionGuardada);
        entityManager.flush();

        // 3.3. Actualizar stock de fertilizante
        fertilizanteTest.setStockDisponible(fertilizanteTest.getStockDisponible().subtract(new BigDecimal("100.00")));
        insumoRepository.save(fertilizanteTest);
        entityManager.flush();

        // 3.4. Cambiar estado del lote a EN_CRECIMIENTO
        loteTest.setEstado(EstadoLote.EN_CRECIMIENTO);
        loteTest.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
        loteTest.setMotivoCambioEstado("Cultivo en crecimiento vegetativo");
        plotRepository.save(loteTest);
        entityManager.flush();

        assertEquals(Labor.EstadoLabor.COMPLETADA, fertilizacionGuardada.getEstado());
        assertEquals(new BigDecimal("4900.00"), fertilizanteTest.getStockDisponible());
        assertEquals(EstadoLote.EN_CRECIMIENTO, loteTest.getEstado());

        // ===== FASE 4: COSECHA =====
        
        // 4.1. Cambiar estado del lote a LISTO_PARA_COSECHA
        loteTest.setEstado(EstadoLote.LISTO_PARA_COSECHA);
        loteTest.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
        loteTest.setMotivoCambioEstado("Cultivo listo para cosecha");
        plotRepository.save(loteTest);
        entityManager.flush();

        // 4.2. Crear labor de cosecha
        Labor laborCosecha = new Labor();
        laborCosecha.setTipoLabor(Labor.TipoLabor.COSECHA);
        laborCosecha.setFechaInicio(LocalDate.now().plusDays(120));
        laborCosecha.setFechaFin(LocalDate.now().plusDays(122));
        laborCosecha.setDescripcion("Cosecha de soja");
        laborCosecha.setCostoTotal(new BigDecimal("2100.00"));
        laborCosecha.setEstado(Labor.EstadoLabor.PLANIFICADA);
        laborCosecha.setObservaciones("Cosecha programada para mayo");
        laborCosecha.setResponsable("Carlos Operario");
        laborCosecha.setLote(loteTest);
        laborCosecha.setUsuario(usuarioTest);
        laborCosecha.setActivo(true);
        Labor cosechaGuardada = laborRepository.save(laborCosecha);
        entityManager.flush();

        // 4.3. Ejecutar cosecha
        cosechaGuardada.setEstado(Labor.EstadoLabor.COMPLETADA);
        cosechaGuardada.setFechaFin(LocalDate.now().plusDays(122));
        laborRepository.save(cosechaGuardada);
        entityManager.flush();

        // 4.4. Registrar cosecha
        Cosecha cosecha = new Cosecha();
        cosecha.setLote(loteTest);
        cosecha.setCultivo(cultivoTest);
        cosecha.setFechaCosecha(LocalDate.now().plusDays(122));
        cosecha.setCantidadCosechada(new BigDecimal("89.25"));
        cosecha.setUnidadMedida("toneladas");
        cosecha.setRendimiento(new BigDecimal("3500.00"));
        cosecha.setPrecioPorUnidad(new BigDecimal("450.00"));
        cosecha.setValorTotal(new BigDecimal("40162.50"));
        cosecha.setObservaciones("Cosecha de soja con buen rendimiento");
        cosecha.setUsuario(usuarioTest);
        cosecha.setActivo(true);
        Cosecha cosechaRegistrada = cosechaRepository.save(cosecha);
        entityManager.flush();

        // 4.5. Cambiar estado del lote a COSECHADO
        loteTest.setEstado(EstadoLote.COSECHADO);
        loteTest.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
        loteTest.setMotivoCambioEstado("Cosecha completada");
        plotRepository.save(loteTest);
        entityManager.flush();

        assertEquals(Labor.EstadoLabor.COMPLETADA, cosechaGuardada.getEstado());
        assertEquals(new BigDecimal("89.25"), cosechaRegistrada.getCantidadCosechada());
        assertEquals(EstadoLote.COSECHADO, loteTest.getEstado());

        // ===== FASE 5: REGISTRO FINANCIERO =====
        
        // 5.1. Registrar ingreso por venta
        Ingreso ingreso = new Ingreso();
        ingreso.setConcepto("Venta Soja 2024");
        ingreso.setDescripcion("Venta de soja cosecha 2024");
        ingreso.setTipoIngreso(Ingreso.TipoIngreso.VENTA_CULTIVO);
        ingreso.setFechaIngreso(LocalDate.now().plusDays(125));
        ingreso.setMonto(new BigDecimal("45000.00"));
        ingreso.setUnidadMedida("toneladas");
        ingreso.setCantidad(new BigDecimal("100.00"));
        ingreso.setClienteComprador("Acopio del Norte");
        ingreso.setEstado(Ingreso.EstadoIngreso.CONFIRMADO);
        ingreso.setLote(loteTest);
        ingreso.setUsuario(usuarioTest);
        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);
        entityManager.flush();

        // 5.2. Registrar egresos
        Egreso egresoSemillas = new Egreso();
        egresoSemillas.setConcepto("Compra Semillas");
        egresoSemillas.setDescripcion("Compra de semillas para siembra");
        egresoSemillas.setTipoEgreso(Egreso.TipoEgreso.INSUMOS);
        egresoSemillas.setFechaEgreso(LocalDate.now().minusDays(10));
        egresoSemillas.setMonto(new BigDecimal("15500.00"));
        egresoSemillas.setProveedor("Semillas del Norte");
        egresoSemillas.setEstado(Egreso.EstadoEgreso.PAGADO);
        egresoSemillas.setLote(loteTest);
        egresoSemillas.setUsuario(usuarioTest);
        egresoRepository.save(egresoSemillas);

        Egreso egresoFertilizantes = new Egreso();
        egresoFertilizantes.setConcepto("Compra Fertilizantes");
        egresoFertilizantes.setDescripcion("Compra de fertilizantes");
        egresoFertilizantes.setTipoEgreso(Egreso.TipoEgreso.INSUMOS);
        egresoFertilizantes.setFechaEgreso(LocalDate.now().minusDays(8));
        egresoFertilizantes.setMonto(new BigDecimal("4250.00"));
        egresoFertilizantes.setProveedor("Fertilizantes SA");
        egresoFertilizantes.setEstado(Egreso.EstadoEgreso.PAGADO);
        egresoFertilizantes.setLote(loteTest);
        egresoFertilizantes.setUsuario(usuarioTest);
        egresoRepository.save(egresoFertilizantes);
        entityManager.flush();

        // ===== VERIFICACIONES FINALES =====
        
        // Verificar que todas las labores están completadas
        List<Labor> laboresLote = laborRepository.findByLoteId(loteTest.getId());
        assertEquals(3, laboresLote.size());
        assertTrue(laboresLote.stream().allMatch(l -> l.getEstado() == Labor.EstadoLabor.COMPLETADA));

        // Verificar costos totales
        BigDecimal costoTotalLabores = laboresLote.stream()
                .map(Labor::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("4200.00"), costoTotalLabores);

        // Verificar ingresos y egresos
        List<Ingreso> ingresos = ingresoRepository.findByUsuarioId(usuarioTest.getId());
        List<Egreso> egresos = egresoRepository.findByUsuarioId(usuarioTest.getId());
        
        assertEquals(1, ingresos.size());
        assertEquals(2, egresos.size());
        
        BigDecimal totalIngresos = ingresos.stream()
                .map(Ingreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEgresos = egresos.stream()
                .map(Egreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        assertEquals(new BigDecimal("45000.00"), totalIngresos);
        assertEquals(new BigDecimal("19750.00"), totalEgresos);

        // Verificar rentabilidad
        BigDecimal rentabilidad = totalIngresos.subtract(totalEgresos);
        assertEquals(new BigDecimal("25250.00"), rentabilidad);

        // Verificar cosecha registrada
        List<Cosecha> cosechas = cosechaRepository.findByLoteId(loteTest.getId());
        assertEquals(1, cosechas.size());
        assertEquals(new BigDecimal("89.25"), cosechas.get(0).getCantidadCosechada());
        assertEquals(new BigDecimal("3500.00"), cosechas.get(0).getRendimiento());

        // Verificar estado final del lote
        Plot loteFinal = plotRepository.findById(loteTest.getId()).orElse(null);
        assertNotNull(loteFinal);
        assertEquals(EstadoLote.COSECHADO, loteFinal.getEstado());
    }

    @Test
    void testValidacionEstadosLote() {
        // Verificar que el lote puede ser sembrado desde DISPONIBLE
        assertTrue(EstadoLote.DISPONIBLE.puedeSembrar());
        assertTrue(EstadoLote.PREPARADO.puedeSembrar());
        assertTrue(EstadoLote.EN_PREPARACION.puedeSembrar());

        // Verificar que el lote no puede ser sembrado desde otros estados
        assertFalse(EstadoLote.SEMBRADO.puedeSembrar());
        assertFalse(EstadoLote.EN_CRECIMIENTO.puedeSembrar());
        assertFalse(EstadoLote.EN_FLORACION.puedeSembrar());
        assertFalse(EstadoLote.EN_FRUTIFICACION.puedeSembrar());
        assertFalse(EstadoLote.LISTO_PARA_COSECHA.puedeSembrar());
        assertFalse(EstadoLote.EN_COSECHA.puedeSembrar());
        assertFalse(EstadoLote.COSECHADO.puedeSembrar());
        assertFalse(EstadoLote.EN_DESCANSO.puedeSembrar());
        assertFalse(EstadoLote.ENFERMO.puedeSembrar());
        assertFalse(EstadoLote.ABANDONADO.puedeSembrar());

        // Verificar que solo LISTO_PARA_COSECHA permite cosecha
        assertTrue(EstadoLote.LISTO_PARA_COSECHA.puedeCosechar());
        assertFalse(EstadoLote.DISPONIBLE.puedeCosechar());
        assertFalse(EstadoLote.PREPARADO.puedeCosechar());
        assertFalse(EstadoLote.SEMBRADO.puedeCosechar());
        assertFalse(EstadoLote.EN_CRECIMIENTO.puedeCosechar());
        assertFalse(EstadoLote.EN_FLORACION.puedeCosechar());
        assertFalse(EstadoLote.EN_FRUTIFICACION.puedeCosechar());
        assertFalse(EstadoLote.EN_COSECHA.puedeCosechar());
        assertFalse(EstadoLote.COSECHADO.puedeCosechar());
        assertFalse(EstadoLote.EN_DESCANSO.puedeCosechar());
        assertFalse(EstadoLote.EN_PREPARACION.puedeCosechar());
        assertFalse(EstadoLote.ENFERMO.puedeCosechar());
        assertFalse(EstadoLote.ABANDONADO.puedeCosechar());

        // Verificar estados de cultivo activo
        assertTrue(EstadoLote.SEMBRADO.esCultivoActivo());
        assertTrue(EstadoLote.EN_CRECIMIENTO.esCultivoActivo());
        assertTrue(EstadoLote.EN_FLORACION.esCultivoActivo());
        assertTrue(EstadoLote.EN_FRUTIFICACION.esCultivoActivo());

        // Verificar estados de descanso
        assertTrue(EstadoLote.EN_DESCANSO.esDescanso());
        assertTrue(EstadoLote.EN_PREPARACION.esDescanso());

        // Verificar estados que requieren atención
        assertTrue(EstadoLote.ENFERMO.requiereAtencion());
        assertTrue(EstadoLote.ABANDONADO.requiereAtencion());
    }
}
