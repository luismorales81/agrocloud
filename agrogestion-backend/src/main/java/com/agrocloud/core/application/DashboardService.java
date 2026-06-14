package com.agrocloud.core.application;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.cultivos.infrastructure.MaquinariaRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.core.infrastructure.IngresoRepository;
import com.agrocloud.core.infrastructure.EgresoRepository;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.cultivos.infrastructure.HistorialCosechaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para obtener estadísticas del dashboard
 */
@Service("dashboardServiceCore")
public class DashboardService {

    @Autowired
    @Qualifier("insumoRepositoryInventario")
    private InsumoRepository insumoRepository;

    @Autowired
    @Qualifier("fieldRepositoryCultivos")
    private FieldRepository fieldRepository;

    @Autowired
    @Qualifier("plotRepositoryCultivos")
    private PlotRepository plotRepository;

    @Autowired
    @Qualifier("maquinariaRepositoryCultivos")
    private MaquinariaRepository maquinariaRepository;

    @Autowired
    @Qualifier("laborRepositoryCultivos")
    private LaborRepository laborRepository;

    @Autowired
    @Qualifier("ingresoRepositoryCore")
    private IngresoRepository ingresoRepository;

    @Autowired
    @Qualifier("egresoRepositoryCore")
    private EgresoRepository egresoRepository;

    @Autowired
    @Qualifier("userServiceCore")
    private com.agrocloud.core.application.UserService userService;
    
    @Autowired
    @Qualifier("userRepositoryCore")
    private com.agrocloud.core.infrastructure.UserRepository userRepository;

    @Autowired
    @Qualifier("cultivoRepositoryCultivos")
    private CultivoRepository cultivoRepository;

    // @Autowired
    // private CosechaRepository cosechaRepository;  // DEPRECADO - Usar historialCosechaRepository
    
    @Autowired
    @Qualifier("historialCosechaRepositoryCultivos")
    private HistorialCosechaRepository historialCosechaRepository;

