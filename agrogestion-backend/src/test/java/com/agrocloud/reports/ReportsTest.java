package com.agrocloud.reports;

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
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para reportes y exportación de datos.
 * Prueba la generación de reportes de rendimiento, costos y rentabilidad.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@DataJpaTest
class ReportsTest extends BaseTest {

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
    private LaborRepository laborRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MaquinariaRepository maquinariaRepository;

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
    }

    @Test
    void testReporteRendimientoPorLote() {
        // Arrange
        Cosecha cosecha1 = crearCosecha(loteTest, cultivoTest, LocalDate.now().minusDays(30), 
                new BigDecimal("89.25"), "toneladas", new BigDecimal("3500.00"), new BigDecimal("450.00"));
        Cosecha cosecha2 = crearCosecha(loteTest, cultivoTest, LocalDate.now().minusDays(60), 
                new BigDecimal("78.50"), "toneladas", new BigDecimal("3080.00"), new BigDecimal("420.00"));

        cosechaRepository.save(cosecha1);
        cosechaRepository.save(cosecha2);
        entityManager.flush();

        // Act
        List<Cosecha> cosechasLote = cosechaRepository.findByLoteId(loteTest.getId());
        BigDecimal rendimientoPromedio = cosechasLote.stream()
                .map(Cosecha::getRendimiento)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(cosechasLote.size()), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal cantidadTotalCosechada = cosechasLote.stream()
                .map(Cosecha::getCantidadCosechada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(2, cosechasLote.size());
        assertEquals(new BigDecimal("3290.00"), rendimientoPromedio);
        assertEquals(new BigDecimal("167.75"), cantidadTotalCosechada);
    }

    @Test
    void testReporteCostosPorLote() {
        // Arrange
        Labor siembra = crearLabor(Labor.TipoLabor.SIEMBRA, new BigDecimal("1250.00"));
        Labor fertilizacion = crearLabor(Labor.TipoLabor.FERTILIZACION, new BigDecimal("850.00"));
        Labor riego = crearLabor(Labor.TipoLabor.RIEGO, new BigDecimal("300.00"));
        Labor cosecha = crearLabor(Labor.TipoLabor.COSECHA, new BigDecimal("2100.00"));

        laborRepository.save(siembra);
        laborRepository.save(fertilizacion);
        laborRepository.save(riego);
        laborRepository.save(cosecha);
        entityManager.flush();

        // Act
        List<Labor> laboresLote = laborRepository.findByLoteId(loteTest.getId());
        BigDecimal costoTotalLabores = laboresLote.stream()
                .map(Labor::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Labor.TipoLabor, BigDecimal> costosPorTipo = laboresLote.stream()
                .collect(Collectors.groupingBy(
                        Labor::getTipoLabor,
                        Collectors.reducing(BigDecimal.ZERO, Labor::getCostoTotal, BigDecimal::add)
                ));

        // Assert
        assertEquals(4, laboresLote.size());
        assertEquals(new BigDecimal("4500.00"), costoTotalLabores);
        assertEquals(new BigDecimal("1250.00"), costosPorTipo.get(Labor.TipoLabor.SIEMBRA));
        assertEquals(new BigDecimal("850.00"), costosPorTipo.get(Labor.TipoLabor.FERTILIZACION));
        assertEquals(new BigDecimal("300.00"), costosPorTipo.get(Labor.TipoLabor.RIEGO));
        assertEquals(new BigDecimal("2100.00"), costosPorTipo.get(Labor.TipoLabor.COSECHA));
    }

    @Test
    void testReporteRentabilidad() {
        // Arrange
        // Crear ingresos
        Ingreso ingreso1 = crearIngreso("Venta Soja 2024", "VENTA_CULTIVO", LocalDate.now().minusDays(30), 
                new BigDecimal("45000.00"), "toneladas", new BigDecimal("100.00"));
        Ingreso ingreso2 = crearIngreso("Venta Soja 2023", "VENTA_CULTIVO", LocalDate.now().minusDays(60), 
                new BigDecimal("36000.00"), "toneladas", new BigDecimal("80.00"));

        // Crear egresos
        Egreso egreso1 = crearEgreso("Compra Semillas", "INSUMOS", LocalDate.now().minusDays(90), 
                new BigDecimal("15500.00"));
        Egreso egreso2 = crearEgreso("Compra Fertilizantes", "INSUMOS", LocalDate.now().minusDays(85), 
                new BigDecimal("4250.00"));
        Egreso egreso3 = crearEgreso("Mantenimiento Maquinaria", "MANTENIMIENTO", LocalDate.now().minusDays(80), 
                new BigDecimal("2500.00"));

        ingresoRepository.save(ingreso1);
        ingresoRepository.save(ingreso2);
        egresoRepository.save(egreso1);
        egresoRepository.save(egreso2);
        egresoRepository.save(egreso3);
        entityManager.flush();

        // Act
        List<Ingreso> ingresos = ingresoRepository.findByUsuarioId(usuarioTest.getId());
        List<Egreso> egresos = egresoRepository.findByUsuarioId(usuarioTest.getId());

        BigDecimal totalIngresos = ingresos.stream()
                .map(Ingreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEgresos = egresos.stream()
                .map(Egreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rentabilidad = totalIngresos.subtract(totalEgresos);
        BigDecimal margenRentabilidad = rentabilidad.divide(totalIngresos, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        // Assert
        assertEquals(2, ingresos.size());
        assertEquals(3, egresos.size());
        assertEquals(new BigDecimal("81000.00"), totalIngresos);
        assertEquals(new BigDecimal("22250.00"), totalEgresos);
        assertEquals(new BigDecimal("58750.00"), rentabilidad);
        assertEquals(new BigDecimal("72.53"), margenRentabilidad);
    }

    @Test
    void testReporteInventarioInsumos() {
        // Arrange
        Insumo semilla = crearInsumo("Semilla Soja", "Semilla", new BigDecimal("15.50"), 
                new BigDecimal("1000.00"), new BigDecimal("100.00"));
        Insumo fertilizante = crearInsumo("Fertilizante Urea", "Fertilizante", new BigDecimal("0.85"), 
                new BigDecimal("5000.00"), new BigDecimal("500.00"));
        Insumo herbicida = crearInsumo("Herbicida Glifosato", "Agroquímico", new BigDecimal("8.50"), 
                new BigDecimal("200.00"), new BigDecimal("50.00"));

        insumoRepository.save(semilla);
        insumoRepository.save(fertilizante);
        insumoRepository.save(herbicida);
        entityManager.flush();

        // Act
        List<Insumo> insumos = insumoRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId());
        
        BigDecimal valorTotalInventario = insumos.stream()
                .map(insumo -> insumo.getPrecioUnitario().multiply(insumo.getStockDisponible()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> valorPorTipo = insumos.stream()
                .collect(Collectors.groupingBy(
                        insumo -> insumo.getTipo().name(),
                        Collectors.reducing(BigDecimal.ZERO, 
                                insumo -> insumo.getPrecioUnitario().multiply(insumo.getStockDisponible()), 
                                BigDecimal::add)
                ));

        List<Insumo> insumosStockBajo = insumos.stream()
                .filter(insumo -> insumo.getStockDisponible().compareTo(insumo.getStockMinimo()) < 0)
                .toList();

        // Assert
        assertEquals(3, insumos.size());
        assertEquals(new BigDecimal("22000.00"), valorTotalInventario);
        assertEquals(new BigDecimal("15500.00"), valorPorTipo.get("Semilla"));
        assertEquals(new BigDecimal("4250.00"), valorPorTipo.get("Fertilizante"));
        assertEquals(new BigDecimal("1700.00"), valorPorTipo.get("Agroquímico"));
        assertEquals(0, insumosStockBajo.size()); // Todos tienen stock suficiente
    }

    @Test
    void testReporteMaquinaria() {
        // Arrange
        Maquinaria tractor = crearMaquinaria("Tractor John Deere", "Tractor", 
                Maquinaria.EstadoMaquinaria.DISPONIBLE, new BigDecimal("25.00"));
        Maquinaria sembradora = crearMaquinaria("Sembradora Neumática", "Sembradora", 
                Maquinaria.EstadoMaquinaria.EN_USO, new BigDecimal("15.00"));
        Maquinaria cosechadora = crearMaquinaria("Cosechadora", "Cosechadora", 
                Maquinaria.EstadoMaquinaria.EN_MANTENIMIENTO, new BigDecimal("35.00"));

        maquinariaRepository.save(tractor);
        maquinariaRepository.save(sembradora);
        maquinariaRepository.save(cosechadora);
        entityManager.flush();

        // Act
        List<Maquinaria> maquinaria = maquinariaRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId());
        
        Map<Maquinaria.EstadoMaquinaria, Long> maquinariaPorEstado = maquinaria.stream()
                .collect(Collectors.groupingBy(Maquinaria::getEstado, Collectors.counting()));

        Map<String, Long> maquinariaPorTipo = maquinaria.stream()
                .collect(Collectors.groupingBy(Maquinaria::getTipo, Collectors.counting()));

        BigDecimal costoPromedioPorHora = maquinaria.stream()
                .map(Maquinaria::getCostoPorHora)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(maquinaria.size()), 2, BigDecimal.ROUND_HALF_UP);

        // Assert
        assertEquals(3, maquinaria.size());
        assertEquals(1L, maquinariaPorEstado.get(Maquinaria.EstadoMaquinaria.DISPONIBLE));
        assertEquals(1L, maquinariaPorEstado.get(Maquinaria.EstadoMaquinaria.EN_USO));
        assertEquals(1L, maquinariaPorEstado.get(Maquinaria.EstadoMaquinaria.EN_MANTENIMIENTO));
        assertEquals(1L, maquinariaPorTipo.get("Tractor"));
        assertEquals(1L, maquinariaPorTipo.get("Sembradora"));
        assertEquals(1L, maquinariaPorTipo.get("Cosechadora"));
        assertEquals(new BigDecimal("25.00"), costoPromedioPorHora);
    }

    @Test
    void testReporteProductividadPorCampo() {
        // Arrange
        // Crear segundo lote en el mismo campo
        Plot lote2 = new Plot();
        lote2.setNombre("Lote Test 2");
        lote2.setDescripcion("Segundo lote de prueba");
        lote2.setAreaHectareas(new BigDecimal("30.00"));
        lote2.setEstado(EstadoLote.DISPONIBLE);
        lote2.setTipoSuelo("Franco");
        lote2.setCampo(campoTest);
        lote2.setUsuario(usuarioTest);
        lote2.setActivo(true);
        plotRepository.save(lote2);

        // Crear cosechas para ambos lotes
        Cosecha cosechaLote1 = crearCosecha(loteTest, cultivoTest, LocalDate.now().minusDays(30), 
                new BigDecimal("89.25"), "toneladas", new BigDecimal("3500.00"), new BigDecimal("450.00"));
        Cosecha cosechaLote2 = crearCosecha(lote2, cultivoTest, LocalDate.now().minusDays(30), 
                new BigDecimal("108.00"), "toneladas", new BigDecimal("3600.00"), new BigDecimal("450.00"));

        cosechaRepository.save(cosechaLote1);
        cosechaRepository.save(cosechaLote2);
        entityManager.flush();

        // Act
        List<Plot> lotesCampo = plotRepository.findByCampoId(campoTest.getId());
        BigDecimal areaTotalCampo = lotesCampo.stream()
                .map(Plot::getAreaHectareas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Cosecha> cosechasCampo = cosechasCampo.stream()
                .filter(cosecha -> lotesCampo.stream()
                        .anyMatch(lote -> lote.getId().equals(cosecha.getLote().getId())))
                .toList();

        BigDecimal cantidadTotalCosechada = cosechasCampo.stream()
                .map(Cosecha::getCantidadCosechada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal productividadCampo = cantidadTotalCosechada.divide(areaTotalCampo, 2, BigDecimal.ROUND_HALF_UP);

        // Assert
        assertEquals(2, lotesCampo.size());
        assertEquals(new BigDecimal("55.50"), areaTotalCampo);
        assertEquals(2, cosechasCampo.size());
        assertEquals(new BigDecimal("197.25"), cantidadTotalCosechada);
        assertEquals(new BigDecimal("3.55"), productividadCampo); // toneladas por hectárea
    }

    @Test
    void testExportarDatosEnFormatoCSV() {
        // Arrange
        Cosecha cosecha1 = crearCosecha(loteTest, cultivoTest, LocalDate.now().minusDays(30), 
                new BigDecimal("89.25"), "toneladas", new BigDecimal("3500.00"), new BigDecimal("450.00"));
        Cosecha cosecha2 = crearCosecha(loteTest, cultivoTest, LocalDate.now().minusDays(60), 
                new BigDecimal("78.50"), "toneladas", new BigDecimal("3080.00"), new BigDecimal("420.00"));

        cosechaRepository.save(cosecha1);
        cosechaRepository.save(cosecha2);
        entityManager.flush();

        // Act
        List<Cosecha> cosechas = cosechaRepository.findByUsuarioId(usuarioTest.getId());
        
        // Simular exportación a CSV
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Fecha Cosecha,Lote,Cultivo,Cantidad,Rendimiento,Precio,Valor Total\n");
        
        for (Cosecha cosecha : cosechas) {
            csvContent.append(cosecha.getFechaCosecha()).append(",")
                    .append(cosecha.getLote().getNombre()).append(",")
                    .append(cosecha.getCultivo().getNombre()).append(",")
                    .append(cosecha.getCantidadCosechada()).append(",")
                    .append(cosecha.getRendimiento()).append(",")
                    .append(cosecha.getPrecioPorUnidad()).append(",")
                    .append(cosecha.getValorTotal()).append("\n");
        }

        // Assert
        assertNotNull(csvContent.toString());
        assertTrue(csvContent.toString().contains("Fecha Cosecha,Lote,Cultivo,Cantidad,Rendimiento,Precio,Valor Total"));
        assertTrue(csvContent.toString().contains("89.25"));
        assertTrue(csvContent.toString().contains("78.50"));
    }

    private Cosecha crearCosecha(Plot lote, Cultivo cultivo, LocalDate fechaCosecha, 
                                BigDecimal cantidad, String unidad, BigDecimal rendimiento, BigDecimal precio) {
        Cosecha cosecha = new Cosecha();
        cosecha.setLote(lote);
        cosecha.setCultivo(cultivo);
        cosecha.setFechaCosecha(fechaCosecha);
        cosecha.setCantidadCosechada(cantidad);
        cosecha.setUnidadMedida(unidad);
        cosecha.setRendimiento(rendimiento);
        cosecha.setPrecioPorUnidad(precio);
        cosecha.setValorTotal(cantidad.multiply(precio));
        cosecha.setObservaciones("Cosecha de prueba");
        cosecha.setUsuario(usuarioTest);
        cosecha.setActivo(true);
        return cosecha;
    }

    private Labor crearLabor(Labor.TipoLabor tipo, BigDecimal costo) {
        Labor labor = new Labor();
        labor.setTipoLabor(tipo);
        labor.setFechaInicio(LocalDate.now());
        labor.setDescripcion("Labor de prueba: " + tipo);
        labor.setCostoTotal(costo);
        labor.setEstado(Labor.EstadoLabor.COMPLETADA);
        labor.setObservaciones("Observaciones de prueba");
        labor.setResponsable("Responsable Test");
        labor.setLote(loteTest);
        labor.setUsuario(usuarioTest);
        labor.setActivo(true);
        return labor;
    }

    private Ingreso crearIngreso(String concepto, String tipo, LocalDate fecha, 
                                BigDecimal monto, String unidad, BigDecimal cantidad) {
        Ingreso ingreso = new Ingreso();
        ingreso.setConcepto(concepto);
        ingreso.setDescripcion("Ingreso de prueba");
        ingreso.setTipoIngreso(Ingreso.TipoIngreso.valueOf(tipo));
        ingreso.setFechaIngreso(fecha);
        ingreso.setMonto(monto);
        ingreso.setUnidadMedida(unidad);
        ingreso.setCantidad(cantidad);
        ingreso.setClienteComprador("Cliente Test");
        ingreso.setEstado(Ingreso.EstadoIngreso.CONFIRMADO);
        ingreso.setLote(loteTest);
        ingreso.setUsuario(usuarioTest);
        return ingreso;
    }

    private Egreso crearEgreso(String concepto, String tipo, LocalDate fecha, BigDecimal monto) {
        Egreso egreso = new Egreso();
        egreso.setConcepto(concepto);
        egreso.setDescripcion("Egreso de prueba");
        egreso.setTipoEgreso(Egreso.TipoEgreso.valueOf(tipo));
        egreso.setFechaEgreso(fecha);
        egreso.setMonto(monto);
        egreso.setProveedor("Proveedor Test");
        egreso.setEstado(Egreso.EstadoEgreso.PAGADO);
        egreso.setLote(loteTest);
        egreso.setUsuario(usuarioTest);
        return egreso;
    }

    private Insumo crearInsumo(String nombre, String tipo, BigDecimal precio, 
                               BigDecimal stock, BigDecimal stockMinimo) {
        Insumo insumo = new Insumo();
        insumo.setNombre(nombre);
        insumo.setTipo(Insumo.TipoInsumo.valueOf(tipo.toUpperCase()));
        insumo.setDescripcion("Descripción de " + nombre);
        insumo.setUnidadMedida("kg");
        insumo.setPrecioUnitario(precio);
        insumo.setStockDisponible(stock);
        insumo.setStockMinimo(stockMinimo);
        insumo.setProveedor("Proveedor Test");
        insumo.setUsuario(usuarioTest);
        insumo.setActivo(true);
        return insumo;
    }

    private Maquinaria crearMaquinaria(String nombre, String tipo, 
                                      Maquinaria.EstadoMaquinaria estado, BigDecimal costoPorHora) {
        Maquinaria maquinaria = new Maquinaria();
        maquinaria.setNombre(nombre);
        maquinaria.setTipo(tipo);
        maquinaria.setMarca("Marca Test");
        maquinaria.setModelo("Modelo Test");
        maquinaria.setAño(2020);
        maquinaria.setDescripcion("Descripción de " + nombre);
        maquinaria.setEstado(estado);
        maquinaria.setCostoPorHora(costoPorHora);
        maquinaria.setUsuario(usuarioTest);
        maquinaria.setActivo(true);
        return maquinaria;
    }
}
