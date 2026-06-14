package com.agrocloud.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Configuración de diagnóstico solo para tests: imprime URL del DataSource
 * y dialecto efectivo al arrancar el contexto.
 * No modifica código productivo.
 */
@Configuration
public class DiagnosticoDataSourceTestConfig {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticoDataSourceTestConfig.class);

    private final DataSource dataSource;
    private final Environment env;

    public DiagnosticoDataSourceTestConfig(DataSource dataSource, Environment env) {
        this.dataSource = dataSource;
        this.env = env;
    }

    @PostConstruct
    public void imprimirDiagnostico() {
        String url = env.getProperty("spring.datasource.url", "no-definida");
        String dialect = env.getProperty("spring.jpa.properties.hibernate.dialect", "no-definido");
        String ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", "no-definido");

        log.info("[DIAGNOSTICO TEST] spring.datasource.url = {}", url);
        log.info("[DIAGNOSTICO TEST] hibernate.dialect = {}", dialect);
        log.info("[DIAGNOSTICO TEST] spring.jpa.hibernate.ddl-auto = {}", ddlAuto);

        if (dataSource instanceof HikariDataSource hikari) {
            log.info("[DIAGNOSTICO TEST] DataSource efectivo (Hikari) URL = {}", hikari.getJdbcUrl());
        }
    }
}
