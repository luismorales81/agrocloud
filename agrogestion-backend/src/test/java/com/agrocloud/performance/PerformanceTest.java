package com.agrocloud.performance;

import com.agrocloud.BaseEntityTest;
import com.agrocloud.model.entity.*;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de rendimiento para medir la eficiencia del sistema.
 * Compara tiempos de ejecución y optimizaciones implementadas.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
class PerformanceTest extends BaseEntityTest {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    private User usuarioTest;
    private Empresa empresaTest;

    @BeforeEach
    void setUp() {
        // Crear datos base para tests de rendimiento
        usuarioTest = crearUsuarioTest();
        empresaTest = crearEmpresaTest();
    }

    private User crearUsuarioTest() {
        User usuario = new User();
        usuario.setNombreUsuario("perfuser");
        usuario.setEmail("perf@test.com");
        usuario.setPassword("password123");
        usuario.setNombre("Usuario");
        usuario.setApellido("Performance");
        usuario.setActivo(true);
        return userRepository.save(usuario);
    }

    private Empresa crearEmpresaTest() {
        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa Performance");
        empresa.setCuit("20-98765432-1");
        empresa.setEmailContacto("perf@empresa.com");
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        empresa.setActivo(true);
        empresa.setCreadoPor(usuarioTest);
        return empresaRepository.save(empresa);
    }

    @Test
    void testRendimientoCreacionMasivaCampos() {
        // Arrange
        int cantidadCampos = 100;
        List<Field> campos = new ArrayList<>();
        
        // Crear campos en memoria
        for (int i = 0; i < cantidadCampos; i++) {
            Field campo = new Field();
            campo.setNombre("Campo Performance " + i);
            campo.setUbicacion("Zona " + i);
            campo.setAreaHectareas(new BigDecimal(100 + i));
            campo.setTipoSuelo("Franco");
            campo.setDescripcion("Campo de rendimiento " + i);
            campo.setUsuario(usuarioTest);
            campo.setEmpresa(empresaTest);
            campo.setActivo(true);
            campos.add(campo);
        }

        // Act - Medir tiempo de inserción
        long inicio = System.currentTimeMillis();
        
        List<Field> camposGuardados = fieldRepository.saveAll(campos);
        
        long fin = System.currentTimeMillis();
        long tiempoEjecucion = fin - inicio;

        // Assert
        assertEquals(cantidadCampos, camposGuardados.size());
        assertTrue(tiempoEjecucion < 5000, "La creación masiva debe tomar menos de 5 segundos");
        
        System.out.println("Tiempo de creación de " + cantidadCampos + " campos: " + tiempoEjecucion + "ms");
    }

    @Test
    void testRendimientoConsultasComplejas() {
        // Arrange - Crear datos de prueba
        crearDatosPrueba(50);

        // Act - Medir tiempo de consultas
        long inicio = System.currentTimeMillis();
        
        List<Field> camposUsuario = fieldRepository.findByUserId(usuarioTest.getId());
        List<Field> camposActivos = fieldRepository.findByUserIdAndActivoTrue(usuarioTest.getId());
        List<Field> camposPorNombre = fieldRepository.findByNombreContainingIgnoreCase("Performance");
        List<Field> camposPorTipoSuelo = fieldRepository.findByTipoSuelo("Franco");
        
        long fin = System.currentTimeMillis();
        long tiempoEjecucion = fin - inicio;

        // Assert
        assertFalse(camposUsuario.isEmpty());
        assertFalse(camposActivos.isEmpty());
        assertTrue(tiempoEjecucion < 1000, "Las consultas complejas deben tomar menos de 1 segundo");
        
        System.out.println("Tiempo de consultas complejas: " + tiempoEjecucion + "ms");
    }

    @Test
    void testRendimientoTransaccionesBatch() {
        // Arrange
        int cantidadLotes = 50;
        List<Plot> lotes = new ArrayList<>();
        
        // Crear un campo base
        Field campo = new Field();
        campo.setNombre("Campo Base Performance");
        campo.setUbicacion("Zona Base");
        campo.setAreaHectareas(new BigDecimal("1000.0"));
        campo.setTipoSuelo("Franco");
        campo.setDescripcion("Campo base para lotes");
        campo.setUsuario(usuarioTest);
        campo.setEmpresa(empresaTest);
        campo.setActivo(true);
        Field campoGuardado = fieldRepository.save(campo);

        // Crear lotes en memoria
        for (int i = 0; i < cantidadLotes; i++) {
            Plot lote = new Plot();
            lote.setNombre("Lote Performance " + i);
            lote.setDescripcion("Lote de rendimiento " + i);
            lote.setAreaHectareas(new BigDecimal(20 + i));
            lote.setTipoSuelo("Franco");
            lote.setCampo(campoGuardado);
            lote.setUsuario(usuarioTest);
            lote.setEmpresa(empresaTest);
            lote.setActivo(true);
            lotes.add(lote);
        }

        // Act - Medir tiempo de inserción batch
        long inicio = System.currentTimeMillis();
        
        List<Plot> lotesGuardados = plotRepository.saveAll(lotes);
        
        long fin = System.currentTimeMillis();
        long tiempoEjecucion = fin - inicio;

        // Assert
        assertEquals(cantidadLotes, lotesGuardados.size());
        assertTrue(tiempoEjecucion < 3000, "La inserción batch debe tomar menos de 3 segundos");
        
        System.out.println("Tiempo de inserción batch de " + cantidadLotes + " lotes: " + tiempoEjecucion + "ms");
    }

