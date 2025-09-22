import React, { useState, useEffect } from 'react';

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

  // API Key de OpenWeatherMap - Usar datos simulados si no hay API key válida
  const API_KEY = '9dee7c2c4e36ce49c32fab5a51d6e25b'; // API key real del backend
  const USE_MOCK_DATA = API_KEY === 'demo-key' || API_KEY === '1234567890abcdef';
  const BASE_URL = 'https://api.openweathermap.org/data/2.5';

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
        getLocationName(latitude, longitude);
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

  const getLocationName = async (lat: number, lon: number) => {
    try {
      console.log('🔧 [WeatherWidget] Obteniendo nombre de ubicación...');
      const response = await fetch(
        `https://api.openweathermap.org/geo/1.0/reverse?lat=${lat}&lon=${lon}&limit=1&appid=${API_KEY}`
      );
      
      if (response.ok) {
        const data = await response.json();
        if (data && data[0]) {
          const locationName = `${data[0].name}, ${data[0].country}`;
          console.log('✅ [WeatherWidget] Nombre de ubicación:', locationName);
          setLocation(locationName);
        }
      }
    } catch (error) {
      console.error('❌ [WeatherWidget] Error obteniendo nombre de ubicación:', error);
      setLocation('Ubicación actual');
    }
  };

  const fetchWeatherData = async () => {
    if (!coordinates) return;

    try {
      console.log('🔧 [WeatherWidget] Obteniendo datos del clima...');
      setLoading(true);
      setError(null);

      let weatherData: WeatherData;

      if (USE_MOCK_DATA) {
        // Usar datos simulados para demostración
        console.log('🔧 [WeatherWidget] Usando datos simulados para demostración');
        
        weatherData = {
          location: location,
          temperature: 24,
          description: 'Parcialmente nublado',
          humidity: 68,
          windSpeed: 15,
          pressure: 1012,
          icon: '02d',
          coordinates: coordinates,
          forecast: [
            {
              date: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().split('T')[0],
              temp: { min: 18, max: 26 },
              description: 'Soleado',
              icon: '01d',
              humidity: 60,
              windSpeed: 12
            },
            {
              date: new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString().split('T')[0],
              temp: { min: 16, max: 23 },
              description: 'Nublado',
              icon: '03d',
              humidity: 75,
              windSpeed: 18
            },
            {
              date: new Date(Date.now() + 72 * 60 * 60 * 1000).toISOString().split('T')[0],
              temp: { min: 14, max: 20 },
              description: 'Lluvia ligera',
              icon: '10d',
              humidity: 85,
              windSpeed: 20
            },
            {
              date: new Date(Date.now() + 96 * 60 * 60 * 1000).toISOString().split('T')[0],
              temp: { min: 12, max: 18 },
              description: 'Lluvia',
              icon: '09d',
              humidity: 90,
              windSpeed: 22
            },
            {
              date: new Date(Date.now() + 120 * 60 * 60 * 1000).toISOString().split('T')[0],
              temp: { min: 15, max: 22 },
              description: 'Parcialmente nublado',
              icon: '02d',
              humidity: 70,
              windSpeed: 14
            }
          ],
          alerts: []
        };

        // Generar alertas basadas en condiciones simuladas
        if (weatherData.temperature > 25) {
          weatherData.alerts.push({
            event: 'Temperatura Elevada',
            description: 'Temperatura alta - Considerar riego adicional',
            severity: 'advisory'
          });
        }
      } else {
        // Obtener datos reales de la API
        console.log('🔧 [WeatherWidget] Obteniendo datos reales de OpenWeatherMap...');
        
        // Obtener clima actual
        const currentWeatherResponse = await fetch(
          `${BASE_URL}/weather?lat=${coordinates.lat}&lon=${coordinates.lon}&appid=${API_KEY}&units=metric&lang=es`
        );

        if (!currentWeatherResponse.ok) {
          throw new Error('Error obteniendo clima actual');
        }

        const currentWeather = await currentWeatherResponse.json();

        // Obtener pronóstico de 5 días
        const forecastResponse = await fetch(
          `${BASE_URL}/forecast?lat=${coordinates.lat}&lon=${coordinates.lon}&appid=${API_KEY}&units=metric&lang=es`
        );

        if (!forecastResponse.ok) {
          throw new Error('Error obteniendo pronóstico');
        }

        const forecastData = await forecastResponse.json();

        // Procesar datos del clima actual
        weatherData = {
          location: location,
          temperature: Math.round(currentWeather.main.temp),
          description: currentWeather.weather[0].description,
          humidity: currentWeather.main.humidity,
          windSpeed: Math.round(currentWeather.wind.speed * 3.6), // Convertir m/s a km/h
          pressure: currentWeather.main.pressure,
          icon: currentWeather.weather[0].icon,
          coordinates: coordinates,
          forecast: [],
          alerts: []
        };

        // Procesar pronóstico (tomar un día por cada 24 horas)
        const dailyForecast = forecastData.list.filter((_: any, index: number) => index % 8 === 0);
        weatherData.forecast = dailyForecast.slice(1, 6).map((day: any) => ({
          date: day.dt_txt.split(' ')[0],
          temp: {
            min: Math.round(day.main.temp_min),
            max: Math.round(day.main.temp_max)
          },
          description: day.weather[0].description,
          icon: day.weather[0].icon,
          humidity: day.main.humidity,
          windSpeed: Math.round(day.wind.speed * 3.6)
        }));

        // Generar consejos agrícolas basados en alertas
        if (weatherData.temperature > 30 || weatherData.windSpeed > 20) {
          weatherData.alerts.push({
            event: 'Condiciones Adversas',
            description: weatherData.temperature > 30 
              ? 'Temperatura alta - Evitar labores en horas pico'
              : 'Viento fuerte - No recomendable para pulverizaciones',
            severity: 'advisory'
          });
        }
      }

      console.log('✅ [WeatherWidget] Datos del clima obtenidos:', weatherData);
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
