package com.agrocloud.service;

import com.agrocloud.model.entity.LogAcceso;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.LogAccesoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class LogAccesoService {

    private static final Logger logger = LoggerFactory.getLogger(LogAccesoService.class);

    @Autowired
    private LogAccesoRepository logAccesoRepository;

    /**
     * Registrar un log de acceso
     */
    public LogAcceso registrarAcceso(User usuario, String ipAddress, String userAgent,
                                     String tipoAcceso, String resultado, String detalles) {
        try {
            LogAcceso logAcceso = new LogAcceso(usuario, ipAddress, userAgent, tipoAcceso, resultado);
            logAcceso.setDetalles(detalles);

            LogAcceso saved = logAccesoRepository.save(logAcceso);
            logger.info("Log de acceso registrado para usuario: {} - Tipo: {} - Resultado: {}",
                        usuario.getUsername(), tipoAcceso, resultado);

            return saved;
        } catch (Exception e) {
            logger.error("Error al registrar log de acceso: {}", e.getMessage());
            throw new RuntimeException("Error al registrar log de acceso", e);
        }
    }

    /**
     * Registrar login exitoso
     */
    public void registrarLoginExitoso(User usuario, String ipAddress, String userAgent) {
        registrarAcceso(usuario, ipAddress, userAgent, "LOGIN", "EXITOSO", "Inicio de sesión exitoso");
    }

    /**
     * Registrar login fallido
     */
    public void registrarLoginFallido(String username, String ipAddress, String userAgent, String motivo) {
        LogAcceso logAcceso = new LogAcceso();
        logAcceso.setIpAddress(ipAddress);
        logAcceso.setUserAgent(userAgent);
        logAcceso.setTipoAcceso("LOGIN");
        logAcceso.setResultado("FALLIDO");
        logAcceso.setDetalles("Intento de login fallido para usuario: " + username + " - Motivo: " + motivo);
        logAcceso.setFechaAcceso(LocalDateTime.now());

        logAccesoRepository.save(logAcceso);
        logger.warn("Login fallido registrado para usuario: {} - IP: {}", username, ipAddress);
    }

    /**
     * Registrar logout
     */
    public void registrarLogout(User usuario, String ipAddress, String userAgent) {
        registrarAcceso(usuario, ipAddress, userAgent, "LOGOUT", "EXITOSO", "Cierre de sesión");
    }

    /**
     * Obtener logs de un usuario
     */
    public List<LogAcceso> obtenerLogsUsuario(User usuario) {
        return logAccesoRepository.findByUsuarioOrderByFechaAccesoDesc(usuario);
    }

    /**
     * Obtener logs de un usuario con paginación
     */
    public Page<LogAcceso> obtenerLogsUsuario(User usuario, Pageable pageable) {
        return logAccesoRepository.findByUsuarioOrderByFechaAccesoDesc(usuario, pageable);
    }

    /**
     * Obtener logs por rango de fechas
     */
    public List<LogAcceso> obtenerLogsPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return logAccesoRepository.findByFechaAccesoBetween(fechaInicio, fechaFin);
    }

    /**
     * Obtener logs de un usuario por rango de fechas
     */
    public List<LogAcceso> obtenerLogsUsuarioPorFecha(User usuario, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return logAccesoRepository.findByUsuarioAndFechaAccesoBetween(usuario, fechaInicio, fechaFin);
    }

    /**
     * Obtener estadísticas de accesos
     */
    public Map<String, Object> obtenerEstadisticasAccesos() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Contar total de logs
        long totalLogs = logAccesoRepository.count();
        estadisticas.put("totalLogs", totalLogs);

        // Contar accesos exitosos
        long accesosExitosos = logAccesoRepository.countByUsuarioAndResultado(null, "EXITOSO");
        estadisticas.put("accesosExitosos", accesosExitosos);

        // Contar accesos fallidos
        long accesosFallidos = logAccesoRepository.countByUsuarioAndResultado(null, "FALLIDO");
        estadisticas.put("accesosFallidos", accesosFallidos);

        // Calcular porcentaje de éxito
        if (totalLogs > 0) {
            double porcentajeExito = (double) accesosExitosos / totalLogs * 100;
            estadisticas.put("porcentajeExito", Math.round(porcentajeExito * 100.0) / 100.0);
        }

        return estadisticas;
    }

    /**
     * Obtener estadísticas de accesos por usuario
     */
    public Map<String, Object> obtenerEstadisticasAccesosUsuario(User usuario) {
        Map<String, Object> estadisticas = new HashMap<>();

        // Contar total de accesos del usuario
        long totalAccesos = logAccesoRepository.countByUsuario(usuario);
        estadisticas.put("totalAccesos", totalAccesos);

        // Contar accesos exitosos del usuario
        long accesosExitosos = logAccesoRepository.countByUsuarioAndResultado(usuario, "EXITOSO");
        estadisticas.put("accesosExitosos", accesosExitosos);

        // Calcular porcentaje de éxito
        if (totalAccesos > 0) {
            double porcentajeExito = (double) accesosExitosos / totalAccesos * 100;
            estadisticas.put("porcentajeExito", Math.round(porcentajeExito * 100.0) / 100.0);
        }

        // Último acceso
        List<LogAcceso> ultimosAccesos = logAccesoRepository.findByUsuarioOrderByFechaAccesoDesc(usuario);
        if (!ultimosAccesos.isEmpty()) {
            estadisticas.put("ultimoAcceso", ultimosAccesos.get(0).getFechaAcceso());
        }

        return estadisticas;
    }
}
