import React, { useState, useEffect } from 'react';

interface FieldWeatherData {
  temperature: number;
  description: string;
  humidity: number;
  windSpeed: number;
  pressure: number;
  icon: string;
  forecast: ForecastDay[];
  alerts: WeatherAlert[];
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

interface FieldWeatherWidgetProps {
  fieldName: string;
  coordinates: {
    lat: number;
    lon: number;
  };
  compact?: boolean;
}

const FieldWeatherWidget: React.FC<FieldWeatherWidgetProps> = ({ 
  fieldName, 
  coordinates, 
  compact = false 
}) => {
  const [weatherData, setWeatherData] = useState<FieldWeatherData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForecast, setShowForecast] = useState(false);

  // API Key de OpenWeatherMap
  const API_KEY: string = '9dee7c2c4e36ce49c32fab5a51d6e25b';
  const USE_MOCK_DATA = API_KEY === 'demo-key' || API_KEY === '1234567890abcdef';
  const BASE_URL = 'https://api.openweathermap.org/data/2.5';

  useEffect(() => {
    if (coordinates) {
      fetchWeatherData();
    }
  }, [coordinates]);

  const fetchWeatherData = async () => {
    try {
      console.log(`🔧 [FieldWeatherWidget] Obteniendo clima para campo: ${fieldName}`);
      setLoading(true);
      setError(null);

      let weatherData: FieldWeatherData;

      if (USE_MOCK_DATA) {
        // Datos simulados específicos para el campo
        weatherData = {
          temperature: 22 + Math.floor(Math.random() * 8), // Variación por campo
          description: 'Parcialmente nublado',
          humidity: 65 + Math.floor(Math.random() * 15),
          windSpeed: 10 + Math.floor(Math.random() * 10),
          pressure: 1010 + Math.floor(Math.random() * 10),
          icon: '02d',
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
            }
          ],
          alerts: []
        };
      } else {
        // Obtener datos reales de la API
        const response = await fetch(
          `${BASE_URL}/weather?lat=${coordinates.lat}&lon=${coordinates.lon}&appid=${API_KEY}&units=metric&lang=es`
        );

        if (!response.ok) {
          throw new Error('Error obteniendo datos del clima');
        }

        const data = await response.json();

        weatherData = {
          temperature: Math.round(data.main.temp),
          description: data.weather[0].description,
          humidity: data.main.humidity,
          windSpeed: Math.round(data.wind.speed * 3.6),
          pressure: data.main.pressure,
          icon: data.weather[0].icon,
          forecast: [],
          alerts: []
        };
      }

      // Generar alertas basadas en condiciones
      if (weatherData.temperature > 30) {
        weatherData.alerts.push({
          event: 'Temperatura Alta',
          description: 'Evitar labores en horas pico',
          severity: 'advisory'
        });
      }

      if (weatherData.windSpeed > 20) {
        weatherData.alerts.push({
          event: 'Viento Fuerte',
          description: 'No recomendable para pulverizaciones',
          severity: 'advisory'
        });
      }

