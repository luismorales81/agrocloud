package com.agrocloud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Configuración de Jackson para manejar la serialización de entidades Hibernate
 * y evitar errores con proxies lazy-loaded
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new Jackson2ObjectMapperBuilder()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        // Configurar módulo de Hibernate para manejar proxies lazy
        Hibernate5JakartaModule hibernateModule = new Hibernate5JakartaModule();
        hibernateModule.disable(Hibernate5JakartaModule.Feature.USE_TRANSIENT_ANNOTATION);
        hibernateModule.enable(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING);
        hibernateModule.enable(Hibernate5JakartaModule.Feature.REPLACE_PERSISTENT_COLLECTIONS);
        
        mapper.registerModule(hibernateModule);
        
        return mapper;
    }
}
