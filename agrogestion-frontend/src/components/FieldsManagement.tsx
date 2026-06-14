import React, { useState, useEffect, useRef } from 'react';
import { TerraDraw, TerraDrawPolygonMode, TerraDrawRenderMode } from 'terra-draw';
import { TerraDrawGoogleMapsAdapter } from 'terra-draw-google-maps-adapter';
import FieldWeatherButton from './FieldWeatherButton';
import OpenMeteoWeatherWidget from './OpenMeteoWeatherWidget';
import PermissionGate from './PermissionGate';
import { loadGoogleMaps, GOOGLE_MAPS_CONFIG } from '../config/googleMaps';
import { camposService } from '../services/apiServices';
import { Icon } from './icons';

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

/** Valida que lat/lng estén en rangos válidos (mismas condiciones que el polígono del mapa). */
function coordenadaValida(lat: number, lng: number): boolean {
  return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180 && !Number.isNaN(lat) && !Number.isNaN(lng);
}

/** Calcula superficie aproximada en hectáreas desde un polígono de coordenadas (misma lógica que el mapa). */
function calcularSuperficieHectareas(coords: Array<{lat: number; lng: number}>): number {
  if (!coords || coords.length < 3) return 0;
  const n = coords.length;
  let areaKm2 = 0;
  for (let i = 0; i < n; i++) {
    const j = (i + 1) % n;
    const latRadI = (coords[i].lat * Math.PI) / 180;
    const latRadJ = (coords[j].lat * Math.PI) / 180;
    const xi = (coords[i].lng * 111320 * Math.cos(latRadI)) / 1000;
    const yi = (coords[i].lat * 110540) / 1000;
    const xj = (coords[j].lng * 111320 * Math.cos(latRadJ)) / 1000;
    const yj = (coords[j].lat * 110540) / 1000;
    areaKm2 += xi * yj - xj * yi;
  }
  areaKm2 = Math.abs(areaKm2) / 2;
  return Math.round((areaKm2 * 100) * 100) / 100;
}

