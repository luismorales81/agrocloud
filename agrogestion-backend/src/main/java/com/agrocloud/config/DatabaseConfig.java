package com.agrocloud.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración JPA multi-módulo (legacy, core, cultivos, porcinos, avícola, trazabilidad, feedlot, lechería, chat IA).
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.agrocloud")
@EntityScan(basePackages = {
        "com.agrocloud.core.domain",
        "com.agrocloud.core.inventory.domain",
        "com.agrocloud.cultivos.domain",
        "com.agrocloud.porcinos.domain",
        "com.agrocloud.porcinos.model.entity",
        "com.agrocloud.avicola",
        "com.agrocloud.trazabilidad",
        "com.agrocloud.feedlot.model.entity",
        "com.agrocloud.lecheria.model.entity",
        "com.agrocloud.chatia.domain"
})
@EnableTransactionManagement
public class DatabaseConfig {
}
