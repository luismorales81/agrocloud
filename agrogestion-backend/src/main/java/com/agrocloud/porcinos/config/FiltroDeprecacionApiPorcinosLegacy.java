package com.agrocloud.porcinos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class FiltroDeprecacionApiPorcinosLegacy extends OncePerRequestFilter {

    private static final String PREFIJO_LEGACY = "/api/v1/porcinos";
    private static final String NUEVA_RUTA = "/api/porcinos";

    private final ObjectMapper objectMapper;

    public FiltroDeprecacionApiPorcinosLegacy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && (path.equals(PREFIJO_LEGACY) || path.startsWith(PREFIJO_LEGACY + "/"))) {
            response.setStatus(HttpServletResponse.SC_GONE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, String> body = new LinkedHashMap<>();
            body.put("message", "Use " + NUEVA_RUTA);
            body.put("nuevaRuta", NUEVA_RUTA);
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
