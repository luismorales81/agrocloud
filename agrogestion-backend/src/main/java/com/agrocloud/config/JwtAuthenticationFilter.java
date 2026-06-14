package com.agrocloud.config;

import com.agrocloud.core.application.AuthService;
import com.agrocloud.core.application.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final AuthService authService;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    public JwtAuthenticationFilter(
            @Qualifier("jwtServiceCore") JwtService jwtService,
            @Qualifier("authServiceCore") AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (activeProfiles.contains("test")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestURI = request.getRequestURI();

        if (requestURI.contains("/api/auth/login")
                || requestURI.contains("/api/auth/register")
                || requestURI.contains("/api/auth/request-password-reset")
                || requestURI.contains("/api/auth/reset-password")
                || requestURI.contains("/api/health")
                || requestURI.contains("/api/version")
                || requestURI.contains("/api/eula/")
                || requestURI.contains("/actuator/health")
                || requestURI.contains("/actuator/info")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token JWT requerido");
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = authService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.warn("Token JWT invalido para usuario: {}", userEmail);
                }
            }
        } catch (Exception e) {
            logger.error("Error procesando token JWT: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token JWT invalido o expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
