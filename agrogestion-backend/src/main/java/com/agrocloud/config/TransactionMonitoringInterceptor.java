package com.agrocloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.annotation.PostConstruct;

/**
 * Interceptor para monitorear transacciones y detectar problemas de conexión
 */
@Component
public class TransactionMonitoringInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMonitoringInterceptor.class);

    @PostConstruct
    public void init() {
        logger.info("🔧 TransactionMonitoringInterceptor inicializado");
    }

    /**
     * Registra un callback para monitorear el final de transacciones
     */
    public static void registerTransactionCallback(String operation) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) {
                        logger.debug("✅ Transacción completada exitosamente para: {}", operation);
                    } else if (status == STATUS_ROLLED_BACK) {
                        logger.warn("⚠️ Transacción revertida para: {}", operation);
                    } else {
                        logger.warn("❓ Estado desconocido de transacción para: {} - Status: {}", operation, status);
                    }
                }

                @Override
                public void beforeCommit(boolean readOnly) {
                    logger.debug("🔄 Preparando commit para: {} (readOnly: {})", operation, readOnly);
                }
            });
        }
    }

    /**
     * Verifica el estado actual de las transacciones
     */
    public static void logTransactionStatus(String context) {
        boolean isActive = TransactionSynchronizationManager.isSynchronizationActive();
        boolean isActualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean isCurrentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        
        logger.debug("📊 Estado de transacciones en {}: Active={}, ActualActive={}, ReadOnly={}", 
                   context, isActive, isActualTransactionActive, isCurrentTransactionReadOnly);
    }
}