/** Extrae coordenadas {lat,lng}[] desde GeoJSON Polygon y calcula superficie en ha. GeoJSON usa [lng,lat]. */
function extraerCoordenadasYSuperficie(coordsGeo: [number, number][]): { coordenadas: Array<{lat: number; lng: number}>; superficie: number } {
  if (!coordsGeo || coordsGeo.length < 3) return { coordenadas: [], superficie: 0 };
  const sinDuplicado = coordsGeo.length > 1 && coordsGeo[0][0] === coordsGeo[coordsGeo.length - 1][0] && coordsGeo[0][1] === coordsGeo[coordsGeo.length - 1][1]
    ? coordsGeo.slice(0, -1) : coordsGeo;
  const [a, b] = sinDuplicado[0];
  const esLngLat = Math.abs(a) <= 180 && Math.abs(b) <= 90;
  const coordenadas = sinDuplicado.map(([x, y]) => esLngLat ? { lat: y, lng: x } : { lat: x, lng: y });
  if (coordenadas.length < 3) return { coordenadas: [], superficie: 0 };
  let superficie = 0;
  if (window.google?.maps?.geometry?.spherical) {
    const path = new window.google.maps.MVCArray(coordenadas.map(c => new window.google.maps.LatLng(c.lat, c.lng)));
    const areaM2 = window.google.maps.geometry.spherical.computeArea(path);
    superficie = Math.round((areaM2 / 10000) * 100) / 100;
  }
  if (superficie === 0) superficie = calcularSuperficieHectareas(coordenadas);
  return { coordenadas, superficie };
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
  const [mapError, setMapError] = useState<string | null>(null);
  const [map, setMap] = useState<any>(null);
  const [mapModal, setMapModal] = useState<any>(null);
  const [terraDraw, setTerraDraw] = useState<TerraDraw | null>(null);
  const [formData, setFormData] = useState<FormData>({
    nombre: '',
    superficie: 0,
    ubicacion: '',
    descripcion: '',
    estado: 'activo',
    coordenadas: []
  });

  const setFormDataWithLog = (newFormData: FormData | ((prev: FormData) => FormData)) => {
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
  /** 'mapa' = dibujar en Google Maps; 'manual' = ingresar coordenadas a mano (mismas validaciones). */
  const [modoCoordenadasCampo, setModoCoordenadasCampo] = useState<'mapa' | 'manual'>('mapa');
  const mapRef = useRef<HTMLDivElement>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);

  // Detectar tamaño de pantalla
  const isMobile = window.innerWidth <= 768;

  useEffect(() => {
    cargarCampos();
    
    // Obtener ubicación del usuario automáticamente al iniciar
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          setUserLocation(location);
          setLocationError(null);
          // Inicializar mapa después de obtener la ubicación
          initializeMap();
        },
        (error) => {
          console.warn('⚠️ No se pudo obtener la ubicación al iniciar:', error.message);
          setLocationError('No se pudo obtener la ubicación. Se usará ubicación por defecto.');
          // Inicializar mapa con ubicación por defecto
          initializeMap();
        },
        {
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 300000 // 5 minutos - usar ubicación en caché si está disponible
        }
      );
    } else {
      initializeMap();
    }
  }, []);

  // Limpiar mapa cuando se cierre el modal
  useEffect(() => {
    if (!showAddModal && !showEditModal && !showMapModal) {
      setMap(null);
      setMapModal(null);
      setTerraDraw(prev => {
        if (prev) try { prev.stop(); } catch { /* ignorar */ }
        return null;
      });
      setIsDrawingMode(false);
    }
  }, [showAddModal, showEditModal, showMapModal]);

  // Inicializar mapa en modal de detalles cuando se abre
  useEffect(() => {
    if (showDetailsModal && selectedField && mapLoaded) {
      setTimeout(() => {
        initializeMapInDetails(selectedField);
      }, 100);
    }
  }, [showDetailsModal, selectedField, mapLoaded]);

  // Inicializar mapa en modal agregar/editar cuando se abre con modo "mapa"
  useEffect(() => {
    if (!(showAddModal || showEditModal) || modoCoordenadasCampo !== 'mapa' || !mapLoaded) return;
    const init = () => {
      if (mapRef.current && window.google) {
        if (showEditModal && selectedField?.coordenadas && selectedField.coordenadas.length > 0) {
          initializeMapInForm(selectedField.coordenadas, true);
        } else {
          initializeMapInForm(undefined, false);
        }
      }
    };
    const t = setTimeout(init, 300);
    return () => clearTimeout(t);
  }, [showAddModal, showEditModal, modoCoordenadasCampo, mapLoaded, selectedField]);

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

  // Función auxiliar para obtener el centro del mapa (userLocation si está disponible, sino DEFAULT_CENTER)
  const getMapCenter = (): {lat: number; lng: number} => {
    return userLocation || GOOGLE_MAPS_CONFIG.DEFAULT_CENTER;
  };

  const getUserLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          setUserLocation(location);
          setLocationError(null);
          // Centrar el mapa en la ubicación del usuario
          const currentMap = showMapModal ? mapModal : map;
          const currentTerraDraw = showMapModal ? null : terraDraw;
          
          if (currentMap) {
            currentMap.setCenter(location);
            currentMap.setZoom(15);
            
            // Si estamos en el modal de agregar/editar campo, activar modo de dibujo
            if (!showMapModal && currentTerraDraw) {
              setTimeout(() => {
                currentTerraDraw.setMode('polygon');
                setIsDrawingMode(true);
              }, 100);
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
      setLocationError('Geolocalización no soportada por este navegador.');
    }
  };

  const centerMapOnAllFields = () => {
    const currentMap = showMapModal ? mapModal : map;
    if (!currentMap || !campos.length) return;

    // Filtrar campos que tienen coordenadas válidas
    const camposConCoordenadas = campos.filter(campo => 
      campo.coordenadas && campo.coordenadas.length > 0
    );

    if (camposConCoordenadas.length === 0) return;

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
    try {
      loadGoogleMaps(() => {
        // Verificar que Google Maps realmente se cargó
        if (window.google && window.google.maps) {
          setMapLoaded(true);
          setMapError(null);
        } else {
          console.warn('⚠️ Google Maps no está disponible');
          setMapError('Google Maps no está disponible. Verifica que la API key sea válida y que la facturación esté habilitada.');
          setMapLoaded(false);
        }
      });
    } catch (error) {
      console.error('❌ Error inicializando Google Maps:', error);
      setMapError('Error al cargar Google Maps. La aplicación continuará funcionando sin mapas.');
      setMapLoaded(false);
    }
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
      const data = await camposService.listar();
      
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
    setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 100);
    // El mapa se inicializa en el useEffect cuando el modal y modoCoordenadasCampo están listos
  };

  const handleAgregarCampo = () => {
    if (isButtonClicked) return;
    
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
    setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 100);
    // El mapa se inicializa en el useEffect cuando el modal y modoCoordenadasCampo están listos
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

    // Función auxiliar para inicializar el mapa completo con un centro específico
    const initializeMapWithLocation = (center: {lat: number; lng: number} = getMapCenter()) => {
      const mapInstance = new window.google.maps.Map(mapRef.current!, {
        center: center,
        zoom: GOOGLE_MAPS_CONFIG.DEFAULT_ZOOM,
        mapTypeId: 'satellite',
        mapTypeControl: true,
        streetViewControl: false,
        fullscreenControl: true,
        disableDoubleClickZoom: true
      });

      setMapModal(mapInstance);
      setUserLocation(center);

      // TerraDraw para dibujar polígonos (reemplaza DrawingManager deprecado)
      const adapter = new TerraDrawGoogleMapsAdapter({ map: mapInstance, lib: window.google.maps, coordinatePrecision: 9 });
      const drawInstance = new TerraDraw({
        adapter,
        modes: [
          new TerraDrawPolygonMode({
            showCoordinatePoints: true,
            styles: {
              fillColor: '#4CAF50',
              outlineColor: '#4CAF50',
              outlineWidth: 2
            }
          }),
          new TerraDrawRenderMode({ modeName: 'static', styles: {} } as ConstructorParameters<typeof TerraDrawRenderMode>[0])
        ]
      });
      // Iniciar TerraDraw cuando el mapa esté completamente renderizado (evita addEventListener null)
      const iniciarTerraDrawModal = () => {
        drawInstance.start();
        drawInstance.setMode('static');
        setTerraDraw(drawInstance);
      };
      window.google.maps.event.addListenerOnce(mapInstance, 'idle', iniciarTerraDrawModal);

      drawInstance.on('finish', (id: string | number, context: { action?: string }) => {
        if (context?.action !== 'draw') return;
        const snapshot = drawInstance.getSnapshot();
        const poligono = snapshot.find((f: any) => f.id === id || f.geometry?.type === 'Polygon');
        if (!poligono?.geometry?.coordinates?.[0]) return;
        const coordsGeo = poligono.geometry.coordinates[0] as [number, number][];
        const { coordenadas, superficie } = extraerCoordenadasYSuperficie(coordsGeo);
        if (coordenadas.length < 3) return;
        setFormData(prev => ({ ...prev, coordenadas, superficie }));
        requestAnimationFrame(() => drawInstance.setMode('static'));
        const bounds = new window.google.maps.LatLngBounds();
        coordenadas.forEach((c: {lat: number; lng: number}) => bounds.extend(new window.google.maps.LatLng(c.lat, c.lng)));
        const superficieTexto = superficie > 0 ? `${superficie} ha` : '< 0.01 ha';
        const infoWindow = new window.google.maps.InfoWindow({
          content: `<div style="padding: 10px;"><h4 style="margin: 0 0 10px 0;">Nuevo Campo</h4><p style="margin: 5px 0;"><strong>Superficie:</strong> ${superficieTexto}</p><p style="margin: 5px 0;"><strong>Coordenadas:</strong> ${coordenadas.length} puntos</p></div>`
        });
        infoWindow.setPosition(bounds.getCenter());
        infoWindow.open(mapInstance);
      });

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
    };

    // Intentar obtener ubicación del usuario
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          initializeMapWithLocation(location);
        },
          (error) => {
            console.warn('⚠️ No se pudo obtener la ubicación del usuario:', error.message);
            initializeMapWithLocation();
        },
        {
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 0
        }
      );
    } else {
      initializeMapWithLocation();
    }
  };

  const initializeMapInForm = (existingCoordinates?: Array<{lat: number; lng: number}>, isEditMode?: boolean) => {
    if (!window.google || !mapRef.current) return;

    // Limpiar el contenido del div del mapa
    if (mapRef.current) {
      mapRef.current.innerHTML = '';
    }

    // Función auxiliar para inicializar el mapa completo con un centro específico
    const createMapInstance = (center: {lat: number; lng: number}) => {
      const mapInstance = new window.google.maps.Map(mapRef.current!, {
        center: center,
        zoom: 15,
        mapTypeId: 'satellite',
        mapTypeControl: true,
        streetViewControl: false,
        fullscreenControl: true,
        disableDoubleClickZoom: true
      });

      setMap(mapInstance);
      setUserLocation(center);

      // TerraDraw para dibujar polígonos (reemplaza DrawingManager deprecado)
      const adapter = new TerraDrawGoogleMapsAdapter({ map: mapInstance, lib: window.google.maps, coordinatePrecision: 9 });
      const drawInstance = new TerraDraw({
        adapter,
        modes: [
          new TerraDrawPolygonMode({
            showCoordinatePoints: true,
            styles: {
              fillColor: getEstadoColor(formData.estado),
              outlineColor: getEstadoColor(formData.estado),
              outlineWidth: 2
            }
          }),
          new TerraDrawRenderMode({ modeName: 'static', styles: {} } as ConstructorParameters<typeof TerraDrawRenderMode>[0])
        ]
      });
      // Iniciar TerraDraw cuando el mapa esté completamente renderizado (evita addEventListener null)
      const iniciarTerraDraw = () => {
        drawInstance.start();
        drawInstance.setMode('static');
        setTerraDraw(drawInstance);
      };
      window.google.maps.event.addListenerOnce(mapInstance, 'idle', iniciarTerraDraw);

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

        const bounds = new window.google.maps.LatLngBounds();
        coordinatesToUse.forEach(coord => {
          bounds.extend(new window.google.maps.LatLng(coord.lat, coord.lng));
        });
        mapInstance.fitBounds(bounds);
      }

      drawInstance.on('finish', (id: string | number, context: { action?: string }) => {
        if (context?.action !== 'draw') return;
        const snapshot = drawInstance.getSnapshot();
        const poligono = snapshot.find((f: any) => f.id === id || f.geometry?.type === 'Polygon');
        if (!poligono?.geometry?.coordinates?.[0]) return;
        const coordsGeo = poligono.geometry.coordinates[0] as [number, number][];
        const { coordenadas, superficie } = extraerCoordenadasYSuperficie(coordsGeo);
        if (coordenadas.length < 3) return;
        setFormData(prev => ({ ...prev, coordenadas, superficie }));
        setIsDrawingMode(false);
        requestAnimationFrame(() => drawInstance.setMode('static'));
        const bounds = new window.google.maps.LatLngBounds();
        coordenadas.forEach((c: {lat: number; lng: number}) => bounds.extend(new window.google.maps.LatLng(c.lat, c.lng)));
        const superficieTexto = superficie > 0 ? `${superficie} ha` : '< 0.01 ha';
        const infoWindow = new window.google.maps.InfoWindow({
          content: `<div style="padding: 10px;"><h4 style="margin: 0 0 10px 0;">Campo Dibujado</h4><p style="margin: 5px 0;"><strong>Superficie:</strong> ${superficieTexto}</p><p style="margin: 5px 0;"><strong>Coordenadas:</strong> ${coordenadas.length} puntos</p></div>`
        });
        infoWindow.setPosition(bounds.getCenter());
        infoWindow.open(mapInstance);
      });
    };

    // Si estamos editando y hay coordenadas existentes, usarlas directamente
    if (isEditMode && existingCoordinates && existingCoordinates.length > 0) {
      createMapInstance(existingCoordinates[0]);
    } else {
      // Intentar obtener la ubicación del usuario automáticamente
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const location = {
              lat: position.coords.latitude,
              lng: position.coords.longitude
            };
            createMapInstance(location);
          },
          (error) => {
            console.warn('⚠️ No se pudo obtener la ubicación del usuario:', error.message);
            createMapInstance(getMapCenter());
          },
          {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
          }
        );
      } else {
        createMapInstance(getMapCenter());
      }
    }
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
      alert('⚠️ Debes definir el polígono del campo: dibuja en el mapa o ingresa al menos 3 coordenadas (lat/lng) válidas.');
      return;
    }
    const algunaInvalida = formData.coordenadas.some(c => !coordenadaValida(c.lat, c.lng));
    if (algunaInvalida) {
      alert('⚠️ Todas las coordenadas deben ser válidas: latitud entre -90 y 90, longitud entre -180 y 180.');
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

      let updatedField;
      if (isEditing && selectedField) {
        // Editar campo existente
        updatedField = await camposService.actualizar(selectedField.id, fieldData);
      } else {
        // Crear nuevo campo
        updatedField = await camposService.crear(fieldData);
      }

      if (updatedField) {
        
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

        
        await camposService.eliminar(campoId);

        
        // Con eliminación lógica, recargar la lista de campos para reflejar el cambio
        alert('Campo eliminado correctamente');
        // Recargar la lista de campos para mostrar solo los activos
        cargarCampos();
      } catch (error) {
        console.error('Error de conexión al eliminar el campo:', error);
        alert('Error de conexión al eliminar el campo. Por favor, verifica tu conexión e intenta nuevamente.');
      }
    }
  };

  const closeModal = () => {
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
    setTerraDraw(prev => {
      if (prev) {
        try { prev.stop(); } catch { /* ignorar */ }
      }
      return null;
    });
    setUserLocation(null);
    setLocationError(null);
    setIsDrawingMode(false);
    setModoCoordenadasCampo('mapa');
  };

  const handleInputChange = (field: keyof FormData, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  /** Actualiza una coordenada en modo manual y recalcula superficie si hay ≥3 puntos válidos. */
  const actualizarCoordenadaManual = (indice: number, campo: 'lat' | 'lng', valorStr: string) => {
    const valor = parseFloat(valorStr);
    setFormData(prev => {
      const nuevaLista = [...(prev.coordenadas || [])];
      if (!nuevaLista[indice]) nuevaLista[indice] = { lat: 0, lng: 0 };
      nuevaLista[indice] = { ...nuevaLista[indice], [campo]: Number.isNaN(valor) ? 0 : valor };
      const todasValidas = nuevaLista.length >= 3 && nuevaLista.every(c => coordenadaValida(c.lat, c.lng));
      const superficie = todasValidas ? calcularSuperficieHectareas(nuevaLista) : prev.superficie;
      return { ...prev, coordenadas: nuevaLista, superficie };
    });
  };

  const agregarPuntoManual = () => {
    setFormData(prev => ({
      ...prev,
      coordenadas: [...(prev.coordenadas || []), { lat: 0, lng: 0 }]
    }));
  };

  const quitarPuntoManual = (indice: number) => {
    setFormData(prev => {
      const nuevaLista = prev.coordenadas.filter((_, i) => i !== indice);
      const todasValidas = nuevaLista.length >= 3 && nuevaLista.every(c => coordenadaValida(c.lat, c.lng));
      const superficie = todasValidas ? calcularSuperficieHectareas(nuevaLista) : prev.superficie;
      return { ...prev, coordenadas: nuevaLista, superficie };
    });
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
          <Icon name="XCircle" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Error
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
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Map" size={28} /> Gestión de Campos</h1>
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
            <Icon name="Plus" size={20} style={{ display: 'inline-block' }} /> Agregar Campo
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
          <Icon name="Map" size={20} style={{ display: 'inline-block' }} /> Ver Mapa
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
                    <Icon name="MapPin" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Ubicación
                  </div>
                  <div style={{ fontSize: '0.95rem', color: '#374151' }}>
                    {campo.ubicacion}
                  </div>
                </div>
                
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    <Icon name="Ruler" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Superficie
                  </div>
                  <div style={{ fontSize: '0.95rem', color: '#374151' }}>
                    {campo.superficie.toFixed(2)} hectáreas
                  </div>
                </div>
                
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    <Icon name="CalendarDays" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Fecha de Creación
                  </div>
                  <div style={{ fontSize: '0.95rem', color: '#374151' }}>
                    {formatDate(campo.fechaCreacion)}
                  </div>
                </div>
                
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                    <Icon name="Map" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Coordenadas
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
                    <Icon name="FileText" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Descripción
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
                  <Icon name="Eye" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Ver Detalles
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
                    <Icon name="Pencil" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Editar
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
                    <Icon name="Trash2" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Eliminar
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
                  <Icon name="Map" size={24} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Mapa de Campos
                </h2>
                {locationError && (
                  <div style={{ fontSize: '0.75rem', color: '#f59e0b', marginTop: '0.25rem' }}>
                    <Icon name="AlertTriangle" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> {locationError}
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
                  <Icon name="X" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Cerrar
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
                  placeholder="Buscar lugar, dirección, ciudad..."
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
                          <Icon name="MapPin" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> {place.name}
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
                  <Icon name="Palette" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Estados de Campos
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
                  <Icon name="BarChart" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Resumen de Campos
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
                  <Icon name="MapPin" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Mi ubicación
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
                  <Icon name="Target" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Todos los campos
                </button>
              </div>
              
              {!mapLoaded && (
                <div style={{
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%)',
                  textAlign: 'center',
                  color: '#666',
                  zIndex: 1000,
                  backgroundColor: 'white',
                  padding: '1rem',
                  borderRadius: '8px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                  maxWidth: '400px'
                }}>
                  {mapError ? (
                    <>
                      <Icon name="AlertTriangle" size={32} />
                      <div style={{ fontWeight: 'bold', marginBottom: '0.5rem', color: '#dc2626' }}>
                        Google Maps no disponible
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#666', marginBottom: '0.5rem' }}>
                        {mapError}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: '#999', marginTop: '0.5rem' }}>
                        Puedes continuar usando la aplicación sin mapas.
                      </div>
                    </>
                  ) : (
                    <>
                      <div style={{ 
                        width: '40px', 
                        height: '40px',
                        border: '4px solid #e5e7eb',
                        borderTopColor: '#3b82f6',
                        borderRadius: '50%',
                        animation: 'spin 1s linear infinite',
                        margin: '0 auto 0.5rem'
                      }}></div>
                      <div>Cargando mapa...</div>
                    </>
                  )}
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
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Icon name={isEditing ? "Pencil" : "Plus"} size={24} /> {isEditing ? 'Editar Campo' : 'Agregar Nuevo Campo'}
              </span>
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

              {/* Coordenadas del campo: mapa o ingreso manual */}
              <div style={{ marginBottom: '2rem' }}>
                <div style={{ marginBottom: '1rem' }}>
                  <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151', fontSize: '1.1rem' }}>
                    <Icon name="Map" size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Definir perímetro del campo
                  </h3>
                  <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
                    <button
                      type="button"
                      onClick={() => setModoCoordenadasCampo('mapa')}
                      style={{
                        padding: '0.5rem 1rem',
                        borderRadius: '0.375rem',
                        border: modoCoordenadasCampo === 'mapa' ? '2px solid #4CAF50' : '1px solid #d1d5db',
                        background: modoCoordenadasCampo === 'mapa' ? '#dcfce7' : 'white',
                        color: modoCoordenadasCampo === 'mapa' ? '#166534' : '#374151',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                        fontWeight: '500'
                      }}
                    >
                      <Icon name="Map" size={18} style={{ marginRight: '0.25rem', verticalAlign: 'middle' }} /> Dibujar en el mapa
                    </button>
                    <button
                      type="button"
                      onClick={() => setModoCoordenadasCampo('manual')}
                      style={{
                        padding: '0.5rem 1rem',
                        borderRadius: '0.375rem',
                        border: modoCoordenadasCampo === 'manual' ? '2px solid #4CAF50' : '1px solid #d1d5db',
                        background: modoCoordenadasCampo === 'manual' ? '#dcfce7' : 'white',
                        color: modoCoordenadasCampo === 'manual' ? '#166534' : '#374151',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                        fontWeight: '500'
                      }}
                    >
                      <Icon name="Edit" size={18} style={{ marginRight: '0.25rem', verticalAlign: 'middle' }} /> Ingresar coordenadas manualmente
                    </button>
                  </div>

                  {modoCoordenadasCampo === 'mapa' && (
                  <>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 1rem 0', lineHeight: '1.5' }}>
                    <p style={{ margin: '0 0 0.5rem 0' }}>
                      <strong><Icon name="Clipboard" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Instrucciones para marcar el campo:</strong>
                    </p>
                    <ol style={{ margin: '0 0 0.5rem 0', paddingLeft: '1.5rem' }}>
                      <li>Usa el <strong>buscador</strong> en la parte superior para encontrar la ubicación del campo</li>
                      <li>O haz clic en <strong>"📍 Mi ubicación"</strong> (esquina inferior izquierda) para centrar el mapa y activar automáticamente el modo de dibujo</li>
                      <li>Alternativamente, haz clic en el <strong>botón de polígono</strong> (centro superior del mapa) para activar manualmente la herramienta de dibujo</li>
                      <li>Dibuja el contorno del campo haciendo clic en los puntos del perímetro</li>
                      <li>Haz doble clic para cerrar el polígono y completar el dibujo</li>
                    </ol>
                    <p style={{ margin: '0', color: '#10b981', fontWeight: '500' }}>
                      <Icon name="CheckCircle" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> La superficie se calculará automáticamente al completar el dibujo
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
                      <Icon name="Pencil" size={18} />
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
                      <Icon name="MapPin" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Mi ubicación
                    </button>
                  </div>

                  {/* Botón de dibujo: Polígono */}
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
                        if (terraDraw) {
                          terraDraw.clear();
                          terraDraw.setMode('polygon');
                          setIsDrawingMode(true);
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
                      <Icon name="Square" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Polígono
                    </button>
                  </div>
                  {isDrawingMode && (
                    <div style={{
                      position: 'absolute',
                      bottom: '10px',
                      left: '50%',
                      transform: 'translateX(-50%)',
                      zIndex: 1000,
                      padding: '8px 14px',
                      backgroundColor: 'rgba(0,0,0,0.75)',
                      color: 'white',
                      borderRadius: '6px',
                      fontSize: '13px',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.3)'
                    }}>
                      Polígono: clic en cada vértice → doble clic para cerrar.
                    </div>
                  )}
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
                    <Icon name="Lightbulb" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Instrucciones para móvil:
                  </h4>
                  <ul style={{ margin: 0, paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#0c4a6e' }}>
                    <li>Usa los botones + y − para hacer zoom</li>
                    <li><strong>Polígono:</strong> clic en "Polígono", luego clic en cada vértice. Doble clic para cerrar</li>
                    <li>La superficie se calculará automáticamente</li>
                  </ul>
                  </div>
                  </>
                  )}

                  {modoCoordenadasCampo === 'manual' && (
                  <div style={{
                    padding: '1rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    backgroundColor: '#f9fafb'
                  }}>
                    <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '1rem', lineHeight: '1.5' }}>
                      Ingresá al menos 3 puntos (latitud, longitud) en orden para definir el perímetro. Mismas condiciones que en el mapa: latitud entre -90 y 90, longitud entre -180 y 180. La superficie se calculará automáticamente.
                    </p>
                    {(formData.coordenadas || []).map((coord, index) => (
                      <div key={index} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.5rem', flexWrap: 'wrap' }}>
                        <span style={{ fontWeight: '500', minWidth: '4.5rem' }}>Punto {index + 1}</span>
                        <input
                          type="number"
                          step="any"
                          placeholder="Lat (-90 a 90)"
                          value={coord.lat === 0 && coord.lng === 0 ? '' : coord.lat}
                          onChange={e => actualizarCoordenadaManual(index, 'lat', e.target.value)}
                          style={{ width: '120px', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
                        />
                        <input
                          type="number"
                          step="any"
                          placeholder="Lng (-180 a 180)"
                          value={coord.lat === 0 && coord.lng === 0 ? '' : coord.lng}
                          onChange={e => actualizarCoordenadaManual(index, 'lng', e.target.value)}
                          style={{ width: '120px', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
                        />
                        <button
                          type="button"
                          onClick={() => quitarPuntoManual(index)}
                          style={{ padding: '0.5rem 0.75rem', background: '#ef4444', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer', fontSize: '0.875rem' }}
                        >
                          Quitar
                        </button>
                      </div>
                    ))}
                    <button
                      type="button"
                      onClick={agregarPuntoManual}
                      style={{ marginTop: '0.5rem', padding: '0.5rem 1rem', background: '#4CAF50', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer', fontSize: '0.875rem' }}
                    >
                      + Agregar punto
                    </button>
                    {formData.coordenadas.length >= 3 && formData.coordenadas.every(c => coordenadaValida(c.lat, c.lng)) && (
                      <p style={{ marginTop: '0.75rem', fontSize: '0.875rem', color: '#059669', fontWeight: '500' }}>
                        <Icon name="CheckCircle" size={16} style={{ marginRight: '0.25rem', verticalAlign: 'middle' }} />
                        Superficie calculada: {formData.superficie > 0 ? `${formData.superficie.toFixed(2)} ha` : '< 0.01 ha'}
                      </p>
                    )}
                  </div>
                  )}
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
                    <Icon name="MapPin" size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Coordenadas del Campo
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