    /**
     * Obtener estadísticas del dashboard para un usuario específico
     */
    public Map<String, Object> obtenerEstadisticasDashboard(User usuario) {
        Map<String, Object> estadisticas = new HashMap<>();

        if (usuario == null) {
            return estadisticas;
        }

        // Obtener estadísticas del usuario actual (usuario + usuarios dependientes)
        Long userId = usuario.getId();
        
        System.out.println("🔍 [DashboardService] Obteniendo estadísticas para usuario ID: " + userId);
        
        // Si es ADMINISTRADOR, obtener datos de todos los usuarios de la empresa
        if (esAdmin(usuario)) {
            System.out.println("🔍 [DashboardService] Usuario es ADMIN - obteniendo datos globales");
            return obtenerEstadisticasAdmin(usuario);
        }
        
        // Obtener estadísticas del usuario actual
        long camposUsuario = fieldRepository.countByUserId(userId);
        long lotesUsuario = plotRepository.countByUserIdAndActivoTrue(userId);
        long cultivosUsuario = cultivoRepository.countByUsuarioId(userId);
        long insumosUsuario = insumoRepository.countByUserId(userId);
        long maquinariaUsuario = maquinariaRepository.countByUserId(userId);
        long laboresUsuario = laborRepository.countByUsuarioId(userId);
        long ingresosUsuario = ingresoRepository.countByUserId(userId);
        long egresosUsuario = egresoRepository.countByUserId(userId);
        long cosechasUsuario = historialCosechaRepository.countByUsuarioId(userId);
        
        System.out.println("📊 [DashboardService] Estadísticas del usuario:");
        System.out.println("  - Campos: " + camposUsuario);
        System.out.println("  - Lotes: " + lotesUsuario);
        System.out.println("  - Cultivos: " + cultivosUsuario);
        System.out.println("  - Insumos: " + insumosUsuario);
        System.out.println("  - Maquinaria: " + maquinariaUsuario);
        System.out.println("  - Labores: " + laboresUsuario);
        System.out.println("  - Ingresos: " + ingresosUsuario);
        System.out.println("  - Egresos: " + egresosUsuario);
        System.out.println("  - Cosechas: " + cosechasUsuario);
        
        // Si es ADMIN, también mostrar estadísticas de usuarios del sistema
        if (esAdmin(usuario)) {
            long totalUsuarios = userService.findAll().size();
            long usuariosActivos = userService.findAll().stream()
                    .filter(user -> user.getActivo() != null && user.getActivo())
                    .count();
            
            estadisticas.put("totalUsuarios", totalUsuarios);
            estadisticas.put("usuariosActivos", usuariosActivos);
        }
        
        // Obtener estadísticas de usuarios dependientes (si es técnico o supervisor)
        long camposDependientes = 0;
        long lotesDependientes = 0;
        long cultivosDependientes = 0;
        long insumosDependientes = 0;
        long maquinariaDependientes = 0;
        long laboresDependientes = 0;
        long ingresosDependientes = 0;
        long egresosDependientes = 0;
        long cosechasDependientes = 0;
        
        // Buscar usuarios que tienen a este usuario como parentUser
        List<User> usuariosDependientes = userService.findByParentUserId(userId);
        if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
            System.out.println("👥 [DashboardService] Usuarios dependientes encontrados: " + usuariosDependientes.size());
            for (User dependiente : usuariosDependientes) {
                camposDependientes += fieldRepository.countByUserId(dependiente.getId());
                lotesDependientes += plotRepository.countByUserIdAndActivoTrue(dependiente.getId());
                cultivosDependientes += cultivoRepository.countByUsuarioId(dependiente.getId());
                insumosDependientes += insumoRepository.countByUserId(dependiente.getId());
                maquinariaDependientes += maquinariaRepository.countByUserId(dependiente.getId());
                laboresDependientes += laborRepository.countByUsuarioId(dependiente.getId());
                ingresosDependientes += ingresoRepository.countByUserId(dependiente.getId());
                egresosDependientes += egresoRepository.countByUserId(dependiente.getId());
                cosechasDependientes += historialCosechaRepository.countByUsuarioId(dependiente.getId());
            }
            
            System.out.println("📊 [DashboardService] Estadísticas de dependientes:");
            System.out.println("  - Campos: " + camposDependientes);
            System.out.println("  - Lotes: " + lotesDependientes);
            System.out.println("  - Cultivos: " + cultivosDependientes);
            System.out.println("  - Insumos: " + insumosDependientes);
            System.out.println("  - Maquinaria: " + maquinariaDependientes);
            System.out.println("  - Labores: " + laboresDependientes);
            System.out.println("  - Ingresos: " + ingresosDependientes);
            System.out.println("  - Egresos: " + egresosDependientes);
            System.out.println("  - Cosechas: " + cosechasDependientes);
        }
        
        // Sumar estadísticas totales (usuario + dependientes)
        estadisticas.put("campos", camposUsuario + camposDependientes);
        estadisticas.put("lotes", lotesUsuario + lotesDependientes);
        estadisticas.put("cultivos", cultivosUsuario + cultivosDependientes);
        estadisticas.put("insumos", insumosUsuario + insumosDependientes);
        estadisticas.put("maquinaria", maquinariaUsuario + maquinariaDependientes);
        estadisticas.put("labores", laboresUsuario + laboresDependientes);
        estadisticas.put("ingresos", ingresosUsuario + ingresosDependientes);
        estadisticas.put("egresos", egresosUsuario + egresosDependientes);
        estadisticas.put("cosechas", cosechasUsuario + cosechasDependientes);
        
        // ========================================
        // BALANCE OPERATIVO (Corto plazo)
        // ========================================
        BigDecimal totalIngresos = ingresoRepository.sumIngresosByUsuarioId(userId);
        BigDecimal totalEgresos = egresoRepository.sumEgresosByUsuarioId(userId);
        
        // Sumar ingresos y egresos de usuarios dependientes
        for (User dependiente : usuariosDependientes) {
            BigDecimal ingresosDep = ingresoRepository.sumIngresosByUsuarioId(dependiente.getId());
            BigDecimal egresosDep = egresoRepository.sumEgresosByUsuarioId(dependiente.getId());
            
            if (ingresosDep != null) totalIngresos = (totalIngresos != null ? totalIngresos : BigDecimal.ZERO).add(ingresosDep);
            if (egresosDep != null) totalEgresos = (totalEgresos != null ? totalEgresos : BigDecimal.ZERO).add(egresosDep);
        }
        
