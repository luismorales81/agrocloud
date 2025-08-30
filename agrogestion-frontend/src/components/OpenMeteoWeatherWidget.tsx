import React, { useState, useEffect } from 'react';
import weatherService, { WeatherData, WeatherCurrent, WeatherForecast } from '../services/weatherService';

interface OpenMeteoWeatherWidgetProps {
  fieldName: string;
  coordinates: {
    lat: number;
    lon: number;
  };
  compact?: boolean;
}

const OpenMeteoWeatherWidget: React.FC<OpenMeteoWeatherWidgetProps> = ({ 
  fieldName, 
  coordinates, 
  compact = false 
}) => {
  const [weatherData, setWeatherData] = useState<WeatherData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentSlide, setCurrentSlide] = useState(0);

  useEffect(() => {
    if (coordinates) {
      fetchWeatherData();
    }
  }, [coordinates]);

  const fetchWeatherData = async () => {
    try {
      console.log(`🌤️ [OpenMeteoWidget] Obteniendo clima para campo: ${fieldName}`);
      setLoading(true);
      setError(null);

      const data = await weatherService.getWeatherByCoordinates(coordinates.lat, coordinates.lon);
      console.log('🌤️ [OpenMeteoWidget] Datos recibidos:', data);
      console.log('🌤️ [OpenMeteoWidget] Forecast length:', data.forecast?.length);
      setWeatherData(data);
      
    } catch (err) {
      console.error(`❌ [OpenMeteoWidget] Error obteniendo clima para ${fieldName}:`, err);
      setError('Error al cargar datos meteorológicos');
    } finally {
      setLoading(false);
    }
  };

  const getAgriculturalAdvice = (current: WeatherCurrent): string => {
    return weatherService.getAgriculturalAdvice(current);
  };

  const getTemperatureColor = (temperature: number): string => {
    return weatherService.getTemperatureColor(temperature);
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    if (date.toDateString() === today.toDateString()) {
      return 'Hoy';
    } else if (date.toDateString() === tomorrow.toDateString()) {
      return 'Mañana';
    } else {
      return date.toLocaleDateString('es-ES', { 
        weekday: 'short', 
        month: 'short', 
        day: 'numeric' 
      });
    }
  };

  const nextSlide = () => {
    if (weatherData?.forecast) {
      setCurrentSlide((prev) => (prev + 1) % weatherData.forecast.length);
    }
  };

  const prevSlide = () => {
    if (weatherData?.forecast) {
      setCurrentSlide((prev) => (prev - 1 + weatherData.forecast.length) % weatherData.forecast.length);
    }
  };

  if (loading) {
    return (
      <div style={{
        background: 'white',
        borderRadius: '8px',
        padding: '1rem',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        border: '1px solid #e5e7eb'
      }}>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: '60px'
        }}>
          <div style={{
            width: '20px',
            height: '20px',
            border: '2px solid #e5e7eb',
            borderTop: '2px solid #3b82f6',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }}></div>
          <span style={{ marginLeft: '8px', color: '#6b7280', fontSize: '12px' }}>
            Cargando clima...
          </span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{
        background: 'white',
        borderRadius: '8px',
        padding: '1rem',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        border: '1px solid #e5e7eb'
      }}>
        <div style={{ textAlign: 'center', color: '#ef4444', fontSize: '12px' }}>
          <div style={{ fontSize: '20px', marginBottom: '6px' }}>🌤️</div>
          <div>Error del clima</div>
          <button 
            onClick={fetchWeatherData}
            style={{
              marginTop: '6px',
              padding: '4px 8px',
              background: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '10px'
            }}
          >
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  if (!weatherData) return null;

  const { current, forecast, location, lastUpdated } = weatherData;

  // Validar que current existe y tiene las propiedades necesarias
  if (!current || typeof current !== 'object') {
    return (
      <div style={{
        background: 'white',
        borderRadius: '8px',
        padding: '1rem',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        border: '1px solid #e5e7eb'
      }}>
        <div style={{ textAlign: 'center', color: '#f59e0b', fontSize: '12px' }}>
          <div style={{ fontSize: '20px', marginBottom: '6px' }}>🌤️</div>
          <div>Datos meteorológicos incompletos</div>
          <button 
            onClick={fetchWeatherData}
            style={{
              marginTop: '6px',
              padding: '4px 8px',
              background: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '10px'
            }}
          >
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  if (compact) {
    // Versión compacta para listas
    return (
      <div style={{
        background: 'white',
        borderRadius: '8px',
        padding: '12px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        border: '2px solid #3b82f6'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <div style={{ fontSize: '24px', marginRight: '8px' }}>
              {current.icon}
            </div>
            <div>
              <div style={{ fontWeight: 'bold', color: '#1f2937', fontSize: '16px' }}>
                {Math.round(current.temperature)}°C
              </div>
              <div style={{ fontSize: '12px', color: '#6b7280', textTransform: 'capitalize' }}>
                {current.weatherDescription}
              </div>
            </div>
          </div>
          <div style={{ textAlign: 'right', fontSize: '12px', color: '#6b7280' }}>
            <div>💧 {current.humidity}%</div>
            <div>💨 {Math.round(current.windSpeed)} km/h</div>
          </div>
        </div>
        
        {/* Consejo agrícola compacto */}
        <div style={{
          marginTop: '8px',
          padding: '8px',
          background: '#f0f9ff',
          borderRadius: '4px',
          fontSize: '12px'
        }}>
          <div style={{ color: '#0c4a6e', fontWeight: '500', marginBottom: '4px' }}>
            💡 Consejo:
          </div>
          <div style={{ color: '#0369a1' }}>
            {getAgriculturalAdvice(current)}
          </div>
        </div>
      </div>
    );
  }

  // Versión completa con pasarela
  return (
    <div style={{
      background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
      borderRadius: '8px',
      padding: '0.75rem',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
      border: '1px solid #cbd5e1',
      fontSize: '12px'
    }}>
      {/* Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '0.5rem',
        borderBottom: '1px solid #e2e8f0',
        paddingBottom: '0.5rem'
      }}>
        <h3 style={{ margin: 0, color: '#1f2937', fontSize: '14px', display: 'flex', alignItems: 'center' }}>
          📅 {fieldName}
        </h3>
        <div style={{ fontSize: '9px', color: '#6b7280' }}>
          {new Date(lastUpdated).toLocaleTimeString('es-ES', {hour: '2-digit', minute: '2-digit'})}
        </div>
      </div>

      {/* Clima Actual */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: '0.5rem',
        padding: '0.5rem',
        background: getTemperatureColor(current.temperature),
        borderRadius: '6px',
        border: '1px solid #e5e7eb'
      }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <div style={{ fontSize: '24px', marginRight: '0.5rem' }}>
            {current.icon}
          </div>
          <div>
            <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937' }}>
              {Math.round(current.temperature)}°C
            </div>
            <div style={{ fontSize: '10px', color: '#6b7280', textTransform: 'capitalize' }}>
              {current.weatherDescription}
            </div>
          </div>
        </div>
        
        <div style={{ textAlign: 'right', fontSize: '10px', color: '#374151' }}>
          <div style={{ marginBottom: '2px' }}>
            💧 {current.humidity}%
          </div>
          <div style={{ marginBottom: '2px' }}>
            💨 {Math.round(current.windSpeed)} km/h
          </div>
        </div>
      </div>

      {/* Pasarela de Pronóstico */}
      {forecast && forecast.length > 0 && (
        <div style={{ marginBottom: '0.5rem' }}>
          {/* Controles de navegación */}
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '0.5rem'
          }}>
            <button
              onClick={prevSlide}
              style={{
                padding: '2px 6px',
                background: '#3b82f6',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '10px',
                fontWeight: '500'
              }}
            >
              ←
            </button>
            <div style={{ fontSize: '10px', color: '#6b7280', fontWeight: '500' }}>
              {currentSlide + 1}/{forecast.length}
            </div>
            <button
              onClick={nextSlide}
              style={{
                padding: '2px 6px',
                background: '#3b82f6',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '10px',
                fontWeight: '500'
              }}
            >
              →
            </button>
          </div>

          {/* Tarjeta del día actual */}
          <div style={{
            background: 'white',
            borderRadius: '6px',
            padding: '0.75rem',
            border: '1px solid #e2e8f0',
            boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
            textAlign: 'center'
          }}>
            <div style={{ 
              fontSize: '12px', 
              color: '#374151', 
              marginBottom: '6px',
              fontWeight: '600'
            }}>
              {formatDate(forecast[currentSlide].date)}
            </div>
            <div style={{ 
              fontSize: '24px', 
              marginBottom: '6px'
            }}>
              {forecast[currentSlide].icon}
            </div>
            <div style={{ 
              fontSize: '14px', 
              fontWeight: '600', 
              color: '#1f2937',
              marginBottom: '4px'
            }}>
              {Math.round(forecast[currentSlide].maxTemperature)}° / {Math.round(forecast[currentSlide].minTemperature)}°
            </div>
            <div style={{ 
              fontSize: '10px', 
              color: '#6b7280', 
              marginBottom: '4px'
            }}>
              {forecast[currentSlide].weatherDescription}
            </div>
            <div style={{ 
              fontSize: '9px', 
              color: '#6b7280', 
              padding: '2px 6px',
              background: forecast[currentSlide].precipitation > 0 ? '#dbeafe' : '#f0fdf4',
              borderRadius: '4px',
              display: 'inline-block'
            }}>
              {forecast[currentSlide].precipitation > 0 ? `🌧️ ${forecast[currentSlide].precipitation.toFixed(1)}mm` : '☀️ Seco'}
            </div>
          </div>
        </div>
      )}

      {/* Consejo Agrícola */}
      <div style={{
        padding: '0.5rem',
        background: '#f0f9ff',
        borderRadius: '6px',
        border: '1px solid #0ea5e9'
      }}>
        <div style={{ fontSize: '10px', fontWeight: '600', color: '#0c4a6e', marginBottom: '2px' }}>
          💡 Consejo
        </div>
        <div style={{ fontSize: '9px', color: '#0369a1' }}>
          {getAgriculturalAdvice(current)}
        </div>
      </div>

      {/* Información */}
      <div style={{
        fontSize: '8px',
        color: '#6b7280',
        textAlign: 'center',
        marginTop: '0.5rem',
        paddingTop: '0.5rem',
        borderTop: '1px solid #e2e8f0'
      }}>
        🌤️ Open-Meteo API
      </div>
    </div>
  );
};

export default OpenMeteoWeatherWidget;