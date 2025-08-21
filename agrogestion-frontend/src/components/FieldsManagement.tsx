import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useLocation } from '../contexts/LocationContext';
import { getMapCenter } from '../config/googleMaps';

// Declaración de tipos para NodeJS
declare global {
  namespace NodeJS {
    interface ProcessEnv {
      NODE_ENV: 'development' | 'production' | 'test';
      REACT_APP_API_URL?: string;
    }
    type Timeout = ReturnType<typeof setTimeout>;
  }
}

interface Field {
  id?: number;
  nombre: string;
  superficie: number;
  poligono: string;
}

// Componente separado para Google Maps con Drawing Manager
const GoogleMapWithDrawing: React.FC<{ 
  onPolygonComplete: (polygon: any) => void;
  onMapReady: () => void;
  center?: { lat: number; lng: number };
  zoom?: number;
}> = ({ onPolygonComplete, onMapReady, center, zoom = 16 }) => {
  const { location } = useLocation();
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const drawingManagerRef = useRef<any>(null);

  // Usar la ubicación del usuario o la proporcionada como prop
  const mapCenter = center || getMapCenter(location || undefined);

  useEffect(() => {
    let isMounted = true;
    let checkInterval: NodeJS.Timeout;

    const initMap = () => {
      if (!mapRef.current || !isMounted) return;

      try {
        if (!(window as any).google || !(window as any).google.maps || !(window as any).google.maps.Map) {
          console.log('❌ Google Maps no está completamente cargado');
          return;
        }

        // Evitar inicialización múltiple
        if (mapInstanceRef.current) {
          console.log('✅ Mapa ya inicializado');
          return;
        }

        const mapOptions = {
          center: mapCenter,
          zoom: zoom,
          mapTypeId: 'satellite',
          disableDefaultUI: false,
          zoomControl: true,
          mapTypeControl: true,
          streetViewControl: true,
          fullscreenControl: true
        };

        mapInstanceRef.current = new (window as any).google.maps.Map(mapRef.current, mapOptions);

        const drawingManager = new (window as any).google.maps.drawing.DrawingManager({
          drawingMode: (window as any).google.maps.drawing.OverlayType.POLYGON,
          drawingControl: true,
          drawingControlOptions: {
            position: (window as any).google.maps.ControlPosition.TOP_CENTER,
            drawingModes: [(window as any).google.maps.drawing.OverlayType.POLYGON]
          },
          polygonOptions: {
            fillColor: '#4CAF50',
            fillOpacity: 0.3,
            strokeWeight: 2,
            strokeColor: '#4CAF50',
            editable: true,
            draggable: true
          }
        });

        drawingManager.setMap(mapInstanceRef.current);
        drawingManagerRef.current = drawingManager;

        (window as any).google.maps.event.addListener(drawingManager, 'polygoncomplete', (polygon: any) => {
          if (drawingManagerRef.current && isMounted) {
            drawingManagerRef.current.setDrawingMode(null);
          }
          onPolygonComplete(polygon);
        });

        // Forzar redibujado una sola vez
        setTimeout(() => {
          if (mapInstanceRef.current && mapInstanceRef.current.setZoom && isMounted) {
            window.dispatchEvent(new Event('resize'));
          }
        }, 500);

        onMapReady();
      } catch (error) {
        console.error('❌ Error creando mapa:', error);
      }
    };

    if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
      initMap();
    } else {
      checkInterval = setInterval(() => {
        if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
          clearInterval(checkInterval);
          initMap();
        }
      }, 100);
    }

    return () => {
      isMounted = false;
      if (checkInterval) {
        clearInterval(checkInterval);
      }
    };
  }, []); // Solo ejecutar una vez al montar el componente

  return (
    <div
      ref={mapRef}
      style={{
        width: '100%',
        height: '100%',
        position: 'absolute',
        top: 0,
        left: 0
      }}
    />
  );
};

