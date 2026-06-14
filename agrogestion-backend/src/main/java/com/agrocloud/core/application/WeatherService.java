package com.agrocloud.core.application;

import com.agrocloud.dto.WeatherDTO;
import com.agrocloud.dto.WeatherForecastDTO;
import com.agrocloud.dto.WeatherCurrentDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service("weatherServiceCore")
public class WeatherService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String OPENWEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5";
    private static final String API_KEY = "8ee79cbd9f27221c0668a98dca8bd466";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("weatherApiUsageServiceCore")
    private WeatherApiUsageService weatherApiUsageService;
    
    public WeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Obtiene el clima actual y pronóstico para las coordenadas especificadas
     * Cacheado por 1 hora para evitar llamadas innecesarias
     */
    @Cacheable(value = "weather", key = "#latitude + '_' + #longitude")
    public WeatherDTO getWeatherData(double latitude, double longitude) {
        try {
            logger.info("Obteniendo datos meteorológicos de OpenWeatherMap para lat: {}, lon: {}", latitude, longitude);
            
            // Validar coordenadas
            if (!isValidCoordinates(latitude, longitude)) {
                throw new IllegalArgumentException("Coordenadas inválidas");
            }
            
            // Verificar si se puede hacer la llamada (no se ha alcanzado el límite)
            if (!weatherApiUsageService.puedeHacerLlamada()) {
                throw new RuntimeException("Límite diario de uso de la API del clima alcanzado (1000 usos/día)");
            }
            
            // Registrar el uso de la API
            weatherApiUsageService.registrarUso();
            
            // Obtener clima actual
            WeatherCurrentDTO currentWeather = getCurrentWeather(latitude, longitude);
            
            // Obtener pronóstico de 7 días
            List<WeatherForecastDTO> forecast = getForecast(latitude, longitude);
            
            // Crear objeto de respuesta
            WeatherDTO weatherData = new WeatherDTO();
            weatherData.setCurrent(currentWeather);
            weatherData.setForecast(forecast);
            weatherData.setLocation("Campo en " + latitude + ", " + longitude);
            weatherData.setLastUpdated(java.time.LocalDateTime.now());
            
            logger.info("Datos meteorológicos obtenidos exitosamente de OpenWeatherMap para lat: {}, lon: {}", latitude, longitude);
            return weatherData;
            
        } catch (Exception e) {
            logger.error("Error obteniendo datos meteorológicos de OpenWeatherMap para lat: {}, lon: {}: {}", 
                        latitude, longitude, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo datos meteorológicos: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el clima actual de OpenWeatherMap
     */
    private WeatherCurrentDTO getCurrentWeather(double latitude, double longitude) {
        String url = String.format("%s/weather?lat=%.4f&lon=%.4f&appid=%s&units=metric&lang=es",
                                 OPENWEATHER_BASE_URL, latitude, longitude, API_KEY);
        
        logger.debug("URL de OpenWeatherMap (current): {}", url);
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error en la respuesta de OpenWeatherMap: " + response.getStatusCode());
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            WeatherCurrentDTO currentWeather = new WeatherCurrentDTO();
            currentWeather.setTemperature(rootNode.get("main").get("temp").asDouble());
            currentWeather.setHumidity(rootNode.get("main").get("humidity").asInt());
            currentWeather.setWindSpeed(rootNode.get("wind").get("speed").asDouble());
            currentWeather.setPrecipitation(0.0); // OpenWeatherMap no proporciona precipitación actual en este endpoint
            
            // Obtener información del clima
            JsonNode weather = rootNode.get("weather").get(0);
            int weatherId = weather.get("id").asInt();
            currentWeather.setWeatherCode(weatherId);
            currentWeather.setWeatherDescription(weather.get("description").asText());
            currentWeather.setIcon(getWeatherIcon(weatherId));
            
            return currentWeather;
            
        } catch (Exception e) {
            logger.error("Error procesando respuesta de OpenWeatherMap: {}", e.getMessage());
            throw new RuntimeException("Error procesando datos del clima actual");
        }
    }
    
    /**
     * Obtiene el pronóstico de 7 días de OpenWeatherMap
     */
    private List<WeatherForecastDTO> getForecast(double latitude, double longitude) {
        String url = String.format("%s/forecast?lat=%.4f&lon=%.4f&appid=%s&units=metric&lang=es&cnt=7",
                                 OPENWEATHER_BASE_URL, latitude, longitude, API_KEY);
        
        logger.debug("URL de OpenWeatherMap (forecast): {}", url);
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error en la respuesta de OpenWeatherMap: " + response.getStatusCode());
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode list = rootNode.get("list");
            
            List<WeatherForecastDTO> forecast = new ArrayList<>();
            
            for (int i = 0; i < list.size(); i++) {
                JsonNode item = list.get(i);
                
                WeatherForecastDTO dayForecast = new WeatherForecastDTO();
                
                // Parsear fecha
                String dateStr = item.get("dt_txt").asText();
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10));
                dayForecast.setDate(date);
                dayForecast.setDayOfWeek(date.format(DateTimeFormatter.ofPattern("EEEE", java.util.Locale.forLanguageTag("es"))));
                
                // Temperaturas
                JsonNode main = item.get("main");
                dayForecast.setMaxTemperature(main.get("temp_max").asDouble());
                dayForecast.setMinTemperature(main.get("temp_min").asDouble());
                
                // Precipitaciones
                JsonNode rain = item.get("rain");
                if (rain != null && rain.has("3h")) {
                    dayForecast.setPrecipitation(rain.get("3h").asDouble());
                } else {
                    dayForecast.setPrecipitation(0.0);
                }
                
                // Código del clima
                JsonNode weather = item.get("weather").get(0);
                int weatherId = weather.get("id").asInt();
                dayForecast.setWeatherCode(weatherId);
                dayForecast.setWeatherDescription(weather.get("description").asText());
                dayForecast.setIcon(getWeatherIcon(weatherId));
                
                // Generar consejo agrícola específico para este día
                String agriculturalAdvice = generateAgriculturalAdvice(dayForecast.getMaxTemperature(), 
                                                                       dayForecast.getMinTemperature(), 
                                                                       dayForecast.getPrecipitation(), 
                                                                       weatherId);
                dayForecast.setAgriculturalAdvice(agriculturalAdvice);
                
                forecast.add(dayForecast);
            }
            
            return forecast;
            
        } catch (Exception e) {
            logger.error("Error procesando pronóstico de OpenWeatherMap: {}", e.getMessage());
            throw new RuntimeException("Error procesando pronóstico del clima");
        }
    }
    
    /**
     * Valida que las coordenadas estén en rangos válidos
     */
    private boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && 
               longitude >= -180 && longitude <= 180;
    }
    
    /**
     * Convierte el código del clima de OpenWeatherMap a emoji/icono
     */
    private String getWeatherIcon(int weatherId) {
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
     * Método simple para obtener datos básicos del clima (sin caché)
     */
    public WeatherDTO getWeatherDataSimple(double latitude, double longitude) {
        try {
            logger.info("Obteniendo datos meteorológicos simples de OpenWeatherMap para lat: {}, lon: {}", latitude, longitude);
            
            // Obtener solo clima actual para simplificar
            WeatherCurrentDTO currentWeather = getCurrentWeather(latitude, longitude);
            
            // Crear objeto de respuesta simple
            WeatherDTO weatherData = new WeatherDTO();
            weatherData.setCurrent(currentWeather);
            weatherData.setForecast(new ArrayList<>()); // Sin pronóstico para simplificar
            weatherData.setLocation("Campo en " + latitude + ", " + longitude);
            weatherData.setLastUpdated(java.time.LocalDateTime.now());
            
            return weatherData;
            
        } catch (Exception e) {
            logger.error("Error obteniendo datos meteorológicos simples: {}", e.getMessage());
            throw new RuntimeException("Error obteniendo datos meteorológicos simples: " + e.getMessage());
        }
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
}
