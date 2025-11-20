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
    
    @Autowired
    private EnmascaramientoDatosService enmascaramientoDatosService;
    
    @Autowired
    private com.agrocloud.repository.UsuarioEmpresaRolRepository usuarioEmpresaRolRepository;

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

        // Obtener el primer SUPERADMIN disponible como creador
        System.out.println("🔍 [AdminGlobalService] Buscando SUPERADMIN...");
        User superAdmin = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> "SUPERADMIN".equals(r.getNombre())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("SUPERADMIN no encontrado"));
        System.out.println("✅ [AdminGlobalService] SUPERADMIN encontrado: " + superAdmin.getUsername());
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

        // Crear mapa con datos enmascarados de la empresa
        Map<String, Object> empresaData = new HashMap<>();
        empresaData.put("id", empresa.getId());
        empresaData.put("nombre", empresa.getNombre());
        empresaData.put("cuit", enmascaramientoDatosService.enmascararCuit(empresa.getCuit()));
        empresaData.put("emailContacto", enmascaramientoDatosService.enmascararEmail(empresa.getEmailContacto()));
        empresaData.put("telefonoContacto", empresa.getTelefonoContacto() != null ? 
            enmascaramientoDatosService.enmascararTelefono(empresa.getTelefonoContacto()) : null);
        empresaData.put("estado", empresa.getEstado() != null ? empresa.getEstado().name() : "ACTIVO");
        empresaData.put("activo", empresa.getActivo());
        
        reporte.put("empresa", empresaData);

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
        
        // Crear mapa con datos enmascarados del usuario
        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("id", usuario.getId());
        usuarioData.put("username", usuario.getUsername());
        usuarioData.put("firstName", usuario.getFirstName());
        usuarioData.put("lastName", usuario.getLastName());
        usuarioData.put("email", enmascaramientoDatosService.enmascararEmail(usuario.getEmail()));
        usuarioData.put("activo", usuario.getActivo());
        usuarioData.put("estado", usuario.getEstado() != null ? usuario.getEstado().name() : "ACTIVO");
        
        reporte.put("usuario", usuarioData);
        
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
        System.out.println("🚀 [AdminGlobalService] obtenerEstadisticasUso() - INICIO");
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Distribución de usuarios por rol de empresa
        Map<String, Long> usuariosPorRol = new HashMap<>();
        
        // Inicializar todos los roles posibles con 0 para que aparezcan aunque no tengan usuarios
        usuariosPorRol.put("SUPERADMIN", 0L);
        usuariosPorRol.put("ADMINISTRADOR", 0L);
        usuariosPorRol.put("JEFE_CAMPO", 0L);
        usuariosPorRol.put("JEFE_FINANCIERO", 0L);
        usuariosPorRol.put("OPERARIO", 0L);
        usuariosPorRol.put("CONSULTOR_EXTERNO", 0L);
        
        // Usar UsuarioEmpresa como fuente principal (tiene más datos y es la fuente de verdad)
        // Luego combinar con UsuarioEmpresaRol para tener el conteo completo
        try {
            System.out.println("📊 [AdminGlobalService] Usando UsuarioEmpresa como fuente principal");
            List<com.agrocloud.model.entity.UsuarioEmpresa> relaciones = usuarioEmpresaRepository.findAll();
            System.out.println("  - Total relaciones encontradas en UsuarioEmpresa: " + relaciones.size());
            
            // Contar usuarios únicos por rol (usando roles actualizados para mapear deprecated)
            Map<String, java.util.Set<Long>> usuariosPorRolSet = new HashMap<>();
            
            for (com.agrocloud.model.entity.UsuarioEmpresa relacion : relaciones) {
                if (relacion.getEstado() == com.agrocloud.model.enums.EstadoUsuarioEmpresa.ACTIVO) {
                    // Usar getRolActualizado() para mapear roles deprecated a los nuevos
                    com.agrocloud.model.enums.RolEmpresa rol = relacion.getRol();
                    if (rol != null) {
                        com.agrocloud.model.enums.RolEmpresa rolActualizado = rol.getRolActualizado();
                        String nombreRol = rolActualizado.name();
                        Long usuarioId = relacion.getUsuario().getId();
                        
                        usuariosPorRolSet.putIfAbsent(nombreRol, new java.util.HashSet<>());
                        usuariosPorRolSet.get(nombreRol).add(usuarioId);
                    }
                }
            }
            
            // Convertir sets a conteos
            for (Map.Entry<String, java.util.Set<Long>> entry : usuariosPorRolSet.entrySet()) {
                usuariosPorRol.put(entry.getKey(), (long) entry.getValue().size());
                System.out.println("  ✅ Rol desde UsuarioEmpresa: " + entry.getKey() + " = " + entry.getValue().size() + " usuarios únicos");
            }
            
            // Ahora agregar datos de UsuarioEmpresaRol para usuarios que puedan estar solo ahí
            try {
                long totalRegistrosRol = usuarioEmpresaRolRepository.count();
                System.out.println("📊 [AdminGlobalService] Total registros en UsuarioEmpresaRol: " + totalRegistrosRol);
                
                if (totalRegistrosRol > 0) {
                    List<Object[]> conteosPorRol = usuarioEmpresaRolRepository.countUsuariosUnicosPorRol();
                    System.out.println("📊 [AdminGlobalService] Combinando con UsuarioEmpresaRol: " + (conteosPorRol != null ? conteosPorRol.size() : 0) + " roles");
                    
                    if (conteosPorRol != null && !conteosPorRol.isEmpty()) {
                        for (Object[] resultado : conteosPorRol) {
                            String nombreRol = (String) resultado[0];
                            Long cantidad = ((Number) resultado[1]).longValue();
                            
                            // Mapear el nombre del rol a RolEmpresa si es necesario
                            try {
                                com.agrocloud.model.enums.RolEmpresa rolEmpresa = com.agrocloud.model.enums.RolEmpresa.valueOf(nombreRol);
                                com.agrocloud.model.enums.RolEmpresa rolActualizado = rolEmpresa.getRolActualizado();
                                String nombreRolFinal = rolActualizado.name();
                                
                                // Combinar con los datos de UsuarioEmpresa (usar el máximo para evitar duplicados)
                                Long cantidadActual = usuariosPorRol.getOrDefault(nombreRolFinal, 0L);
                                // Si UsuarioEmpresaRol tiene más usuarios, actualizar (puede haber usuarios solo en esta tabla)
                                if (cantidad > cantidadActual) {
                                    usuariosPorRol.put(nombreRolFinal, cantidad);
                                    System.out.println("  🔄 Actualizado desde UsuarioEmpresaRol: " + nombreRolFinal + " = " + cantidad);
                                }
                            } catch (IllegalArgumentException e) {
                                // Si no es un RolEmpresa válido, ignorar
                                System.out.println("    ⚠️ Rol no reconocido como RolEmpresa: " + nombreRol);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error combinando con UsuarioEmpresaRol: " + e.getMessage());
            }
            
        } catch (Exception ex) {
            System.err.println("❌ Error contando usuarios por rol desde UsuarioEmpresa: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        System.out.println("✅ [AdminGlobalService] Distribución final de usuarios por rol: " + usuariosPorRol);
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
            List<User> todosUsuarios = userRepository.findAll();
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
        long totalUsuarios = userRepository.count();
        estadisticas.put("sesionesHoy", totalUsuarios); // Aproximación
        estadisticas.put("sesionesEstaSemana", totalUsuarios * 2); // Aproximación
        estadisticas.put("sesionesEsteMes", totalUsuarios * 10); // Aproximación
        
        return estadisticas;
    }
}
