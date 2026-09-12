package com.agrocloud.config;

import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Resuelve el usuario autenticado de la petición HTTP.
 * Componente en {@code com.agrocloud.config} para evitar problemas de classloader con DevTools
 * en controladores de módulos nuevos.
 */
@Component
public class ResolvedorUsuarioPeticion {

    private final UserService userService;

    public ResolvedorUsuarioPeticion(UserService userService) {
        this.userService = userService;
    }

    public User requerirUsuario(UserDetails detalles) {
        if (detalles == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        User usuario = userService.findByEmailWithAllRelations(detalles.getUsername());
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        return usuario;
    }
}
