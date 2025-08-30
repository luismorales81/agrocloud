package com.agrocloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.agrocloud.service.WeatherService;
import com.agrocloud.dto.WeatherDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agrocloud.dto.WeatherCurrentDTO;
import com.agrocloud.dto.WeatherForecastDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/weather-simple")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173"})
public class WeatherSimpleController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherSimpleController.class);
    
    @Autowired
    private WeatherService weatherService;
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Weather simple controller is working!");
    }
    
    @GetMapping("/test-direct")
    public ResponseEntity<String> testDirect() {
        try {
            logger.info("🌤️ [WeatherSimpleController] Probando llamada directa a OpenWeatherMap");
            
            // Llamada directa sin caché para diagnosticar
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=-34.612&lon=-58.396&appid=9dee7c2c4e36ce49c32fab5a51d6e25b&units=metric&lang=es";
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            logger.info("🌤️ [WeatherSimpleController] Respuesta directa de OpenWeatherMap: {}", response.getStatusCode());
            return ResponseEntity.ok("OpenWeatherMap directo funcionando. Status: " + response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("❌ [WeatherSimpleController] Error en llamada directa: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/coordinates")
    public ResponseEntity<WeatherDTO> coordinates(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            logger.info("🌤️ [WeatherSimpleController] Obteniendo clima real para lat: {}, lon: {}", latitude, longitude);
            
            // Usar el método simple en lugar del servicio con caché
            WeatherDTO weatherData = getWeatherDataSimple(latitude, longitude);
            logger.info("🌤️ [WeatherSimpleController] Datos meteorológicos reales obtenidos exitosamente");
            return ResponseEntity.ok(weatherData);
            
        } catch (Exception e) {
            logger.error("❌ [WeatherSimpleController] Error obteniendo clima real: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/coordinates-simple")
    public ResponseEntity<WeatherDTO> coordinatesSimple(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            logger.info("🌤️ [WeatherSimpleController] Obteniendo clima simple para lat: {}, lon: {}", latitude, longitude);
            
            // Versión simplificada sin caché
            WeatherDTO weatherData = getWeatherDataSimple(latitude, longitude);
            logger.info("🌤️ [WeatherSimpleController] Datos meteorológicos simples obtenidos exitosamente");
            return ResponseEntity.ok(weatherData);
            
        } catch (Exception e) {
            logger.error("❌ [WeatherSimpleController] Error obteniendo clima simple: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private WeatherDTO getWeatherDataSimple(double latitude, double longitude) throws Exception {
        try {
            // Construir URL para OpenWeatherMap con la nueva API key
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%.4f&lon=%.4f&appid=9dee7c2c4e36ce49c32fab5a51d6e25b&units=metric&lang=es",
                                     latitude, longitude);
            
            logger.info("🌤️ [WeatherSimpleController] Llamando a OpenWeatherMap: {}", url);
            
            // Llamada a API
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().value() != 200) {
                throw new RuntimeException("Error en respuesta de OpenWeatherMap: " + response.getStatusCode());
            }
            
            logger.info("🌤️ [WeatherSimpleController] Respuesta de OpenWeatherMap recibida");
            
            // Procesar JSON manualmente
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            // Extraer datos actuales de OpenWeatherMap
            WeatherCurrentDTO currentWeather = new WeatherCurrentDTO();
            
            try {
                JsonNode main = rootNode.get("main");
                JsonNode weather = rootNode.get("weather").get(0);
                JsonNode wind = rootNode.get("wind");
                
                currentWeather.setTemperature(main.get("temp").asDouble());
                currentWeather.setHumidity(main.get("humidity").asInt());
                currentWeather.setWindSpeed(wind.get("speed").asDouble());
                currentWeather.setPrecipitation(0.0); // OpenWeatherMap no proporciona precipitación actual en este endpoint
                
                int weatherId = weather.get("id").asInt();
                currentWeather.setWeatherCode(weatherId);
                currentWeather.setWeatherDescription(weather.get("description").asText());
                currentWeather.setIcon(getWeatherIconOpenWeather(weatherId));
                
            } catch (Exception e) {
                logger.error("Error procesando datos actuales: {}", e.getMessage());
                // Valores por defecto si hay error
                currentWeather.setTemperature(25.0);
                currentWeather.setHumidity(65);
                currentWeather.setWindSpeed(12.0);
                currentWeather.setPrecipitation(0.0);
                currentWeather.setWeatherCode(800);
                currentWeather.setWeatherDescription("Clima actual");
                currentWeather.setIcon("🌤️");
            }
            
            // Crear pronóstico simple (solo datos actuales por ahora)
            List<WeatherForecastDTO> forecast = new ArrayList<>();
            
            // Crear pronóstico por defecto ya que este endpoint solo da datos actuales
            for (int i = 0; i < 7; i++) {
                WeatherForecastDTO day = new WeatherForecastDTO();
                LocalDate date = LocalDate.now().plusDays(i);
                day.setDate(date);
                day.setDayOfWeek("Día " + (i + 1));
                day.setMaxTemperature(28.0);
                day.setMinTemperature(15.0);
                day.setPrecipitation(0.0);
                day.setWeatherCode(800);
                day.setWeatherDescription("Soleado");
                day.setIcon("☀️");
                forecast.add(day);
            }
            
            // Crear objeto de respuesta
            WeatherDTO weatherData = new WeatherDTO();
            weatherData.setCurrent(currentWeather);
            weatherData.setForecast(forecast);
            weatherData.setLocation("Campo en " + latitude + ", " + longitude);
            weatherData.setLastUpdated(LocalDateTime.now());
            
            logger.info("🌤️ [WeatherSimpleController] Datos meteorológicos procesados exitosamente");
            return weatherData;
            
        } catch (Exception e) {
            logger.error("❌ [WeatherSimpleController] Error general en getWeatherDataSimple: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private String getWeatherIconOpenWeather(int weatherId) {
        // Códigos de OpenWeatherMap
        if (weatherId >= 200 && weatherId < 300) return "⛈️"; // Tormenta
        if (weatherId >= 300 && weatherId < 400) return "🌦️"; // Llovizna
        if (weatherId >= 500 && weatherId < 600) return "🌧️"; // Lluvia
        if (weatherId >= 600 && weatherId < 700) return "❄️"; // Nieve
        if (weatherId >= 700 && weatherId < 800) return "🌫️"; // Atmósfera (niebla, etc.)
        if (weatherId == 800) return "☀️"; // Despejado
        if (weatherId == 801) return "🌤️"; // Pocas nubes
        if (weatherId == 802) return "⛅"; // Nubes dispersas
        if (weatherId >= 803 && weatherId <= 804) return "☁️"; // Nublado
        return "🌤️"; // Por defecto
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Weather simple health endpoint is working!");
    }
    
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<WeatherDTO> field(@PathVariable Long fieldId) {
        try {
            logger.info("🌤️ [WeatherSimpleController] Obteniendo clima real para campo ID: {}", fieldId);
            
            // Coordenadas simuladas para campos específicos
            double latitude, longitude;
            switch (fieldId.intValue()) {
                case 1: // Campo Norte
                    latitude = -34.612;
                    longitude = -58.396;
                    break;
                case 2: // Campo Sur
                    latitude = -34.650;
                    longitude = -58.380;
                    break;
                case 3: // Campo Este
                    latitude = -34.580;
                    longitude = -58.420;
                    break;
                default:
                    latitude = -34.612;
                    longitude = -58.396;
            }
            
            // Usar el método simple en lugar del servicio con caché
            WeatherDTO weatherData = getWeatherDataSimple(latitude, longitude);
            logger.info("🌤️ [WeatherSimpleController] Datos meteorológicos reales obtenidos para campo {}", fieldId);
            return ResponseEntity.ok(weatherData);
        } catch (Exception e) {
            logger.error("❌ [WeatherSimpleController] Error obteniendo clima real para campo {}: {}", fieldId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
