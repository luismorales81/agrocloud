package com.agrocloud.service;

import com.agrocloud.dto.BalanceDTO;
import com.agrocloud.dto.DetalleBalanceDTO;
import com.agrocloud.model.entity.Ingreso;
import com.agrocloud.model.entity.Egreso;
import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.IngresoRepository;
import com.agrocloud.repository.EgresoRepository;
import com.agrocloud.repository.LaborRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para el cálculo del balance de costos y beneficios.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service
public class BalanceService {

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private LaborRepository laborRepository;


    @Autowired
    private UserService userService;

    /**
     * Calcula el balance completo para un usuario en un rango de fechas.
     * Incluye datos del usuario y sus dependientes (jerarquía).
     * Para SUPERADMIN: muestra datos consolidados de todo el sistema.
     */
    public BalanceDTO calcularBalance(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener el usuario para verificar si es superadmin
        User usuario = userService.findById(usuarioId).orElse(null);
        boolean esSuperAdmin = esSuperAdmin(usuario);
        
        if (esSuperAdmin) {
            System.out.println("🔍 [BalanceService] Usuario es SUPERADMIN - calculando balance de todo el sistema");
            return calcularBalanceSistemaCompleto(fechaInicio, fechaFin);
        }
        
        // Calcular total de ingresos del usuario actual
        BigDecimal totalIngresos = ingresoRepository.calcularTotalIngresosPorUsuarioYFecha(usuarioId, fechaInicio, fechaFin);
        
        // Calcular total de egresos del usuario actual
        BigDecimal totalEgresos = egresoRepository.calcularTotalEgresosPorUsuarioYFecha(usuarioId, fechaInicio, fechaFin);
        
        // Calcular total de costos del usuario actual (labores + mantenimiento)
        BigDecimal totalCostos = calcularTotalCostos(usuarioId, fechaInicio, fechaFin);
        
        // ========================================
        // INCLUIR DATOS DE USUARIOS DEPENDIENTES
        // ========================================
        List<User> usuariosDependientes = userService.findByParentUserId(usuarioId);
        if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
            System.out.println("🔍 [BalanceService] Calculando balance para usuario ID: " + usuarioId + " y " + usuariosDependientes.size() + " dependientes");
            
            for (User dependiente : usuariosDependientes) {
                // Sumar ingresos de dependientes
                BigDecimal ingresosDep = ingresoRepository.calcularTotalIngresosPorUsuarioYFecha(dependiente.getId(), fechaInicio, fechaFin);
                if (ingresosDep != null) {
                    totalIngresos = (totalIngresos != null ? totalIngresos : BigDecimal.ZERO).add(ingresosDep);
                }
                
                // Sumar egresos de dependientes
                BigDecimal egresosDep = egresoRepository.calcularTotalEgresosPorUsuarioYFecha(dependiente.getId(), fechaInicio, fechaFin);
                if (egresosDep != null) {
                    totalEgresos = (totalEgresos != null ? totalEgresos : BigDecimal.ZERO).add(egresosDep);
                }
                
                // Sumar costos de dependientes
                BigDecimal costosDep = calcularTotalCostos(dependiente.getId(), fechaInicio, fechaFin);
                if (costosDep != null) {
                    totalCostos = (totalCostos != null ? totalCostos : BigDecimal.ZERO).add(costosDep);
                }
            }
        }
        
        // Total de costos incluyendo egresos
        BigDecimal totalCostosCompleto = totalCostos.add(totalEgresos);
        
        // Crear balance
        BalanceDTO balance = new BalanceDTO(fechaInicio, fechaFin, totalIngresos, totalCostosCompleto);
        
        // Agregar detalles del usuario actual
        List<DetalleBalanceDTO> detalles = new ArrayList<>();
        detalles.addAll(obtenerDetallesIngresos(usuarioId, fechaInicio, fechaFin));
        detalles.addAll(obtenerDetallesEgresos(usuarioId, fechaInicio, fechaFin));
        detalles.addAll(obtenerDetallesCostos(usuarioId, fechaInicio, fechaFin));
        
