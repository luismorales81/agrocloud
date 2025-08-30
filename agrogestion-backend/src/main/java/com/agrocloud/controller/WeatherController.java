package com.agrocloud.controller;

import com.agrocloud.dto.WeatherDTO;
import com.agrocloud.dto.WeatherCurrentDTO;
import com.agrocloud.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/weather")
@CrossOrigin(origins = "*")
public class WeatherController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);
    
    @Autowired
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

    /**
     * Endpoint de prueba sin usar el servicio
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        try {
            logger.info("Test endpoint sin WeatherService");
            return ResponseEntity.ok("Weather controller is working without service");
        } catch (Exception e) {
            logger.error("Error en test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint de prueba con datos simulados
     */
    @GetMapping("/test-data")
    public ResponseEntity<WeatherDTO> testData() {
        try {
            logger.info("Test endpoint con datos simulados");
            
            // Crear datos simulados
            WeatherCurrentDTO current = new WeatherCurrentDTO();
            current.setTemperature(25.0);
            current.setHumidity(65);
            current.setWindSpeed(12.0);
            current.setPrecipitation(0.0);
            current.setWeatherCode(1);
            current.setWeatherDescription("Mayormente despejado");
            current.setIcon("🌤️");
            
            WeatherDTO weatherData = new WeatherDTO();
            weatherData.setCurrent(current);
            weatherData.setForecast(new ArrayList<>());
            weatherData.setLocation("Campo de prueba");
            weatherData.setLastUpdated(java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(weatherData);
            
        } catch (Exception e) {
            logger.error("Error en test-data endpoint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
