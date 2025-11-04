package com.agrocloud.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para cálculos de rendimiento de cultivos
 * Migrado desde el frontend para centralizar la lógica de negocio
 */
@Service
public class RendimientoService {

    /**
     * Calcula el rendimiento real de una cosecha en la unidad del cultivo
     * 
     * @param cantidad Cantidad cosechada
     * @param unidadCantidad Unidad de la cantidad cosechada (kg, tn, qq)
     * @param superficie Superficie del lote en hectáreas
     * @param unidadCultivo Unidad esperada del cultivo (kg/ha, tn/ha, qq/ha)
     * @return Rendimiento real en la unidad del cultivo por hectárea
     */
    public BigDecimal calcularRendimientoReal(
            BigDecimal cantidad, 
            String unidadCantidad, 
            BigDecimal superficie, 
            String unidadCultivo) {
        
        if (superficie == null || superficie.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Paso 1: Convertir la cantidad cosechada a kilogramos
        BigDecimal cantidadEnKg = convertirAKilogramos(cantidad, unidadCantidad);
        
        // Paso 2: Convertir de kg a la unidad del cultivo
        BigDecimal cantidadEnUnidadCultivo = convertirDesdeKilogramos(cantidadEnKg, unidadCultivo);
        
        // Paso 3: Calcular rendimiento por hectárea
        BigDecimal rendimiento = cantidadEnUnidadCultivo.divide(superficie, 2, RoundingMode.HALF_UP);
        
        return rendimiento;
    }
    
    /**
     * Convierte una cantidad a kilogramos según su unidad
     * 
     * @param cantidad Cantidad a convertir
     * @param unidad Unidad original (kg, tn, qq)
     * @return Cantidad en kilogramos
     */
    private BigDecimal convertirAKilogramos(BigDecimal cantidad, String unidad) {
        if (unidad == null) {
            return cantidad; // Asumir kg por defecto
        }
        
        String unidadLower = unidad.toLowerCase();
        
        if (unidadLower.contains("tn") || unidadLower.contains("tonelada")) {
            // 1 tonelada = 1000 kg
            return cantidad.multiply(new BigDecimal("1000"));
        } else if (unidadLower.contains("qq") || unidadLower.contains("quintal")) {
            // 1 quintal = 46 kg (estándar argentino)
            return cantidad.multiply(new BigDecimal("46"));
        } else {
            // Ya está en kg
            return cantidad;
        }
    }
    
    /**
     * Convierte una cantidad en kilogramos a la unidad objetivo
     * 
     * @param cantidadEnKg Cantidad en kilogramos
     * @param unidadObjetivo Unidad objetivo (kg/ha, tn/ha, qq/ha)
     * @return Cantidad en la unidad objetivo
     */
    private BigDecimal convertirDesdeKilogramos(BigDecimal cantidadEnKg, String unidadObjetivo) {
        if (unidadObjetivo == null) {
            return cantidadEnKg; // Mantener en kg por defecto
        }
        
        String unidadLower = unidadObjetivo.toLowerCase();
        
        if (unidadLower.contains("tn") || unidadLower.contains("tonelada")) {
            // kg a toneladas
            return cantidadEnKg.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        } else if (unidadLower.contains("qq") || unidadLower.contains("quintal")) {
            // kg a quintales
            return cantidadEnKg.divide(new BigDecimal("46"), 2, RoundingMode.HALF_UP);
        } else {
            // Mantener en kg
            return cantidadEnKg;
        }
    }
    
    /**
     * Calcula la diferencia porcentual entre el rendimiento esperado y el real
     * 
     * @param rendimientoEsperado Rendimiento esperado
     * @param rendimientoReal Rendimiento real obtenido
     * @return Diferencia porcentual (positivo = supera expectativa, negativo = no alcanza)
     */
    public BigDecimal calcularDiferenciaPorcentual(BigDecimal rendimientoEsperado, BigDecimal rendimientoReal) {
        if (rendimientoEsperado == null || rendimientoEsperado.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if (rendimientoReal == null) {
            return new BigDecimal("-100"); // -100% si no hay rendimiento real
        }
        
        // ((real - esperado) / esperado) * 100
        BigDecimal diferencia = rendimientoReal.subtract(rendimientoEsperado);
        BigDecimal porcentaje = diferencia
                .divide(rendimientoEsperado, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        return porcentaje.setScale(2, RoundingMode.HALF_UP);
    }
}














