import React, { useState } from 'react';
import OpenMeteoWeatherWidget from './OpenMeteoWeatherWidget';

interface FieldWeatherButtonProps {
  fieldName: string;
  coordinates: Array<{lat: number; lng: number}>;
}

const FieldWeatherButton: React.FC<FieldWeatherButtonProps> = ({ 
  fieldName, 
  coordinates 
}) => {
  const [showWeather, setShowWeather] = useState(false);

  // Función para calcular el centro del campo
  const getFieldCenter = (coordinates: Array<{lat: number; lng: number}>) => {
    if (!coordinates || coordinates.length === 0) return null;
    
    const latSum = coordinates.reduce((sum, coord) => sum + coord.lat, 0);
    const lngSum = coordinates.reduce((sum, coord) => sum + coord.lng, 0);
    
    return {
      lat: latSum / coordinates.length,
      lon: lngSum / coordinates.length
    };
  };

  const fieldCenter = getFieldCenter(coordinates);

  if (!fieldCenter) {
    return null;
  }

  return (
    <div>
      <button
        onClick={() => setShowWeather(!showWeather)}
        style={{
          background: showWeather ? '#FF5722' : '#9C27B0',
          color: 'white',
          border: 'none',
          padding: '8px 16px',
          borderRadius: '5px',
          cursor: 'pointer',
          fontSize: '12px'
        }}
      >
        {showWeather ? '❌ Ocultar Clima' : '🌤️ Ver Clima'}
      </button>
      
      {showWeather && (
        <div style={{ 
          marginTop: '10px', 
          padding: '20px', 
          backgroundColor: 'transparent', 
          borderTop: '1px solid #eee',
          borderRadius: '5px'
        }}>
          <OpenMeteoWeatherWidget
            fieldName={fieldName}
            coordinates={fieldCenter}
            compact={false}
          />
        </div>
      )}
    </div>
  );
};

export default FieldWeatherButton;
