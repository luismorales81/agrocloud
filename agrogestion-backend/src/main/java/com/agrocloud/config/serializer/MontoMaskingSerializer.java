package com.agrocloud.config.serializer;

import com.agrocloud.service.EnmascaramientoDatosService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Serializer personalizado para enmascarar montos financieros en respuestas JSON
 * Solo muestra decimales, oculta el monto completo
 */
@Component
public class MontoMaskingSerializer extends JsonSerializer<BigDecimal> implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        MontoMaskingSerializer.applicationContext = applicationContext;
    }

    private EnmascaramientoDatosService getEnmascaramientoService() {
        if (applicationContext == null) {
            // Si el contexto aún no está inicializado, crear instancia temporal
            return new com.agrocloud.service.EnmascaramientoDatosService();
        }
        return applicationContext.getBean(EnmascaramientoDatosService.class);
    }

    @Override
    public void serialize(BigDecimal monto, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (monto == null) {
            gen.writeNull();
        } else {
            try {
                EnmascaramientoDatosService service = getEnmascaramientoService();
                String montoEnmascarado = service.enmascararMonto(monto);
                gen.writeString(montoEnmascarado);
            } catch (Exception e) {
                // Si hay error, enmascarar de forma básica
                gen.writeString("******");
            }
        }
    }
}

