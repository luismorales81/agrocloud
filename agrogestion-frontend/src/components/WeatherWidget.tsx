import React, { useState, useEffect } from 'react';
import { climaService } from '../services/climaService';

interface WeatherData {
  location: string;
  temperature: number;
  description: string;
  humidity: number;
  windSpeed: number;
  pressure: number;
  icon: string;
  forecast: ForecastDay[];
  alerts: WeatherAlert[];
  coordinates?: {
    lat: number;
    lon: number;
  };
}

interface ForecastDay {
  date: string;
  temp: {
    min: number;
    max: number;
  };
  description: string;
  icon: string;
  humidity: number;
  windSpeed: number;
}

interface WeatherAlert {
  event: string;
  description: string;
  severity: 'warning' | 'watch' | 'advisory';
}

const WeatherWidget: React.FC = () => {
  const [weatherData, setWeatherData] = useState<WeatherData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [location, setLocation] = useState<string>('Detectando ubicación...');
  const [showForecast, setShowForecast] = useState(false);
  const [showAlerts, setShowAlerts] = useState(false);
  const [coordinates, setCoordinates] = useState<{lat: number, lon: number} | null>(null);
  const [locationPermission, setLocationPermission] = useState<'granted' | 'denied' | 'prompt'>('prompt');

  useEffect(() => {
    getCurrentLocation();
  }, []);

  useEffect(() => {
    if (coordinates) {
      fetchWeatherData();
      // Actualizar cada 30 minutos
      const interval = setInterval(fetchWeatherData, 30 * 60 * 1000);
      return () => clearInterval(interval);
    }
  }, [coordinates]);

  const getCurrentLocation = () => {
    console.log('🔧 [WeatherWidget] Solicitando ubicación del navegador...');
    
    if (!navigator.geolocation) {
      setError('Geolocalización no soportada por este navegador');
      setLoading(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        console.log('✅ [WeatherWidget] Ubicación obtenida:', position.coords);
        const { latitude, longitude } = position.coords;
        setCoordinates({ lat: latitude, lon: longitude });
        setLocationPermission('granted');
      },
      (error) => {
        console.error('❌ [WeatherWidget] Error obteniendo ubicación:', error);
        setLocationPermission('denied');
        setError('No se pudo obtener la ubicación. Usando ubicación por defecto.');
        // Usar ubicación por defecto (Buenos Aires)
        setCoordinates({ lat: -34.6118, lon: -58.3960 });
        setLocation('Buenos Aires, AR');
        setLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000 // 5 minutos
      }
    );
  };

  const mapearRespuestaClima = (
    datos: Awaited<ReturnType<typeof climaService.obtenerPorCoordenadas>>,
    coords: { lat: number; lon: number }
  ): WeatherData => {
    const actual = datos.current;
    const pronostico: ForecastDay[] = (datos.forecast ?? []).slice(0, 5).map((dia) => ({
      date: typeof dia.date === 'string' ? dia.date : String(dia.date),
      temp: { min: Math.round(dia.minTemperature), max: Math.round(dia.maxTemperature) },
      description: dia.weatherDescription,
      icon: dia.icon,
      humidity: actual.humidity,
      windSpeed: Math.round(actual.windSpeed * 3.6),
    }));
    const resultado: WeatherData = {
      location: datos.location || 'Ubicación actual',
      temperature: Math.round(actual.temperature),
      description: actual.weatherDescription,
      humidity: actual.humidity,
      windSpeed: Math.round(actual.windSpeed * 3.6),
      pressure: 1013,
      icon: actual.icon,
      coordinates: coords,
      forecast: pronostico,
      alerts: [],
    };
    if (resultado.temperature > 30 || resultado.windSpeed > 20) {
      resultado.alerts.push({
        event: 'Condiciones Adversas',
        description:
          resultado.temperature > 30
            ? 'Temperatura alta - Evitar labores en horas pico'
            : 'Viento fuerte - No recomendable para pulverizaciones',
        severity: 'advisory',
      });
    }
    return resultado;
  };

  const fetchWeatherData = async () => {
    if (!coordinates) return;

    try {
      console.log('🔧 [WeatherWidget] Obteniendo datos del clima...');
      setLoading(true);
      setError(null);

      const datos = await climaService.obtenerPorCoordenadas(coordinates.lat, coordinates.lon);
      const weatherData = mapearRespuestaClima(datos, coordinates);
      setLocation(weatherData.location);

      setWeatherData(weatherData);
    } catch (err) {
      console.error('❌ [WeatherWidget] Error obteniendo datos del clima:', err);
      setError('Error al cargar datos meteorológicos. Verificando conexión...');
      
      // Datos de respaldo en caso de error
      const fallbackData: WeatherData = {
        location: location,
        temperature: 22,
        description: 'Datos no disponibles',
        humidity: 65,
        windSpeed: 12,
        pressure: 1013,
        icon: '02d',
        coordinates: coordinates,
        forecast: [],
        alerts: []
      };
      setWeatherData(fallbackData);
    } finally {
      setLoading(false);
    }
  };

  const getWeatherIcon = (iconCode: string) => {
    // Usar iconos locales o emojis como fallback
    const iconMap: { [key: string]: string } = {
      '01d': '☀️', // Soleado
      '01n': '🌙', // Noche despejada
      '02d': '⛅', // Parcialmente nublado
      '02n': '☁️', // Noche nublada
      '03d': '☁️', // Nublado
      '03n': '☁️', // Nublado noche
      '04d': '☁️', // Muy nublado
      '04n': '☁️', // Muy nublado noche
      '09d': '🌧️', // Lluvia
      '09n': '🌧️', // Lluvia noche
      '10d': '🌦️', // Lluvia con sol
      '10n': '🌧️', // Lluvia noche
      '11d': '⛈️', // Tormenta
      '11n': '⛈️', // Tormenta noche
      '13d': '❄️', // Nieve
      '13n': '❄️', // Nieve noche
      '50d': '🌫️', // Niebla
      '50n': '🌫️', // Niebla noche
    };
    
    return iconMap[iconCode] || '🌤️';
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'warning': return 'text-red-600 bg-red-100';
      case 'watch': return 'text-orange-600 bg-orange-100';
      case 'advisory': return 'text-yellow-600 bg-yellow-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getWeatherAdvice = () => {
    if (!weatherData) return '';
    
    const temp = weatherData.temperature;
    const humidity = weatherData.humidity;
    const windSpeed = weatherData.windSpeed;
    
    if (temp < 10) return '❄️ Temperatura baja - Considerar retrasar labores sensibles';
    if (temp > 30) return '🌡️ Temperatura alta - Evitar labores en horas pico';
    if (humidity > 80) return '💧 Humedad alta - Riesgo de enfermedades fúngicas';
    if (windSpeed > 20) return '💨 Viento fuerte - No recomendable para pulverizaciones';
    
    return '✅ Condiciones favorables para labores agrícolas';
  };

  const handleRefreshLocation = () => {
    console.log('🔄 [WeatherWidget] Actualizando ubicación...');
    setLoading(true);
    setError(null);
    getCurrentLocation();
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-md p-4 mb-4">
        <div className="flex items-center justify-center h-32">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-2 text-gray-600">Obteniendo clima local...</span>
        </div>
      </div>
    );
  }

  if (error && !weatherData) {
    return (
      <div className="bg-white rounded-lg shadow-md p-4 mb-4">
        <div className="text-center text-red-600">
          <div className="text-2xl mb-2">🌤️</div>
          <div className="font-semibold">Error del Clima</div>
          <div className="text-sm">{error}</div>
          <button
            onClick={handleRefreshLocation}
            className="mt-2 px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700"
          >
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  if (!weatherData) return null;

  return (
    <div className="bg-white rounded-lg shadow-md p-4 mb-4">
      {/* Header */}
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-800 flex items-center">
          🌤️ Clima Actual
        </h3>
        <div className="flex space-x-2">
          <button
            onClick={() => setShowForecast(!showForecast)}
            className={`px-2 py-1 text-xs rounded ${showForecast ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'}`}
          >
            Pronóstico
          </button>
          <button
            onClick={() => setShowAlerts(!showAlerts)}
            className={`px-2 py-1 text-xs rounded ${showAlerts ? 'bg-red-600 text-white' : 'bg-gray-200 text-gray-700'}`}
          >
            Alertas
          </button>
        </div>
      </div>

      {/* Ubicación */}
      <div className="mb-3 flex items-center gap-2">
        <div className="flex-1">
          <div className="text-sm text-gray-600 mb-1">📍 Ubicación actual</div>
          <div className="text-sm font-medium text-gray-800">
            {location === 'Detectando ubicación...' ? '🌍 Detectando ubicación...' : location}
          </div>
          {coordinates && (
            <div className="text-xs text-gray-500">
              {coordinates.lat.toFixed(4)}, {coordinates.lon.toFixed(4)}
              {USE_MOCK_DATA && (
                <span className="ml-2 text-orange-600">(Datos de demostración)</span>
              )}
            </div>
          )}
        </div>
        <button
          onClick={handleRefreshLocation}
          className="px-2 py-1 bg-blue-100 text-blue-600 rounded text-xs hover:bg-blue-200"
          title="Actualizar ubicación"
        >
          🔄
        </button>
      </div>

      {/* Clima Actual */}
      <div className="flex items-center justify-between mb-4 p-3 bg-gradient-to-r from-blue-50 to-cyan-50 rounded-lg">
        <div className="flex items-center">
          <div className="text-6xl mr-4">
            {getWeatherIcon(weatherData.icon)}
          </div>
          <div className="ml-3">
            <div className="text-2xl font-bold text-gray-800">
              {weatherData.temperature}°C
            </div>
            <div className="text-sm text-gray-600 capitalize">
              {weatherData.description}
            </div>
            <div className="text-xs text-gray-500">
              {weatherData.location}
            </div>
          </div>
        </div>
        
        <div className="text-right text-sm text-gray-600">
          <div>💧 {weatherData.humidity}%</div>
          <div>💨 {weatherData.windSpeed} km/h</div>
          <div>📊 {weatherData.pressure} hPa</div>
        </div>
      </div>

      {/* Consejo Agrícola */}
      <div className="mb-4 p-3 bg-green-50 border-l-4 border-green-400 rounded">
        <div className="text-sm font-medium text-green-800">
          💡 Consejo Agrícola
        </div>
        <div className="text-xs text-green-700 mt-1">
          {getWeatherAdvice()}
        </div>
      </div>

      {/* Pronóstico Extendido */}
      {showForecast && (
        <div className="mb-4">
          <h4 className="font-semibold text-gray-800 mb-2">📅 Pronóstico 5 Días</h4>
          <div className="grid grid-cols-5 gap-2">
            {weatherData.forecast.map((day, index) => (
              <div key={index} className="text-center p-2 bg-gray-50 rounded">
                <div className="text-xs text-gray-600">
                  {new Date(day.date).toLocaleDateString('es-ES', { weekday: 'short' })}
                </div>
                <div className="text-2xl my-1">
                  {getWeatherIcon(day.icon)}
                </div>
                <div className="text-xs font-semibold">
                  {day.temp.max}° / {day.temp.min}°
                </div>
                <div className="text-xs text-gray-500">
                  {day.humidity}%
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Alertas Meteorológicas */}
      {showAlerts && weatherData.alerts.length > 0 && (
        <div className="mb-4">
          <h4 className="font-semibold text-gray-800 mb-2">⚠️ Alertas Meteorológicas</h4>
          <div className="space-y-2">
            {weatherData.alerts.map((alert, index) => (
              <div 
                key={index} 
                className={`p-3 rounded-lg border-l-4 ${getSeverityColor(alert.severity)}`}
              >
                <div className="font-semibold text-sm">{alert.event}</div>
                <div className="text-xs mt-1">{alert.description}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Información de API */}
      <div className="text-xs text-gray-500 text-center mt-3 pt-2 border-t">
        {USE_MOCK_DATA ? (
          <>
            🌤️ Datos de demostración - <a href="https://openweathermap.org/" target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">Obtener API key gratuita</a>
            <br />
            Última actualización: {new Date().toLocaleTimeString('es-ES')}
          </>
        ) : (
          <>
            Datos proporcionados por OpenWeatherMap API
            <br />
            Última actualización: {new Date().toLocaleTimeString('es-ES')}
          </>
        )}
        {locationPermission === 'denied' && (
          <div className="mt-1 text-orange-600">
            ⚠️ Permiso de ubicación denegado
          </div>
        )}
      </div>
    </div>
  );
};

export default WeatherWidget;
