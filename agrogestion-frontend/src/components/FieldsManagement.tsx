import React, { useState, useEffect, useRef } from 'react';
import FieldWeatherButton from './FieldWeatherButton';
import OpenMeteoWeatherWidget from './OpenMeteoWeatherWidget';
import PermissionGate from './PermissionGate';
import { loadGoogleMaps, GOOGLE_MAPS_CONFIG } from '../config/googleMaps';
import api from '../services/api';

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
  const [mapModal, setMapModal] = useState<any>(null);
  const [drawingManager, setDrawingManager] = useState<any>(null);
  const [formData, setFormData] = useState<FormData>({
    nombre: '',
    superficie: 0,
    ubicacion: '',
    descripcion: '',
    estado: 'activo',
    coordenadas: []
  });

  // Wrapper para setFormData con logging
  const setFormDataWithLog = (newFormData: FormData | ((prev: FormData) => FormData)) => {
    console.log('=== SET FORMDATA EJECUTADO ===');
    console.log('Timestamp:', new Date().toISOString());
    console.log('New formData:', newFormData);
    console.log('Current formData:', formData);
    console.log('Stack trace:', new Error().stack);
    setFormData(newFormData);
  };
  const [isEditing, setIsEditing] = useState(false);
  const [userLocation, setUserLocation] = useState<{lat: number; lng: number} | null>(null);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [showSearchResults, setShowSearchResults] = useState<boolean>(false);
  const [isDrawingMode, setIsDrawingMode] = useState<boolean>(false);
  const [isButtonClicked, setIsButtonClicked] = useState<boolean>(false);
  const mapRef = useRef<HTMLDivElement>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);

  // Detectar tamaño de pantalla
  const isMobile = window.innerWidth <= 768;

  useEffect(() => {
    console.log('=== useEffect INITIAL - Cargando campos e inicializando mapa ===');
    cargarCampos();
    initializeMap();
  }, []);

  // Limpiar mapa cuando se cierre el modal
  useEffect(() => {
    console.log('=== useEffect MODAL CLEANUP ===');
    console.log('Timestamp:', new Date().toISOString());
    console.log('showAddModal:', showAddModal, 'showEditModal:', showEditModal, 'showMapModal:', showMapModal);
    console.log('Stack trace:', new Error().stack);
    if (!showAddModal && !showEditModal && !showMapModal) {
      console.log('Cleaning up map state');
      setMap(null);
      setMapModal(null);
      setDrawingManager(null);
      setIsDrawingMode(false);
    }
  }, [showAddModal, showEditModal, showMapModal]);

  // Inicializar mapa en modal de detalles cuando se abre
  useEffect(() => {
    if (showDetailsModal && selectedField && mapLoaded) {
      // Pequeño delay para asegurar que el DOM esté listo
      setTimeout(() => {
        initializeMapInDetails(selectedField);
      }, 100);
    }
  }, [showDetailsModal, selectedField, mapLoaded]);

  // Cerrar resultados de búsqueda al hacer clic fuera
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchInputRef.current && !searchInputRef.current.contains(event.target as Node)) {
        setShowSearchResults(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const getUserLocation = () => {
    console.log('=== GET USER LOCATION INICIADO ===');
    console.log('Timestamp:', new Date().toISOString());
    console.log('getUserLocation called, showMapModal:', showMapModal, 'mapModal:', !!mapModal, 'map:', !!map);
    console.log('Current formData before getUserLocation:', formData);
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          setUserLocation(location);
          setLocationError(null);
          console.log('=== UBICACIÓN OBTENIDA ===');
          console.log('Timestamp:', new Date().toISOString());
          console.log('Ubicación del usuario obtenida:', location);
          console.log('formData after setting location:', formData);
          
          // Centrar el mapa en la ubicación del usuario
          const currentMap = showMapModal ? mapModal : map;
          const currentDrawingManager = showMapModal ? null : drawingManager;
          
          if (currentMap) {
            currentMap.setCenter(location);
            currentMap.setZoom(15);
            
            // Si estamos en el modal de agregar/editar campo, activar modo de dibujo
            if (!showMapModal && currentDrawingManager) {
              // Activar modo de dibujo de polígono
              currentDrawingManager.setDrawingMode(window.google.maps.drawing.OverlayType.POLYGON);
              setIsDrawingMode(true);
              console.log('=== MODO DE DIBUJO ACTIVADO ===');
              console.log('Timestamp:', new Date().toISOString());
              console.log('Modo de dibujo de polígono activado');
              console.log('formData después de activar modo de dibujo:', formData);
            }
          }
        },
        (error) => {
          console.error('Error obteniendo ubicación:', error);
          setLocationError('No se pudo obtener la ubicación. Verifica que tengas permisos de ubicación habilitados.');
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 300000 // 5 minutos
        }
      );
    } else {
      console.log('Geolocalización no soportada por este navegador');
      setLocationError('Geolocalización no soportada por este navegador.');
    }
    console.log('=== GET USER LOCATION TERMINADO ===');
    console.log('Timestamp:', new Date().toISOString());
    console.log('formData final después de getUserLocation:', formData);
  };

  const centerMapOnAllFields = () => {
    console.log('centerMapOnAllFields called, showMapModal:', showMapModal, 'mapModal:', !!mapModal, 'map:', !!map, 'campos:', campos.length);
    const currentMap = showMapModal ? mapModal : map;
    if (!currentMap || !campos.length) return;

    // Filtrar campos que tienen coordenadas válidas
    const camposConCoordenadas = campos.filter(campo => 
      campo.coordenadas && campo.coordenadas.length > 0
    );

    if (camposConCoordenadas.length === 0) {
      console.log('No hay campos con coordenadas para mostrar');
      return;
    }

    if (camposConCoordenadas.length === 1) {
      // Si solo hay un campo, centrar en él
      const campo = camposConCoordenadas[0];
      const center = {
        lat: campo.coordenadas[0].lat,
        lng: campo.coordenadas[0].lng
      };
      currentMap.setCenter(center);
      currentMap.setZoom(15);
      return;
    }

    // Si hay múltiples campos, crear bounds para incluir todos
    const bounds = new window.google.maps.LatLngBounds();
    
    camposConCoordenadas.forEach(campo => {
      campo.coordenadas.forEach(coord => {
        bounds.extend(new window.google.maps.LatLng(coord.lat, coord.lng));
      });
    });

    currentMap.fitBounds(bounds);
    
    // Ajustar el zoom si es muy cercano
    const listener = window.google.maps.event.addListener(currentMap, 'idle', () => {
      if (currentMap.getZoom() > 15) currentMap.setZoom(15);
      window.google.maps.event.removeListener(listener);
    });
  };

  const initializeMapInDetails = (campo: Campo) => {
    if (!window.google || !mapRef.current || !campo.coordenadas || campo.coordenadas.length === 0) return;

    // Calcular el centro del campo
    const center = {
      lat: campo.coordenadas.reduce((sum, coord) => sum + coord.lat, 0) / campo.coordenadas.length,
      lng: campo.coordenadas.reduce((sum, coord) => sum + coord.lng, 0) / campo.coordenadas.length
    };

    // Crear el mapa centrado en el campo
    const mapInstance = new window.google.maps.Map(mapRef.current, {
      center: center,
      zoom: 15,
      mapTypeId: 'satellite',
      mapTypeControl: true,
      streetViewControl: false,
      fullscreenControl: false
    });

    // Dibujar el polígono del campo
    const polygon = new window.google.maps.Polygon({
      paths: campo.coordenadas,
      strokeColor: getEstadoColor(campo.estado),
      strokeOpacity: 0.8,
      strokeWeight: 2,
      fillColor: getEstadoColor(campo.estado),
      fillOpacity: 0.35,
      map: mapInstance
    });

    // Crear info window con información del campo
    const infoWindow = new window.google.maps.InfoWindow({
      content: `
        <div style="padding: 10px; min-width: 200px;">
          <h3 style="margin: 0 0 10px 0; color: #1f2937;">${campo.nombre}</h3>
          <p style="margin: 5px 0; color: #6b7280;"><strong>Superficie:</strong> ${campo.superficie.toFixed(2)} ha</p>
          <p style="margin: 5px 0; color: #6b7280;"><strong>Estado:</strong> ${getEstadoTexto(campo.estado)}</p>
          <p style="margin: 5px 0; color: #6b7280;"><strong>Ubicación:</strong> ${campo.ubicacion}</p>
        </div>
      `
    });

    // Mostrar info window automáticamente
    infoWindow.setPosition(center);
    infoWindow.open(mapInstance);

    // Hacer clickeable el polígono
    polygon.addListener('click', () => {
      infoWindow.setPosition(polygon.getPath().getArray()[0]);
      infoWindow.open(mapInstance);
    });
  };

  const searchPlaces = async (query: string) => {
    if (!window.google || !query.trim()) {
      setSearchResults([]);
      setShowSearchResults(false);
      return;
    }

    try {
      const service = new window.google.maps.places.PlacesService(map || document.createElement('div'));
      const request = {
        query: query,
        fields: ['name', 'geometry', 'formatted_address', 'place_id']
      };

      service.textSearch(request, (results: any, status: any) => {
        if (status === window.google.maps.places.PlacesServiceStatus.OK && results) {
          setSearchResults(results.slice(0, 5)); // Limitar a 5 resultados
          setShowSearchResults(true);
        } else {
          setSearchResults([]);
          setShowSearchResults(false);
        }
      });
    } catch (error) {
      console.error('Error en búsqueda de lugares:', error);
      setSearchResults([]);
      setShowSearchResults(false);
    }
  };

  const handleSearchInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setSearchQuery(query);
    
    if (query.length > 2) {
      searchPlaces(query);
    } else {
      setSearchResults([]);
      setShowSearchResults(false);
    }
  };

  const selectSearchResult = (place: any) => {
    if (place.geometry && place.geometry.location && map) {
      const location = {
        lat: place.geometry.location.lat(),
        lng: place.geometry.location.lng()
      };
      
      // Centrar el mapa en el lugar seleccionado
      map.setCenter(location);
      map.setZoom(15);
      
      // Agregar marcador temporal para el lugar seleccionado
      const marker = new window.google.maps.Marker({
        position: location,
        map: map,
        title: place.name,
        icon: {
          path: window.google.maps.SymbolPath.CIRCLE,
          scale: 10,
          fillColor: '#ef4444',
          fillOpacity: 1,
          strokeColor: '#ffffff',
          strokeWeight: 2
        }
      });

      // Mostrar InfoWindow con información del lugar
      const infoWindow = new window.google.maps.InfoWindow({
        content: `
          <div style="padding: 10px; text-align: center;">
            <h4 style="margin: 0 0 5px 0; color: #ef4444;">📍 ${place.name}</h4>
            <p style="margin: 0; font-size: 12px; color: #666;">
              ${place.formatted_address || 'Dirección no disponible'}
            </p>
          </div>
        `
      });

      infoWindow.open(map, marker);
      
      // Limpiar búsqueda
      setSearchQuery('');
      setSearchResults([]);
      setShowSearchResults(false);
      
      // Remover el marcador después de 5 segundos
      setTimeout(() => {
        marker.setMap(null);
        infoWindow.close();
      }, 5000);
    }
  };

  const initializeMap = () => {
    loadGoogleMaps(() => {
      console.log('Google Maps cargado, inicializando mapa...');
      setMapLoaded(true);
    });
  };

  const cargarCampos = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Obtener token de autenticación
      const token = localStorage.getItem('token');
      if (!token) {
        setError('No hay token de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      // Llamar a la API real para obtener los campos del usuario
      const response = await api.get('/api/campos');
      const data = response.data;
      
      // Mapear los datos de la API al formato del frontend
      const camposMapeados: Campo[] = data.map((field: any) => ({
        id: field.id,
        nombre: field.nombre,
        superficie: field.areaHectareas || 0,
        ubicacion: field.ubicacion || '',
        coordenadas: field.coordenadas || [],
        estado: field.estado?.toLowerCase() || 'activo',
        fechaCreacion: field.fechaCreacion || new Date().toISOString().split('T')[0],
        descripcion: field.descripcion || ''
      }));
      
      setCampos(camposMapeados);
      console.log('✅ Campos cargados exitosamente:', camposMapeados.length);
      
    } catch (error) {
      console.error('❌ Error cargando campos:', error);
      setError('Error al cargar los campos. Por favor, inténtalo de nuevo.');
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
    // Scroll al modal
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 100);
    // Inicializar mapa cuando se abre el modal
    setTimeout(() => {
      if (mapLoaded && mapRef.current) {
        // Limpiar mapa existente si hay uno
        if (map) {
          setMap(null);
          setDrawingManager(null);
        }
        initializeMapInForm(campo.coordenadas, true); // Pasar coordenadas y modo edición
      }
    }, 100);
  };

  const handleAgregarCampo = () => {
    console.log('=== HANDLE AGREGAR CAMPO EJECUTADO ===');
    console.log('Timestamp:', new Date().toISOString());
    console.log('isButtonClicked:', isButtonClicked);
    console.log('Stack trace:', new Error().stack);
    
    // Prevenir ejecuciones múltiples
    if (isButtonClicked) {
      console.log('⚠️ PREVENIENDO DOBLE CLIC - handleAgregarCampo ya se ejecutó');
      return;
    }
    
    setIsButtonClicked(true);
    setFormDataWithLog({
      nombre: '',
      superficie: 0,
      ubicacion: '',
      descripcion: '',
      estado: 'activo',
      coordenadas: []
    });
    setIsEditing(false);
    setShowAddModal(true);
    // Scroll al modal
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 100);
    // Inicializar mapa cuando se abre el modal
    setTimeout(() => {
      if (mapLoaded && mapRef.current) {
        // Limpiar mapa existente si hay uno
        if (map) {
          setMap(null);
          setDrawingManager(null);
        }
        initializeMapInForm(undefined, false);
      }
    }, 100);
  };

  const handleVerMapa = () => {
    setShowMapModal(true);
    // Inicializar mapa cuando se abre el modal
    setTimeout(() => {
      if (mapLoaded && mapRef.current) {
        // Limpiar mapa existente si hay uno
        if (mapModal) {
          setMapModal(null);
        }
        initializeMapInModal();
      }
    }, 100);
  };

  const initializeMapInModal = () => {
    if (!window.google || !mapRef.current) return;

    // Limpiar el contenido del div del mapa
    if (mapRef.current) {
      mapRef.current.innerHTML = '';
    }

    // Usar ubicación por defecto
    const mapInstance = new window.google.maps.Map(mapRef.current, {
      center: GOOGLE_MAPS_CONFIG.DEFAULT_CENTER,
      zoom: GOOGLE_MAPS_CONFIG.DEFAULT_ZOOM,
      mapTypeId: 'satellite',
      mapTypeControl: true,
      streetViewControl: false,
      fullscreenControl: true
    });

    setMapModal(mapInstance);

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
            <div style="padding: 15px; min-width: 250px; font-family: Arial, sans-serif;">
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px;">
                <div style="
                  width: 12px; 
                  height: 12px; 
                  background-color: ${getEstadoColor(campo.estado)}; 
                  border-radius: 50%;
                  border: 1px solid ${getEstadoColor(campo.estado)};
                "></div>
                <h3 style="margin: 0; color: #1f2937; font-size: 16px;">${campo.nombre}</h3>
              </div>
              <div style="margin-bottom: 8px;">
                <span style="color: #6b7280; font-size: 12px;"><strong>📍 Ubicación:</strong></span>
                <div style="color: #374151; font-size: 13px; margin-top: 2px;">${campo.ubicacion}</div>
              </div>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 8px;">
                <div>
                  <span style="color: #6b7280; font-size: 12px;"><strong>📏 Superficie:</strong></span>
                  <div style="color: #374151; font-size: 13px;">${campo.superficie.toFixed(2)} ha</div>
                </div>
                <div>
                  <span style="color: #6b7280; font-size: 12px;"><strong>🏷️ Estado:</strong></span>
                  <div style="color: #374151; font-size: 13px;">${getEstadoTexto(campo.estado)}</div>
                </div>
              </div>
              ${campo.descripcion ? `
                <div style="margin-bottom: 8px;">
                  <span style="color: #6b7280; font-size: 12px;"><strong>📝 Descripción:</strong></span>
                  <div style="color: #374151; font-size: 13px; margin-top: 2px;">${campo.descripcion}</div>
                </div>
              ` : ''}
              <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #e5e7eb;">
                <span style="color: #6b7280; font-size: 11px;">
                  🗓️ Creado: ${formatDate(campo.fechaCreacion)}
                </span>
              </div>
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
      const superficie = Math.round((area / 10000) * 100) / 100; // Convertir a hectáreas con 2 decimales

      setFormData(prev => ({
        ...prev,
        coordenadas: coordinates,
        superficie: superficie
      }));

      // Calcular el centro del polígono
      const bounds = new window.google.maps.LatLngBounds();
      coordinates.forEach((coord: {lat: number; lng: number}) => {
        bounds.extend(new window.google.maps.LatLng(coord.lat, coord.lng));
      });
      const center = bounds.getCenter();

      // Agregar marcador con información
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

  const initializeMapInForm = (existingCoordinates?: Array<{lat: number; lng: number}>, isEditMode?: boolean) => {
    if (!window.google || !mapRef.current) return;

    // Limpiar el contenido del div del mapa
    if (mapRef.current) {
      mapRef.current.innerHTML = '';
    }

    // Usar ubicación por defecto
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
    const coordinatesToUse = existingCoordinates || formData.coordenadas;
    if (isEditMode && coordinatesToUse.length > 2) {
      const polygon = new window.google.maps.Polygon({
        paths: coordinatesToUse,
        strokeColor: getEstadoColor(formData.estado),
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: getEstadoColor(formData.estado),
        fillOpacity: 0.35,
        map: mapInstance
      });

      // Ajustar el zoom para mostrar el polígono
      const bounds = new window.google.maps.LatLngBounds();
      coordinatesToUse.forEach(coord => {
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
      const superficie = Math.round((area / 10000) * 100) / 100; // Convertir a hectáreas con 2 decimales

      setFormData(prev => ({
        ...prev,
        coordenadas: coordinates,
        superficie: superficie
      }));

      // Desactivar modo de dibujo
      setIsDrawingMode(false);
      drawingManagerInstance.setDrawingMode(null);

      // Cambiar color del polígono según el estado
      polygon.setOptions({
        strokeColor: getEstadoColor(formData.estado),
        fillColor: getEstadoColor(formData.estado)
      });

      // Calcular el centro del polígono
      const bounds = new window.google.maps.LatLngBounds();
      coordinates.forEach((coord: {lat: number; lng: number}) => {
        bounds.extend(new window.google.maps.LatLng(coord.lat, coord.lng));
      });
      const center = bounds.getCenter();

      // Mostrar información del área
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

  const handleSaveField = async () => {
    // Validaciones requeridas
    if (!formData.nombre.trim()) {
      alert('⚠️ El nombre del campo es obligatorio');
      return;
    }

    if (!formData.ubicacion.trim()) {
      alert('⚠️ La ubicación del campo es obligatoria');
      return;
    }

    if (!formData.coordenadas || formData.coordenadas.length < 3) {
      alert('⚠️ Debes dibujar el polígono del campo en el mapa para definir su área');
      return;
    }

    try {
      setLoading(true);
      
      const token = localStorage.getItem('token');
      if (!token) {
        alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      // Preparar datos para enviar al backend
      const fieldData = {
        nombre: formData.nombre.trim(),
        ubicacion: formData.ubicacion.trim(),
        areaHectareas: formData.superficie,
        descripcion: formData.descripcion.trim(),
        estado: formData.estado.toUpperCase(),
        coordenadas: formData.coordenadas, // Enviar como array, no como string JSON
        poligono: formData.coordenadas.map(coord => `${coord.lat},${coord.lng}`).join(';')
      };

      let response;
    if (isEditing && selectedField) {
      // Editar campo existente
        response = await api.put(`/api/campos/${selectedField.id}`, fieldData);
    } else {
        // Crear nuevo campo
        response = await api.post('/api/campos', fieldData);
    }

      if (response.status >= 200 && response.status < 300) {
        const updatedField = response.data;
        
        if (isEditing && selectedField) {
          // Actualizar campo existente en el estado local
          const mappedField: Campo = {
            id: updatedField.id,
            nombre: updatedField.nombre,
            superficie: updatedField.areaHectareas || 0,
            ubicacion: updatedField.ubicacion,
            coordenadas: updatedField.coordenadas || [],
            estado: updatedField.estado?.toLowerCase() || 'activo',
            fechaCreacion: updatedField.fechaCreacion || new Date().toISOString().split('T')[0],
            descripcion: updatedField.descripcion || ''
          };
          setCampos(prev => prev.map(f => f.id === selectedField.id ? mappedField : f));
        } else {
          // Agregar nuevo campo al estado local
          const mappedField: Campo = {
            id: updatedField.id,
            nombre: updatedField.nombre,
            superficie: updatedField.areaHectareas || 0,
            ubicacion: updatedField.ubicacion,
            coordenadas: updatedField.coordenadas || [],
            estado: updatedField.estado?.toLowerCase() || 'activo',
            fechaCreacion: updatedField.fechaCreacion || new Date().toISOString().split('T')[0],
            descripcion: updatedField.descripcion || ''
          };
          setCampos(prev => [...prev, mappedField]);
        }
        
        alert(`✅ Campo ${isEditing ? 'actualizado' : 'creado'} exitosamente`);
    closeModal();
      } else {
        const errorData = await response.text();
        console.error('Error guardando campo:', errorData);
        alert(`❌ Error al ${isEditing ? 'actualizar' : 'crear'} el campo. Por favor, inténtalo de nuevo.`);
      }
    } catch (error) {
      console.error('Error de conexión al guardar el campo:', error);
      alert('❌ Error de conexión. Por favor, verifica tu conexión e intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleEliminarCampo = async (campoId: number) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el campo?`)) {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          console.error('No hay token de autenticación');
          alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
          return;
        }

        console.log('Intentando eliminar campo con ID:', campoId);
        
        const response = await api.delete(`/api/campos/${campoId}`);

        console.log('Respuesta del servidor:', {
          status: response.status,
          statusText: response.statusText
        });

        if (response.status >= 200 && response.status < 300) {
          // Con eliminación lógica, recargar la lista de campos para reflejar el cambio
          console.log('Campo eliminado exitosamente (eliminación lógica)');
          alert('Campo eliminado correctamente');
          // Recargar la lista de campos para mostrar solo los activos
          cargarCampos();
        } else {
          // Intentar obtener el mensaje de error del servidor
          let errorMessage = 'Error desconocido';
          try {
            const errorData = await response.text();
            console.error('Error del servidor:', errorData);
            errorMessage = errorData || `Error ${response.status}: ${response.statusText}`;
          } catch (e) {
            console.error('No se pudo leer el mensaje de error del servidor');
          }
          
          console.error('Error al eliminar el campo:', response.status, response.statusText);
          alert(`Error al eliminar el campo: ${errorMessage}`);
        }
      } catch (error) {
        console.error('Error de conexión al eliminar el campo:', error);
        alert('Error de conexión al eliminar el campo. Por favor, verifica tu conexión e intenta nuevamente.');
      }
    }
  };

  const closeModal = () => {
    console.log('=== CLOSE MODAL EJECUTADO ===');
    console.log('Timestamp:', new Date().toISOString());
    setShowDetailsModal(false);
    setShowEditModal(false);
    setShowAddModal(false);
    setShowMapModal(false);
    setSelectedField(null);
    setIsEditing(false);
    setIsButtonClicked(false); // Resetear estado del botón
    // Limpiar búsqueda al cerrar modal
    setSearchQuery('');
    setSearchResults([]);
    setShowSearchResults(false);
    // Limpiar estados del mapa
    setMap(null);
    setMapModal(null);
    setDrawingManager(null);
    setUserLocation(null);
    setLocationError(null);
    setIsDrawingMode(false);
  };

  const handleInputChange = (field: keyof FormData, value: any) => {
    console.log('=== HANDLE INPUT CHANGE EJECUTADO ===');
    console.log('Timestamp:', new Date().toISOString());
    console.log('Field:', field, 'Value:', value);
    console.log('Current formData:', formData);
    console.log('Stack trace:', new Error().stack);
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
            {campos.reduce((sum, campo) => sum + campo.superficie, 0).toFixed(2)} ha
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
        <PermissionGate permission="canCreateFields">
          <button 
            onClick={(e) => {
              console.log('=== BOTÓN AGREGAR CAMPO CLICKEADO ===');
              console.log('Timestamp:', new Date().toISOString());
              console.log('Event:', e);
              console.log('Event type:', e.type);
              console.log('Event target:', e.target);
              console.log('Stack trace:', new Error().stack);
              e.preventDefault();
              e.stopPropagation();
              handleAgregarCampo();
            }}
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
        </PermissionGate>
        
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
                    {campo.superficie.toFixed(2)} hectáreas
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
                
                <PermissionGate permission="canEditFields">
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
                </PermissionGate>
                
                <PermissionGate permission="canDeleteFields">
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
                </PermissionGate>
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
        <div 
          onClick={closeModal}
          style={{
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
          <div 
            onClick={(e) => e.stopPropagation()}
            style={{
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
              <div>
                <h2 style={{ margin: 0, color: '#1f2937' }}>
                  🗺️ Mapa de Campos
                </h2>
                {locationError && (
                  <div style={{ fontSize: '0.75rem', color: '#f59e0b', marginTop: '0.25rem' }}>
                    ⚠️ {locationError}
                  </div>
                )}
              </div>
              <div>
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
            </div>

            {/* Buscador de lugares */}
            <div style={{
              padding: '1rem',
              borderBottom: '1px solid #e5e7eb',
              backgroundColor: '#f9fafb'
            }}>
              <div style={{ position: 'relative' }}>
                <input
                  ref={searchInputRef}
                  type="text"
                  placeholder="🔍 Buscar lugar, dirección, ciudad..."
                  value={searchQuery}
                  onChange={handleSearchInputChange}
                  style={{
                    width: '100%',
                    padding: '0.75rem 1rem',
                    border: '2px solid #d1d5db',
                    borderRadius: '0.5rem',
                    fontSize: '1rem',
                    backgroundColor: 'white',
                    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
                  }}
                />
                
                {/* Resultados de búsqueda */}
                {showSearchResults && searchResults.length > 0 && (
                  <div style={{
                    position: 'absolute',
                    top: '100%',
                    left: 0,
                    right: 0,
                    backgroundColor: 'white',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                    zIndex: 1000,
                    maxHeight: '200px',
                    overflowY: 'auto'
                  }}>
                    {searchResults.map((place, index) => (
                      <div
                        key={place.place_id || index}
                        onClick={() => selectSearchResult(place)}
                        style={{
                          padding: '0.75rem 1rem',
                          cursor: 'pointer',
                          borderBottom: index < searchResults.length - 1 ? '1px solid #f3f4f6' : 'none',
                          transition: 'background-color 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                      >
                        <div style={{ fontWeight: '500', color: '#1f2937', marginBottom: '0.25rem' }}>
                          📍 {place.name}
                        </div>
                        <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                          {place.formatted_address}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
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
              
              {/* Leyenda de estados */}
              <div style={{
                position: 'absolute',
                top: '20px',
                right: '20px',
                backgroundColor: 'white',
                padding: '1rem',
                borderRadius: '0.5rem',
                boxShadow: '0 4px 12px rgba(0,0,0,0.25)',
                zIndex: 9999,
                minWidth: '200px',
                border: '1px solid #e5e7eb',
                opacity: 0.95
              }}>
                <h4 style={{ margin: '0 0 0.75rem 0', fontSize: '0.875rem', color: '#1f2937' }}>
                  🎨 Estados de Campos
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <div style={{
                      width: '16px',
                      height: '16px',
                      backgroundColor: '#10b981',
                      borderRadius: '2px',
                      border: '1px solid #059669'
                    }}></div>
                    <span style={{ fontSize: '0.75rem', color: '#374151' }}>Activo</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <div style={{
                      width: '16px',
                      height: '16px',
                      backgroundColor: '#f59e0b',
                      borderRadius: '2px',
                      border: '1px solid #d97706'
                    }}></div>
                    <span style={{ fontSize: '0.75rem', color: '#374151' }}>En Mantenimiento</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <div style={{
                      width: '16px',
                      height: '16px',
                      backgroundColor: '#ef4444',
                      borderRadius: '2px',
                      border: '1px solid #dc2626'
                    }}></div>
                    <span style={{ fontSize: '0.75rem', color: '#374151' }}>Inactivo</span>
                  </div>
                </div>
              </div>
              
              {/* Información de campos */}
              <div style={{
                position: 'absolute',
                top: '20px',
                left: '20px',
                backgroundColor: 'white',
                padding: '1rem',
                borderRadius: '0.5rem',
                boxShadow: '0 4px 12px rgba(0,0,0,0.25)',
                zIndex: 9999,
                minWidth: '200px',
                border: '1px solid #e5e7eb',
                opacity: 0.95
              }}>
                <h4 style={{ margin: '0 0 0.75rem 0', fontSize: '0.875rem', color: '#1f2937' }}>
                  📊 Resumen de Campos
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                  <div style={{ fontSize: '0.75rem', color: '#374151' }}>
                    <strong>Total:</strong> {campos.length} campos
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#374151' }}>
                    <strong>Activos:</strong> {campos.filter(c => c.estado === 'activo').length}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#374151' }}>
                    <strong>Superficie total:</strong> {campos.reduce((sum, c) => sum + c.superficie, 0).toFixed(2)} ha
                  </div>
                </div>
              </div>
              
              {/* Botones de control - Esquina inferior izquierda del modal */}
              <div style={{
                position: 'absolute',
                bottom: '20px',
                left: '20px',
                zIndex: 9999,
                display: 'flex',
                flexDirection: 'column',
                gap: '0.5rem'
              }}>
                <button
                  onClick={(e) => {
                    console.log('=== BOTÓN MI UBICACIÓN CLICKEADO ===');
                    console.log('Event:', e);
                    console.log('Current formData:', formData);
                    console.log('showAddModal:', showAddModal);
                    console.log('showEditModal:', showEditModal);
                    console.log('isEditing:', isEditing);
                    e.stopPropagation();
                    getUserLocation();
                  }}
                  style={{
                    background: '#4285F4',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1rem',
                    borderRadius: '0.5rem',
                    cursor: 'pointer',
                    fontSize: '0.875rem',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    fontWeight: '500'
                  }}
                  title="Centrar en mi ubicación"
                >
                  📍 Mi ubicación
                </button>
                
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    centerMapOnAllFields();
                  }}
                  style={{
                    background: '#10b981',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1rem',
                    borderRadius: '0.5rem',
                    cursor: 'pointer',
                    fontSize: '0.875rem',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    fontWeight: '500'
                  }}
                  title="Centrar en todos los campos"
                >
                  🎯 Todos los campos
                </button>
              </div>
              
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
        <div 
          onClick={closeModal}
          style={{
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
          <div 
            onClick={(e) => e.stopPropagation()}
            style={{
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
              <strong>Superficie:</strong> {selectedField.superficie.toFixed(2)} hectáreas
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Estado:</strong> {getEstadoTexto(selectedField.estado)}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Fecha de Creación:</strong> {formatDate(selectedField.fechaCreacion)}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Coordenadas:</strong>
              <div style={{ fontFamily: 'monospace', fontSize: '0.875rem', marginTop: '0.5rem', marginBottom: '1rem' }}>
                {selectedField.coordenadas && selectedField.coordenadas.length > 0 ? (
                  selectedField.coordenadas.map((coord, index) => (
                  <div key={index}>
                    Punto {index + 1}: {coord.lat.toFixed(6)}, {coord.lng.toFixed(6)}
                  </div>
                  ))
                ) : (
                  <div style={{ color: '#6b7280' }}>No hay coordenadas disponibles</div>
                )}
              </div>
              
              {/* Mapa del campo */}
              {selectedField.coordenadas && selectedField.coordenadas.length > 0 && (
                <div style={{ marginTop: '1rem' }}>
                  <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '1rem' }}>🗺️ Ubicación en el Mapa</h4>
                  <div 
                    ref={mapRef}
                    style={{
                      width: '100%',
                      height: '300px',
                      border: '2px solid #d1d5db',
                      borderRadius: '0.5rem',
                      overflow: 'hidden'
                    }}
                  />
                </div>
              )}
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
        <div 
          onClick={closeModal}
          style={{
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
          <div 
            onClick={(e) => e.stopPropagation()}
            style={{
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
                  </div>
                  </div>

              {/* Separador visual */}
                      <div style={{
                margin: '2rem 0', 
                height: '1px', 
                backgroundColor: '#e5e7eb' 
              }} />

              {/* Mapa - Parte Media */}
              <div style={{ marginBottom: '2rem' }}>
                <div style={{ marginBottom: '1rem' }}>
                  <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151', fontSize: '1.1rem' }}>
                    🗺️ Dibujar Campo en el Mapa
                  </h3>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 1rem 0', lineHeight: '1.5' }}>
                    <p style={{ margin: '0 0 0.5rem 0' }}>
                      <strong>📋 Instrucciones para marcar el campo:</strong>
                    </p>
                    <ol style={{ margin: '0 0 0.5rem 0', paddingLeft: '1.5rem' }}>
                      <li>Usa el <strong>buscador</strong> en la parte superior para encontrar la ubicación del campo</li>
                      <li>O haz clic en <strong>"📍 Mi ubicación"</strong> (esquina inferior izquierda) para centrar el mapa y activar automáticamente el modo de dibujo</li>
                      <li>Alternativamente, haz clic en el <strong>botón de polígono</strong> (centro superior del mapa) para activar manualmente la herramienta de dibujo</li>
                      <li>Dibuja el contorno del campo haciendo clic en los puntos del perímetro</li>
                      <li>Haz doble clic para cerrar el polígono y completar el dibujo</li>
                    </ol>
                    <p style={{ margin: '0', color: '#10b981', fontWeight: '500' }}>
                      ✅ La superficie se calculará automáticamente al completar el dibujo
                    </p>
                  </div>
                  
                  {/* Indicador de modo de dibujo activo */}
                  {isDrawingMode && (
                    <div style={{ 
                      fontSize: '0.875rem', 
                      color: '#059669', 
                      marginBottom: '0.5rem', 
                      padding: '0.75rem', 
                      backgroundColor: '#d1fae5', 
                      borderRadius: '0.5rem',
                      border: '1px solid #10b981',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem'
                    }}>
                      <span>✏️</span>
                      <span><strong>Modo de dibujo activo</strong> - Haz clic en el mapa para dibujar el polígono del campo</span>
                    </div>
                  )}

                  {/* Mapa */}
                <div style={{
                    width: '100%',
                    height: '400px',
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

                  {/* Botón de centrar en mi ubicación - Esquina inferior izquierda */}
                  <div style={{
                    position: 'absolute',
                    bottom: '10px',
                    left: '10px',
                    zIndex: 1000
                  }}>
                    <button
                        onClick={(e) => {
                          e.stopPropagation();
                          getUserLocation();
                        }}
                      style={{
                        background: '#4285F4',
                        color: 'white',
                        border: 'none',
                        padding: '0.75rem',
                        borderRadius: '0.5rem',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                        boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem',
                        fontWeight: '500'
                      }}
                      title="Centrar en mi ubicación"
                    >
                      📍 Mi ubicación
                    </button>
                  </div>

                  {/* Botón de herramienta de polígono personalizado */}
                  <div style={{
                    position: 'absolute',
                    top: '10px',
                    left: '50%',
                    transform: 'translateX(-50%)',
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
              </div>

              {/* Separador visual */}
              <div style={{ 
                margin: '2rem 0', 
                height: '1px', 
                backgroundColor: '#e5e7eb' 
              }} />

              {/* Campos restantes */}
              <div style={{ marginBottom: '2rem' }}>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
                  gap: '1rem'
                }}>
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
                      step="0.01"
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
                </div>
              </div>

              {/* Separador visual */}
              <div style={{ 
                margin: '2rem 0', 
                height: '1px', 
                backgroundColor: '#e5e7eb' 
              }} />

              {/* Coordenadas - Mostrar después del mapa */}
              {formData.coordenadas.length > 0 && (
                <div style={{ marginBottom: '2rem' }}>
                  <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151', fontSize: '1.1rem' }}>
                    📍 Coordenadas del Campo
                  </h3>
                  <div style={{
                    padding: '1rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    backgroundColor: '#f9fafb',
                    maxHeight: '150px',
                    overflowY: 'auto'
                  }}>
                    <div style={{ 
                      fontSize: '0.875rem', 
                      color: '#6b7280', 
                      marginBottom: '0.5rem' 
                    }}>
                      Se han registrado {formData.coordenadas.length} puntos que definen el perímetro del campo:
                    </div>
                    {formData.coordenadas.map((coord, index) => (
                      <div key={index} style={{
                        fontSize: '0.875rem',
                        fontFamily: 'monospace',
                        marginBottom: '0.25rem',
                        padding: '0.25rem 0.5rem',
                        backgroundColor: 'white',
                        borderRadius: '0.25rem',
                        border: '1px solid #e5e7eb'
                      }}>
                        <strong>Punto {index + 1}:</strong> Lat: {coord.lat.toFixed(6)}, Lng: {coord.lng.toFixed(6)}
                      </div>
                    ))}
                  </div>
                </div>
              )}

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
