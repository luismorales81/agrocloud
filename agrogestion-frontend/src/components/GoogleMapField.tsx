import React, { useEffect, useRef, useState } from 'react';
import { loadGoogleMaps, GOOGLE_MAPS_CONFIG } from '../config/googleMaps';

interface GoogleMapFieldProps {
  coordinates: Array<{lat: number; lng: number}>;
  onCoordinatesChange: (coordinates: Array<{lat: number; lng: number}>) => void;
  height?: string;
}

const GoogleMapField: React.FC<GoogleMapFieldProps> = ({ 
  coordinates, 
  onCoordinatesChange, 
  height = '400px' 
}) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const [map, setMap] = useState<any>(null);
  const [polygon, setPolygon] = useState<any>(null);
  const [drawingManager, setDrawingManager] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    initializeMapComponent();
  }, []);

  useEffect(() => {
    if (map && coordinates.length > 0) {
      updatePolygon();
    }
  }, [coordinates, map]);

  const initializeMapComponent = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      if (!mapRef.current) return;

      // Usar la función de carga centralizada
      loadGoogleMaps(() => {
        if (!mapRef.current || !window.google || !window.google.maps) {
          setError('Google Maps no está disponible');
          setIsLoading(false);
          return;
        }

        try {
          // Asegurar que el contenedor del mapa esté listo
          if (!mapRef.current.offsetParent) {
            setTimeout(() => initializeMapComponent(), 100);
            return;
          }

          const mapOptions = {
            center: coordinates.length > 0 ? coordinates[0] : GOOGLE_MAPS_CONFIG.DEFAULT_CENTER,
            zoom: GOOGLE_MAPS_CONFIG.DEFAULT_ZOOM,
            mapTypeId: window.google.maps.MapTypeId.SATELLITE,
            mapTypeControl: true,
            streetViewControl: false,
            fullscreenControl: true,
            zoomControl: true,
            gestureHandling: 'cooperative',
            disableDefaultUI: false,
            backgroundColor: '#f0f0f0'
          };

          const newMap = new window.google.maps.Map(mapRef.current, mapOptions);
          setMap(newMap);

          // Configurar Drawing Manager
          const drawingManagerOptions = {
            drawingMode: window.google.maps.drawing.OverlayType.POLYGON,
            drawingControl: true,
            drawingControlOptions: {
              position: window.google.maps.ControlPosition.TOP_CENTER,
              drawingModes: [window.google.maps.drawing.OverlayType.POLYGON]
            },
            polygonOptions: GOOGLE_MAPS_CONFIG.POLYGON_STYLES
          };

          const newDrawingManager = new window.google.maps.drawing.DrawingManager(drawingManagerOptions);
          newDrawingManager.setMap(newMap);
          setDrawingManager(newDrawingManager);

          // Evento para capturar el polígono dibujado
          window.google.maps.event.addListener(newDrawingManager, 'polygoncomplete', (newPolygon: any) => {
            // Limpiar polígonos anteriores
            if (polygon) {
              polygon.setMap(null);
            }
            
            // Configurar el nuevo polígono
            newPolygon.setOptions(GOOGLE_MAPS_CONFIG.POLYGON_STYLES);
            setPolygon(newPolygon);
            
            // Obtener coordenadas del polígono
            const path = newPolygon.getPath();
            const newCoordinates: Array<{lat: number; lng: number}> = [];
            for (let i = 0; i < path.getLength(); i++) {
              const vertex = path.getAt(i);
              newCoordinates.push({ lat: vertex.lat(), lng: vertex.lng() });
            }
            
            onCoordinatesChange(newCoordinates);
          });

          // Si hay coordenadas iniciales, dibujar el polígono
          if (coordinates.length > 0) {
            updatePolygon();
          }

          setIsLoading(false);
        } catch (err) {
          console.error('Error inicializando mapa:', err);
          setError('Error al cargar el mapa. Verifica tu conexión a internet.');
          setIsLoading(false);
        }
      });
    } catch (err) {
      console.error('Error inicializando mapa:', err);
      setError('Error al cargar el mapa. Verifica tu conexión a internet.');
      setIsLoading(false);
    }
  };

  const updatePolygon = () => {
    if (!map || coordinates.length === 0) return;

    // Limpiar polígono anterior
    if (polygon) {
      polygon.setMap(null);
    }

    // Crear nuevo polígono usando la configuración centralizada
    const newPolygon = new window.google.maps.Polygon({
      paths: coordinates,
      ...GOOGLE_MAPS_CONFIG.POLYGON_STYLES,
      map
    });
    setPolygon(newPolygon);

    // Centrar el mapa en el polígono
    const bounds = new window.google.maps.LatLngBounds();
    coordinates.forEach(coord => {
      bounds.extend(new window.google.maps.LatLng(coord.lat, coord.lng));
    });
    map.fitBounds(bounds);
  };

  const clearPolygon = () => {
    if (polygon) {
      polygon.setMap(null);
      setPolygon(null);
    }
    onCoordinatesChange([]);
  };

  if (error) {
    return (
      <div style={{
        height,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: '#f9fafb',
        border: '2px solid #e5e7eb',
        borderRadius: '0.375rem',
        color: '#ef4444'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>🗺️</div>
          <div>{error}</div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div style={{
        width: '100%',
        height,
        border: '2px solid #d1d5db',
        borderRadius: '0.375rem',
        overflow: 'hidden',
        position: 'relative'
      }}>
        {isLoading && (
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            background: '#f9fafb',
            color: '#6b7280',
            zIndex: 1
          }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>🗺️</div>
              <div>Cargando mapa...</div>
            </div>
          </div>
        )}
        <div ref={mapRef} style={{ width: '100%', height: '100%' }} />
      </div>
      
      <div style={{ marginTop: '0.5rem', display: 'flex', gap: '0.5rem' }}>
        <button
          onClick={clearPolygon}
          style={{
            background: '#ef4444',
            color: 'white',
            border: 'none',
            padding: '0.5rem 1rem',
            borderRadius: '0.25rem',
            fontSize: '0.875rem',
            cursor: 'pointer'
          }}
        >
          🗑️ Limpiar
        </button>
        <div style={{ fontSize: '0.75rem', color: '#6b7280', alignSelf: 'center' }}>
          Usa la herramienta de dibujo para crear el polígono del campo
        </div>
      </div>
    </div>
  );
};

// Declaraciones de tipos para Google Maps
declare global {
  interface Window {
    google: any;
  }
}

export default GoogleMapField;
