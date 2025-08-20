import React, { useState, useEffect } from 'react';
import { useLocation } from '../contexts/LocationContext';

const WeatherWidget: React.FC = () => {
  const [weather, setWeather] = useState<any>(null);
  const [weatherLoading, setWeatherLoading] = useState(true);
  const { location, loading: locationLoading, error: locationError, requestLocation } = useLocation();

  useEffect(() => {
    const fetchWeather = async () => {
      if (!location) {
        // Si no hay ubicación, solicitar una
        requestLocation();
        return;
      }

      try {
        setWeatherLoading(true);
        
        const response = await fetch(
          `https://api.open-meteo.com/v1/forecast?latitude=${location.lat}&longitude=${location.lng}&current_weather=true&timezone=auto`
        );
        
        if (response.ok) {
          const data = await response.json();
          setWeather(data);
        }
      } catch (error) {
        console.error('Error fetching weather:', error);
      } finally {
        setWeatherLoading(false);
      }
    };

    fetchWeather();
  }, [location, requestLocation]);

  // Mostrar botón para solicitar ubicación si no se ha obtenido
  if (!location && !locationLoading) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        fontSize: '0.875rem'
      }}>
        <button
          onClick={requestLocation}
          style={{
            background: '#2563eb',
            color: 'white',
            border: 'none',
            padding: '0.25rem 0.5rem',
            borderRadius: '0.25rem',
            fontSize: '0.75rem',
            cursor: 'pointer'
          }}
        >
          📍 Obtener ubicación
        </button>
      </div>
    );
  }

  // Mostrar error de ubicación
  if (locationError) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        fontSize: '0.875rem',
        color: '#ef4444'
      }}>
        <span>⚠️ {locationError}</span>
        <button
          onClick={requestLocation}
          style={{
            background: '#2563eb',
            color: 'white',
            border: 'none',
            padding: '0.25rem 0.5rem',
            borderRadius: '0.25rem',
            fontSize: '0.75rem',
            cursor: 'pointer'
          }}
        >
          Reintentar
        </button>
      </div>
    );
  }

  // Mostrar loading
  if (locationLoading || weatherLoading) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        fontSize: '0.875rem',
        color: '#6b7280'
      }}>
        <div style={{
          width: '1rem',
          height: '1rem',
          border: '2px solid #d1d5db',
          borderTop: '2px solid #2563eb',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite'
        }}></div>
        <span>{locationLoading ? 'Obteniendo ubicación...' : 'Cargando clima...'}</span>
        <style>{`
          @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  if (!weather || !weather.current_weather) {
    return (
      <div style={{
        fontSize: '0.875rem',
        color: '#6b7280'
      }}>
        🌤️ Clima no disponible
      </div>
    );
  }

  const temp = Math.round(weather.current_weather.temperature);
  const weatherCode = weather.current_weather.weathercode;

  // Íconos simples basados en el código del clima
  const getWeatherIcon = (code: number) => {
    if (code === 0) return '☀️';
    if (code >= 1 && code <= 3) return '🌤️';
    if (code >= 45 && code <= 48) return '🌫️';
    if (code >= 51 && code <= 67) return '🌧️';
    if (code >= 71 && code <= 77) return '❄️';
    if (code >= 80 && code <= 99) return '⛈️';
    return '🌤️';
  };

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: '0.5rem',
      fontSize: '0.875rem',
      color: '#374151'
    }}>
      <span>{getWeatherIcon(weatherCode)}</span>
      <span>{temp}°C</span>
      {location && location.locality && (
        <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>
          📍 {location.locality}
        </span>
      )}
    </div>
  );
};

export default WeatherWidget;
