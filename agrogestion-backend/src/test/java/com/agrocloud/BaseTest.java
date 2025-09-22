package com.agrocloud;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase base para todas las pruebas del sistema AgroGestion.
 * Configura el entorno de pruebas con MySQL y transacciones.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class BaseTest {

    /**
     * Configuración inicial para cada test.
     * Se ejecuta antes de cada método de prueba.
     */
    @BeforeEach
    void setUp() {
        // Configuración común para todos los tests
        // Las transacciones se revierten automáticamente después de cada test
    }
}
