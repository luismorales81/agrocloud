package com.agrocloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Falla el arranque en perfiles de despliegue si faltan secretos obligatorios.
 */
@Component
@Profile({"prod", "railway", "railway-mysql"})
public class ValidacionSecretosArranque implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ValidacionSecretosArranque.class);

    private final Environment entorno;

    public ValidacionSecretosArranque(Environment entorno) {
        this.entorno = entorno;
    }

    @Override
    public void afterPropertiesSet() {
        exigir("JWT_SECRET", entorno.getProperty("jwt.secret"));
        exigir("DATABASE_PASSWORD", entorno.getProperty("spring.datasource.password"));
        exigir("CHAT_IA_ENCRYPTION_KEY", entorno.getProperty("chat.ia.encryption-key"));
        log.info("Validación de secretos de despliegue completada correctamente");
    }

    private void exigir(String nombreVariable, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Variable de entorno obligatoria no configurada en perfil de despliegue: " + nombreVariable);
        }
    }
}
