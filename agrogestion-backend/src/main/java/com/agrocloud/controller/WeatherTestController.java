package com.agrocloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather-test")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173"})
public class WeatherTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherTestController.class);
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("🌤️ [WeatherTestController] Endpoint de prueba funcionando");
        return ResponseEntity.ok("Weather test controller is working! " + LocalDateTime.now());
    }
    
    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simple(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            logger.info("🌤️ [WeatherTestController] Obteniendo clima simple para lat: {}, lon: {}", latitude, longitude);
            
            // Datos de prueba estáticos
            Map<String, Object> weatherData = new HashMap<>();
            
            // Datos actuales
            Map<String, Object> current = new HashMap<>();
            current.put("temperature", 25.5);
            current.put("humidity", 65);
            current.put("windSpeed", 12.0);
            current.put("precipitation", 0.0);
            current.put("weatherCode", 1);
            current.put("weatherDescription", "Mayormente despejado");
            current.put("icon", "🌤️");
            
            weatherData.put("current", current);
            weatherData.put("location", "Campo en " + latitude + ", " + longitude);
            weatherData.put("lastUpdated", LocalDateTime.now().toString());
            
            logger.info("🌤️ [WeatherTestController] Datos de prueba enviados exitosamente");
            return ResponseEntity.ok(weatherData);
            
        } catch (Exception e) {
            logger.error("❌ [WeatherTestController] Error en endpoint simple: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Weather test health endpoint is working!");
    }
}
