package com.agrocloud.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class CampanaRequestFilter extends OncePerRequestFilter {

    public static final String HEADER_CAMPANA = "X-Campaign-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader(HEADER_CAMPANA);
            if (header != null && !header.isBlank()) {
                try {
                    CampanaRequestContext.setCampanaId(Long.parseLong(header.trim()));
                } catch (NumberFormatException ignored) {
                    // ignorar header inválido; el servicio resolverá campaña activa
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            CampanaRequestContext.clear();
        }
    }
}