// Componente para mostrar un campo específico en el mapa
const GoogleMapViewField: React.FC<{ 
  fieldPolygon: string;
  fieldName: string;
  center?: { lat: number; lng: number };
  zoom?: number;
}> = ({ fieldPolygon, fieldName, center = { lat: -34.6118, lng: -58.3960 }, zoom = 16 }) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const fieldPolygonRef = useRef<any>(null);

  useEffect(() => {
    let isMounted = true;
    let checkInterval: NodeJS.Timeout;

    const initMap = () => {
      if (!mapRef.current || !isMounted) return;

      try {
        // Verificar que Google Maps esté completamente cargado
        if (!(window as any).google || !(window as any).google.maps || !(window as any).google.maps.Map) {
          console.log('❌ Google Maps no está completamente cargado para vista de campo');
          return;
        }

        // Evitar inicialización múltiple
        if (mapInstanceRef.current) {
          console.log('✅ Mapa ya inicializado para campo');
          return;
        }

        console.log('🗺️ Inicializando mapa para mostrar campo:', fieldName);
        
        const mapOptions = {
          center: center,
          zoom: zoom,
          mapTypeId: 'satellite',
          disableDefaultUI: false,
          zoomControl: true,
          mapTypeControl: true,
          streetViewControl: true,
          fullscreenControl: true
        };

        mapInstanceRef.current = new (window as any).google.maps.Map(mapRef.current, mapOptions);
        console.log('✅ Mapa creado para mostrar campo');

        // Dibujar el polígono del campo
        if (fieldPolygon) {
          try {
            const polygonData = JSON.parse(fieldPolygon);
            const coordinates = polygonData.coordinates[0].map((coord: number[]) => ({
              lat: coord[1],
              lng: coord[0]
            }));

            const fieldPolygonShape = new (window as any).google.maps.Polygon({
              paths: coordinates,
              strokeColor: '#4CAF50',
              strokeOpacity: 0.8,
              strokeWeight: 3,
              fillColor: '#4CAF50',
              fillOpacity: 0.2,
              map: mapInstanceRef.current
            });

            fieldPolygonRef.current = fieldPolygonShape;
            console.log('✅ Polígono del campo dibujado en modal');

            // Centrar mapa en el campo
            const bounds = new (window as any).google.maps.LatLngBounds();
            coordinates.forEach((coord: any) => bounds.extend(coord));
            mapInstanceRef.current.fitBounds(bounds);
          } catch (error) {
            console.error('Error dibujando polígono del campo en modal:', error);
          }
        }

        // Forzar redibujado una sola vez
        setTimeout(() => {
          if (mapInstanceRef.current && isMounted) {
            window.dispatchEvent(new Event('resize'));
          }
        }, 500);

      } catch (error) {
        console.error('❌ Error creando mapa para mostrar campo:', error);
      }
    };

    // Verificar si Google Maps está disponible
    if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
      console.log('✅ Google Maps disponible, inicializando mapa para campo...');
      initMap();
    } else {
      console.log('⏳ Google Maps no disponible para campo, esperando...');
      checkInterval = setInterval(() => {
        if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
          clearInterval(checkInterval);
          console.log('✅ Google Maps cargado, inicializando mapa para campo...');
          initMap();
        }
      }, 100);
    }

    return () => {
      isMounted = false;
      if (checkInterval) {
        clearInterval(checkInterval);
      }
    };
  }, []); // Solo ejecutar una vez al montar el componente

  return (
    <div
      ref={mapRef}
      style={{
        width: '100%',
        height: '100%',
        position: 'absolute',
        top: 0,
        left: 0
      }}
    />
  );
};