      setWeatherData(weatherData);
    } catch (err) {
      console.error(`❌ [FieldWeatherWidget] Error obteniendo clima para ${fieldName}:`, err);
      setError('Error al cargar datos meteorológicos');
    } finally {
      setLoading(false);
    }
  };

  const getWeatherIcon = (iconCode: string) => {
    const iconMap: { [key: string]: string } = {
      '01d': '☀️', '01n': '🌙', '02d': '⛅', '02n': '☁️',
      '03d': '☁️', '03n': '☁️', '04d': '☁️', '04n': '☁️',
      '09d': '🌧️', '09n': '🌧️', '10d': '🌦️', '10n': '🌧️',
      '11d': '⛈️', '11n': '⛈️', '13d': '❄️', '13n': '❄️',
      '50d': '🌫️', '50n': '🌫️'
    };
    return iconMap[iconCode] || '🌤️';
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

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-md p-3">
        <div className="flex items-center justify-center h-16">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <span className="ml-2 text-gray-600 text-sm">Cargando clima...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-md p-3">
        <div className="text-center text-red-600 text-sm">
          <div className="text-lg mb-1">🌤️</div>
          <div>Error del clima</div>
        </div>
      </div>
    );
  }

  if (!weatherData) return null;

  if (compact) {
    // Versión compacta para listas
    return (
      <div className="bg-white rounded-lg shadow-md p-3 border-l-4 border-blue-500">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <div className="text-2xl mr-2">
              {getWeatherIcon(weatherData.icon)}
            </div>
            <div>
              <div className="font-semibold text-gray-800">
                {weatherData.temperature}°C
              </div>
              <div className="text-xs text-gray-600 capitalize">
                {weatherData.description}
              </div>
            </div>
          </div>
          <div className="text-right text-xs text-gray-500">
            <div>💧 {weatherData.humidity}%</div>
            <div>💨 {weatherData.windSpeed} km/h</div>
          </div>
        </div>
        
        {/* Consejo agrícola compacto */}
        <div className="mt-2 p-2 bg-green-50 rounded text-xs">
          <div className="text-green-800 font-medium">💡 Consejo:</div>
          <div className="text-green-700">{getWeatherAdvice()}</div>
        </div>

        {/* Alertas si las hay */}
        {weatherData.alerts.length > 0 && (
          <div className="mt-2">
            {weatherData.alerts.map((alert, index) => (
              <div key={index} className="text-xs p-2 bg-yellow-50 border-l-2 border-yellow-400 rounded">
                <div className="font-medium text-yellow-800">⚠️ {alert.event}</div>
                <div className="text-yellow-700">{alert.description}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  }

  // Versión completa
  return (
    <div className="bg-white rounded-lg shadow-md p-4">
      {/* Header */}
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-lg font-semibold text-gray-800 flex items-center">
          🌤️ Clima - {fieldName}
        </h3>
        <button
          onClick={() => setShowForecast(!showForecast)}
          className={`px-2 py-1 text-xs rounded ${showForecast ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'}`}
        >
          Pronóstico
        </button>
      </div>

      {/* Clima Actual */}
      <div className="flex items-center justify-between mb-4 p-3 bg-gradient-to-r from-blue-50 to-cyan-50 rounded-lg">
        <div className="flex items-center">
          <div className="text-5xl mr-4">
            {getWeatherIcon(weatherData.icon)}
          </div>
          <div>
            <div className="text-2xl font-bold text-gray-800">
              {weatherData.temperature}°C
            </div>
            <div className="text-sm text-gray-600 capitalize">
              {weatherData.description}
            </div>
            <div className="text-xs text-gray-500">
              {coordinates.lat.toFixed(4)}, {coordinates.lon.toFixed(4)}
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

      {/* Pronóstico */}
      {showForecast && weatherData.forecast.length > 0 && (
        <div className="mb-4">
          <h4 className="font-semibold text-gray-800 mb-2">📅 Pronóstico</h4>
          <div className="grid grid-cols-2 gap-2">
            {weatherData.forecast.map((day, index) => (
              <div key={index} className="text-center p-2 bg-gray-50 rounded">
                <div className="text-xs text-gray-600">
                  {new Date(day.date).toLocaleDateString('es-ES', { weekday: 'short' })}
                </div>
                <div className="text-xl my-1">
                  {getWeatherIcon(day.icon)}
                </div>
                <div className="text-xs font-semibold">
                  {day.temp.max}° / {day.temp.min}°
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Alertas */}
      {weatherData.alerts.length > 0 && (
        <div className="mb-4">
          <h4 className="font-semibold text-gray-800 mb-2">⚠️ Alertas</h4>
          <div className="space-y-2">
            {weatherData.alerts.map((alert, index) => (
              <div 
                key={index} 
                className="p-3 rounded-lg border-l-4 border-yellow-400 bg-yellow-50"
              >
                <div className="font-semibold text-sm text-yellow-800">{alert.event}</div>
                <div className="text-xs mt-1 text-yellow-700">{alert.description}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Información */}
      <div className="text-xs text-gray-500 text-center mt-3 pt-2 border-t">
        {USE_MOCK_DATA ? (
          <>🌤️ Datos de demostración - Campo: {fieldName}</>
        ) : (
          <>Datos proporcionados por OpenWeatherMap API - Campo: {fieldName}</>
        )}
      </div>
    </div>
  );
};

export default FieldWeatherWidget;
