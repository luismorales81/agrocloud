package com.agrocloud;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase base optimizada para tests unitarios de repositorios.
 * Usa @DataJpaTest para cargar solo el contexto JPA necesario.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class BaseUnitTest {

    /**
     * Configuración inicial optimizada para cada test unitario.
     * Se ejecuta antes de cada método de prueba.
     */
    @BeforeEach
    void setUp() {
        // Configuración común para todos los tests unitarios
        // Las transacciones se revierten automáticamente después de cada test
    }
}
