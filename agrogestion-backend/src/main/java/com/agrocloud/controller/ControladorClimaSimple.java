package com.agrocloud.controller;

import com.agrocloud.cultivos.application.UtilCentroideCoordenadasCampo;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.dto.WeatherCurrentDTO;
import com.agrocloud.dto.WeatherDTO;
import com.agrocloud.dto.WeatherForecastDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Clima que consume el frontend en /api/v1/weather-simple.
 * Usa Open-Meteo, que no exige clave y cubre Santiago del Estero.
 */
@RestController
@RequestMapping("/api/v1/weather-simple")
public class ControladorClimaSimple {

    private static final Logger logger = LoggerFactory.getLogger(ControladorClimaSimple.class);
    private static final String URL_PRONOSTICO = "https://api.open-meteo.com/v1/forecast";
    private static final Locale ESPANOL = Locale.forLanguageTag("es");

    private final FieldRepository repositorioCampos;
    private final ObjectMapper mapeadorJson;
    private final RestTemplate clienteHttp = new RestTemplate();

    public ControladorClimaSimple(
            @Qualifier("fieldRepositoryCultivos") FieldRepository repositorioCampos,
            ObjectMapper mapeadorJson) {
        this.repositorioCampos = repositorioCampos;
        this.mapeadorJson = mapeadorJson;
    }

    @GetMapping("/coordinates")
    public ResponseEntity<WeatherDTO> coordenadas(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(obtenerClima(latitude, longitude));
    }

    @GetMapping("/coordinates-simple")
    public ResponseEntity<WeatherDTO> coordenadasSimples(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return coordenadas(latitude, longitude);
    }

    @GetMapping("/field/{campoId}")
    public ResponseEntity<WeatherDTO> climaDeCampo(@PathVariable Long campoId) {
        Optional<Field> campo = repositorioCampos.findById(campoId);
        if (campo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<double[]> punto = UtilCentroideCoordenadasCampo.calcularCentroide(
                campo.get().getCoordenadas(), mapeadorJson);
        if (punto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        WeatherDTO clima = obtenerClima(punto.get()[0], punto.get()[1]);
        clima.setLocation(campo.get().getNombre());
        return ResponseEntity.ok(clima);
    }

    @GetMapping("/health")
    public ResponseEntity<String> salud() {
        return ResponseEntity.ok("Servicio de clima disponible");
    }

    private WeatherDTO obtenerClima(double latitud, double longitud) {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Coordenadas inválidas");
        }
        String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,weather_code"
                        + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum"
                        + "&timezone=America/Argentina/Buenos_Aires&forecast_days=7&wind_speed_unit=ms",
                URL_PRONOSTICO, latitud, longitud);
        logger.info("Consultando clima para lat: {}, lon: {}", latitud, longitud);
        String cuerpo = clienteHttp.getForObject(url, String.class);
        try {
            JsonNode raiz = mapeadorJson.readTree(cuerpo);
            WeatherDTO clima = new WeatherDTO();
            clima.setCurrent(climaActual(raiz.path("current")));
            clima.setForecast(pronostico(raiz.path("daily")));
            clima.setLocation("Campo en " + latitud + ", " + longitud);
            clima.setLastUpdated(java.time.LocalDateTime.now());
            return clima;
        } catch (Exception e) {
            logger.error("No se pudo interpretar el clima: {}", e.getMessage());
            throw new IllegalStateException("No se pudo obtener el clima");
        }
    }

    private WeatherCurrentDTO climaActual(JsonNode actual) {
        int codigo = actual.path("weather_code").asInt();
        WeatherCurrentDTO dto = new WeatherCurrentDTO();
        dto.setTemperature(actual.path("temperature_2m").asDouble());
        dto.setHumidity(actual.path("relative_humidity_2m").asInt());
        dto.setWindSpeed(actual.path("wind_speed_10m").asDouble());
        dto.setPrecipitation(actual.path("precipitation").asDouble());
        dto.setWeatherCode(codigo);
        dto.setWeatherDescription(descripcion(codigo));
        dto.setIcon(icono(codigo));
        return dto;
    }

    private List<WeatherForecastDTO> pronostico(JsonNode diario) {
        JsonNode fechas = diario.path("time");
        List<WeatherForecastDTO> dias = new ArrayList<>();
        for (int i = 0; i < fechas.size(); i++) {
            int codigo = diario.path("weather_code").path(i).asInt();
            double maxima = diario.path("temperature_2m_max").path(i).asDouble();
            double minima = diario.path("temperature_2m_min").path(i).asDouble();
            double lluvia = diario.path("precipitation_sum").path(i).asDouble();
            LocalDate fecha = LocalDate.parse(fechas.path(i).asText(), DateTimeFormatter.ISO_LOCAL_DATE);
            WeatherForecastDTO dia = new WeatherForecastDTO();
            dia.setDate(fecha);
            dia.setDayOfWeek(fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, ESPANOL));
            dia.setMaxTemperature(maxima);
            dia.setMinTemperature(minima);
            dia.setPrecipitation(lluvia);
            dia.setWeatherCode(codigo);
            dia.setWeatherDescription(descripcion(codigo));
            dia.setIcon(icono(codigo));
            dia.setAgriculturalAdvice(consejo(maxima, minima, lluvia, codigo));
            dias.add(dia);
        }
        return dias;
    }

    private String descripcion(int codigo) {
        if (codigo == 0) return "Despejado";
        if (codigo == 1) return "Mayormente despejado";
        if (codigo == 2) return "Parcialmente nublado";
        if (codigo == 3) return "Nublado";
        if (codigo == 45 || codigo == 48) return "Niebla";
        if (codigo >= 51 && codigo <= 57) return "Llovizna";
        if (codigo >= 61 && codigo <= 67) return "Lluvia";
        if (codigo >= 71 && codigo <= 77) return "Nieve";
        if (codigo >= 80 && codigo <= 82) return "Chubascos";
        if (codigo >= 85 && codigo <= 86) return "Chubascos de nieve";
        if (codigo >= 95) return "Tormenta";
        return "Condición variable";
    }

    private String icono(int codigo) {
        if (codigo == 0) return "☀️";
        if (codigo == 1) return "🌤️";
        if (codigo == 2) return "⛅";
        if (codigo == 3) return "☁️";
        if (codigo == 45 || codigo == 48) return "🌫️";
        if (codigo >= 51 && codigo <= 67) return "🌧️";
        if (codigo >= 71 && codigo <= 77) return "❄️";
        if (codigo >= 80 && codigo <= 82) return "🌦️";
        if (codigo >= 95) return "⛈️";
        return "🌤️";
    }

    private String consejo(double maxima, double minima, double lluvia, int codigo) {
        StringBuilder texto = new StringBuilder();
        if (minima < 10) {
            texto.append("Temperatura baja: conviene retrasar labores sensibles al frío");
        } else if (maxima > 35) {
            texto.append("Temperatura muy alta: evitar labores en las horas de más calor");
        } else if (maxima > 30) {
            texto.append("Temperatura alta: programar labores temprano o al final del día");
        } else {
            texto.append("Temperatura favorable para labores");
        }
        if (lluvia > 5 || (codigo >= 61 && codigo <= 82) || codigo >= 95) {
            texto.append(". Hay lluvia: evitar pulverizaciones y revisar el drenaje");
        } else if (lluvia > 0) {
            texto.append(". Lluvia leve: monitorear antes de entrar al lote");
        }
        return texto.toString();
    }
}
