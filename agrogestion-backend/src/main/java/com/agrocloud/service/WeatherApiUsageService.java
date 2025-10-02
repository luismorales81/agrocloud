package com.agrocloud.service;

import com.agrocloud.model.entity.WeatherApiUsage;
import com.agrocloud.repository.WeatherApiUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para manejar el tracking del uso de la API del clima
 */
@Service
@Transactional
public class WeatherApiUsageService {
    
    @Autowired
    private WeatherApiUsageRepository weatherApiUsageRepository;
    
    /**
     * Registra un uso de la API del clima
     */
    public void registrarUso() {
        LocalDate hoy = LocalDate.now();
        Optional<WeatherApiUsage> usageOpt = weatherApiUsageRepository.findByFecha(hoy);
        
        WeatherApiUsage usage;
        if (usageOpt.isPresent()) {
            usage = usageOpt.get();
            usage.incrementarUso();
        } else {
            usage = new WeatherApiUsage(hoy);
            usage.incrementarUso();
        }
        
        weatherApiUsageRepository.save(usage);
    }
    
    /**
     * Obtiene las estadísticas de uso de la API del clima para hoy
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasUsoHoy() {
        LocalDate hoy = LocalDate.now();
        Optional<WeatherApiUsage> usageOpt = weatherApiUsageRepository.findByFecha(hoy);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        if (usageOpt.isPresent()) {
            WeatherApiUsage usage = usageOpt.get();
            estadisticas.put("usosHoy", usage.getUsosHoy());
            estadisticas.put("limiteDiario", usage.getLimiteDiario());
            estadisticas.put("porcentajeUso", Math.round(usage.getPorcentajeUso() * 100.0) / 100.0);
            estadisticas.put("limiteAlcanzado", usage.isLimiteAlcanzado());
            estadisticas.put("usosRestantes", usage.getLimiteDiario() - usage.getUsosHoy());
        } else {
            // Si no hay registro para hoy, crear uno nuevo
            estadisticas.put("usosHoy", 0);
            estadisticas.put("limiteDiario", 1000);
            estadisticas.put("porcentajeUso", 0.0);
            estadisticas.put("limiteAlcanzado", false);
            estadisticas.put("usosRestantes", 1000);
        }
        
        estadisticas.put("fecha", hoy);
        return estadisticas;
    }
    
    /**
     * Obtiene las estadísticas de uso de la API del clima para los últimos 7 días
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasUsoSemanal() {
        LocalDate hoy = LocalDate.now();
        LocalDate hace7Dias = hoy.minusDays(7);
        
        Long totalUsos = weatherApiUsageRepository.getTotalUsosEnRango(hace7Dias, hoy);
        Double promedioDiario = weatherApiUsageRepository.getPromedioUsosDiarios(hace7Dias);
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalUsos7Dias", totalUsos);
        estadisticas.put("promedioDiario", Math.round(promedioDiario * 100.0) / 100.0);
        estadisticas.put("fechaInicio", hace7Dias);
        estadisticas.put("fechaFin", hoy);
        
        return estadisticas;
    }
    
    /**
     * Verifica si se puede hacer una llamada a la API (no se ha alcanzado el límite)
     */
    @Transactional(readOnly = true)
    public boolean puedeHacerLlamada() {
        LocalDate hoy = LocalDate.now();
        Optional<WeatherApiUsage> usageOpt = weatherApiUsageRepository.findByFecha(hoy);
        
        if (usageOpt.isPresent()) {
            return !usageOpt.get().isLimiteAlcanzado();
        }
        
        return true; // Si no hay registro para hoy, se puede hacer la llamada
    }
    
    /**
     * Obtiene el registro de uso para hoy, creándolo si no existe
     */
    @Transactional(readOnly = true)
    public WeatherApiUsage obtenerRegistroHoy() {
        LocalDate hoy = LocalDate.now();
        Optional<WeatherApiUsage> usageOpt = weatherApiUsageRepository.findByFecha(hoy);
        
        if (usageOpt.isPresent()) {
            return usageOpt.get();
        } else {
            // Crear un nuevo registro para hoy
            WeatherApiUsage nuevoRegistro = new WeatherApiUsage(hoy);
            return weatherApiUsageRepository.save(nuevoRegistro);
        }
    }
}