        // Balance operativo = Ingresos - Egresos (flujo de caja puro)
        BigDecimal balanceOperativo = (totalIngresos != null ? totalIngresos : BigDecimal.ZERO)
                    .subtract(totalEgresos != null ? totalEgresos : BigDecimal.ZERO);
        
        estadisticas.put("balanceOperativo", balanceOperativo);
        estadisticas.put("totalIngresos", totalIngresos != null ? totalIngresos : BigDecimal.ZERO);
        estadisticas.put("totalEgresos", totalEgresos != null ? totalEgresos : BigDecimal.ZERO);
        
        // ========================================
        // BALANCE PATRIMONIAL (Largo plazo)
        // ========================================
        // Calcular valor de activos del usuario
        BigDecimal valorActivosUsuario = calcularValorActivosUsuario(userId);
        
        // Calcular valor de activos de usuarios dependientes
        BigDecimal valorActivosDependientes = BigDecimal.ZERO;
        for (User dependiente : usuariosDependientes) {
            valorActivosDependientes = valorActivosDependientes.add(calcularValorActivosUsuario(dependiente.getId()));
        }
        
        BigDecimal valorTotalActivos = valorActivosUsuario.add(valorActivosDependientes);
        
        // Balance patrimonial = Ingresos - Egresos + Valor de Activos
        BigDecimal balancePatrimonial = balanceOperativo.add(valorTotalActivos);
        
        estadisticas.put("balancePatrimonial", balancePatrimonial);
        estadisticas.put("valorActivos", valorTotalActivos);
        
        // Mantener compatibilidad con el balance anterior
        estadisticas.put("balance", balancePatrimonial);

        // Agregar información del usuario
        estadisticas.put("usuario", usuario.getUsername());
        estadisticas.put("esAdmin", esAdmin(usuario));
        estadisticas.put("fechaConsulta", java.time.LocalDateTime.now());