const FieldsManagement: React.FC = () => {
  const [fields, setFields] = useState<Field[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [showMap, setShowMap] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedField, setSelectedField] = useState<Field | null>(null);
  const [formData, setFormData] = useState<Field>({
    nombre: '',
    superficie: 0,
    poligono: ''
  });
  const [loading, setLoading] = useState(false);
  const [googleMapsReady, setGoogleMapsReady] = useState(false);
  // const [mapReady, setMapReady] = useState(false); // Variable no utilizada
  const [currentPolygon, setCurrentPolygon] = useState<any>(null);

  // Cargar campos desde la API
  const loadFields = async () => {
    try {
      setLoading(true);
      // Simulación de carga desde API
      const mockFields: Field[] = [
        {
          id: 1,
          nombre: 'Campo Norte',
          superficie: 150.5,
          poligono: '{"type":"Polygon","coordinates":[[[-58.3960,-34.6118],[-58.3950,-34.6118],[-58.3950,-34.6128],[-58.3960,-34.6128],[-58.3960,-34.6118]]]}'
        },
        {
          id: 2,
          nombre: 'Campo Sur',
          superficie: 200.0,
          poligono: '{"type":"Polygon","coordinates":[[[-58.3970,-34.6138],[-58.3960,-34.6138],[-58.3960,-34.6148],[-58.3970,-34.6148],[-58.3970,-34.6138]]]}'
        }
      ];
      setFields(mockFields);
    } catch (error) {
      console.error('Error cargando campos:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFields();
  }, []);

  // Cargar Google Maps cuando se muestra el formulario
  useEffect(() => {
    if (showForm && !googleMapsReady) {
      const loadGoogleMaps = async () => {
        try {
          if (!(window as any).google || !(window as any).google.maps) {
            const script = document.createElement('script');
            script.src = `https://maps.googleapis.com/maps/api/js?key=AIzaSyCWz9FKCHBdLqbjhHCPcECww5hs2ugiWA0&libraries=drawing,geometry&loading=async`;
            script.async = true;
            script.defer = true;
            
            script.onload = () => {
              const checkInterval = setInterval(() => {
                if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
                  clearInterval(checkInterval);
                  console.log('✅ Google Maps cargado correctamente');
                  setGoogleMapsReady(true);
                }
              }, 100);
            };
            
            document.head.appendChild(script);
          } else {
            console.log('✅ Google Maps ya está cargado');
            setGoogleMapsReady(true);
          }
        } catch (error) {
          console.error('Error cargando Google Maps:', error);
        }
      };

      loadGoogleMaps();
    }
  }, [showForm]); // Remover googleMapsReady de las dependencias

  // Manejar cuando se completa un polígono
  const handlePolygonComplete = useCallback((polygon: any) => {
    setCurrentPolygon(polygon);

    // Calcular superficie del polígono
    const area = (window as any).google.maps.geometry.spherical.computeArea(polygon.getPath());
    const areaHectareas = area / 10000; // Convertir m² a hectáreas

    setFormData(prev => ({
      ...prev,
      superficie: Math.round(areaHectareas * 100) / 100
    }));

    // Convertir polígono a GeoJSON
    const coordinates = polygon.getPath().getArray().map((latLng: any) => [latLng.lng(), latLng.lat()]);
    const geoJson = {
      type: 'Polygon',
      coordinates: [coordinates]
    };

    setFormData(prev => ({
      ...prev,
      poligono: JSON.stringify(geoJson)
    }));
  }, []);

  // Manejar cuando el mapa está listo
  const handleMapReady = useCallback(() => {
    // setMapReady(true); // Variable no utilizada
  }, []);

  // Guardar campo
  const saveField = async () => {
    try {
      setLoading(true);
      
      // Simulación de guardado en API
      const newField: Field = {
        id: fields.length + 1,
        ...formData
      };
      
      setFields(prev => [...prev, newField]);
      setFormData({ nombre: '', superficie: 0, poligono: '' });
      setShowForm(false);
      setCurrentPolygon(null);
      // setMapReady(false); // Variable no utilizada
      
      // Limpiar polígono del mapa
      if (currentPolygon) {
        currentPolygon.setMap(null);
      }
    } catch (error) {
      console.error('Error guardando campo:', error);
    } finally {
      setLoading(false);
    }
  };

  // Cancelar formulario
  const cancelForm = () => {
    setFormData({ nombre: '', superficie: 0, poligono: '' });
    setShowForm(false);
    setCurrentPolygon(null);
    // setMapReady(false); // Variable no utilizada
    
    // Limpiar polígono del mapa
    if (currentPolygon) {
      currentPolygon.setMap(null);
    }
  };

  // Mostrar campo en el mapa
  const showFieldOnMap = (field: Field) => {
    setSelectedField(field);
    setShowMap(true);
    // Asegurar que Google Maps esté cargado para el modal
    if (!googleMapsReady) {
      const loadGoogleMaps = async () => {
        try {
          if (!(window as any).google || !(window as any).google.maps) {
            const script = document.createElement('script');
            script.src = `https://maps.googleapis.com/maps/api/js?key=AIzaSyCWz9FKCHBdLqbjhHCPcECww5hs2ugiWA0&libraries=drawing,geometry&loading=async`;
            script.async = true;
            script.defer = true;
            
            script.onload = () => {
              const checkGoogleMaps = () => {
                if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
                  console.log('✅ Google Maps cargado para modal');
                  setGoogleMapsReady(true);
                } else {
                  setTimeout(checkGoogleMaps, 100);
                }
              };
              checkGoogleMaps();
            };
            
            document.head.appendChild(script);
          } else {
            setGoogleMapsReady(true);
          }
        } catch (error) {
          console.error('Error cargando Google Maps:', error);
        }
      };
      loadGoogleMaps();
    }
  };

  // Filtrar campos por búsqueda
  const filteredFields = fields.filter(field =>
    field.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #4CAF50 0%, #45a049 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🏞️ Gestión de Campos</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Administra los campos agrícolas de tu propiedad
        </p>
      </div>

      {/* Botón para agregar nuevo campo */}
      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={() => setShowForm(true)}
          style={{
            background: '#4CAF50',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: 'bold'
          }}
        >
          ➕ Agregar Nuevo Campo
        </button>
      </div>

      {/* Formulario para nuevo campo */}
      {showForm && (
        <div style={{ 
          background: '#f9f9f9', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #ddd'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#333' }}>📝 Nuevo Campo</h3>
          
          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Nombre del Campo:
            </label>
            <input
              type="text"
              value={formData.nombre}
              onChange={(e) => setFormData(prev => ({ ...prev, nombre: e.target.value }))}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
              placeholder="Ej: Campo Norte"
            />
          </div>

          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Superficie (hectáreas):
            </label>
            <input
              type="number"
              value={formData.superficie}
              onChange={(e) => setFormData(prev => ({ ...prev, superficie: parseFloat(e.target.value) || 0 }))}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
              placeholder="0.00"
              step="0.01"
            />
          </div>

          {/* Mapa para dibujar polígono */}
          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              🗺️ Dibuja el polígono del campo:
            </label>
            <div style={{
              width: '100%',
              height: '400px',
              border: '2px solid #ddd',
              borderRadius: '8px',
              position: 'relative',
              overflow: 'hidden',
              background: '#f0f0f0'
            }}>
              {!googleMapsReady && (
                <div style={{
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%)',
                  color: '#666',
                  fontSize: '16px',
                  textAlign: 'center'
                }}>
                  🔄 Cargando Google Maps...
                </div>
              )}
              
              {googleMapsReady && (
                <GoogleMapWithDrawing
                  onPolygonComplete={handlePolygonComplete}
                  onMapReady={handleMapReady}
                />
              )}
            </div>
            <p style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>
              💡 Haz clic en el botón de polígono y dibuja el área del campo en el mapa
            </p>
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            <button
              onClick={saveField}
              disabled={loading || !formData.nombre || formData.superficie === 0}
              style={{
                background: '#4CAF50',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: loading || !formData.nombre || formData.superficie === 0 ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                opacity: loading || !formData.nombre || formData.superficie === 0 ? 0.6 : 1
              }}
            >
              {loading ? '💾 Guardando...' : '💾 Guardar Campo'}
            </button>
            
            <button
              onClick={cancelForm}
              style={{
                background: '#f44336',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              ❌ Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Búsqueda */}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="🔍 Buscar campos..."
          style={{
            width: '100%',
            padding: '12px',
            border: '1px solid #ddd',
            borderRadius: '8px',
            fontSize: '16px'
          }}
        />
      </div>

      {/* Lista de campos */}
      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f5f5f5', 
          padding: '15px', 
          borderBottom: '1px solid #ddd',
          fontWeight: 'bold'
        }}>
          📋 Campos Registrados ({filteredFields.length})
        </div>
        
        {filteredFields.length === 0 ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
            {searchTerm ? 'No se encontraron campos que coincidan con la búsqueda' : 'No hay campos registrados'}
          </div>
        ) : (
          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {filteredFields.map((field) => (
              <div key={field.id} style={{ 
                padding: '15px', 
                borderBottom: '1px solid #eee',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div>
                  <h4 style={{ margin: '0 0 5px 0', color: '#333' }}>{field.nombre}</h4>
                  <p style={{ margin: '0', color: '#666', fontSize: '14px' }}>
                    Superficie: {field.superficie} hectáreas
                  </p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button
                    onClick={() => showFieldOnMap(field)}
                    style={{
                      background: '#2196F3',
                      color: 'white',
                      border: 'none',
                      padding: '8px 16px',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontSize: '12px'
                    }}
                  >
                    🗺️ Ver en Mapa
                  </button>
                  <button
                    onClick={() => {
                      // Implementar eliminación
                      setFields(prev => prev.filter(f => f.id !== field.id));
                    }}
                    style={{
                      background: '#f44336',
                      color: 'white',
                      border: 'none',
                      padding: '8px 16px',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontSize: '12px'
                    }}
                  >
                    🗑️ Eliminar
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal para mostrar campo en mapa */}
      {showMap && selectedField && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0,0,0,0.8)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '20px',
            width: '90%',
            height: '80%',
            position: 'relative'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '15px'
            }}>
              <div>
                <h3 style={{ margin: '0 0 5px 0' }}>🗺️ {selectedField.nombre}</h3>
                <p style={{ margin: '0', color: '#666', fontSize: '14px' }}>
                  Superficie: {selectedField.superficie} hectáreas
                </p>
              </div>
              <button
                onClick={() => setShowMap(false)}
                style={{
                  background: '#f44336',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
              >
                ❌ Cerrar
              </button>
            </div>
            
            <div style={{
              width: '100%',
              height: 'calc(100% - 60px)',
              border: '1px solid #ddd',
              borderRadius: '8px',
              position: 'relative',
              overflow: 'hidden',
              background: '#f0f0f0'
            }}>
              {!googleMapsReady && (
                <div style={{
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%)',
                  color: '#666',
                  fontSize: '16px',
                  textAlign: 'center',
                  zIndex: 10
                }}>
                  🔄 Cargando mapa...
                </div>
              )}
              
              {googleMapsReady && (
                <GoogleMapViewField
                  fieldPolygon={selectedField.poligono}
                  fieldName={selectedField.nombre}
                />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FieldsManagement;
