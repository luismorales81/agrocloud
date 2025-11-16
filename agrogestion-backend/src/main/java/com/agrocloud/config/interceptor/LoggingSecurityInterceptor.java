package com.agrocloud.config.interceptor;

import com.agrocloud.service.EnmascaramientoDatosService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Interceptor para enmascarar datos sensibles en logs
 */
@Component
public class LoggingSecurityInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSecurityInterceptor.class);

    @Autowired
    private EnmascaramientoDatosService enmascaramientoService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // No enmascarar en logs de desarrollo, solo registrar que se está procesando
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // Solo loguear métodos que puedan contener datos sensibles
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
            logger.debug("Procesando {} {} - Los datos sensibles serán enmascarados si es necesario", method, uri);
        }
        
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable ModelAndView modelAndView) {
        // Aquí se podría enmascarar datos en el modelo antes de enviar a la vista
        // Pero como usamos API REST, esto no es necesario
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
        // Registrar acceso sin exponer datos sensibles
        if (ex != null) {
            logger.error("Error procesando {} {}: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                ex.getMessage());
        }
    }

    /**
     * Enmascara parámetros de request antes de loguearlos
     */
    public String enmascararParametros(Map<String, String[]> parametros) {
        if (parametros == null || parametros.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean primero = true;
        
        for (Map.Entry<String, String[]> entry : parametros.entrySet()) {
            if (!primero) {
                sb.append(", ");
            }
            primero = false;
            
            String clave = entry.getKey();
            String[] valores = entry.getValue();
            
            sb.append(clave).append("=");
            
            if (enmascaramientoService.esCampoSensible(clave)) {
                if (valores.length > 0) {
                    sb.append(enmascaramientoService.enmascararPorTipo(valores[0], clave));
                } else {
                    sb.append("***");
                }
            } else {
                sb.append(valores.length > 0 ? valores[0] : "null");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
}

