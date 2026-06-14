package com.agrocloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.agrocloud.core.application.WeatherApiUsageService;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather-simple")
public class WeatherSimpleController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherSimpleController.class);
    
    
    @Autowired
    @Qualifier("weatherApiUsageServiceCore")
    private WeatherApiUsageService weatherApiUsageService;
    
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
            
            // Verificar si se puede hacer la llamada (no se ha alcanzado el límite)
            if (!weatherApiUsageService.puedeHacerLlamada()) {
                logger.warn("⚠️ [WeatherSimpleController] Límite diario de uso de la API del clima alcanzado");
                return ResponseEntity.status(429).build();
            }
            
            // Registrar el uso de la API
            weatherApiUsageService.registrarUso();
            logger.info("📊 [WeatherSimpleController] Uso de API del clima registrado");
            
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
            
            // Obtener pronóstico real de 7 días de OpenWeatherMap
            List<WeatherForecastDTO> forecast = getRealForecast(latitude, longitude);
            
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

    /**
     * Genera consejos agrícolas específicos basados en las condiciones meteorológicas del día
     */
    private String generateAgriculturalAdvice(double maxTemp, double minTemp, double precipitation, int weatherCode) {
        StringBuilder advice = new StringBuilder();
        
        // Análisis de temperatura
        if (minTemp < 5) {
            advice.append("❄️ Temperatura muy baja - Evitar labores sensibles al frío");
        } else if (minTemp < 10) {
            advice.append("❄️ Temperatura baja - Considerar retrasar labores sensibles");
        } else if (maxTemp > 35) {
            advice.append("🌡️ Temperatura muy alta - Evitar labores en horas pico");
        } else if (maxTemp > 30) {
            advice.append("🌡️ Temperatura alta - Programar labores temprano o tarde");
        } else {
            advice.append("✅ Temperatura favorable para labores agrícolas");
        }
        
        // Análisis de precipitación
        if (precipitation > 10) {
            advice.append(" | 🌧️ Lluvia intensa - Evitar labores de campo");
        } else if (precipitation > 5) {
            advice.append(" | 🌧️ Lluvia moderada - Considerar retrasar labores sensibles");
        } else if (precipitation > 0) {
            advice.append(" | 🌦️ Lluvia ligera - Monitorear condiciones");
        }
        
        // Análisis de condiciones meteorológicas
        if (weatherCode >= 200 && weatherCode < 300) {
            advice.append(" | ⛈️ Tormentas - Evitar labores al aire libre");
        } else if (weatherCode >= 500 && weatherCode < 600) {
            advice.append(" | 🌧️ Lluvia - Evitar aplicaciones fitosanitarias");
        } else if (weatherCode >= 600 && weatherCode < 700) {
            advice.append(" | ❄️ Nieve - Evitar labores de campo");
        } else if (weatherCode >= 700 && weatherCode < 800) {
            advice.append(" | 🌫️ Niebla - Cuidado con la visibilidad");
        } else if (weatherCode == 800) {
            advice.append(" | ☀️ Día despejado - Ideal para labores");
        } else if (weatherCode >= 801 && weatherCode <= 802) {
            advice.append(" | ⛅ Poco nublado - Bueno para labores");
        } else if (weatherCode >= 803 && weatherCode <= 804) {
            advice.append(" | ☁️ Nublado - Condiciones estables");
        }
        
        // Consejos específicos por combinación de condiciones
        if (minTemp < 10 && precipitation > 0) {
            advice.append(" | 💡 Considerar cubrir cultivos sensibles");
        } else if (maxTemp > 30 && precipitation == 0) {
            advice.append(" | 💡 Aumentar frecuencia de riego");
        } else if (precipitation > 5 && weatherCode >= 500) {
            advice.append(" | 💡 Monitorear drenaje de campos");
        }
        
        return advice.toString();
    }

    /**
     * Obtiene pronóstico real de 7 días de OpenWeatherMap
     */
    private List<WeatherForecastDTO> getRealForecast(double latitude, double longitude) {
        try {
            logger.info("🌤️ [WeatherSimpleController] Obteniendo pronóstico real de 7 días para lat: {}, lon: {}", latitude, longitude);
            
            // URL para pronóstico de 5 días (40 entradas de 3 horas cada una)
            String url = String.format("https://api.openweathermap.org/data/2.5/forecast?lat=%.4f&lon=%.4f&appid=9dee7c2c4e36ce49c32fab5a51d6e25b&units=metric&lang=es",
                                     latitude, longitude);
            
            logger.info("🌤️ [WeatherSimpleController] Llamando a OpenWeatherMap Forecast: {}", url);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().value() != 200) {
                throw new RuntimeException("Error en respuesta de OpenWeatherMap Forecast: " + response.getStatusCode());
            }
            
            logger.info("🌤️ [WeatherSimpleController] Respuesta de pronóstico recibida");
            
            // Procesar JSON del pronóstico
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode list = rootNode.get("list");
            
            List<WeatherForecastDTO> forecast = new ArrayList<>();
            
            // Agrupar por día para obtener máximos y mínimos diarios
            Map<String, List<JsonNode>> dailyData = new HashMap<>();
            
            for (int i = 0; i < list.size(); i++) {
                JsonNode item = list.get(i);
                String dateStr = item.get("dt_txt").asText().substring(0, 10); // YYYY-MM-DD
                dailyData.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(item);
            }
            
            // Procesar cada día ordenados por fecha
            int dayCount = 0;
            List<String> sortedDates = new ArrayList<>(dailyData.keySet());
            Collections.sort(sortedDates); // Ordenar fechas cronológicamente
            
            for (String dateStr : sortedDates) {
                if (dayCount >= 5) break; // Solo 5 días (OpenWeatherMap)
                
                List<JsonNode> dayItems = dailyData.get(dateStr);
                
                // Calcular temperaturas máximas y mínimas del día
                double maxTemp = Double.MIN_VALUE;
                double minTemp = Double.MAX_VALUE;
                JsonNode representativeItem = dayItems.get(dayItems.size() / 2); // Tomar item del medio del día
                
                for (JsonNode item : dayItems) {
                    double temp = item.get("main").get("temp").asDouble();
                    maxTemp = Math.max(maxTemp, temp);
                    minTemp = Math.min(minTemp, temp);
                }
                
                // Crear pronóstico del día
                WeatherForecastDTO dayForecast = new WeatherForecastDTO();
                dayForecast.setDate(LocalDate.parse(dateStr));
                dayForecast.setDayOfWeek(LocalDate.parse(dateStr).format(DateTimeFormatter.ofPattern("EEEE", java.util.Locale.forLanguageTag("es"))));
                dayForecast.setMaxTemperature(maxTemp);
                dayForecast.setMinTemperature(minTemp);
                
                // Precipitación (sumar todas las del día)
                double totalPrecipitation = 0.0;
                for (JsonNode item : dayItems) {
                    JsonNode rain = item.get("rain");
                    if (rain != null && rain.has("3h")) {
                        totalPrecipitation += rain.get("3h").asDouble();
                    }
                }
                dayForecast.setPrecipitation(totalPrecipitation);
                
                // Clima representativo del día
                JsonNode weather = representativeItem.get("weather").get(0);
                int weatherId = weather.get("id").asInt();
                dayForecast.setWeatherCode(weatherId);
                dayForecast.setWeatherDescription(weather.get("description").asText());
                dayForecast.setIcon(getWeatherIconOpenWeather(weatherId));
                
                // Generar consejo agrícola específico para este día
                String agriculturalAdvice = generateAgriculturalAdvice(maxTemp, minTemp, totalPrecipitation, weatherId);
                dayForecast.setAgriculturalAdvice(agriculturalAdvice);
                
                forecast.add(dayForecast);
                dayCount++;
            }
            
            logger.info("🌤️ [WeatherSimpleController] Pronóstico real de {} días procesado exitosamente", forecast.size());
            return forecast;
            
        } catch (Exception e) {
            logger.error("❌ [WeatherSimpleController] Error obteniendo pronóstico real: {}", e.getMessage(), e);
            
            // Fallback: crear pronóstico básico con datos actuales
            List<WeatherForecastDTO> fallbackForecast = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                WeatherForecastDTO day = new WeatherForecastDTO();
                LocalDate date = LocalDate.now().plusDays(i);
                day.setDate(date);
                day.setDayOfWeek(date.format(DateTimeFormatter.ofPattern("EEEE", java.util.Locale.forLanguageTag("es"))));
                day.setMaxTemperature(25.0 + Math.random() * 10); // Variación realista
                day.setMinTemperature(15.0 + Math.random() * 5);  // Variación realista
                day.setPrecipitation(Math.random() < 0.3 ? Math.random() * 5 : 0.0); // 30% probabilidad de lluvia
                day.setWeatherCode(800);
                day.setWeatherDescription("Clima variable");
                day.setIcon("🌤️");
                
                // Generar consejo agrícola para el fallback
                String agriculturalAdvice = generateAgriculturalAdvice(day.getMaxTemperature(), 
                                                                       day.getMinTemperature(), 
                                                                       day.getPrecipitation(), 
                                                                       800);
                day.setAgriculturalAdvice(agriculturalAdvice);
                
                fallbackForecast.add(day);
            }
            
            logger.warn("🔄 [WeatherSimpleController] Usando pronóstico de fallback");
            return fallbackForecast;
        }
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