    @Test
    void testRendimientoConsultasConJoins() {
        // Arrange - Crear datos relacionados
        crearDatosRelacionados(20);

        // Act - Medir tiempo de consultas con joins
        long inicio = System.currentTimeMillis();
        
        List<Field> camposConLotes = fieldRepository.findAll();
        List<Plot> lotesConCampos = plotRepository.findAll();
        
        long fin = System.currentTimeMillis();
        long tiempoEjecucion = fin - inicio;

        // Assert
        assertFalse(camposConLotes.isEmpty());
        assertFalse(lotesConCampos.isEmpty());
        assertTrue(tiempoEjecucion < 2000, "Las consultas con joins deben tomar menos de 2 segundos");
        
        System.out.println("Tiempo de consultas con joins: " + tiempoEjecucion + "ms");
    }

    @Test
    void testRendimientoMemoria() {
        // Arrange
        int cantidadInsumos = 200;
        List<Insumo> insumos = new ArrayList<>();

        // Act - Medir uso de memoria
        Runtime runtime = Runtime.getRuntime();
        long memoriaInicial = runtime.totalMemory() - runtime.freeMemory();
        
        for (int i = 0; i < cantidadInsumos; i++) {
            Insumo insumo = new Insumo();
            insumo.setNombre("Insumo Performance " + i);
            insumo.setTipo(Insumo.TipoInsumo.FERTILIZANTE);
            insumo.setDescripcion("Insumo de rendimiento " + i);
            insumo.setPrecioUnitario(new BigDecimal(100 + i));
            insumo.setStockActual(new BigDecimal(1000 + i));
            insumo.setStockMinimo(new BigDecimal(100));
            insumo.setUnidadMedida("kg");
            insumo.setUsuario(usuarioTest);
            insumo.setEmpresa(empresaTest);
            insumo.setActivo(true);
            insumos.add(insumo);
        }
        
        insumoRepository.saveAll(insumos);
        
        long memoriaFinal = runtime.totalMemory() - runtime.freeMemory();
        long memoriaUsada = memoriaFinal - memoriaInicial;

        // Assert
        assertTrue(memoriaUsada < 50 * 1024 * 1024, "El uso de memoria debe ser menor a 50MB");
        
        System.out.println("Memoria usada para " + cantidadInsumos + " insumos: " + (memoriaUsada / 1024 / 1024) + "MB");
    }

    private void crearDatosPrueba(int cantidad) {
        List<Field> campos = new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            Field campo = new Field();
            campo.setNombre("Campo Performance " + i);
            campo.setUbicacion("Zona " + i);
            campo.setAreaHectareas(new BigDecimal(100 + i));
            campo.setTipoSuelo("Franco");
            campo.setDescripcion("Campo de rendimiento " + i);
            campo.setUsuario(usuarioTest);
            campo.setEmpresa(empresaTest);
            campo.setActivo(true);
            campos.add(campo);
        }
        
        fieldRepository.saveAll(campos);
    }

    private void crearDatosRelacionados(int cantidad) {
        List<Field> campos = new ArrayList<>();
        List<Plot> lotes = new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            // Crear campo
            Field campo = new Field();
            campo.setNombre("Campo Relacionado " + i);
            campo.setUbicacion("Zona " + i);
            campo.setAreaHectareas(new BigDecimal(100 + i));
            campo.setTipoSuelo("Franco");
            campo.setDescripcion("Campo relacionado " + i);
            campo.setUsuario(usuarioTest);
            campo.setEmpresa(empresaTest);
            campo.setActivo(true);
            campos.add(campo);
        }
        
        List<Field> camposGuardados = fieldRepository.saveAll(campos);
        
        // Crear lotes para cada campo
        for (Field campo : camposGuardados) {
            Plot lote = new Plot();
            lote.setNombre("Lote de " + campo.getNombre());
            lote.setDescripcion("Lote relacionado");
            lote.setAreaHectareas(new BigDecimal(20));
            lote.setTipoSuelo("Franco");
            lote.setCampo(campo);
            lote.setUsuario(usuarioTest);
            lote.setEmpresa(empresaTest);
            lote.setActivo(true);
            lotes.add(lote);
        }
        
        plotRepository.saveAll(lotes);
    }
}