        // Agregar detalles de usuarios dependientes
        if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
            for (User dependiente : usuariosDependientes) {
                detalles.addAll(obtenerDetallesIngresos(dependiente.getId(), fechaInicio, fechaFin));
                detalles.addAll(obtenerDetallesEgresos(dependiente.getId(), fechaInicio, fechaFin));
                detalles.addAll(obtenerDetallesCostos(dependiente.getId(), fechaInicio, fechaFin));
            }
        }
        
        balance.setDetalles(detalles);
        
        return balance;
    }

    /**
     * Calcula el balance para un lote específico.
     * Incluye movimientos del usuario y sus dependientes sobre ese lote.
     */
    public BalanceDTO calcularBalancePorLote(Long loteId, Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Calcular total de ingresos del lote (solo del lote específico)
        BigDecimal totalIngresos = ingresoRepository.calcularTotalIngresosPorLoteYFecha(loteId, fechaInicio, fechaFin);
        
        // Calcular total de costos del lote (solo del lote específico)
        BigDecimal totalCostos = calcularTotalCostosPorLote(loteId, fechaInicio, fechaFin);
        
        // ========================================
        // INCLUIR DATOS DE USUARIOS DEPENDIENTES
        // ========================================
        List<User> usuariosDependientes = userService.findByParentUserId(usuarioId);
        if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
            System.out.println("🔍 [BalanceService] Calculando balance por lote " + loteId + " para usuario ID: " + usuarioId + " y " + usuariosDependientes.size() + " dependientes");
            
            // Nota: Para el balance por lote, solo incluimos movimientos que estén específicamente
            // asociados a ese lote, independientemente del usuario que los haya realizado.
            // Los repositorios ya filtran por loteId, por lo que no necesitamos iterar por usuarios.
        }
        
        // Crear balance
        BalanceDTO balance = new BalanceDTO(fechaInicio, fechaFin, totalIngresos, totalCostos);
        
        // Agregar detalles del lote específico
        List<DetalleBalanceDTO> detalles = new ArrayList<>();
        detalles.addAll(obtenerDetallesIngresosPorLote(loteId, fechaInicio, fechaFin));
        detalles.addAll(obtenerDetallesCostosPorLote(loteId, fechaInicio, fechaFin));
        
        balance.setDetalles(detalles);
        
        return balance;
    }

    /**
     * Calcula el total de costos para un usuario.
     */
    private BigDecimal calcularTotalCostos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal costosLabores = laborRepository.findByUsuarioIdAndFechaInicioBetween(usuarioId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCostoTotal() != null)
                .map(Labor::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return costosLabores;
    }

    /**
     * Calcula el total de costos para un lote específico.
     */
    private BigDecimal calcularTotalCostosPorLote(Long loteId, LocalDate fechaInicio, LocalDate fechaFin) {
        return laborRepository.findByLoteIdAndFechaInicioBetween(loteId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCostoTotal() != null)
                .map(Labor::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtiene los detalles de ingresos.
     */
    private List<DetalleBalanceDTO> obtenerDetallesIngresos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return ingresoRepository.findByUserIdAndFechaBetweenOrderByFechaDesc(usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(ingreso -> new DetalleBalanceDTO(
                        "INGRESO",
                        ingreso.getConcepto(),
                        ingreso.getFecha(),
                        ingreso.getMonto(),
                        ingreso.getTipoIngreso().toString(),
                        ingreso.getLote() != null ? ingreso.getLote().getNombre() : null,
                        ingreso.getDescripcion()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de ingresos por lote.
     */
    private List<DetalleBalanceDTO> obtenerDetallesIngresosPorLote(Long loteId, LocalDate fechaInicio, LocalDate fechaFin) {
        return ingresoRepository.findByLoteIdOrderByFechaDesc(loteId)
                .stream()
                .filter(ingreso -> !ingreso.getFecha().isBefore(fechaInicio) && !ingreso.getFecha().isAfter(fechaFin))
                .map(ingreso -> new DetalleBalanceDTO(
                        "INGRESO",
                        ingreso.getConcepto(),
                        ingreso.getFecha(),
                        ingreso.getMonto(),
                        ingreso.getTipoIngreso().toString(),
                        ingreso.getLote() != null ? ingreso.getLote().getNombre() : null,
                        ingreso.getDescripcion()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de egresos.
     */
    private List<DetalleBalanceDTO> obtenerDetallesEgresos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return egresoRepository.findByUserIdAndFechaBetweenOrderByFechaDesc(usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(egreso -> new DetalleBalanceDTO(
                        "EGRESO",
                        egreso.getTipo().toString(),
                        egreso.getFecha(),
                        egreso.getCostoTotal(),
                        egreso.getTipo().toString(),
                        egreso.getLote() != null ? egreso.getLote().getNombre() : null,
                        egreso.getObservaciones()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de costos.
     */
    private List<DetalleBalanceDTO> obtenerDetallesCostos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<DetalleBalanceDTO> detalles = new ArrayList<>();

        // Costos de labores
        laborRepository.findByUsuarioIdAndFechaInicioBetween(usuarioId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCostoTotal() != null)
                .forEach(labor -> detalles.add(new DetalleBalanceDTO(
                        "COSTO",
                        labor.getDescripcion(),
                        labor.getFechaInicio(),
                        labor.getCostoTotal(),
                        "LABOR - " + labor.getTipoLabor().toString(),
                        labor.getLote() != null ? labor.getLote().getNombre() : null,
                        labor.getDescripcion()
                )));

        // Costos de mantenimiento - No implementado en el sistema actual

        return detalles;
    }

    /**
     * Obtiene los detalles de costos por lote.
     */
    private List<DetalleBalanceDTO> obtenerDetallesCostosPorLote(Long loteId, LocalDate fechaInicio, LocalDate fechaFin) {
        return laborRepository.findByLoteIdAndFechaInicioBetween(loteId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCostoTotal() != null)
                .map(labor -> new DetalleBalanceDTO(
                        "COSTO",
                        labor.getDescripcion(),
                        labor.getFechaInicio(),
                        labor.getCostoTotal(),
                        "LABOR - " + labor.getTipoLabor().toString(),
                        labor.getLote() != null ? labor.getLote().getNombre() : null,
                        labor.getDescripcion()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de balance por mes.
     */
    public List<BalanceDTO> obtenerEstadisticasPorMes(Long usuarioId, int año) {
        List<BalanceDTO> balances = new ArrayList<>();
        
        for (int mes = 1; mes <= 12; mes++) {
            LocalDate fechaInicio = LocalDate.of(año, mes, 1);
            LocalDate fechaFin = fechaInicio.plusMonths(1).minusDays(1);
            
            BalanceDTO balance = calcularBalance(usuarioId, fechaInicio, fechaFin);
            balances.add(balance);
        }
        
        return balances;
    }

    /**
     * Verificar si un usuario es SUPERADMIN
     */
    private boolean esSuperAdmin(User usuario) {
        if (usuario == null || usuario.getRoles() == null) {
            return false;
        }
        
        return usuario.getRoles().stream()
                .anyMatch(role -> "SUPERADMIN".equals(role.getNombre()) || "SUPERADMIN".equals(role.getNombre()));
    }

    /**
     * Calcula el balance de todo el sistema (para SUPERADMIN)
     */
    private BalanceDTO calcularBalanceSistemaCompleto(LocalDate fechaInicio, LocalDate fechaFin) {
        System.out.println("🔍 [BalanceService] Calculando balance de todo el sistema");
        
        // Obtener todos los usuarios activos del sistema
        List<User> todosUsuarios = userService.findAll().stream()
                .filter(user -> user.getActivo() != null && user.getActivo())
                .collect(Collectors.toList());
        
        BigDecimal totalIngresosSistema = BigDecimal.ZERO;
        BigDecimal totalEgresosSistema = BigDecimal.ZERO;
        BigDecimal totalCostosSistema = BigDecimal.ZERO;
        
        List<DetalleBalanceDTO> detallesSistema = new ArrayList<>();
        
        // Sumar datos de todos los usuarios
        for (User usuario : todosUsuarios) {
            // Ingresos del usuario
            BigDecimal ingresosUsuario = ingresoRepository.calcularTotalIngresosPorUsuarioYFecha(usuario.getId(), fechaInicio, fechaFin);
            if (ingresosUsuario != null) {
                totalIngresosSistema = totalIngresosSistema.add(ingresosUsuario);
            }
            
            // Egresos del usuario
            BigDecimal egresosUsuario = egresoRepository.calcularTotalEgresosPorUsuarioYFecha(usuario.getId(), fechaInicio, fechaFin);
            if (egresosUsuario != null) {
                totalEgresosSistema = totalEgresosSistema.add(egresosUsuario);
            }
            
            // Costos del usuario
            BigDecimal costosUsuario = calcularTotalCostos(usuario.getId(), fechaInicio, fechaFin);
            if (costosUsuario != null) {
                totalCostosSistema = totalCostosSistema.add(costosUsuario);
            }
            
            // Agregar detalles del usuario
            detallesSistema.addAll(obtenerDetallesIngresos(usuario.getId(), fechaInicio, fechaFin));
            detallesSistema.addAll(obtenerDetallesEgresos(usuario.getId(), fechaInicio, fechaFin));
            detallesSistema.addAll(obtenerDetallesCostos(usuario.getId(), fechaInicio, fechaFin));
        }
        
        // Total de costos incluyendo egresos
        BigDecimal totalCostosCompleto = totalCostosSistema.add(totalEgresosSistema);
        
        // Crear balance del sistema
        BalanceDTO balance = new BalanceDTO(fechaInicio, fechaFin, totalIngresosSistema, totalCostosCompleto);
        balance.setDetalles(detallesSistema);
        
        System.out.println("📊 [BalanceService] Balance del sistema - Ingresos: " + totalIngresosSistema + 
                          ", Egresos: " + totalEgresosSistema + ", Costos: " + totalCostosSistema);
        
        return balance;
    }
}
