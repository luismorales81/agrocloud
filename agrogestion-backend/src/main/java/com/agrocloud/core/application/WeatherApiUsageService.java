package com.agrocloud.core.application;

import com.agrocloud.core.domain.WeatherApiUsage;
import com.agrocloud.core.infrastructure.WeatherApiUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para manejar el tracking del uso de la API del clima
 */
@Service("weatherApiUsageServiceCore")
@Transactional
public class WeatherApiUsageService {
    
    @Autowired
    @Qualifier("weatherApiUsageRepositoryCore")
        private WeatherApiUsageRepository weatherApiUsageRepository;

    private final Object candadoRegistroDiario = new Object();
    private TransactionTemplate transaccionNueva;

    @Autowired
    public void configurarTransaccionNueva(PlatformTransactionManager administradorTransacciones) {
        TransactionTemplate plantilla = new TransactionTemplate(administradorTransacciones);
        plantilla.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transaccionNueva = plantilla;
    }
    
    /**
     * Registra un uso de la API del clima.
     * El candado cubre el commit para que dos llamadas simultáneas no inserten la misma fecha.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void registrarUso() {
        synchronized (candadoRegistroDiario) {
            try {
                transaccionNueva.executeWithoutResult(estado -> persistirUso(LocalDate.now()));
            } catch (DataIntegrityViolationException duplicado) {
                transaccionNueva.executeWithoutResult(estado -> persistirUso(LocalDate.now()));
            }
        }
    }

    private void persistirUso(LocalDate hoy) {
        WeatherApiUsage uso = weatherApiUsageRepository.findByFecha(hoy)
                .orElseGet(() -> new WeatherApiUsage(hoy));
        uso.incrementarUso();
        weatherApiUsageRepository.saveAndFlush(uso);
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
