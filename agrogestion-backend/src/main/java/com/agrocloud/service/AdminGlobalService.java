package com.agrocloud.service;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import com.agrocloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la administración global del sistema multiempresa.
 * Este servicio es utilizado por el SuperAdmin para gestionar todas las empresas.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Service
@Transactional
public class AdminGlobalService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene el dashboard del administrador global
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDashboardGlobal() {
        Map<String, Object> dashboard = new HashMap<>();

        try {
            // Estadísticas de empresas
            long totalEmpresas = empresaRepository.count();
            long empresasActivas = empresaRepository.countByActivoTrue();
            long empresasTrial = empresaRepository.countByEstado(EstadoEmpresa.TRIAL);
            long empresasSuspendidas = empresaRepository.countByEstado(EstadoEmpresa.SUSPENDIDO);
        
        System.out.println("🔍 [AdminGlobalService] Estadísticas de empresas:");
        System.out.println("  - Total: " + totalEmpresas);
        System.out.println("  - Activas: " + empresasActivas);
        System.out.println("  - Trial: " + empresasTrial);
        System.out.println("  - Suspendidas: " + empresasSuspendidas);
        
        dashboard.put("totalEmpresas", totalEmpresas);
        dashboard.put("empresasActivas", empresasActivas);
        dashboard.put("empresasTrial", empresasTrial);
        dashboard.put("empresasSuspendidas", empresasSuspendidas);

        // Estadísticas de usuarios
        long totalUsuarios = userRepository.count();
        long usuariosActivos = userRepository.countByActivoTrue();
        long usuariosPendientes = userRepository.countByEstado(com.agrocloud.model.entity.EstadoUsuario.PENDIENTE);
        long usuariosSuspendidos = userRepository.countByEstado(com.agrocloud.model.entity.EstadoUsuario.SUSPENDIDO);
        
        System.out.println("🔍 [AdminGlobalService] Estadísticas de usuarios:");
        System.out.println("  - Total: " + totalUsuarios);
        System.out.println("  - Activos: " + usuariosActivos);
        System.out.println("  - Pendientes: " + usuariosPendientes);
        System.out.println("  - Suspendidos: " + usuariosSuspendidos);
        
        dashboard.put("totalUsuarios", totalUsuarios);
        dashboard.put("usuariosActivos", usuariosActivos);
        dashboard.put("usuariosPendientes", usuariosPendientes);
        dashboard.put("usuariosSuspendidos", usuariosSuspendidos);

        // Estadísticas del sistema (simplificadas por ahora)
        dashboard.put("totalCampos", 0L);
        dashboard.put("totalLotes", 0L);
        dashboard.put("totalCultivos", 0L);
        dashboard.put("totalInsumos", 0L);
        dashboard.put("totalMaquinaria", 0L);
        dashboard.put("totalLabores", 0L);
        dashboard.put("totalIngresos", 0.0);
        dashboard.put("totalEgresos", 0.0);
        dashboard.put("balanceGeneral", 0.0);
        dashboard.put("fechaConsulta", java.time.LocalDateTime.now().toString());

        // Estadísticas de uso del sistema (simplificadas para evitar referencias circulares)
        Map<String, Object> estadisticasUso = new HashMap<>();
        estadisticasUso.put("usuariosMasActivos", new java.util.ArrayList<>());
        estadisticasUso.put("empresasMasActivas", new java.util.ArrayList<>());
        estadisticasUso.put("sesionesHoy", 0L);
        estadisticasUso.put("sesionesEstaSemana", 0L);
        estadisticasUso.put("sesionesEsteMes", 0L);
        dashboard.putAll(estadisticasUso);

        } catch (Exception e) {
            System.err.println("Error en obtenerDashboardGlobal: " + e.getMessage());
            e.printStackTrace();
            
            // Valores por defecto en caso de error
            dashboard.put("totalEmpresas", 0L);
            dashboard.put("empresasActivas", 0L);
            dashboard.put("empresasTrial", 0L);
            dashboard.put("empresasSuspendidas", 0L);
            dashboard.put("totalUsuarios", 0L);
            dashboard.put("usuariosActivos", 0L);
            dashboard.put("usuariosPendientes", 0L);
            dashboard.put("usuariosSuspendidos", 0L);
            dashboard.put("totalCampos", 0L);
            dashboard.put("totalLotes", 0L);
            dashboard.put("totalCultivos", 0L);
            dashboard.put("totalInsumos", 0L);
            dashboard.put("totalMaquinaria", 0L);
            dashboard.put("totalLabores", 0L);
            dashboard.put("totalIngresos", 0.0);
            dashboard.put("totalEgresos", 0.0);
            dashboard.put("balanceGeneral", 0.0);
            dashboard.put("fechaConsulta", java.time.LocalDateTime.now().toString());
        }

        return dashboard;
    }

    /**
     * Obtiene todas las empresas con paginación
     */
    @Transactional(readOnly = true)
    public Page<Empresa> obtenerTodasLasEmpresas(Pageable pageable) {
        return empresaRepository.findAll(pageable);
    }

    /**
     * Obtiene todos los usuarios del sistema
     */
    @Transactional(readOnly = true)
    public List<User> obtenerTodosLosUsuarios() {
        return userRepository.findAll();
    }

    /**
     * Obtiene empresas con filtros avanzados
     */
    @Transactional(readOnly = true)
    public Page<Empresa> buscarEmpresas(String nombre, EstadoEmpresa estado, 
                                        Boolean activo, Pageable pageable) {
        return empresaRepository.findEmpresasConFiltros(nombre, estado, activo, pageable);
    }

    /**
     * Obtiene una empresa por ID
     */
    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPorId(Long id) {
        return empresaRepository.findById(id);
    }

    /**
     * Crea una nueva empresa desde el panel de administración global
     */
    @Transactional
    public Empresa crearEmpresaDesdeAdmin(String nombre, String cuit, String emailContacto, 
                                         String telefonoContacto, String direccion) {
        
        System.out.println("🏢 [AdminGlobalService] Iniciando creación de empresa: " + nombre);
        System.out.println("🏢 [AdminGlobalService] CUIT: " + cuit);
        System.out.println("🏢 [AdminGlobalService] Email contacto: " + emailContacto);
        
        // Crear la empresa
        Empresa empresa = new Empresa();
        empresa.setNombre(nombre);
        empresa.setCuit(cuit);
        empresa.setEmailContacto(emailContacto);
        empresa.setTelefonoContacto(telefonoContacto);
        empresa.setDireccion(direccion);
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        empresa.setActivo(true);
        empresa.setFechaInicioTrial(LocalDate.now());
        empresa.setFechaFinTrial(LocalDate.now().plusDays(30));

        // Obtener el super admin como creador
        System.out.println("🔍 [AdminGlobalService] Buscando super admin...");
        User superAdmin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Super admin no encontrado"));
        System.out.println("✅ [AdminGlobalService] Super admin encontrado: " + superAdmin.getUsername());
        empresa.setCreadoPor(superAdmin);

        System.out.println("💾 [AdminGlobalService] Guardando empresa en base de datos...");
        empresa = empresaRepository.save(empresa);
        System.out.println("✅ [AdminGlobalService] Empresa guardada con ID: " + empresa.getId());

        return empresa;
    }

    /**
     * Actualiza una empresa desde el panel de administración global
     */
    public Empresa actualizarEmpresaDesdeAdmin(Long id, String nombre, String cuit, String emailContacto,
                                              String telefonoContacto, String direccion,
                                              EstadoEmpresa estado, Boolean activo) {
        
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        if (nombre != null) empresa.setNombre(nombre);
        if (cuit != null) empresa.setCuit(cuit);
        if (emailContacto != null) empresa.setEmailContacto(emailContacto);
        if (telefonoContacto != null) empresa.setTelefonoContacto(telefonoContacto);
        if (direccion != null) empresa.setDireccion(direccion);
        if (estado != null) empresa.setEstado(estado);
        if (activo != null) empresa.setActivo(activo);

        return empresaRepository.save(empresa);
    }

    /**
     * Activa una empresa
     */
    public Empresa activarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresa.setActivo(true);
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        return empresaRepository.save(empresa);
    }

    /**
     * Suspende una empresa
     */
    public Empresa suspenderEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresa.setActivo(false);
        empresa.setEstado(EstadoEmpresa.SUSPENDIDO);
        return empresaRepository.save(empresa);
    }


    /**
     * Resetea la contraseña de un usuario
     */
    public void resetearPasswordUsuario(Long usuarioId, String nuevaPassword) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        userRepository.save(usuario);
    }

    /**
     * Desactiva un usuario
     */
    public User desactivarUsuario(Long usuarioId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setActivo(false);
        return userRepository.save(usuario);
    }

    /**
     * Activa un usuario
     */
    public User activarUsuario(Long usuarioId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setActivo(true);
        return userRepository.save(usuario);
    }

    /**
     * Obtiene usuarios de una empresa específica
     */
    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> obtenerUsuariosDeEmpresa(Long empresaId) {
        return usuarioEmpresaRepository.findByEmpresaId(empresaId);
    }

    /**
     * Obtiene usuarios activos de una empresa específica
     */
    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> obtenerUsuariosActivosDeEmpresa(Long empresaId) {
        return usuarioEmpresaRepository.findUsuariosActivosByEmpresaId(empresaId);
    }

    /**
     * Obtiene reporte de uso del sistema por empresa
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerReporteUsoEmpresa(Long empresaId) {
        Map<String, Object> reporte = new HashMap<>();

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));

        reporte.put("empresa", empresa);

        // Estadísticas de usuarios
        long totalUsuarios = usuarioEmpresaRepository.countByEmpresa(empresa);
        long usuariosActivos = usuarioEmpresaRepository.countUsuariosActivosByEmpresaId(empresaId);
        long administradores = usuarioEmpresaRepository.countByEmpresaIdAndRol(empresaId, RolEmpresa.ADMINISTRADOR);
        long operarios = usuarioEmpresaRepository.countByEmpresaIdAndRol(empresaId, RolEmpresa.OPERARIO);

        reporte.put("totalUsuarios", totalUsuarios);
        reporte.put("usuariosActivos", usuariosActivos);
        reporte.put("administradores", administradores);
        reporte.put("operarios", operarios);

        // Aquí podrías agregar más estadísticas como:
        // - Cantidad de campos
        // - Cantidad de lotes
        // - Cantidad de insumos
        // - Últimos accesos
        // - Actividad reciente

        return reporte;
    }

    /**
     * Obtiene empresas con trial próximo a vencer
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasTrialProximoVencer(int dias) {
        LocalDate fechaLimite = LocalDate.now().plusDays(dias);
        return empresaRepository.findEmpresasTrialProximoVencer(fechaLimite);
    }

    /**
     * Obtiene empresas con trial vencido
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasTrialVencido() {
        return empresaRepository.findEmpresasTrialVencido(LocalDate.now());
    }

    /**
     * Extiende el trial de una empresa
     */
    public Empresa extenderTrialEmpresa(Long id, int diasAdicionales) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        if (empresa.getFechaFinTrial() == null) {
            empresa.setFechaFinTrial(LocalDate.now().plusDays(diasAdicionales));
        } else {
            empresa.setFechaFinTrial(empresa.getFechaFinTrial().plusDays(diasAdicionales));
        }

        return empresaRepository.save(empresa);
    }


    /**
     * Obtiene reporte detallado de uso por usuario
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerReporteUsoPorUsuario(Long usuarioId) {
        Map<String, Object> reporte = new HashMap<>();
        
        User usuario = userRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        reporte.put("usuario", usuario);
        
        // Estadísticas de actividad del usuario
        reporte.put("ultimoAcceso", usuario.getFechaActualizacion());
        reporte.put("fechaCreacion", usuario.getFechaCreacion());
        reporte.put("estado", usuario.getEstado());
        reporte.put("activo", usuario.getActivo());
        
        // Aquí podrías agregar más estadísticas específicas del usuario
        // como cantidad de campos, lotes, etc. que ha creado
        
        return reporte;
    }

    /**
     * Obtiene reporte de actividad del sistema
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerReporteActividadSistema() {
        Map<String, Object> reporte = new HashMap<>();
        
        // Usuarios creados en los últimos 30 días
        long usuariosRecientes = userRepository.countByFechaCreacionAfter(
            java.time.LocalDateTime.now().minusDays(30));
        reporte.put("usuariosRecientes", usuariosRecientes);
        
        // Empresas creadas en los últimos 30 días
        long empresasRecientes = empresaRepository.countByFechaCreacionAfter(
            java.time.LocalDateTime.now().minusDays(30));
        reporte.put("empresasRecientes", empresasRecientes);
        
        // Usuarios activos en las últimas 24 horas (simplificado)
        reporte.put("usuariosActivosHoy", 0L);
        
        // Usuarios activos en la última semana (simplificado)
        reporte.put("usuariosActivosSemana", 0L);
        
        return reporte;
    }

    /**
     * Obtiene estadísticas de uso del sistema
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasUso() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Distribución de usuarios por rol
        Map<String, Long> usuariosPorRol = new HashMap<>();
        List<User> todosUsuarios = userRepository.findAll();
        
        for (User usuario : todosUsuarios) {
            if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                String rol = usuario.getRoles().iterator().next().getNombre();
                usuariosPorRol.put(rol, usuariosPorRol.getOrDefault(rol, 0L) + 1);
            }
        }
        estadisticas.put("usuariosPorRol", usuariosPorRol);
        
        // Usuarios más activos basados en contenido creado
        List<Map<String, Object>> usuariosMasActivos = new java.util.ArrayList<>();
        try {
            // Obtener usuarios con más campos creados
            List<Object[]> usuariosConCampos = userRepository.findUsuariosConMasCampos();
            for (Object[] resultado : usuariosConCampos) {
                Map<String, Object> usuarioActivo = new HashMap<>();
                usuarioActivo.put("nombre", resultado[0] + " " + resultado[1]);
                usuarioActivo.put("email", resultado[2]);
                Long camposCreados = ((Number) resultado[3]).longValue();
                usuarioActivo.put("actividad", camposCreados + " campos creados");
                usuarioActivo.put("totalActividad", camposCreados);
                usuariosMasActivos.add(usuarioActivo);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo usuarios activos: " + e.getMessage());
            // Fallback a usuarios recientes
            todosUsuarios.stream()
                .filter(u -> u.getUpdatedAt() != null)
                .sorted((u1, u2) -> u2.getUpdatedAt().compareTo(u1.getUpdatedAt()))
                .limit(5)
                .forEach(usuario -> {
                    Map<String, Object> usuarioActivo = new HashMap<>();
                    usuarioActivo.put("nombre", usuario.getFirstName() + " " + usuario.getLastName());
                    usuarioActivo.put("email", usuario.getEmail());
                    usuarioActivo.put("actividad", "Usuario activo");
                    usuariosMasActivos.add(usuarioActivo);
                });
        }
        estadisticas.put("usuariosMasActivos", usuariosMasActivos);
        
        // Empresas más activas basadas en contenido
        List<Map<String, Object>> empresasMasActivas = new java.util.ArrayList<>();
        try {
            List<Object[]> empresasConActividad = empresaRepository.findEmpresasConMasActividad();
            for (Object[] resultado : empresasConActividad) {
                Map<String, Object> empresaActiva = new HashMap<>();
                empresaActiva.put("nombre", resultado[0]);
                empresaActiva.put("cuit", resultado[1]);
                Long totalActividad = ((Number) resultado[2]).longValue();
                Long campos = ((Number) resultado[3]).longValue();
                Long lotes = ((Number) resultado[4]).longValue();
                Long labores = ((Number) resultado[5]).longValue();
                empresaActiva.put("actividad", campos + " campos, " + lotes + " lotes, " + labores + " labores");
                empresaActiva.put("totalActividad", totalActividad);
                empresasMasActivas.add(empresaActiva);
            }
        } catch (Exception ex) {
            System.err.println("Error obteniendo empresas activas: " + ex.getMessage());
            // Fallback a empresas recientes
            List<Empresa> todasEmpresas = empresaRepository.findAll();
            todasEmpresas.stream()
                .filter(empresa -> empresa.getFechaActualizacion() != null)
                .sorted((e1, e2) -> e2.getFechaActualizacion().compareTo(e1.getFechaActualizacion()))
                .limit(5)
                .forEach(empresa -> {
                    Map<String, Object> empresaActiva = new HashMap<>();
                    empresaActiva.put("nombre", empresa.getNombre());
                    empresaActiva.put("cuit", empresa.getCuit());
                    empresaActiva.put("actividad", "Empresa activa");
                    empresasMasActivas.add(empresaActiva);
                });
        }
        estadisticas.put("empresasMasActivas", empresasMasActivas);
        
        // Estadísticas de sesiones (simplificado)
        estadisticas.put("sesionesHoy", todosUsuarios.size()); // Aproximación
        estadisticas.put("sesionesEstaSemana", todosUsuarios.size() * 2); // Aproximación
        estadisticas.put("sesionesEsteMes", todosUsuarios.size() * 10); // Aproximación
        
        return estadisticas;
    }
}
