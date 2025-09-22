package com.agrocloud.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Monitor de salud de la base de datos para detectar conexiones colgadas
 */
@Component
public class DatabaseHealthMonitor implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthMonitor.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try {
            // Verificar conexión básica
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    logger.debug("✅ Conexión a BD válida");
                    
                    // Si es HikariCP, obtener métricas adicionales
                    if (dataSource instanceof HikariDataSource) {
                        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                        
                        int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
                        int idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
                        int totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
                        int threadsAwaitingConnection = hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
                        
                        logger.debug("📊 Pool de conexiones - Activas: {}, Inactivas: {}, Total: {}, Esperando: {}", 
                                   activeConnections, idleConnections, totalConnections, threadsAwaitingConnection);
                        
                        // Alertar si hay muchas conexiones esperando
                        if (threadsAwaitingConnection > 5) {
                            logger.warn("⚠️ Muchas conexiones esperando: {}", threadsAwaitingConnection);
                        }
                        
                        return Health.up()
                                .withDetail("database", "MySQL")
                                .withDetail("activeConnections", activeConnections)
                                .withDetail("idleConnections", idleConnections)
                                .withDetail("totalConnections", totalConnections)
                                .withDetail("threadsAwaitingConnection", threadsAwaitingConnection)
                                .build();
                    }
                    
                    return Health.up()
                            .withDetail("database", "MySQL")
                            .withDetail("status", "Connected")
                            .build();
                } else {
                    logger.error("❌ Conexión a BD inválida");
                    return Health.down()
                            .withDetail("database", "MySQL")
                            .withDetail("error", "Connection is not valid")
                            .build();
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error de conexión a BD: {}", e.getMessage());
            return Health.down()
                    .withDetail("database", "MySQL")
                    .withDetail("error", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado en monitor de BD: {}", e.getMessage());
            return Health.down()
                    .withDetail("database", "MySQL")
                    .withDetail("error", "Unexpected error: " + e.getMessage())
                    .build();
        }
    }
}
