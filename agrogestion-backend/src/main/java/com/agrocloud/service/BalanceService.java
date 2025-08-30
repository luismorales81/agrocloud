package com.agrocloud.service;

import com.agrocloud.dto.BalanceDTO;
import com.agrocloud.dto.DetalleBalanceDTO;
import com.agrocloud.model.entity.Ingreso;
import com.agrocloud.model.entity.Egreso;
import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.MantenimientoMaquinaria;
import com.agrocloud.repository.IngresoRepository;
import com.agrocloud.repository.EgresoRepository;
import com.agrocloud.repository.LaborRepository;
import com.agrocloud.repository.MantenimientoMaquinariaRepository;
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
    private MantenimientoMaquinariaRepository mantenimientoRepository;

    /**
     * Calcula el balance completo para un usuario en un rango de fechas.
     */
    public BalanceDTO calcularBalance(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Calcular total de ingresos
        BigDecimal totalIngresos = ingresoRepository.calcularTotalIngresosPorUsuarioYFecha(usuarioId, fechaInicio, fechaFin);
        
        // Calcular total de egresos
        BigDecimal totalEgresos = egresoRepository.calcularTotalEgresosPorUsuarioYFecha(usuarioId, fechaInicio, fechaFin);
        
        // Calcular total de costos (labores + mantenimiento)
        BigDecimal totalCostos = calcularTotalCostos(usuarioId, fechaInicio, fechaFin);
        
        // Total de costos incluyendo egresos
        BigDecimal totalCostosCompleto = totalCostos.add(totalEgresos);
        
        // Crear balance
        BalanceDTO balance = new BalanceDTO(fechaInicio, fechaFin, totalIngresos, totalCostosCompleto);
        
        // Agregar detalles
        List<DetalleBalanceDTO> detalles = new ArrayList<>();
        detalles.addAll(obtenerDetallesIngresos(usuarioId, fechaInicio, fechaFin));
        detalles.addAll(obtenerDetallesEgresos(usuarioId, fechaInicio, fechaFin));
        detalles.addAll(obtenerDetallesCostos(usuarioId, fechaInicio, fechaFin));
        
        balance.setDetalles(detalles);
        
        return balance;
    }

    /**
     * Calcula el balance para un lote específico.
     */
    public BalanceDTO calcularBalancePorLote(Long loteId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Calcular total de ingresos del lote
        BigDecimal totalIngresos = ingresoRepository.calcularTotalIngresosPorLoteYFecha(loteId, fechaInicio, fechaFin);
        
        // Calcular total de costos del lote
        BigDecimal totalCostos = calcularTotalCostosPorLote(loteId, fechaInicio, fechaFin);
        
        // Crear balance
        BalanceDTO balance = new BalanceDTO(fechaInicio, fechaFin, totalIngresos, totalCostos);
        
        // Agregar detalles
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
        BigDecimal costosLabores = laborRepository.findByUsuarioIdAndFechaLaborBetween(usuarioId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCosto() != null)
                .map(Labor::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal costosMantenimiento = mantenimientoRepository.findByUsuarioIdAndFechaRealizadaBetween(usuarioId, fechaInicio, fechaFin)
                .stream()
                .filter(mant -> mant.getCosto() != null)
                .map(MantenimientoMaquinaria::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return costosLabores.add(costosMantenimiento);
    }

    /**
     * Calcula el total de costos para un lote específico.
     */
    private BigDecimal calcularTotalCostosPorLote(Long loteId, LocalDate fechaInicio, LocalDate fechaFin) {
        return laborRepository.findByLoteIdAndFechaLaborBetween(loteId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCosto() != null)
                .map(Labor::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtiene los detalles de ingresos.
     */
    private List<DetalleBalanceDTO> obtenerDetallesIngresos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return ingresoRepository.findByUsuarioIdAndFechaIngresoBetweenOrderByFechaIngresoDesc(usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(ingreso -> new DetalleBalanceDTO(
                        "INGRESO",
                        ingreso.getConcepto(),
                        ingreso.getFechaIngreso(),
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
        return ingresoRepository.findByLoteIdOrderByFechaIngresoDesc(loteId)
                .stream()
                .filter(ingreso -> !ingreso.getFechaIngreso().isBefore(fechaInicio) && !ingreso.getFechaIngreso().isAfter(fechaFin))
                .map(ingreso -> new DetalleBalanceDTO(
                        "INGRESO",
                        ingreso.getConcepto(),
                        ingreso.getFechaIngreso(),
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
        return egresoRepository.findByUsuarioIdAndFechaEgresoBetweenOrderByFechaEgresoDesc(usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(egreso -> new DetalleBalanceDTO(
                        "EGRESO",
                        egreso.getConcepto(),
                        egreso.getFechaEgreso(),
                        egreso.getMonto(),
                        egreso.getTipoEgreso().toString(),
                        egreso.getLote() != null ? egreso.getLote().getNombre() : null,
                        egreso.getDescripcion()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de costos.
     */
    private List<DetalleBalanceDTO> obtenerDetallesCostos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<DetalleBalanceDTO> detalles = new ArrayList<>();

        // Costos de labores
        laborRepository.findByUsuarioIdAndFechaLaborBetween(usuarioId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCosto() != null)
                .forEach(labor -> detalles.add(new DetalleBalanceDTO(
                        "COSTO",
                        labor.getNombre(),
                        labor.getFechaLabor(),
                        labor.getCosto(),
                        "LABOR - " + labor.getTipoLabor().toString(),
                        labor.getLote() != null ? labor.getLote().getNombre() : null,
                        labor.getDescripcion()
                )));

        // Costos de mantenimiento
        mantenimientoRepository.findByUsuarioIdAndFechaRealizadaBetween(usuarioId, fechaInicio, fechaFin)
                .stream()
                .filter(mant -> mant.getCosto() != null)
                .forEach(mant -> detalles.add(new DetalleBalanceDTO(
                        "COSTO",
                        mant.getTipoMantenimiento().toString(),
                        mant.getFechaRealizada(),
                        mant.getCosto(),
                        "MANTENIMIENTO",
                        null,
                        mant.getDescripcion()
                )));

        return detalles;
    }

    /**
     * Obtiene los detalles de costos por lote.
     */
    private List<DetalleBalanceDTO> obtenerDetallesCostosPorLote(Long loteId, LocalDate fechaInicio, LocalDate fechaFin) {
        return laborRepository.findByLoteIdAndFechaLaborBetween(loteId, fechaInicio, fechaFin)
                .stream()
                .filter(labor -> labor.getCosto() != null)
                .map(labor -> new DetalleBalanceDTO(
                        "COSTO",
                        labor.getNombre(),
                        labor.getFechaLabor(),
                        labor.getCosto(),
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
}
