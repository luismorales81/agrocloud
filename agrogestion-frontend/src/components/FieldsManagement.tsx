import React, { useState, useEffect, useRef } from 'react';
import FieldWeatherButton from './FieldWeatherButton';
import OpenMeteoWeatherWidget from './OpenMeteoWeatherWidget';
import { loadGoogleMaps, GOOGLE_MAPS_CONFIG } from '../config/googleMaps';

// Declaraciones de tipos para Google Maps
declare global {
  interface Window {
    google: any;
    initMap: () => void;
  }
}

interface Campo {
  id: number;
  nombre: string;
  superficie: number;
  ubicacion: string;
  coordenadas: Array<{lat: number; lng: number}>;
  estado: 'activo' | 'inactivo' | 'en_mantenimiento';
  fechaCreacion: string;
  descripcion?: string;
}

interface FormData {
  nombre: string;
  superficie: number;
  ubicacion: string;
  descripcion: string;
  estado: 'activo' | 'inactivo' | 'en_mantenimiento';
  coordenadas: Array<{lat: number; lng: number}>;
}

const FieldsManagement: React.FC = () => {
  const [campos, setCampos] = useState<Campo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedField, setSelectedField] = useState<Campo | null>(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showMapModal, setShowMapModal] = useState(false);
  const [mapLoaded, setMapLoaded] = useState(false);
  const [map, setMap] = useState<any>(null);
  const [drawingManager, setDrawingManager] = useState<any>(null);
  const [formData, setFormData] = useState<FormData>({
    nombre: '',
    superficie: 0,
    ubicacion: '',
    descripcion: '',
    estado: 'activo',
    coordenadas: []
  });
  const [isEditing, setIsEditing] = useState(false);
  const mapRef = useRef<HTMLDivElement>(null);

  // Detectar tamaño de pantalla
  const isMobile = window.innerWidth <= 768;

  useEffect(() => {
    cargarCampos();
    initializeMap();
  }, []);

  const initializeMap = () => {
    loadGoogleMaps(() => {
      console.log('Google Maps cargado, inicializando mapa...');
      setMapLoaded(true);
    });
  };

  const cargarCampos = async () => {
    try {
      setLoading(true);
      // Simular datos de campos con coordenadas
      const camposSimulados: Campo[] = [
        {
          id: 1,
          nombre: 'Campo Norte',
          superficie: 150.5,
          ubicacion: 'Ruta 9, Km 45',
          coordenadas: [
            { lat: -34.6118, lng: -58.3960 },
            { lat: -34.6120, lng: -58.3962 },
            { lat: -34.6122, lng: -58.3960 },
            { lat: -34.6120, lng: -58.3958 }
          ],
          estado: 'activo',
          fechaCreacion: '2024-01-15',
          descripcion: 'Campo principal para cultivo de soja'
        },
        {
          id: 2,
          nombre: 'Campo Sur',
          superficie: 89.3,
          ubicacion: 'Ruta 9, Km 47',
          coordenadas: [
            { lat: -34.6130, lng: -58.3970 },
            { lat: -34.6132, lng: -58.3972 },
            { lat: -34.6134, lng: -58.3970 },
            { lat: -34.6132, lng: -58.3968 }
          ],
          estado: 'activo',
          fechaCreacion: '2024-02-20',
          descripcion: 'Campo para rotación de cultivos'
        },
        {
          id: 3,
          nombre: 'Campo Este',
          superficie: 120.0,
          ubicacion: 'Ruta 9, Km 50',
          coordenadas: [
            { lat: -34.6140, lng: -58.3980 },
            { lat: -34.6142, lng: -58.3982 },
            { lat: -34.6144, lng: -58.3980 },
            { lat: -34.6142, lng: -58.3978 }
          ],
          estado: 'en_mantenimiento',
          fechaCreacion: '2024-03-10',
          descripcion: 'Campo en preparación para maíz'
        }
      ];

      setCampos(camposSimulados);
    } catch (err) {
      setError('Error al cargar los campos');
      console.error('Error cargando campos:', err);
    } finally {
      setLoading(false);
    }
  };

  const getEstadoColor = (estado: string) => {
    switch (estado) {
      case 'activo': return '#10b981';
      case 'inactivo': return '#6b7280';
      case 'en_mantenimiento': return '#f59e0b';
      default: return '#6b7280';
    }
  };

  const getEstadoTexto = (estado: string) => {
    switch (estado) {
      case 'activo': return 'Activo';
      case 'inactivo': return 'Inactivo';
      case 'en_mantenimiento': return 'En Mantenimiento';
      default: return 'Desconocido';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('es-AR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const handleVerDetalles = (campo: Campo) => {
    setSelectedField(campo);
    setShowDetailsModal(true);
  };

  const handleEditarCampo = (campo: Campo) => {
    setSelectedField(campo);
    setFormData({
      nombre: campo.nombre,
      superficie: campo.superficie,
      ubicacion: campo.ubicacion,
      descripcion: campo.descripcion || '',
      estado: campo.estado,
      coordenadas: campo.coordenadas
    });
    setIsEditing(true);
    setShowEditModal(true);
    // Inicializar mapa cuando se abre el modal
    setTimeout(() => {
      if (mapLoaded && mapRef.current && !map) {
        initializeMapInForm();
      }
    }, 100);
  };

  const handleAgregarCampo = () => {
    setFormData({
      nombre: '',
      superficie: 0,
      ubicacion: '',
      descripcion: '',
      estado: 'activo',
      coordenadas: []
    });
    setIsEditing(false);
    setShowAddModal(true);
    // Inicializar mapa cuando se abre el modal
    setTimeout(() => {
      if (mapLoaded && mapRef.current && !map) {
        initializeMapInForm();
      }
    }, 100);
  };

  const handleVerMapa = () => {
    setShowMapModal(true);
    // Inicializar mapa cuando se abre el modal
    setTimeout(() => {
      if (mapLoaded && mapRef.current && !map) {
        initializeMapInModal();
      }
    }, 100);
  };

  const initializeMapInModal = () => {
    if (!window.google || !mapRef.current) return;

    const mapInstance = new window.google.maps.Map(mapRef.current, {
      center: GOOGLE_MAPS_CONFIG.DEFAULT_CENTER,
      zoom: GOOGLE_MAPS_CONFIG.DEFAULT_ZOOM,
      mapTypeId: 'satellite',
      mapTypeControl: true,
      streetViewControl: false,
      fullscreenControl: true
    });

    setMap(mapInstance);

    // Agregar Drawing Manager
    const drawingManagerInstance = new window.google.maps.drawing.DrawingManager({
      drawingMode: null,
      drawingControl: true,
      drawingControlOptions: {
        position: window.google.maps.ControlPosition.TOP_CENTER,
        drawingModes: [window.google.maps.drawing.OverlayType.POLYGON]
      }
    });

    drawingManagerInstance.setMap(mapInstance);
    setDrawingManager(drawingManagerInstance);

    // Dibujar campos existentes
    campos.forEach((campo, index) => {
      if (campo.coordenadas.length > 2) {
        const polygon = new window.google.maps.Polygon({
          paths: campo.coordenadas,
          strokeColor: getEstadoColor(campo.estado),
          strokeOpacity: 0.8,
          strokeWeight: 2,
          fillColor: getEstadoColor(campo.estado),
          fillOpacity: 0.35,
          map: mapInstance
        });

        // Info window para cada campo
        const infoWindow = new window.google.maps.InfoWindow({
          content: `
            <div style="padding: 10px; min-width: 200px;">
              <h3 style="margin: 0 0 10px 0; color: #1f2937;">${campo.nombre}</h3>
              <p style="margin: 5px 0; color: #6b7280;"><strong>Superficie:</strong> ${campo.superficie} ha</p>
              <p style="margin: 5px 0; color: #6b7280;"><strong>Estado:</strong> ${getEstadoTexto(campo.estado)}</p>
              <p style="margin: 5px 0; color: #6b7280;"><strong>Ubicación:</strong> ${campo.ubicacion}</p>
            </div>
          `
        });

        polygon.addListener('click', () => {
          infoWindow.setPosition(polygon.getPath().getArray()[0]);
          infoWindow.open(mapInstance);
        });
      }
    });

    // Escuchar eventos de dibujo
    window.google.maps.event.addListener(drawingManagerInstance, 'polygoncomplete', (polygon: any) => {
      const path = polygon.getPath();
      const coordinates = path.getArray().map((latLng: any) => ({
        lat: latLng.lat(),
        lng: latLng.lng()
      }));

      // Calcular superficie aproximada
      const area = window.google.maps.geometry.spherical.computeArea(path);
      const superficie = Math.round((area / 10000) * 100) / 100; // Convertir a hectáreas

      setFormData(prev => ({
        ...prev,
        coordenadas: coordinates,
        superficie: superficie
      }));

      // Agregar marcador con información
      const center = polygon.getBounds().getCenter();
      const infoWindow = new window.google.maps.InfoWindow({
        content: `
          <div style="padding: 10px;">
            <h4 style="margin: 0 0 10px 0;">Nuevo Campo</h4>
            <p style="margin: 5px 0;"><strong>Superficie:</strong> ${superficie} ha</p>
            <p style="margin: 5px 0;"><strong>Coordenadas:</strong> ${coordinates.length} puntos</p>
          </div>
        `
      });

      infoWindow.setPosition(center);
      infoWindow.open(mapInstance);
    });
  };

  const initializeMapInForm = () => {
    if (!window.google || !mapRef.current) return;

    const mapInstance = new window.google.maps.Map(mapRef.current, {
      center: GOOGLE_MAPS_CONFIG.DEFAULT_CENTER,
      zoom: 15,
      mapTypeId: 'satellite',
      mapTypeControl: true,
      streetViewControl: false,
      fullscreenControl: true
    });

    setMap(mapInstance);

    // Agregar Drawing Manager
    const drawingManagerInstance = new window.google.maps.drawing.DrawingManager({
      drawingMode: null,
      drawingControl: true,
      drawingControlOptions: {
        position: window.google.maps.ControlPosition.TOP_CENTER,
        drawingModes: [window.google.maps.drawing.OverlayType.POLYGON]
      }
    });

    drawingManagerInstance.setMap(mapInstance);
    setDrawingManager(drawingManagerInstance);

    // Si estamos editando, mostrar el polígono existente
    if (isEditing && formData.coordenadas.length > 2) {
      const polygon = new window.google.maps.Polygon({
        paths: formData.coordenadas,
        strokeColor: getEstadoColor(formData.estado),
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: getEstadoColor(formData.estado),
        fillOpacity: 0.35,
        map: mapInstance
      });

      // Ajustar el zoom para mostrar el polígono
      const bounds = new window.google.maps.LatLngBounds();
      formData.coordenadas.forEach(coord => {
        bounds.extend(new window.google.maps.LatLng(coord.lat, coord.lng));
      });
      mapInstance.fitBounds(bounds);
    }

    // Escuchar eventos de dibujo
    window.google.maps.event.addListener(drawingManagerInstance, 'polygoncomplete', (polygon: any) => {
      const path = polygon.getPath();
      const coordinates = path.getArray().map((latLng: any) => ({
        lat: latLng.lat(),
        lng: latLng.lng()
      }));

      // Calcular superficie aproximada
      const area = window.google.maps.geometry.spherical.computeArea(path);
      const superficie = Math.round((area / 10000) * 100) / 100; // Convertir a hectáreas

      setFormData(prev => ({
        ...prev,
        coordenadas: coordinates,
        superficie: superficie
      }));

      // Cambiar color del polígono según el estado
      polygon.setOptions({
        strokeColor: getEstadoColor(formData.estado),
        fillColor: getEstadoColor(formData.estado)
      });

      // Mostrar información del área
      const center = polygon.getBounds().getCenter();
      const infoWindow = new window.google.maps.InfoWindow({
        content: `
          <div style="padding: 10px;">
            <h4 style="margin: 0 0 10px 0;">Campo Dibujado</h4>
            <p style="margin: 5px 0;"><strong>Superficie:</strong> ${superficie} ha</p>
            <p style="margin: 5px 0;"><strong>Coordenadas:</strong> ${coordinates.length} puntos</p>
          </div>
        `
      });

      infoWindow.setPosition(center);
      infoWindow.open(mapInstance);
    });
  };

  const handleSaveField = () => {
    if (isEditing && selectedField) {
      // Editar campo existente
      const updatedCampos = campos.map((campo: Campo) => 
        campo.id === selectedField.id 
          ? { ...campo, ...formData }
          : campo
      );
      setCampos(updatedCampos);
    } else {
      // Agregar nuevo campo
      const nuevoCampo: Campo = {
        id: Math.max(...campos.map((c: Campo) => c.id)) + 1,
        ...formData,
        fechaCreacion: new Date().toISOString().split('T')[0]
      };
      setCampos([...campos, nuevoCampo]);
    }
    closeModal();
  };

  const handleEliminarCampo = (campoId: number) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el campo?`)) {
      setCampos(campos.filter((c: Campo) => c.id !== campoId));
    }
  };

  const closeModal = () => {
    setShowDetailsModal(false);
    setShowEditModal(false);
    setShowAddModal(false);
    setShowMapModal(false);
    setSelectedField(null);
    setIsEditing(false);
  };

  const handleInputChange = (field: keyof FormData, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '50vh',
        fontFamily: 'Arial, sans-serif'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ 
            width: '40px', 
            height: '40px', 
            border: '4px solid #f3f3f3', 
            borderTop: '4px solid #4CAF50', 
            borderRadius: '50%', 
            animation: 'spin 1s linear infinite',
            margin: '0 auto 1rem'
          }}></div>
          <p style={{ color: '#666' }}>Cargando campos...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        padding: '2rem', 
        textAlign: 'center',
        fontFamily: 'Arial, sans-serif'
      }}>
        <div style={{ 
          color: '#ef4444', 
          fontSize: '1.5rem', 
          marginBottom: '1rem' 
        }}>
          ❌ Error
        </div>
        <p style={{ color: '#666' }}>{error}</p>
        <button 
          onClick={cargarCampos}
          style={{
            background: '#4CAF50',
            color: 'white',
            border: 'none',
            padding: '0.75rem 1.5rem',
            borderRadius: '0.5rem',
            cursor: 'pointer',
            marginTop: '1rem'
          }}
        >
          Reintentar
        </button>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      {/* Header */}
      <div style={{ 
        background: 'linear-gradient(135deg, #4CAF50 0%, #45a049 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🏞️ Gestión de Campos</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Administra los campos agrícolas con información meteorológica específica por ubicación
        </p>
      </div>

      {/* Estadísticas */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: isMobile ? 'repeat(2, 1fr)' : 'repeat(4, 1fr)',
        gap: '1rem',
        marginBottom: '2rem'
      }}>
        <div style={{
          background: '#eff6ff',
          padding: '1rem',
          borderRadius: '0.5rem',
          border: '1px solid #3b82f6',
          textAlign: 'center'
        }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#2563eb' }}>
            {campos.length}
          </div>
          <div style={{ fontSize: '0.875rem', color: '#1e40af' }}>Total Campos</div>
        </div>
        
        <div style={{
          background: '#f0fdf4',
          padding: '1rem',
          borderRadius: '0.5rem',
          border: '1px solid #22c55e',
          textAlign: 'center'
        }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#16a34a' }}>
            {campos.filter(c => c.estado === 'activo').length}
          </div>
          <div style={{ fontSize: '0.875rem', color: '#15803d' }}>Activos</div>
        </div>
        
        <div style={{
          background: '#fef3c7',
          padding: '1rem',
          borderRadius: '0.5rem',
          border: '1px solid #eab308',
          textAlign: 'center'
        }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ca8a04' }}>
            {campos.filter(c => c.estado === 'en_mantenimiento').length}
          </div>
          <div style={{ fontSize: '0.875rem', color: '#a16207' }}>En Mantenimiento</div>
        </div>
        
        <div style={{
          background: '#fdf4ff',
          padding: '1rem',
          borderRadius: '0.5rem',
          border: '1px solid #a855f7',
          textAlign: 'center'
        }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#9333ea' }}>
            {campos.reduce((sum, campo) => sum + campo.superficie, 0).toFixed(1)} ha
          </div>
          <div style={{ fontSize: '0.875rem', color: '#7c3aed' }}>Superficie Total</div>
        </div>
      </div>

      {/* Botones de acción */}
      <div style={{
        display: 'flex',
        gap: '1rem',
        marginBottom: '2rem',
        flexWrap: 'wrap'
      }}>
        <button 
          onClick={handleAgregarCampo}
          style={{
            background: '#4CAF50',
            color: 'white',
            border: 'none',
            padding: '0.75rem 1.5rem',
            borderRadius: '0.5rem',
            cursor: 'pointer',
            fontSize: '1rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          ➕ Agregar Campo
        </button>
        
        <button 
          onClick={handleVerMapa}
          style={{
            background: '#3b82f6',
            color: 'white',
            border: 'none',
            padding: '0.75rem 1.5rem',
            borderRadius: '0.5rem',
            cursor: 'pointer',
            fontSize: '1rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          🗺️ Ver Mapa
        </button>
      </div>

      {/* Lista de Campos */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '1rem'
      }}>
        {campos.map(campo => (
          <div key={campo.id} style={{
            background: 'white',
            borderRadius: '10px',
            padding: '1rem',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
            border: '1px solid #e5e7eb',
            display: 'flex',
            gap: '1rem',
            alignItems: 'flex-start'
          }}>
            {/* Información del campo - Lado izquierdo */}
            <div style={{
              flex: '1',
              minWidth: '0'
            }}>
              {/* Header del campo */}
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                marginBottom: '0.75rem'
              }}>
                <div>
                  <h3 style={{ 
                    margin: '0 0 0.5rem 0', 
                    fontSize: '1.25rem',
                    color: '#1f2937'
                  }}>
                    {campo.nombre}
                  </h3>
                  <div style={{
                    display: 'inline-block',
                    padding: '0.25rem 0.75rem',
                    borderRadius: '1rem',
                    fontSize: '0.75rem',
                    fontWeight: '500',
                    color: 'white',
                    backgroundColor: getEstadoColor(campo.estado)
                  }}>
                    {getEstadoTexto(campo.estado)}
                  </div>
                </div>
              </div>

              {/* Información del campo */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                gap: '0.75rem',
                marginBottom: '0.75rem'
              }}>
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    📍 Ubicación
                  </div>
                  <div style={{ fontSize: '0.95rem', color: '#374151' }}>
                    {campo.ubicacion}
                  </div>
                </div>
                
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    📏 Superficie
                  </div>
                  <div style={{ fontSize: '0.95rem', color: '#374151' }}>
                    {campo.superficie} hectáreas
                  </div>
                </div>
                
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    📅 Fecha de Creación
                  </div>
                  <div style={{ fontSize: '0.95rem', color: '#374151' }}>
                    {formatDate(campo.fechaCreacion)}
                  </div>
                </div>
                
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    🗺️ Coordenadas
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#374151', fontFamily: 'monospace' }}>
                    {campo.coordenadas[0]?.lat.toFixed(4)}, {campo.coordenadas[0]?.lng.toFixed(4)}
                  </div>
                </div>
              </div>

              {/* Descripción */}
              {campo.descripcion && (
                <div style={{
                  padding: '0.75rem',
                  background: '#f9fafb',
                  borderRadius: '0.5rem',
                  marginBottom: '0.75rem'
                }}>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    📝 Descripción
                  </div>
                  <div style={{ fontSize: '0.875rem', color: '#374151' }}>
                    {campo.descripcion}
                  </div>
                </div>
              )}

              {/* Acciones */}
              <div style={{
                display: 'flex',
                gap: '0.5rem',
                flexWrap: 'wrap'
              }}>
                <button 
                  onClick={() => handleVerDetalles(campo)}
                  style={{
                    background: '#3b82f6',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#2563eb'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#3b82f6'}
                >
                  👁️ Ver Detalles
                </button>
                
                <button 
                  onClick={() => handleEditarCampo(campo)}
                  style={{
                    background: '#f59e0b',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#d97706'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#f59e0b'}
                >
                  ✏️ Editar
                </button>
                
                <button 
                  onClick={() => handleEliminarCampo(campo.id)}
                  style={{
                    background: '#ef4444',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#dc2626'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#ef4444'}
                >
                  🗑️ Eliminar
                </button>
              </div>
            </div>

            {/* Carrusel del clima - Lado derecho */} 
            <div style={{
              width: '250px',
              flexShrink: 0
            }}>
              <OpenMeteoWeatherWidget
                fieldName={campo.nombre}
                coordinates={{
                  lat: campo.coordenadas[0]?.lat || 0,
                  lon: campo.coordenadas[0]?.lng || 0
                }}
                compact={false}
              />
            </div>
          </div>
        ))}
      </div>

      {/* Modal de Mapa */}
      {showMapModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.8)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            width: '90%',
            height: '90%',
            display: 'flex',
            flexDirection: 'column'
          }}>
            <div style={{
              padding: '1rem',
              borderBottom: '1px solid #e5e7eb',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <h2 style={{ margin: 0, color: '#1f2937' }}>
                🗺️ Mapa de Campos
              </h2>
              <button
                onClick={closeModal}
                style={{
                  background: '#6b7280',
                  color: 'white',
                  border: 'none',
                  padding: '0.5rem 1rem',
                  borderRadius: '0.375rem',
                  cursor: 'pointer'
                }}
              >
                ✕ Cerrar
              </button>
            </div>
            
            <div style={{
              flex: 1,
              position: 'relative'
            }}>
              <div 
                ref={mapRef}
                style={{
                  width: '100%',
                  height: '100%',
                  borderRadius: '0 0 10px 10px'
                }}
              />
              
              {!mapLoaded && (
                <div style={{
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%)',
                  textAlign: 'center',
                  color: '#666'
                }}>
                  <div style={{ 
                    width: '40px', 
                    height: '40px', 
                    border: '4px solid #f3f3f3', 
                    borderTop: '4px solid #4CAF50', 
                    borderRadius: '50%', 
                    animation: 'spin 1s linear infinite',
                    margin: '0 auto 1rem'
                  }}></div>
                  <p>Cargando mapa...</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Modal de Detalles */}
      {showDetailsModal && selectedField && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '2rem',
            maxWidth: '500px',
            width: '90%',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ margin: '0 0 1rem 0', color: '#1f2937' }}>
              Detalles del Campo: {selectedField.nombre}
            </h2>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Descripción:</strong> {selectedField.descripcion || 'Sin descripción'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Ubicación:</strong> {selectedField.ubicacion}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Superficie:</strong> {selectedField.superficie} hectáreas
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Estado:</strong> {getEstadoTexto(selectedField.estado)}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Fecha de Creación:</strong> {formatDate(selectedField.fechaCreacion)}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Coordenadas:</strong>
              <div style={{ fontFamily: 'monospace', fontSize: '0.875rem', marginTop: '0.5rem' }}>
                {selectedField.coordenadas.map((coord, index) => (
                  <div key={index}>
                    Punto {index + 1}: {coord.lat.toFixed(6)}, {coord.lng.toFixed(6)}
                  </div>
                ))}
              </div>
            </div>
            <button 
              onClick={closeModal}
              style={{
                background: '#6b7280',
                color: 'white',
                border: 'none',
                padding: '0.75rem 1.5rem',
                borderRadius: '0.375rem',
                cursor: 'pointer'
              }}
            >
              Cerrar
            </button>
          </div>
        </div>
      )}

      {/* Modal de Agregar/Editar Campo */}
      {(showAddModal || showEditModal) && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '2rem',
            maxWidth: '800px',
            width: '95%',
            maxHeight: '95vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ margin: '0 0 1.5rem 0', color: '#1f2937' }}>
              {isEditing ? '✏️ Editar Campo' : '➕ Agregar Nuevo Campo'}
            </h2>
            
            <form onSubmit={(e) => { e.preventDefault(); handleSaveField(); }}>
              {/* Formulario - Parte Superior */}
              <div style={{ marginBottom: '2rem' }}>
                <div style={{ display: 'grid', gap: '1rem' }}>
                  {/* Nombre del campo */}
                  <div>
                    <label style={{ 
                      display: 'block', 
                      marginBottom: '0.5rem',
                      fontWeight: '500',
                      color: '#374151'
                    }}>
                      Nombre del Campo *
                    </label>
                    <input
                      type="text"
                      value={formData.nombre}
                      onChange={(e) => handleInputChange('nombre', e.target.value)}
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '1rem'
                      }}
                      placeholder="Ej: Campo Norte"
                      required
                    />
                  </div>

                  {/* Ubicación */}
                  <div>
                    <label style={{ 
                      display: 'block', 
                      marginBottom: '0.5rem',
                      fontWeight: '500',
                      color: '#374151'
                    }}>
                      Ubicación *
                    </label>
                    <input
                      type="text"
                      value={formData.ubicacion}
                      onChange={(e) => handleInputChange('ubicacion', e.target.value)}
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '1rem'
                      }}
                      placeholder="Ej: Ruta 9, Km 45"
                      required
                    />
                  </div>

                  {/* Superficie */}
                  <div>
                    <label style={{ 
                      display: 'block', 
                      marginBottom: '0.5rem',
                      fontWeight: '500',
                      color: '#374151'
                    }}>
                      Superficie (hectáreas) *
                    </label>
                    <input
                      type="number"
                      step="0.1"
                      min="0"
                      value={formData.superficie}
                      onChange={(e) => handleInputChange('superficie', parseFloat(e.target.value) || 0)}
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '1rem'
                      }}
                      placeholder="0.0"
                      required
                    />
                  </div>

                  {/* Estado */}
                  <div>
                    <label style={{ 
                      display: 'block', 
                      marginBottom: '0.5rem',
                      fontWeight: '500',
                      color: '#374151'
                    }}>
                      Estado *
                    </label>
                    <select
                      value={formData.estado}
                      onChange={(e) => handleInputChange('estado', e.target.value)}
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '1rem'
                      }}
                      required
                    >
                      <option value="activo">Activo</option>
                      <option value="inactivo">Inactivo</option>
                      <option value="en_mantenimiento">En Mantenimiento</option>
                    </select>
                  </div>

                  {/* Descripción */}
                  <div>
                    <label style={{ 
                      display: 'block', 
                      marginBottom: '0.5rem',
                      fontWeight: '500',
                      color: '#374151'
                    }}>
                      Descripción
                    </label>
                    <textarea
                      value={formData.descripcion}
                      onChange={(e) => handleInputChange('descripcion', e.target.value)}
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '1rem',
                        minHeight: '80px',
                        resize: 'vertical'
                      }}
                      placeholder="Descripción del campo, cultivos, etc."
                    />
                  </div>

                  {/* Coordenadas */}
                  {formData.coordenadas.length > 0 && (
                    <div>
                      <label style={{ 
                        display: 'block', 
                        marginBottom: '0.5rem',
                        fontWeight: '500',
                        color: '#374151'
                      }}>
                        Coordenadas ({formData.coordenadas.length} puntos)
                      </label>
                      <div style={{
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        backgroundColor: '#f9fafb',
                        maxHeight: '120px',
                        overflowY: 'auto'
                      }}>
                        {formData.coordenadas.map((coord, index) => (
                          <div key={index} style={{
                            fontSize: '0.875rem',
                            fontFamily: 'monospace',
                            marginBottom: '0.25rem'
                          }}>
                            Punto {index + 1}: {coord.lat.toFixed(6)}, {coord.lng.toFixed(6)}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* Mapa - Parte Inferior */}
              <div style={{ marginBottom: '2rem' }}>
                <div style={{ marginBottom: '1rem' }}>
                  <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151', fontSize: '1.1rem' }}>
                    🗺️ Dibujar Campo en el Mapa
                  </h3>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 1rem 0' }}>
                    Usa la herramienta de polígono para dibujar el contorno del campo. La superficie se calculará automáticamente.
                  </p>
                </div>
                
                <div style={{
                  height: '350px',
                  border: '2px solid #d1d5db',
                  borderRadius: '0.5rem',
                  position: 'relative',
                  overflow: 'hidden'
                }}>
                  <div 
                    ref={mapRef}
                    style={{
                      width: '100%',
                      height: '100%'
                    }}
                  />
                  
                  {!mapLoaded && (
                    <div style={{
                      position: 'absolute',
                      top: '50%',
                      left: '50%',
                      transform: 'translate(-50%, -50%)',
                      textAlign: 'center',
                      color: '#666'
                    }}>
                      <div style={{ 
                        width: '40px', 
                        height: '40px', 
                        border: '4px solid #f3f3f3', 
                        borderTop: '4px solid #4CAF50', 
                        borderRadius: '50%', 
                        animation: 'spin 1s linear infinite',
                        margin: '0 auto 1rem'
                      }}></div>
                      <p>Cargando mapa...</p>
                    </div>
                  )}

                  {/* Controles de zoom personalizados */}
                  <div style={{
                    position: 'absolute',
                    top: '10px',
                    right: '10px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '5px',
                    zIndex: 1000
                  }}>
                    <button
                      type="button"
                      onClick={() => map?.setZoom((map.getZoom() || 15) + 1)}
                      style={{
                        width: '40px',
                        height: '40px',
                        backgroundColor: 'white',
                        border: '1px solid #ccc',
                        borderRadius: '4px',
                        fontSize: '18px',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                      }}
                    >
                      +
                    </button>
                    <button
                      type="button"
                      onClick={() => map?.setZoom((map.getZoom() || 15) - 1)}
                      style={{
                        width: '40px',
                        height: '40px',
                        backgroundColor: 'white',
                        border: '1px solid #ccc',
                        borderRadius: '4px',
                        fontSize: '18px',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                      }}
                    >
                      −
                    </button>
                  </div>

                  {/* Botón de herramienta de polígono personalizado */}
                  <div style={{
                    position: 'absolute',
                    top: '10px',
                    left: '10px',
                    zIndex: 1000
                  }}>
                    <button
                      type="button"
                      onClick={() => {
                        if (drawingManager) {
                          drawingManager.setDrawingMode(window.google.maps.drawing.OverlayType.POLYGON);
                        }
                      }}
                      style={{
                        padding: '8px 12px',
                        backgroundColor: '#4CAF50',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontSize: '14px',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '5px',
                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                      }}
                    >
                      🔷 Dibujar
                    </button>
                  </div>
                </div>

                {/* Instrucciones del mapa */}
                <div style={{
                  marginTop: '1rem',
                  padding: '1rem',
                  backgroundColor: '#f0f9ff',
                  borderRadius: '0.5rem',
                  border: '1px solid #0ea5e9'
                }}>
                  <h4 style={{ margin: '0 0 0.5rem 0', color: '#0c4a6e', fontSize: '1rem' }}>
                    💡 Instrucciones para móvil:
                  </h4>
                  <ul style={{ margin: 0, paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#0c4a6e' }}>
                    <li>Usa los botones + y − para hacer zoom</li>
                    <li>Haz clic en "🔷 Dibujar" para activar la herramienta</li>
                    <li>Toca en cada punto del contorno del campo</li>
                    <li>Doble toque para finalizar el polígono</li>
                    <li>La superficie se calculará automáticamente</li>
                  </ul>
                </div>
              </div>

              {/* Botones de acción */}
              <div style={{
                display: 'flex',
                gap: '1rem',
                justifyContent: 'flex-end',
                paddingTop: '1rem',
                borderTop: '1px solid #e5e7eb'
              }}>
                <button
                  type="button"
                  onClick={closeModal}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#4CAF50',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  {isEditing ? 'Guardar Cambios' : 'Crear Campo'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Estilos CSS para la animación de carga */}
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default FieldsManagement;
