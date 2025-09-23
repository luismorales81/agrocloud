package com.agrocloud.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuración específica para tests unitarios.
 * Optimiza el rendimiento y reduce el tiempo de ejecución de las pruebas.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@TestConfiguration
@ActiveProfiles("test")
public class TestConfig {

    /**
     * Configuración optimizada para tests unitarios.
     * Desactiva componentes innecesarios para mejorar el rendimiento.
     */
    @Bean
    @Primary
    @Profile("test")
    public TestOptimizationConfig testOptimizationConfig() {
        return new TestOptimizationConfig();
    }

    /**
     * Clase de configuración para optimización de tests.
     */
    public static class TestOptimizationConfig {
        
        /**
         * Configuraciones optimizadas para tests:
         * - Desactiva logging innecesario
         * - Reduce timeouts
         * - Optimiza conexiones de BD
         */
        public TestOptimizationConfig() {
            // Configuraciones de optimización se aplican automáticamente
            // a través de application-test.properties
        }
    }
}
