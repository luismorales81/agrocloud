import React, { useState, useEffect, useRef } from 'react';
import LoteHistorial from './LoteHistorial';

interface Campo {
  id: number;
  nombre: string;
  superficie: number;
  poligono: string;
}

interface Lote {
  id?: number;
  nombre: string;
  superficie: number;
  cultivo: string;
  campo_id: number;
  poligono: string;
}

// Componente separado para Google Maps con Drawing Manager para lotes
const GoogleMapWithLotesDrawing: React.FC<{ 
  onPolygonComplete: (polygon: any) => void;
  onMapReady: () => void;
  campoPolygon?: string;
  center?: { lat: number; lng: number };
  zoom?: number;
}> = ({ onPolygonComplete, onMapReady, campoPolygon, center = { lat: -34.6118, lng: -58.3960 }, zoom = 16 }) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const drawingManagerRef = useRef<any>(null);
  const campoPolygonRef = useRef<any>(null);

  useEffect(() => {
    const initMap = () => {
      if (!mapRef.current) return;

      try {
        // Verificar que Google Maps esté completamente cargado
        if (!(window as any).google || !(window as any).google.maps || !(window as any).google.maps.Map) {
          console.log('❌ Google Maps no está completamente cargado');
          return;
        }

        console.log('🗺️ Inicializando mapa para lotes...');
        
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
        console.log('✅ Mapa creado correctamente');

        // Dibujar el polígono del campo si existe
        if (campoPolygon) {
          try {
            const polygonData = JSON.parse(campoPolygon);
            const coordinates = polygonData.coordinates[0].map((coord: number[]) => ({
              lat: coord[1],
              lng: coord[0]
            }));

            const campoPolygonShape = new (window as any).google.maps.Polygon({
              paths: coordinates,
              strokeColor: '#FF0000',
              strokeOpacity: 0.8,
              strokeWeight: 2,
              fillColor: '#FF0000',
              fillOpacity: 0.1,
              map: mapInstanceRef.current
            });

            campoPolygonRef.current = campoPolygonShape;
            console.log('✅ Polígono del campo dibujado');

            // Centrar mapa en el campo
            const bounds = new (window as any).google.maps.LatLngBounds();
            coordinates.forEach((coord: any) => bounds.extend(coord));
            mapInstanceRef.current.fitBounds(bounds);
          } catch (error) {
            console.error('Error dibujando polígono del campo:', error);
          }
        }

        // Configurar Drawing Manager para dibujar polígonos de lotes
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
        console.log('✅ Drawing Manager configurado');

        // Evento cuando se completa el dibujo del polígono
        (window as any).google.maps.event.addListener(drawingManager, 'polygoncomplete', (polygon: any) => {
          console.log('✅ Polígono de lote dibujado');
          // Limpiar polígonos anteriores
          if (drawingManagerRef.current) {
            drawingManagerRef.current.setDrawingMode(null);
          }
          
          onPolygonComplete(polygon);
        });

        // Forzar redibujado después de un delay
        setTimeout(() => {
          if (mapInstanceRef.current && mapInstanceRef.current.setZoom) {
            const currentZoom = mapInstanceRef.current.getZoom();
            mapInstanceRef.current.setZoom(currentZoom);
          }
          window.dispatchEvent(new Event('resize'));
        }, 1000);

        onMapReady();
      } catch (error) {
        console.error('❌ Error creando mapa:', error);
      }
    };

    // Verificar si Google Maps está disponible
    if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
      console.log('✅ Google Maps disponible, inicializando mapa...');
      initMap();
    } else {
      console.log('⏳ Google Maps no disponible, esperando...');
      // Esperar a que Google Maps se cargue
      const checkGoogleMaps = () => {
        if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
          console.log('✅ Google Maps cargado, inicializando mapa...');
          initMap();
        } else {
          console.log('⏳ Esperando que Google Maps se cargue...');
          setTimeout(checkGoogleMaps, 100);
        }
      };
      checkGoogleMaps();
    }
  }, [onPolygonComplete, onMapReady, campoPolygon, center, zoom]);

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