        return estadisticas;
    }

    /**
     * Obtener estadísticas del dashboard para un usuario por ID
     */
    public Map<String, Object> obtenerEstadisticasDashboardPorUsuarioId(Long userId) {
        try {
            User usuario = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
            
            return obtenerEstadisticasDashboard(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Verificar si un usuario es ADMIN
     */
    private boolean esAdmin(User usuario) {
        if (usuario == null) {
            System.out.println("❌ [DashboardService] Usuario es null");
            return false;
        }
        
        System.out.println("🔍 [DashboardService] Verificando roles para usuario: " + usuario.getUsername());
        
        // Usar el método isAdmin() del modelo User que ya maneja ambos sistemas
        boolean esAdmin = usuario.isAdmin();
        
        System.out.println("✅ [DashboardService] Usuario " + usuario.getUsername() + " es admin: " + esAdmin);
        
        return esAdmin;
    }

    /**
     * Obtener estadísticas comparativas entre usuarios
     */
    public Map<String, Object> obtenerEstadisticasComparativas() {
        Map<String, Object> comparativas = new HashMap<>();
        
        try {
            // Estadísticas por tipo de usuario
            long totalUsuarios = userService.findAll().size();
            long usuariosActivos = userService.findAll().stream()
                    .filter(user -> user.getActivo() != null && user.getActivo())
                    .count();
            
            comparativas.put("totalUsuarios", totalUsuarios);
            comparativas.put("usuariosActivos", usuariosActivos);
            
            // Datos por usuario
            Map<String, Object> datosPorUsuario = new HashMap<>();
            List<User> todosUsuarios = userService.findAll();
            
            for (User user : todosUsuarios) {
                Map<String, Object> estadisticasUsuario = obtenerEstadisticasDashboard(user);
                datosPorUsuario.put(user.getUsername(), estadisticasUsuario);
            }
            
            comparativas.put("datosPorUsuario", datosPorUsuario);
            
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, retornar valores por defecto
            comparativas.put("totalUsuarios", 0);
            comparativas.put("usuariosActivos", 0);
            comparativas.put("datosPorUsuario", new HashMap<>());
        }
        
        return comparativas;
    }
    
    /**
     * Calcula el valor total de los activos de un usuario
     */
    private BigDecimal calcularValorActivosUsuario(Long userId) {
        BigDecimal valorTotal = BigDecimal.ZERO;
        
        try {
            // 1. Valor de insumos en stock (stock actual × precio unitario)
            BigDecimal valorInsumos = insumoRepository.findByUserId(userId)
                    .stream()
                    .filter(insumo -> insumo.getPrecioUnitario() != null && insumo.getStockActual() != null)
                    .map(insumo -> {
                        try {
                            return insumo.getPrecioUnitario().multiply(BigDecimal.valueOf(insumo.getStockActual().doubleValue()));
                        } catch (Exception e) {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 2. Valor de maquinaria (estimado por unidad)
            BigDecimal valorMaquinaria = maquinariaRepository.findByUserId(userId)
                    .stream()
                    .map(maq -> {
                        // Estimación: $500,000 por maquinaria (ajustable)
                        return new BigDecimal("500000");
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 3. Valor de cultivos en campo (estimado por hectárea)
            BigDecimal valorCultivos = cultivoRepository.findByUsuarioId(userId)
                    .stream()
                    .map(cultivo -> {
                        // Estimación: $50,000 por hectárea (ajustable según el cultivo)
                        return new BigDecimal("50000");
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 4. Valor de lotes (estimado por hectárea)
            BigDecimal valorLotes = plotRepository.findByUserId(userId)
                    .stream()
                    .map(lote -> {
                        // Estimación: $100,000 por hectárea (valor de la tierra)
                        return new BigDecimal("100000");
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            valorTotal = valorInsumos.add(valorMaquinaria).add(valorCultivos).add(valorLotes);
            
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, retornar 0
            valorTotal = BigDecimal.ZERO;
        }
        
        return valorTotal;
    }
    
    /**
     * Obtener estadísticas globales para administradores
     */
    private Map<String, Object> obtenerEstadisticasAdmin(User usuario) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            System.out.println("🔍 [DashboardService] Obteniendo estadísticas de la empresa para ADMIN");
            
            // Obtener la empresa del usuario administrador desde usuarioEmpresas (ya cargada con JOIN FETCH)
            Empresa empresa = null;
            if (usuario.getUsuarioEmpresas() != null && !usuario.getUsuarioEmpresas().isEmpty()) {
                for (UsuarioEmpresa ue : usuario.getUsuarioEmpresas()) {
                    if (ue.getEstado() == com.agrocloud.model.enums.EstadoUsuarioEmpresa.ACTIVO) {
                        empresa = ue.getEmpresa();
                        break;
                    }
                }
            }
            
            if (empresa == null) {
                System.out.println("❌ [DashboardService] Usuario ADMIN no tiene empresa asignada");
                return estadisticas;
            }
            Long empresaId = empresa.getId();
            
            // Obtener todos los usuarios de la empresa usando el repositorio directamente
            // para evitar LazyInitializationException
            List<User> usuariosEmpresa = userRepository.findByUsuarioEmpresasEmpresaId(empresaId);
            
            System.out.println("🏢 [DashboardService] Empresa ID: " + empresaId + ", Usuarios: " + usuariosEmpresa.size());
            
            // Obtener estadísticas de la empresa (solo usuarios de la empresa)
            long camposTotal = 0;
            long lotesTotal = 0;
            long cultivosTotal = 0;
            long insumosTotal = 0;
            long maquinariaTotal = 0;
            long laboresTotal = 0;
            long ingresosTotal = 0;
            long egresosTotal = 0;
            long cosechasTotal = 0;
            
            // Sumar estadísticas de todos los usuarios de la empresa
            for (User userEmpresa : usuariosEmpresa) {
                // Contar solo campos activos para que coincida con FieldService
                List<Field> camposActivos = fieldRepository.findByUserIdAndActivoTrue(userEmpresa.getId());
                camposTotal += camposActivos != null ? camposActivos.size() : 0;
                // Contar solo lotes activos para que coincida con PlotService
                lotesTotal += plotRepository.countByUserIdAndActivoTrue(userEmpresa.getId());
                cultivosTotal += cultivoRepository.countByUsuarioId(userEmpresa.getId());
                insumosTotal += insumoRepository.countByUserId(userEmpresa.getId());
                maquinariaTotal += maquinariaRepository.countByUserId(userEmpresa.getId());
                laboresTotal += laborRepository.countByUsuarioId(userEmpresa.getId());
                ingresosTotal += ingresoRepository.countByUserId(userEmpresa.getId());
                egresosTotal += egresoRepository.countByUserId(userEmpresa.getId());
                cosechasTotal += historialCosechaRepository.countByUsuarioId(userEmpresa.getId());
            }
            
            System.out.println("📊 [DashboardService] Estadísticas globales:");
            System.out.println("  - Campos: " + camposTotal);
            System.out.println("  - Lotes: " + lotesTotal);
            System.out.println("  - Cultivos: " + cultivosTotal);
            System.out.println("  - Insumos: " + insumosTotal);
            System.out.println("  - Maquinaria: " + maquinariaTotal);
            System.out.println("  - Labores: " + laboresTotal);
            System.out.println("  - Ingresos: " + ingresosTotal);
            System.out.println("  - Egresos: " + egresosTotal);
            System.out.println("  - Cosechas: " + cosechasTotal);
            
            // Agregar estadísticas globales
            estadisticas.put("campos", camposTotal);
            estadisticas.put("lotes", lotesTotal);
            estadisticas.put("cultivos", cultivosTotal);
            estadisticas.put("insumos", insumosTotal);
            estadisticas.put("maquinaria", maquinariaTotal);
            estadisticas.put("labores", laboresTotal);
            estadisticas.put("ingresos", ingresosTotal);
            estadisticas.put("egresos", egresosTotal);
            estadisticas.put("cosechas", cosechasTotal);
            
            // ========================================
            // BALANCE OPERATIVO (Corto plazo)
            // ========================================
            BigDecimal totalIngresos = ingresoRepository.sumAllIngresos();
            BigDecimal totalEgresos = egresoRepository.sumAllEgresos();
            
            // Balance operativo = Ingresos - Egresos (flujo de caja puro)
            BigDecimal balanceOperativo = (totalIngresos != null ? totalIngresos : BigDecimal.ZERO)
                        .subtract(totalEgresos != null ? totalEgresos : BigDecimal.ZERO);
            
            estadisticas.put("balanceOperativo", balanceOperativo);
            estadisticas.put("totalIngresos", totalIngresos != null ? totalIngresos : BigDecimal.ZERO);
            estadisticas.put("totalEgresos", totalEgresos != null ? totalEgresos : BigDecimal.ZERO);
            
            // ========================================
            // BALANCE PATRIMONIAL (Largo plazo)
            // ========================================
            // Para administradores, calcular valor total de activos
            BigDecimal valorTotalActivos = BigDecimal.ZERO;
            
            // Calcular valor de todos los insumos
            List<Insumo> todosInsumos = insumoRepository.findAll();
            for (Insumo insumo : todosInsumos) {
                if (insumo.getPrecioUnitario() != null && insumo.getStockActual() != null) {
                    valorTotalActivos = valorTotalActivos.add(
                        insumo.getPrecioUnitario().multiply(insumo.getStockActual())
                    );
                }
            }
            
            // Balance patrimonial = Ingresos - Egresos + Valor de Activos
            BigDecimal balancePatrimonial = balanceOperativo.add(valorTotalActivos);
            
            estadisticas.put("balancePatrimonial", balancePatrimonial);
            estadisticas.put("valorActivos", valorTotalActivos);
            
            // Mantener compatibilidad con el balance anterior
            estadisticas.put("balance", balancePatrimonial);

            // Agregar información del usuario
            estadisticas.put("usuario", usuario.getUsername());
            estadisticas.put("esAdmin", esAdmin(usuario));
            estadisticas.put("fechaConsulta", java.time.LocalDateTime.now());
            
            System.out.println("✅ [DashboardService] Estadísticas globales calculadas exitosamente");
            
        } catch (Exception e) {
            System.out.println("❌ [DashboardService] Error calculando estadísticas globales: " + e.getMessage());
            e.printStackTrace();
        }
        
        return estadisticas;
    }
}
