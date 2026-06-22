package com.agrocloud.config;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rechaza escrituras cuando la campaña activa está CERRADA.
 */
@Component
public class CampanaEscrituraInterceptor implements HandlerInterceptor {

    @Autowired
    private ServicioSeguridadContexto servicioSeguridadContexto;

    @Autowired
    @Qualifier("campanaContextServiceCore")
    private CampanaContextService campanaContextService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (HttpMethod.GET.matches(request.getMethod()) || HttpMethod.OPTIONS.matches(request.getMethod())
                || HttpMethod.HEAD.matches(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.contains("/api/auth/") || uri.contains("/api/v1/campanas/") && uri.contains("/activar")) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return true;
        }
        try {
            Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
            campanaContextService.validarCampanaEditable(empresaId);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            return false;
        }
        return true;
    }
}
