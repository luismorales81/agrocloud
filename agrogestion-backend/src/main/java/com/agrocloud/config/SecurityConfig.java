package com.agrocloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                // Permitir endpoints públicos
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/test", "/api/auth/test-auth", "/api/auth/test-productor", "/api/auth/generate-hash", "/api/auth/test-password", "/api/health", "/api/public/**", "/api/admin-global/dashboard-test", "/api/admin-global/test-simple", "/api/admin-global/dashboard-simple", "/api/admin-global/test-connectivity", "/api/admin-global/empresas-basic", "/api/admin-global/usuarios-basic", "/api/admin-global/estadisticas-uso", "/api/admin-global/diagnostico-roles", "/api/v1/weather-simple/**", "/api/v1/weather/**", "/api/eula/**", "/actuator/**").permitAll()
                // Endpoints de administración global (solo SuperAdmin)
                .requestMatchers("/api/admin-global/**").hasAuthority("ROLE_SUPERADMIN")
                // Endpoints de administración de empresa
                .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_SUPERADMIN", "ROLE_ADMINISTRADOR")
                // Endpoints de roles (solo administradores)
                .requestMatchers("/api/roles/**").hasAnyAuthority("ROLE_SUPERADMIN", "ROLE_ADMINISTRADOR")
                // Endpoints de gestión de empresas (solo SuperAdmin)
                .requestMatchers("/api/empresas/**").hasAuthority("ROLE_SUPERADMIN")
                // Endpoints de gestión de usuarios-empresas (solo SuperAdmin)
                .requestMatchers("/api/empresa-usuario/**").hasAuthority("ROLE_SUPERADMIN")
                // Endpoints del dashboard
                .requestMatchers("/api/dashboard/**").authenticated()
                // Endpoints de entidades principales (requieren autenticación y pertenencia a empresa)
                .requestMatchers("/api/fields/**", "/api/campos/**", "/api/plots/**", "/api/cultivos/**", "/api/v1/cultivos/**", "/api/cosechas/**", "/api/v1/cosechas/**", "/api/insumos/**", "/api/maquinaria/**", "/api/labores/**", "/api/ingresos/**", "/api/egresos/**", "/api/v1/balance/**", "/api/lotes/**", "/api/v1/lotes/**").authenticated()
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    

    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Obtener origenes permitidos de variables de entorno o usar defaults para desarrollo
        String allowedOriginsEnv = System.getenv("CORS_ALLOWED_ORIGINS");
        List<String> allowedOrigins;
        
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            // Producción: usar variables de entorno
            allowedOrigins = Arrays.asList(allowedOriginsEnv.split(","));
        } else {
            // Desarrollo: usar localhost
            allowedOrigins = Arrays.asList(
                "http://localhost:3000", 
                "http://localhost:3001", 
                "http://localhost:5173", 
                "http://127.0.0.1:3000", 
                "http://127.0.0.1:3001", 
                "http://127.0.0.1:5173",
                "null"
            );
        }
        
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
