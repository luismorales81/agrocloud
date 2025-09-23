package com.agrocloud;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase base optimizada para tests de entidades y servicios.
 * Usa @SpringBootTest con configuración mínima para mejor rendimiento.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class BaseEntityTest {

    /**
     * Configuración inicial optimizada para cada test de entidad.
     * Se ejecuta antes de cada método de prueba.
     */
    @BeforeEach
    void setUp() {
        // Configuración común para todos los tests de entidades
        // Las transacciones se revierten automáticamente después de cada test
    }
}
