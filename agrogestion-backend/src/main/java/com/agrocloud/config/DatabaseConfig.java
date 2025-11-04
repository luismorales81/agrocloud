package com.agrocloud.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de la base de datos y JPA
 * Usa configuración automática de Spring Boot
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.agrocloud.repository")
@EntityScan(basePackages = "com.agrocloud.model.entity")
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuración automática de Spring Boot
}
