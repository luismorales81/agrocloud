package com.agrocloud.entity;

import com.agrocloud.BaseTest;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.InsumoRepository;
import com.agrocloud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la entidad Insumo.
 * Prueba la gestión de insumos: altas, bajas, stock mínimo, alertas de stock bajo.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@DataJpaTest
class InsumosTest extends BaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private UserRepository userRepository;

    private User usuarioTest;

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
    }

    @Test
    void testCrearInsumo() {
        // Arrange
        Insumo insumo = new Insumo();
        insumo.setNombre("Semilla Soja DM 53i53");
        insumo.setTipo(Insumo.TipoInsumo.SEMILLA);
        insumo.setDescripcion("Semilla de soja de primera calidad");
        insumo.setUnidadMedida("kg");
        insumo.setPrecioUnitario(new BigDecimal("15.50"));
        insumo.setStockDisponible(new BigDecimal("1000.00"));
        insumo.setStockMinimo(new BigDecimal("100.00"));
        insumo.setProveedor("Semillas del Norte");
        insumo.setUsuario(usuarioTest);
        insumo.setActivo(true);

        // Act
        Insumo insumoGuardado = insumoRepository.save(insumo);
        entityManager.flush();

        // Assert
        assertNotNull(insumoGuardado.getId());
        assertEquals("Semilla Soja DM 53i53", insumoGuardado.getNombre());
        assertEquals("Semilla", insumoGuardado.getTipo());
        assertEquals("Semilla de soja de primera calidad", insumoGuardado.getDescripcion());
        assertEquals("kg", insumoGuardado.getUnidadMedida());
        assertEquals(new BigDecimal("15.50"), insumoGuardado.getPrecioUnitario());
        assertEquals(new BigDecimal("1000.00"), insumoGuardado.getStockDisponible());
        assertEquals(new BigDecimal("100.00"), insumoGuardado.getStockMinimo());
        assertEquals("Semillas del Norte", insumoGuardado.getProveedor());
        assertTrue(insumoGuardado.getActivo());
        assertEquals(usuarioTest.getId(), insumoGuardado.getUsuario().getId());
    }

    @Test
    void testEditarInsumo() {
        // Arrange
        Insumo insumo = crearInsumo("Fertilizante Urea", "Fertilizante", "kg", 
                new BigDecimal("0.85"), new BigDecimal("5000.00"), new BigDecimal("500.00"));
        Insumo insumoGuardado = insumoRepository.save(insumo);
        entityManager.flush();

        // Act
        insumoGuardado.setPrecioUnitario(new BigDecimal("0.90"));
        insumoGuardado.setStockDisponible(new BigDecimal("4500.00"));
        insumoGuardado.setStockMinimo(new BigDecimal("600.00"));
        insumoGuardado.setProveedor("Fertilizantes SA Actualizado");
        Insumo insumoModificado = insumoRepository.save(insumoGuardado);
        entityManager.flush();

        // Assert
        assertEquals(new BigDecimal("0.90"), insumoModificado.getPrecioUnitario());
        assertEquals(new BigDecimal("4500.00"), insumoModificado.getStockDisponible());
        assertEquals(new BigDecimal("600.00"), insumoModificado.getStockMinimo());
        assertEquals("Fertilizantes SA Actualizado", insumoModificado.getProveedor());
    }

    @Test
    void testConsultarInsumoPorId() {
        // Arrange
        Insumo insumo = crearInsumo("Herbicida Glifosato", "Agroquímico", "litro", 
                new BigDecimal("8.50"), new BigDecimal("200.00"), new BigDecimal("50.00"));
        Insumo insumoGuardado = insumoRepository.save(insumo);
        entityManager.flush();

        // Act
        Optional<Insumo> insumoEncontrado = insumoRepository.findById(insumoGuardado.getId());

        // Assert
        assertTrue(insumoEncontrado.isPresent());
        assertEquals(insumoGuardado.getId(), insumoEncontrado.get().getId());
        assertEquals("Herbicida Glifosato", insumoEncontrado.get().getNombre());
    }

    @Test
    void testConsultarInsumosPorTipo() {
        // Arrange
        Insumo semilla = crearInsumo("Semilla Maíz", "Semilla", "kg", 
                new BigDecimal("12.00"), new BigDecimal("500.00"), new BigDecimal("50.00"));
        Insumo fertilizante = crearInsumo("Fertilizante NPK", "Fertilizante", "kg", 
                new BigDecimal("1.20"), new BigDecimal("1000.00"), new BigDecimal("100.00"));
        Insumo herbicida = crearInsumo("Herbicida 2,4-D", "Agroquímico", "litro", 
                new BigDecimal("6.50"), new BigDecimal("150.00"), new BigDecimal("25.00"));

        insumoRepository.save(semilla);
        insumoRepository.save(fertilizante);
        insumoRepository.save(herbicida);
        entityManager.flush();

        // Act
        List<Insumo> semillas = insumoRepository.findByTipo("Semilla");
        List<Insumo> fertilizantes = insumoRepository.findByTipo("Fertilizante");
        List<Insumo> agroquimicos = insumoRepository.findByTipo("Agroquímico");

        // Assert
        assertEquals(1, semillas.size());
        assertEquals("Semilla Maíz", semillas.get(0).getNombre());

        assertEquals(1, fertilizantes.size());
        assertEquals("Fertilizante NPK", fertilizantes.get(0).getNombre());

        assertEquals(1, agroquimicos.size());
        assertEquals("Herbicida 2,4-D", agroquimicos.get(0).getNombre());
    }

    @Test
    void testConsultarInsumosPorProveedor() {
        // Arrange
        Insumo insumo1 = crearInsumo("Semilla Soja", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("1000.00"), new BigDecimal("100.00"));
        insumo1.setProveedor("Semillas del Norte");

        Insumo insumo2 = crearInsumo("Semilla Maíz", "Semilla", "kg", 
                new BigDecimal("12.00"), new BigDecimal("800.00"), new BigDecimal("80.00"));
        insumo2.setProveedor("Semillas del Norte");

        Insumo insumo3 = crearInsumo("Fertilizante Urea", "Fertilizante", "kg", 
                new BigDecimal("0.85"), new BigDecimal("2000.00"), new BigDecimal("200.00"));
        insumo3.setProveedor("Fertilizantes SA");

        insumoRepository.save(insumo1);
        insumoRepository.save(insumo2);
        insumoRepository.save(insumo3);
        entityManager.flush();

        // Act
        List<Insumo> insumosProveedor1 = insumoRepository.findByProveedor("Semillas del Norte");
        List<Insumo> insumosProveedor2 = insumoRepository.findByProveedor("Fertilizantes SA");

        // Assert
        assertEquals(2, insumosProveedor1.size());
        assertTrue(insumosProveedor1.stream().allMatch(i -> i.getProveedor().equals("Semillas del Norte")));

        assertEquals(1, insumosProveedor2.size());
        assertEquals("Fertilizante Urea", insumosProveedor2.get(0).getNombre());
    }

    @Test
    void testAlertasStockBajo() {
        // Arrange
        Insumo insumoStockBajo = crearInsumo("Semilla Stock Bajo", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("50.00"), new BigDecimal("100.00")); // Stock < Mínimo

        Insumo insumoStockNormal = crearInsumo("Semilla Stock Normal", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("500.00"), new BigDecimal("100.00")); // Stock > Mínimo

        insumoRepository.save(insumoStockBajo);
        insumoRepository.save(insumoStockNormal);
        entityManager.flush();

        // Act
        List<Insumo> insumosStockBajo = insumoRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId())
                .stream()
                .filter(insumo -> insumo.getStockDisponible().compareTo(insumo.getStockMinimo()) < 0)
                .toList();

        List<Insumo> insumosStockNormal = insumoRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId())
                .stream()
                .filter(insumo -> insumo.getStockDisponible().compareTo(insumo.getStockMinimo()) >= 0)
                .toList();

        // Assert
        assertEquals(1, insumosStockBajo.size());
        assertEquals("Semilla Stock Bajo", insumosStockBajo.get(0).getNombre());
        assertTrue(insumosStockBajo.get(0).getStockDisponible().compareTo(insumosStockBajo.get(0).getStockMinimo()) < 0);

        assertEquals(1, insumosStockNormal.size());
        assertEquals("Semilla Stock Normal", insumosStockNormal.get(0).getNombre());
        assertTrue(insumosStockNormal.get(0).getStockDisponible().compareTo(insumosStockNormal.get(0).getStockMinimo()) >= 0);
    }

    @Test
    void testActualizarStock() {
        // Arrange
        Insumo insumo = crearInsumo("Semilla Test", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("1000.00"), new BigDecimal("100.00"));
        Insumo insumoGuardado = insumoRepository.save(insumo);
        entityManager.flush();

        // Act - Simular consumo de insumo
        insumoGuardado.setStockDisponible(insumoGuardado.getStockDisponible().subtract(new BigDecimal("50.00")));
        Insumo insumoActualizado = insumoRepository.save(insumoGuardado);
        entityManager.flush();

        // Assert
        assertEquals(new BigDecimal("950.00"), insumoActualizado.getStockDisponible());
    }

    @Test
    void testReponerStock() {
        // Arrange
        Insumo insumo = crearInsumo("Semilla Test", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("50.00"), new BigDecimal("100.00"));
        Insumo insumoGuardado = insumoRepository.save(insumo);
        entityManager.flush();

        // Act - Simular reposición de stock
        insumoGuardado.setStockDisponible(insumoGuardado.getStockDisponible().add(new BigDecimal("500.00")));
        Insumo insumoRepuesto = insumoRepository.save(insumoGuardado);
        entityManager.flush();

        // Assert
        assertEquals(new BigDecimal("550.00"), insumoRepuesto.getStockDisponible());
    }

    @Test
    void testCalcularValorInventario() {
        // Arrange
        Insumo insumo1 = crearInsumo("Semilla 1", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("100.00"), new BigDecimal("10.00"));
        Insumo insumo2 = crearInsumo("Fertilizante 1", "Fertilizante", "kg", 
                new BigDecimal("1.00"), new BigDecimal("500.00"), new BigDecimal("50.00"));

        insumoRepository.save(insumo1);
        insumoRepository.save(insumo2);
        entityManager.flush();

        // Act
        List<Insumo> insumos = insumoRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId());
        BigDecimal valorTotal = insumos.stream()
                .map(insumo -> insumo.getPrecioUnitario().multiply(insumo.getStockDisponible()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        BigDecimal valorEsperado = new BigDecimal("15.00").multiply(new BigDecimal("100.00"))
                .add(new BigDecimal("1.00").multiply(new BigDecimal("500.00")));
        assertEquals(valorEsperado, valorTotal);
    }

    @Test
    void testBuscarInsumosPorNombre() {
        // Arrange
        Insumo insumo1 = crearInsumo("Semilla Soja DM 53i53", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("1000.00"), new BigDecimal("100.00"));
        Insumo insumo2 = crearInsumo("Semilla Maíz DK 72-10", "Semilla", "kg", 
                new BigDecimal("12.00"), new BigDecimal("800.00"), new BigDecimal("80.00"));
        Insumo insumo3 = crearInsumo("Fertilizante Urea", "Fertilizante", "kg", 
                new BigDecimal("0.85"), new BigDecimal("2000.00"), new BigDecimal("200.00"));

        insumoRepository.save(insumo1);
        insumoRepository.save(insumo2);
        insumoRepository.save(insumo3);
        entityManager.flush();

        // Act
        List<Insumo> insumosSemilla = insumoRepository.findByNombreContainingIgnoreCase("Semilla");
        List<Insumo> insumosSoja = insumoRepository.findByNombreContainingIgnoreCase("Soja");

        // Assert
        assertEquals(2, insumosSemilla.size());
        assertTrue(insumosSemilla.stream().anyMatch(i -> i.getNombre().contains("Soja")));
        assertTrue(insumosSemilla.stream().anyMatch(i -> i.getNombre().contains("Maíz")));

        assertEquals(1, insumosSoja.size());
        assertEquals("Semilla Soja DM 53i53", insumosSoja.get(0).getNombre());
    }

    @Test
    void testConsultarInsumosActivos() {
        // Arrange
        Insumo insumoActivo = crearInsumo("Insumo Activo", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("1000.00"), new BigDecimal("100.00"));
        insumoActivo.setActivo(true);

        Insumo insumoInactivo = crearInsumo("Insumo Inactivo", "Semilla", "kg", 
                new BigDecimal("15.00"), new BigDecimal("1000.00"), new BigDecimal("100.00"));
        insumoInactivo.setActivo(false);

        insumoRepository.save(insumoActivo);
        insumoRepository.save(insumoInactivo);
        entityManager.flush();

        // Act
        List<Insumo> insumosActivos = insumoRepository.findByUsuarioIdAndActivoTrue(usuarioTest.getId());

        // Assert
        assertEquals(1, insumosActivos.size());
        assertEquals("Insumo Activo", insumosActivos.get(0).getNombre());
        assertTrue(insumosActivos.get(0).getActivo());
    }

    @Test
    void testValidarDatosObligatorios() {
        // Arrange
        Insumo insumoSinNombre = new Insumo();
        insumoSinNombre.setTipo(Insumo.TipoInsumo.SEMILLA);
        insumoSinNombre.setUnidadMedida("kg");
        insumoSinNombre.setPrecioUnitario(new BigDecimal("15.00"));
        insumoSinNombre.setStockDisponible(new BigDecimal("1000.00"));
        insumoSinNombre.setUsuario(usuarioTest);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            insumoRepository.save(insumoSinNombre);
            entityManager.flush();
        });
    }

    private Insumo crearInsumo(String nombre, String tipo, String unidadMedida, 
                               BigDecimal precioUnitario, BigDecimal stockDisponible, BigDecimal stockMinimo) {
        Insumo insumo = new Insumo();
        insumo.setNombre(nombre);
        insumo.setTipo(Insumo.TipoInsumo.valueOf(tipo.toUpperCase()));
        insumo.setDescripcion("Descripción de " + nombre);
        insumo.setUnidadMedida(unidadMedida);
        insumo.setPrecioUnitario(precioUnitario);
        insumo.setStockDisponible(stockDisponible);
        insumo.setStockMinimo(stockMinimo);
        insumo.setProveedor("Proveedor Test");
        insumo.setUsuario(usuarioTest);
        insumo.setActivo(true);
        return insumo;
    }
}