const LotesManagement: React.FC = () => {
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [campos, setCampos] = useState<Campo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [showMap, setShowMap] = useState(false);
  const [selectedCampo, setSelectedCampo] = useState<Campo | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCultivo, setSelectedCultivo] = useState('');
  const [formData, setFormData] = useState<Lote>({
    nombre: '',
    superficie: 0,
    cultivo: '',
    campo_id: 0,
    poligono: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [googleMapsReady, setGoogleMapsReady] = useState(false);
  // const [mapReady, setMapReady] = useState(false); // Variable no utilizada
  const [currentPolygon, setCurrentPolygon] = useState<any>(null);
  const [showHistorial, setShowHistorial] = useState(false);
  const [selectedLoteForHistorial, setSelectedLoteForHistorial] = useState<Lote | null>(null);

  // Cultivos disponibles
  const cultivos = ['Soja', 'Maíz', 'Trigo', 'Girasol', 'Sorgo', 'Cebada', 'Avena', 'Arroz'];

  // Cargar campos desde la API (simulado)
  const loadCampos = async () => {
    try {
      setLoading(true);
      // Simulación de API call
      const mockCampos: Campo[] = [
        { id: 1, nombre: 'Campo 1', superficie: 100.00, poligono: '{"type":"Polygon","coordinates":[[[-58.3960,-34.6118],[-58.3950,-34.6118],[-58.3950,-34.6128],[-58.3960,-34.6128],[-58.3960,-34.6118]]]}' },
        { id: 2, nombre: 'Campo 2', superficie: 200.00, poligono: '{"type":"Polygon","coordinates":[[[-58.3970,-34.6138],[-58.3960,-34.6138],[-58.3960,-34.6148],[-58.3970,-34.6148],[-58.3970,-34.6138]]]}' },
        { id: 3, nombre: 'Campo 3', superficie: 125.75, poligono: '{"type":"Polygon","coordinates":[[[-58.3940,-34.6118],[-58.3930,-34.6118],[-58.3930,-34.6128],[-58.3940,-34.6128],[-58.3940,-34.6118]]]}' },
        { id: 4, nombre: 'Campo 4', superficie: 150.50, poligono: '{"type":"Polygon","coordinates":[[[-58.3980,-34.6158],[-58.3970,-34.6158],[-58.3970,-34.6168],[-58.3980,-34.6168],[-58.3980,-34.6158]]]}' }
      ];
      setCampos(mockCampos);
    } catch (error) {
      setError('Error al cargar campos');
    } finally {
      setLoading(false);
    }
  };

  // Cargar lotes desde la API (simulado)
  const loadLotes = async () => {
    try {
      setLoading(true);
      // Simulación de API call
      const mockLotes: Lote[] = [
        { id: 1, nombre: 'Lote A1', superficie: 25.50, cultivo: 'Soja', campo_id: 1, poligono: '{"type":"Polygon","coordinates":[[[-58.3960,-34.6118],[-58.3955,-34.6118],[-58.3955,-34.6123],[-58.3960,-34.6123],[-58.3960,-34.6118]]]}' },
        { id: 2, nombre: 'Lote A2', superficie: 30.25, cultivo: 'Maíz', campo_id: 1, poligono: '{"type":"Polygon","coordinates":[[[-58.3955,-34.6118],[-58.3950,-34.6118],[-58.3950,-34.6123],[-58.3955,-34.6123],[-58.3955,-34.6118]]]}' },
        { id: 3, nombre: 'Lote B1', superficie: 40.00, cultivo: 'Trigo', campo_id: 2, poligono: '{"type":"Polygon","coordinates":[[[-58.3970,-34.6138],[-58.3965,-34.6138],[-58.3965,-34.6143],[-58.3970,-34.6143],[-58.3970,-34.6138]]]}' },
        { id: 4, nombre: 'Lote B2', superficie: 35.75, cultivo: 'Soja', campo_id: 2, poligono: '{"type":"Polygon","coordinates":[[[-58.3965,-34.6138],[-58.3960,-34.6138],[-58.3960,-34.6143],[-58.3965,-34.6143],[-58.3965,-34.6138]]]}' }
      ];
      setLotes(mockLotes);
    } catch (error) {
      setError('Error al cargar lotes');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCampos();
    loadLotes();
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
              const checkGoogleMaps = () => {
                if ((window as any).google && (window as any).google.maps && (window as any).google.maps.Map) {
                  console.log('✅ Google Maps cargado correctamente');
                  setGoogleMapsReady(true);
                } else {
                  console.log('⏳ Esperando que Google Maps se cargue...');
                  setTimeout(checkGoogleMaps, 100);
                }
              };
              checkGoogleMaps();
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
  }, [showForm, googleMapsReady]);

  // Manejar cuando se completa un polígono
  const handlePolygonComplete = (polygon: any) => {
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
  };

  // Manejar cuando el mapa está listo
  const handleMapReady = () => {
    // setMapReady(true); // Variable no utilizada
  };

  // Guardar lote
  const saveLote = async () => {
    try {
      setLoading(true);
      
      if (!formData.nombre || !formData.cultivo || formData.campo_id === 0) {
        setError('Por favor complete todos los campos');
        return;
      }

      // Simulación de guardado en API
      const newLote: Lote = {
        id: lotes.length + 1,
        ...formData
      };
      
      setLotes(prev => [...prev, newLote]);
      setFormData({ nombre: '', superficie: 0, cultivo: '', campo_id: 0, poligono: '' });
      setShowForm(false);
      setCurrentPolygon(null);
      setSuccess('Lote guardado exitosamente');
      
      // Limpiar polígono del mapa
      if (currentPolygon) {
        currentPolygon.setMap(null);
      }
    } catch (error) {
      console.error('Error guardando lote:', error);
      setError('Error al guardar el lote');
    } finally {
      setLoading(false);
    }
  };

  // Eliminar lote
  const deleteLote = async (id: number) => {
    if (window.confirm('¿Estás seguro de que quieres eliminar este lote?')) {
      try {
        setLoading(true);
        // Simulación de API call
        setLotes(prev => prev.filter(lote => lote.id !== id));
        setSuccess('Lote eliminado exitosamente');
      } catch (error) {
        setError('Error al eliminar lote');
      } finally {
        setLoading(false);
      }
    }
  };

  // Filtrar lotes por búsqueda
  const filteredLotes = lotes.filter(lote =>
    lote.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    lote.cultivo.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Obtener nombre del campo
  const getCampoName = (campoId: number) => {
    const campo = campos.find(c => c.id === campoId);
    return campo ? campo.nombre : 'Campo no encontrado';
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🏞️ Gestión de Lotes</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Divide campos en lotes y asigna cultivos específicos
        </p>
      </div>

      {/* Mensajes de estado */}
      {error && (
        <div style={{ 
          background: '#ffebee', 
          color: '#c62828', 
          padding: '10px', 
          borderRadius: '5px', 
          marginBottom: '10px',
          border: '1px solid #ffcdd2'
        }}>
          {error}
        </div>
      )}
      {success && (
        <div style={{ 
          background: '#e8f5e8', 
          color: '#2e7d32', 
          padding: '10px', 
          borderRadius: '5px', 
          marginBottom: '10px',
          border: '1px solid #c8e6c9'
        }}>
          {success}
        </div>
      )}

      {/* Botones de acción */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        <button
          onClick={() => setShowForm(true)}
          style={{
            background: '#4CAF50',
            color: 'white',
            border: 'none',
            padding: '10px 20px',
            borderRadius: '5px',
            cursor: 'pointer',
            fontSize: '14px'
          }}
        >
          ➕ Nuevo Lote
        </button>
        <button
          onClick={() => setShowMap(!showMap)}
          style={{
            background: '#2196F3',
            color: 'white',
            border: 'none',
            padding: '10px 20px',
            borderRadius: '5px',
            cursor: 'pointer',
            fontSize: '14px'
          }}
        >
          🗺️ {showMap ? 'Ocultar Mapa' : 'Mostrar Mapa'}
        </button>
      </div>

      {/* Filtros */}
      <div style={{ 
        background: '#f5f5f5', 
        padding: '15px', 
        borderRadius: '8px', 
        marginBottom: '20px',
        display: 'flex',
        gap: '15px',
        flexWrap: 'wrap',
        alignItems: 'center'
      }}>
        <div>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Buscar:</label>
          <input
            type="text"
            placeholder="Buscar por nombre..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              padding: '8px 12px',
              border: '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '14px',
              width: '200px'
            }}
          />
        </div>
        <div>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Cultivo:</label>
          <select
            value={selectedCultivo}
            onChange={(e) => setSelectedCultivo(e.target.value)}
            style={{
              padding: '8px 12px',
              border: '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '14px',
              width: '150px'
            }}
          >
            <option value="">Todos los cultivos</option>
            {cultivos.map(cultivo => (
              <option key={cultivo} value={cultivo}>{cultivo}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Mapa */}
      {showMap && (
        <div style={{ marginBottom: '20px' }}>
          <div style={{ 
            background: '#f5f5f5', 
            padding: '15px', 
            borderRadius: '8px', 
            marginBottom: '10px' 
          }}>
            <h3 style={{ margin: '0 0 10px 0' }}>🗺️ Mapa de Lotes</h3>
            <p style={{ margin: '0', fontSize: '14px', color: '#666' }}>
              Selecciona un campo para dibujar lotes. Los polígonos verdes son campos, los rojos son lotes.
            </p>
          </div>
          <GoogleMapWithLotesDrawing
            onPolygonComplete={handlePolygonComplete}
            onMapReady={handleMapReady}
            campoPolygon={selectedCampo ? JSON.stringify(JSON.parse(selectedCampo.poligono)) : undefined}
            center={selectedCampo ? { lat: JSON.parse(selectedCampo.poligono).coordinates[0][0][1], lng: JSON.parse(selectedCampo.poligono).coordinates[0][0][0] } : undefined}
            zoom={selectedCampo ? 16 : 14}
          />
        </div>
      )}

      {/* Formulario de nuevo lote */}
      {showForm && (
        <div style={{ 
          background: '#fff3e0', 
          padding: '20px', 
          borderRadius: '8px', 
          marginBottom: '20px',
          border: '1px solid #ffcc02'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#e65100' }}>📝 Nuevo Lote</h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '15px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Campo:</label>
              <select
                value={formData.campo_id}
                onChange={(e) => {
                  const campoId = Number(e.target.value);
                  const campo = campos.find(c => c.id === campoId);
                  setFormData(prev => ({ ...prev, campo_id: campoId }));
                  if (campo) {
                    setSelectedCampo(campo);
                    // El polígono del campo se dibujará automáticamente en el componente GoogleMapWithLotesDrawing
                  }
                }}
                style={{
                  padding: '8px 12px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '14px',
                  width: '100%'
                }}
              >
                <option value={0}>Seleccionar campo</option>
                {campos.map(campo => (
                  <option key={campo.id} value={campo.id}>{campo.nombre}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Nombre del Lote:</label>
              <input
                type="text"
                placeholder="Ej: Lote A1"
                value={formData.nombre}
                onChange={(e) => setFormData(prev => ({ ...prev, nombre: e.target.value }))}
                style={{
                  padding: '8px 12px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '14px',
                  width: '100%'
                }}
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Cultivo:</label>
              <select
                value={formData.cultivo}
                onChange={(e) => setFormData(prev => ({ ...prev, cultivo: e.target.value }))}
                style={{
                  padding: '8px 12px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '14px',
                  width: '100%'
                }}
              >
                <option value="">Seleccionar cultivo</option>
                {cultivos.map(cultivo => (
                  <option key={cultivo} value={cultivo}>{cultivo}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Superficie (ha):</label>
              <input
                type="number"
                step="0.01"
                placeholder="0.00"
                value={formData.superficie}
                onChange={(e) => setFormData(prev => ({ ...prev, superficie: Number(e.target.value) }))}
                style={{
                  padding: '8px 12px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '14px',
                  width: '100%'
                }}
              />
            </div>
          </div>

          <div style={{ marginTop: '15px', display: 'flex', gap: '10px' }}>
            <button
              onClick={saveLote}
              disabled={loading}
              style={{
                background: '#4CAF50',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                opacity: loading ? 0.6 : 1
              }}
            >
              {loading ? 'Guardando...' : '💾 Guardar Lote'}
            </button>
            <button
              onClick={() => {
                setShowForm(false);
                setFormData({
                  nombre: '',
                  superficie: 0,
                  cultivo: '',
                  campo_id: 0,
                  poligono: ''
                });
                if (currentPolygon) { // Use currentPolygon from GoogleMapWithLotesDrawing
                  currentPolygon.setMap(null);
                }
              }}
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

      {/* Tabla de lotes */}
      <div style={{ 
        background: 'white', 
        borderRadius: '8px', 
        overflow: 'hidden',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f8f9fa', 
          padding: '15px', 
          borderBottom: '1px solid #dee2e6' 
        }}>
          <h3 style={{ margin: '0', color: '#495057' }}>📊 Lista de Lotes ({filteredLotes.length})</h3>
        </div>

        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            Cargando lotes...
          </div>
        ) : filteredLotes.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            No se encontraron lotes
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ 
              width: '100%', 
              borderCollapse: 'collapse',
              fontSize: '14px'
            }}>
              <thead>
                <tr style={{ background: '#f8f9fa' }}>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Lote</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Campo</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Superficie</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cultivo</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {filteredLotes.map(lote => (
                  <tr key={lote.id} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}>
                      <strong>{lote.nombre}</strong>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {getCampoName(lote.campo_id)}
                    </td>
                    <td style={{ padding: '12px' }}>
                      {lote.superficie} ha
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        background: '#e3f2fd',
                        color: '#1976d2',
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold'
                      }}>
                        {lote.cultivo}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button
                          onClick={() => {
                            setSelectedLoteForHistorial(lote);
                            setShowHistorial(true);
                          }}
                          style={{
                            background: '#2196F3',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                        >
                          📋 Historial
                        </button>
                        <button
                          onClick={() => deleteLote(lote.id!)}
                          style={{
                            background: '#f44336',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                        >
                          🗑️ Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Estadísticas */}
      <div style={{ 
        background: '#e8f5e8', 
        padding: '15px', 
        borderRadius: '8px', 
        marginTop: '20px' 
      }}>
        <h4 style={{ margin: '0 0 10px 0', color: '#2e7d32' }}>📈 Estadísticas</h4>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '15px' }}>
          <div>
            <strong>Total de Lotes:</strong> {lotes.length}
          </div>
          <div>
            <strong>Superficie Total:</strong> {lotes.reduce((sum, lote) => sum + lote.superficie, 0).toFixed(2)} ha
          </div>
          <div>
            <strong>Cultivos Diferentes:</strong> {new Set(lotes.map(l => l.cultivo)).size}
          </div>
          <div>
            <strong>Campos con Lotes:</strong> {new Set(lotes.map(l => l.campo_id)).size}
          </div>
        </div>
      </div>

      {/* Modal de Historial */}
      {showHistorial && selectedLoteForHistorial && (
        <LoteHistorial
          loteId={selectedLoteForHistorial.id!}
          loteNombre={selectedLoteForHistorial.nombre}
          onClose={() => {
            setShowHistorial(false);
            setSelectedLoteForHistorial(null);
          }}
        />
      )}
    </div>
  );
};

export default LotesManagement;
