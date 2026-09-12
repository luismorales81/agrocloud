package com.agrocloud.controller;

import com.agrocloud.dto.WeatherDTO;
import com.agrocloud.dto.WeatherCurrentDTO;
import com.agrocloud.core.application.WeatherService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);
    
    @Autowired
    @Qualifier("weatherServiceCore")
    private WeatherService weatherService;
    
    /**
     * Obtiene datos meteorológicos para coordenadas específicas
     */
    @GetMapping("/coordinates")
    public ResponseEntity<WeatherDTO> getWeatherByCoordinates(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        
        try {
            logger.info("Solicitud de clima para lat: {}, lon: {}", latitude, longitude);
            
            WeatherDTO weatherData = weatherService.getWeatherData(latitude, longitude);
            
            return ResponseEntity.ok(weatherData);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error obteniendo datos meteorológicos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtiene datos meteorológicos para un campo específico
     * (asumiendo que el campo tiene coordenadas almacenadas)
     */
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<WeatherDTO> getWeatherByField(@PathVariable Long fieldId) {
        try {
            logger.info("Solicitud de clima para campo ID: {}", fieldId);
            
            // TODO: Obtener coordenadas del campo desde la base de datos
            // Por ahora, usamos coordenadas de ejemplo para Buenos Aires
            double latitude = -34.6118;
            double longitude = -58.3960;
            
            WeatherDTO weatherData = weatherService.getWeatherData(latitude, longitude);
            
            return ResponseEntity.ok(weatherData);
            
        } catch (Exception e) {
            logger.error("Error obteniendo clima para campo {}: {}", fieldId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint de salud para verificar que el servicio esté funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        try {
            logger.info("Health check del servicio meteorológico");
            return ResponseEntity.ok("Weather service is running");
        } catch (Exception e) {
            logger.error("Error en health check: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
