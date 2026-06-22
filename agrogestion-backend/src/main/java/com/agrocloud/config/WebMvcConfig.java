package com.agrocloud.config;

import com.agrocloud.config.interceptor.LoggingSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Web MVC para registrar interceptores de seguridad
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingSecurityInterceptor loggingSecurityInterceptor;

    @Autowired
    private CampanaEscrituraInterceptor campanaEscrituraInterceptor;

    @Autowired
    private ModuleAccessInterceptor moduleAccessInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingSecurityInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/actuator/**");
        registry.addInterceptor(moduleAccessInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/admin-global/**", "/api/health", "/actuator/**");
        registry.addInterceptor(campanaEscrituraInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/health", "/actuator/**", "/api/v1/campanas/**");
    }
}

