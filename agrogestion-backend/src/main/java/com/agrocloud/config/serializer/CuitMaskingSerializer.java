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

/**
 * Serializer personalizado para enmascarar CUIT en respuestas JSON
 */
@Component
public class CuitMaskingSerializer extends JsonSerializer<String> implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        CuitMaskingSerializer.applicationContext = applicationContext;
    }

    private EnmascaramientoDatosService getEnmascaramientoService() {
        if (applicationContext == null) {
            // Si el contexto aún no está inicializado, crear instancia temporal
            return new com.agrocloud.service.EnmascaramientoDatosService();
        }
        return applicationContext.getBean(EnmascaramientoDatosService.class);
    }

    @Override
    public void serialize(String cuit, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (cuit == null) {
            gen.writeNull();
        } else {
            try {
                EnmascaramientoDatosService service = getEnmascaramientoService();
                String cuitEnmascarado = service.enmascararCuit(cuit);
                gen.writeString(cuitEnmascarado);
            } catch (Exception e) {
                // Si hay error, enmascarar de forma básica
                gen.writeString("**-**-*");
            }
        }
    }
}

