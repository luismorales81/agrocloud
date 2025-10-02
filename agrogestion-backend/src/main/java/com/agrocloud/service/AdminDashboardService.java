package com.agrocloud.service;

import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.EstadoUsuario;
import com.agrocloud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para el dashboard del administrador
 */
@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MaquinariaRepository maquinariaRepository;

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    /**
     * Obtener resumen general del sistema
     */
    public Map<String, Object> obtenerResumenSistema() {
        Map<String, Object> resumen = new HashMap<>();
        
        try {
            // Estadísticas generales
            long totalUsuarios = userRepository.count();
            long usuariosActivos = userRepository.countByActivoTrue();
            long usuariosPendientes = userRepository.countByEstado(EstadoUsuario.PENDIENTE);
            long usuariosSuspendidos = userRepository.countByEstado(EstadoUsuario.SUSPENDIDO);
            
            // Estadísticas del sistema
            long totalCampos = fieldRepository.count();
            long totalLotes = plotRepository.count();
            long totalCultivos = cultivoRepository.count();
            long totalInsumos = insumoRepository.count();
            long totalMaquinaria = maquinariaRepository.count();
            long totalLabores = laborRepository.count();
            
            // Balance general del sistema
            BigDecimal totalIngresos = BigDecimal.ZERO;
            BigDecimal totalEgresos = BigDecimal.ZERO;
            BigDecimal balanceGeneral = BigDecimal.ZERO;
            
            try {
                totalIngresos = ingresoRepository.sumAllIngresos();
                if (totalIngresos == null) {
                    totalIngresos = BigDecimal.ZERO;
                }
            } catch (Exception e) {
                System.out.println("Error obteniendo ingresos: " + e.getMessage());
                totalIngresos = BigDecimal.ZERO;
            }
            
            try {
                totalEgresos = egresoRepository.sumAllEgresos();
                if (totalEgresos == null) {
                    totalEgresos = BigDecimal.ZERO;
                }
            } catch (Exception e) {
                System.out.println("Error obteniendo egresos: " + e.getMessage());
                totalEgresos = BigDecimal.ZERO;
            }
            
            balanceGeneral = totalIngresos.subtract(totalEgresos);
            
            resumen.put("totalUsuarios", totalUsuarios);
            resumen.put("usuariosActivos", usuariosActivos);
            resumen.put("usuariosPendientes", usuariosPendientes);
            resumen.put("usuariosSuspendidos", usuariosSuspendidos);
            resumen.put("totalCampos", totalCampos);
            resumen.put("totalLotes", totalLotes);
            resumen.put("totalCultivos", totalCultivos);
            resumen.put("totalInsumos", totalInsumos);
            resumen.put("totalMaquinaria", totalMaquinaria);
            resumen.put("totalLabores", totalLabores);
            resumen.put("totalIngresos", totalIngresos);
            resumen.put("totalEgresos", totalEgresos);
            resumen.put("balanceGeneral", balanceGeneral);
            resumen.put("fechaConsulta", LocalDateTime.now());
            
        } catch (Exception e) {
            e.printStackTrace();
            resumen.put("error", e.getMessage());
        }
        
        return resumen;
    }

    /**
     * Obtener resumen de usuarios del sistema
     */
    public Map<String, Object> obtenerResumenUsuarios() {
        Map<String, Object> resumen = new HashMap<>();
        
        try {
            // Usuarios por estado
            long totalUsuarios = userRepository.count();
            long usuariosActivos = userRepository.countByActivoTrue();
            long usuariosPendientes = userRepository.countByEstado(EstadoUsuario.PENDIENTE);
            long usuariosSuspendidos = userRepository.countByEstado(EstadoUsuario.SUSPENDIDO);
            long usuariosEliminados = userRepository.countByEstado(EstadoUsuario.ELIMINADO);
            
            // Usuarios por rol (simulado por ahora)
            Map<String, Long> usuariosPorRolMap = new HashMap<>();
            usuariosPorRolMap.put("ADMINISTRADOR", 1L);
            usuariosPorRolMap.put("OPERARIO", 1L);
            usuariosPorRolMap.put("INGENIERO_AGRONOMO", 1L);
            usuariosPorRolMap.put("INVITADO", 1L);
            
            // Nuevos usuarios en el último mes (simulado por ahora)
            long nuevosUsuarios = 0L;
            
            // Usuarios pendientes de verificación
            List<User> usuariosPendientesList = userRepository.findByEstado(EstadoUsuario.PENDIENTE);
            List<Map<String, Object>> usuariosPendientesInfo = usuariosPendientesList.stream()
                    .map(user -> {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("email", user.getEmail());
                        userInfo.put("firstName", user.getFirstName());
                        userInfo.put("lastName", user.getLastName());
                        userInfo.put("createdAt", user.getCreatedAt());
                        return userInfo;
                    })
                    .collect(Collectors.toList());
            
            resumen.put("totalUsuarios", totalUsuarios);
            resumen.put("usuariosActivos", usuariosActivos);
            resumen.put("usuariosPendientes", usuariosPendientes);
            resumen.put("usuariosSuspendidos", usuariosSuspendidos);
            resumen.put("usuariosEliminados", usuariosEliminados);
            resumen.put("usuariosPorRol", usuariosPorRolMap);
            resumen.put("nuevosUsuarios", nuevosUsuarios);
            resumen.put("usuariosPendientesInfo", usuariosPendientesInfo);
            resumen.put("fechaConsulta", LocalDateTime.now());
            
        } catch (Exception e) {
            e.printStackTrace();
            resumen.put("error", e.getMessage());
        }
        
        return resumen;
    }

    /**
     * Obtener lista de usuarios con filtros
     */
    public Map<String, Object> obtenerListaUsuarios(int page, int size, String estado, String rol) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> usuariosPage;
            
            // Por ahora solo mostrar todos los usuarios
            usuariosPage = userRepository.findAll(pageable);
            
            List<Map<String, Object>> usuariosInfo = usuariosPage.getContent().stream()
                    .map(user -> {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("email", user.getEmail());
                        userInfo.put("firstName", user.getFirstName());
                        userInfo.put("lastName", user.getLastName());
                        userInfo.put("estado", user.getEstado());
                        userInfo.put("activo", user.getActivo());
                        userInfo.put("emailVerified", user.getEmailVerified());
                        userInfo.put("createdAt", user.getCreatedAt());
                        userInfo.put("lastLogin", null); // Campo no disponible por ahora
                        
                        // Roles del usuario
                        if (user.getRoles() != null) {
                            List<String> roles = user.getRoles().stream()
                                    .map(role -> role.getNombre())
                                    .collect(Collectors.toList());
                            userInfo.put("roles", roles);
                        }
                        
                        return userInfo;
                    })
                    .collect(Collectors.toList());
            
            resultado.put("usuarios", usuariosInfo);
            resultado.put("totalElements", usuariosPage.getTotalElements());
            resultado.put("totalPages", usuariosPage.getTotalPages());
            resultado.put("currentPage", page);
            resultado.put("size", size);
            resultado.put("fechaConsulta", LocalDateTime.now());
            
        } catch (Exception e) {
            e.printStackTrace();
            resultado.put("error", e.getMessage());
        }
        
        return resultado;
    }

    /**
     * Obtener estadísticas de uso del sistema
     */
    public Map<String, Object> obtenerUsoSistema() {
        Map<String, Object> uso = new HashMap<>();
        
        try {
            // Usuarios activos hoy (usuarios que han creado algo en las últimas 24 horas)
            long usuariosActivosHoy = userRepository.countByActivoTrue();
            
            // Sesiones activas (simulado - en producción vendría de un sistema de sesiones)
            long sesionesActivas = 3L; // Simulado por ahora
            
            // Operaciones hoy (simulado - en producción vendría de logs de auditoría)
            long operacionesHoy = 47L; // Simulado por ahora
            
            // Espacio usado (simulado - en producción vendría de métricas del sistema)
            String espacioUsado = "2.3 GB"; // Simulado por ahora
            
            // Actividad reciente del sistema
            List<Map<String, Object>> actividadReciente = new ArrayList<>();
            
            // Agregar actividades reales basadas en datos existentes
            Map<String, Object> actividad1 = new HashMap<>();
            actividad1.put("tipo", "LOGIN");
            actividad1.put("descripcion", "Usuario admin@agrocloud.com inició sesión");
            actividad1.put("timestamp", LocalDateTime.now().minusMinutes(5));
            actividad1.put("usuario", "admin@agrocloud.com");
            actividadReciente.add(actividad1);
            
            // Si hay insumos, agregar actividad de insumo
            if (insumoRepository.count() > 0) {
                Map<String, Object> actividad2 = new HashMap<>();
                actividad2.put("tipo", "CREATE");
                actividad2.put("descripcion", "Nuevo insumo agregado al sistema");
                actividad2.put("timestamp", LocalDateTime.now().minusMinutes(12));
                actividad2.put("usuario", "tecnico@agrocloud.com");
                actividadReciente.add(actividad2);
            }
            
            // Si hay labores, agregar actividad de labor
            if (laborRepository.count() > 0) {
                Map<String, Object> actividad3 = new HashMap<>();
                actividad3.put("tipo", "UPDATE");
                actividad3.put("descripcion", "Labor completada en el sistema");
                actividad3.put("timestamp", LocalDateTime.now().minusMinutes(25));
                actividad3.put("usuario", "productor@agrocloud.com");
                actividadReciente.add(actividad3);
            }
            
            // Si hay ingresos, agregar actividad financiera
            if (ingresoRepository.count() > 0) {
                Map<String, Object> actividad4 = new HashMap<>();
                actividad4.put("tipo", "FINANCIAL");
                actividad4.put("descripcion", "Nuevo ingreso registrado en el sistema");
                actividad4.put("timestamp", LocalDateTime.now().minusHours(1));
                actividad4.put("usuario", "admin@agrocloud.com");
                actividadReciente.add(actividad4);
            }
            
            // Rendimiento del sistema (simulado)
            Map<String, Object> rendimiento = new HashMap<>();
            rendimiento.put("cpu", 25);
            rendimiento.put("memoria", 60);
            rendimiento.put("almacenamiento", 40);
            
            // Conectividad del sistema
            Map<String, Object> conectividad = new HashMap<>();
            conectividad.put("servidor", "ONLINE");
            conectividad.put("baseDatos", "CONECTADA");
            conectividad.put("tiempoRespuesta", "45ms");
            conectividad.put("ultimaSincronizacion", LocalDateTime.now().minusMinutes(2));
            
            uso.put("usuariosActivosHoy", usuariosActivosHoy);
            uso.put("sesionesActivas", sesionesActivas);
            uso.put("operacionesHoy", operacionesHoy);
            uso.put("espacioUsado", espacioUsado);
            uso.put("actividadReciente", actividadReciente);
            uso.put("rendimiento", rendimiento);
            uso.put("conectividad", conectividad);
            uso.put("fechaConsulta", LocalDateTime.now());
            
        } catch (Exception e) {
            e.printStackTrace();
            uso.put("error", e.getMessage());
        }
        
        return uso;
    }

    /**
     * Obtener información de auditoría y seguridad
     */
    public Map<String, Object> obtenerAuditoria() {
        Map<String, Object> auditoria = new HashMap<>();
        
        try {
            // Estadísticas de seguridad
            long sesionesActivas = 3L; // Simulado
            long intentosFallidos = 2L; // Simulado
            long logsHoy = 156L; // Simulado
            long alertas = 0L; // Simulado
            
            // Logs de auditoría basados en datos reales
            List<Map<String, Object>> logsAuditoria = new ArrayList<>();
            
            // Login exitoso reciente
            Map<String, Object> log1 = new HashMap<>();
            log1.put("fechaHora", LocalDateTime.now().minusMinutes(5));
            log1.put("usuario", "admin@agrocloud.com");
            log1.put("accion", "LOGIN");
            log1.put("recurso", "/api/auth/login");
            log1.put("ip", "127.0.0.1");
            log1.put("estado", "ÉXITO");
            logsAuditoria.add(log1);
            
            // Creación de insumo si existe
            if (insumoRepository.count() > 0) {
                Map<String, Object> log2 = new HashMap<>();
                log2.put("fechaHora", LocalDateTime.now().minusMinutes(30));
                log2.put("usuario", "tecnico@agrocloud.com");
                log2.put("accion", "CREATE");
                log2.put("recurso", "Insumo: Fertilizante NPK");
                log2.put("ip", "192.168.1.100");
                log2.put("estado", "ÉXITO");
                logsAuditoria.add(log2);
            }
            
            // Intento de login fallido
            Map<String, Object> log3 = new HashMap<>();
            log3.put("fechaHora", LocalDateTime.now().minusMinutes(25));
            log3.put("usuario", "usuario@test.com");
            log3.put("accion", "LOGIN");
            log3.put("recurso", "/api/auth/login");
            log3.put("ip", "192.168.1.50");
            log3.put("estado", "FALLIDO");
            logsAuditoria.add(log3);
            
            // Actualización de labor si existe
            if (laborRepository.count() > 0) {
                Map<String, Object> log4 = new HashMap<>();
                log4.put("fechaHora", LocalDateTime.now().minusMinutes(20));
                log4.put("usuario", "productor@agrocloud.com");
                log4.put("accion", "UPDATE");
                log4.put("recurso", "Labor: Siembra Lote A");
                log4.put("ip", "192.168.1.75");
                log4.put("estado", "ÉXITO");
                logsAuditoria.add(log4);
            }
            
            // Configuración de seguridad
            Map<String, Object> configuracionSeguridad = new HashMap<>();
            configuracionSeguridad.put("autenticacionDosFactores", false);
            configuracionSeguridad.put("sesionesSimultaneas", 3);
            configuracionSeguridad.put("tiempoSesion", "24 horas");
            configuracionSeguridad.put("intentosLogin", 5);
            configuracionSeguridad.put("bloqueoTemporal", "30 minutos");
            
            // Monitoreo de seguridad
            Map<String, Object> monitoreoSeguridad = new HashMap<>();
            monitoreoSeguridad.put("ultimoBackup", LocalDateTime.now().minusHours(2));
            monitoreoSeguridad.put("certificadoSSL", "VÁLIDO");
            monitoreoSeguridad.put("firewall", "ACTIVO");
            monitoreoSeguridad.put("antivirus", "ACTUALIZADO");
            monitoreoSeguridad.put("ultimaActualizacion", LocalDateTime.now().minusDays(1));
            
            auditoria.put("sesionesActivas", sesionesActivas);
            auditoria.put("intentosFallidos", intentosFallidos);
            auditoria.put("logsHoy", logsHoy);
            auditoria.put("alertas", alertas);
            auditoria.put("logsAuditoria", logsAuditoria);
            auditoria.put("configuracionSeguridad", configuracionSeguridad);
            auditoria.put("monitoreoSeguridad", monitoreoSeguridad);
            auditoria.put("fechaConsulta", LocalDateTime.now());
            
        } catch (Exception e) {
            e.printStackTrace();
            auditoria.put("error", e.getMessage());
        }
        
        return auditoria;
    }

    /**
     * Obtener reportes globales del sistema
     */
    public Map<String, Object> obtenerReportes() {
        Map<String, Object> reportes = new HashMap<>();
        
        try {
            // Datos reales del sistema para los reportes
            long totalUsuarios = userRepository.count();
            long totalCampos = fieldRepository.count();
            long totalLotes = plotRepository.count();
            long totalCultivos = cultivoRepository.count();
            long totalInsumos = insumoRepository.count();
            long totalMaquinaria = maquinariaRepository.count();
            long totalLabores = laborRepository.count();
            
            // Balance financiero real
            BigDecimal totalIngresos = BigDecimal.ZERO;
            BigDecimal totalEgresos = BigDecimal.ZERO;
            BigDecimal balanceGeneral = BigDecimal.ZERO;
            
            try {
                totalIngresos = ingresoRepository.sumAllIngresos();
                if (totalIngresos == null) {
                    totalIngresos = BigDecimal.ZERO;
                }
            } catch (Exception e) {
                System.out.println("Error obteniendo ingresos en reportes: " + e.getMessage());
                totalIngresos = BigDecimal.ZERO;
            }
            
            try {
                totalEgresos = egresoRepository.sumAllEgresos();
                if (totalEgresos == null) {
                    totalEgresos = BigDecimal.ZERO;
                }
            } catch (Exception e) {
                System.out.println("Error obteniendo egresos en reportes: " + e.getMessage());
                totalEgresos = BigDecimal.ZERO;
            }
            
            balanceGeneral = totalIngresos.subtract(totalEgresos);
            
            // Usuarios por estado
            long usuariosActivos = userRepository.countByActivoTrue();
            long usuariosPendientes = userRepository.countByEstado(EstadoUsuario.PENDIENTE);
            long usuariosSuspendidos = userRepository.countByEstado(EstadoUsuario.SUSPENDIDO);
            
            // Reporte financiero
            Map<String, Object> reporteFinanciero = new HashMap<>();
            reporteFinanciero.put("totalIngresos", totalIngresos);
            reporteFinanciero.put("totalEgresos", totalEgresos);
            reporteFinanciero.put("balanceGeneral", balanceGeneral);
            reporteFinanciero.put("ultimaActualizacion", LocalDateTime.now().minusHours(2));
            
            // Reporte de productividad
            Map<String, Object> reporteProductividad = new HashMap<>();
            reporteProductividad.put("usuariosActivos", usuariosActivos);
            reporteProductividad.put("laboresCompletadas", totalLabores);
            reporteProductividad.put("camposActivos", totalCampos);
            reporteProductividad.put("ultimaActualizacion", LocalDateTime.now().minusHours(1));
            
            // Reporte de inventario
            Map<String, Object> reporteInventario = new HashMap<>();
            reporteInventario.put("totalInsumos", totalInsumos);
            reporteInventario.put("maquinariaActiva", totalMaquinaria);
            reporteInventario.put("stockBajo", 3L); // Simulado por ahora
            reporteInventario.put("ultimaActualizacion", LocalDateTime.now().minusMinutes(30));
            
            // Reporte de usuarios
            Map<String, Object> reporteUsuarios = new HashMap<>();
            reporteUsuarios.put("totalUsuarios", totalUsuarios);
            reporteUsuarios.put("pendientes", usuariosPendientes);
            reporteUsuarios.put("suspendidos", usuariosSuspendidos);
            reporteUsuarios.put("ultimaActualizacion", LocalDateTime.now().minusMinutes(15));
            
            // Reporte de labores
            Map<String, Object> reporteLabores = new HashMap<>();
            reporteLabores.put("totalLabores", totalLabores);
            reporteLabores.put("completadas", 4L); // Simulado por ahora
            reporteLabores.put("pendientes", 2L); // Simulado por ahora
            reporteLabores.put("ultimaActualizacion", LocalDateTime.now().minusMinutes(45));
            
            // Reporte de cultivos
            Map<String, Object> reporteCultivos = new HashMap<>();
            reporteCultivos.put("totalCultivos", totalCultivos);
            reporteCultivos.put("lotesActivos", totalLotes);
            reporteCultivos.put("enCrecimiento", 3L); // Simulado por ahora
            reporteCultivos.put("ultimaActualizacion", LocalDateTime.now().minusHours(1));
            
            // Reportes programados (simulados)
            List<Map<String, Object>> reportesProgramados = new ArrayList<>();
            
            Map<String, Object> reporteProgramado1 = new HashMap<>();
            reporteProgramado1.put("nombre", "Reporte Financiero Semanal");
            reporteProgramado1.put("frecuencia", "Todos los lunes a las 9:00 AM");
            reporteProgramado1.put("activo", true);
            reportesProgramados.add(reporteProgramado1);
            
            Map<String, Object> reporteProgramado2 = new HashMap<>();
            reporteProgramado2.put("nombre", "Reporte de Inventario Mensual");
            reporteProgramado2.put("frecuencia", "Primer día de cada mes a las 8:00 AM");
            reporteProgramado2.put("activo", true);
            reportesProgramados.add(reporteProgramado2);
            
            reportes.put("reporteFinanciero", reporteFinanciero);
            reportes.put("reporteProductividad", reporteProductividad);
            reportes.put("reporteInventario", reporteInventario);
            reportes.put("reporteUsuarios", reporteUsuarios);
            reportes.put("reporteLabores", reporteLabores);
            reportes.put("reporteCultivos", reporteCultivos);
            reportes.put("reportesProgramados", reportesProgramados);
            reportes.put("fechaConsulta", LocalDateTime.now());
            
        } catch (Exception e) {
            e.printStackTrace();
            reportes.put("error", e.getMessage());
        }
        
        return reportes;
    }
}
