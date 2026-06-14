package com.agrocloud.core.security;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Acceso al contexto de seguridad: empresa y usuario actualmente autenticados.
 */
@Service
public class ServicioSeguridadContexto {

    private final UserService userService;
    private final EmpresaContextService empresaContextService;

    public ServicioSeguridadContexto(UserService userService, EmpresaContextService empresaContextService) {
        this.userService = userService;
        this.empresaContextService = empresaContextService;
    }

    /**
     * Empresa del contexto HTTP: si existe la cabecera {@code X-Company-Id} y el usuario pertenece a esa empresa,
     * se usa; si no, la primera empresa activa del usuario.
     */
    @Transactional(readOnly = true)
    public Long obtenerEmpresaIdActual() {
        User usuario = requerirUsuarioActual();
        ServletRequestAttributes atributos = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (atributos != null) {
            HttpServletRequest solicitud = atributos.getRequest();
            String cabeceraEmpresa = solicitud.getHeader("X-Company-Id");
            if (cabeceraEmpresa != null && !cabeceraEmpresa.isBlank()) {
                try {
                    long empresaCabecera = Long.parseLong(cabeceraEmpresa.trim());
                    if (!usuario.perteneceAEmpresa(empresaCabecera)) {
                        throw new IllegalStateException("El usuario no pertenece a la empresa del contexto");
                    }
                    return empresaCabecera;
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Identificador de empresa inválido en la cabecera X-Company-Id");
                }
            }
        }
        Empresa empresa = empresaContextService
                .obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene una empresa asignada"));
        return empresa.getId();
    }

    @Transactional(readOnly = true)
    public Long obtenerUsuarioIdActual() {
        return requerirUsuarioActual().getId();
    }

    private User requerirUsuarioActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }
        String email = autenticacion.getName();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }
        User usuario = userService.findByEmailWithAllRelations(email);
        if (usuario == null) {
            throw new IllegalStateException("Usuario no encontrado");
        }
        return usuario;
    }
}
