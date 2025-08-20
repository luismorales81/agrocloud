// Configuración de Google Maps
export const GOOGLE_MAPS_CONFIG = {
  // Reemplaza 'YOUR_API_KEY' con tu API Key real de Google Maps
  API_KEY: 'AIzaSyCWz9FKCHBdLqbjhHCPcECww5hs2ugiWA0',
  
  // Configuración por defecto del mapa (Buenos Aires)
  DEFAULT_CENTER: {
    lat: -34.6118,
    lng: -58.3960
  },
  
  DEFAULT_ZOOM: 16,
  
  // Estilos del mapa
  MAP_STYLES: [
    {
      featureType: 'poi',
      elementType: 'labels',
      stylers: [{ visibility: 'off' }]
    }
  ]
};

// Función para obtener el centro del mapa basado en la ubicación del usuario
export const getMapCenter = (userLocation?: { lat: number; lng: number }) => {
  if (userLocation) {
    return {
      lat: userLocation.lat,
      lng: userLocation.lng
    };
  }
  
  // Intentar obtener ubicación guardada
  try {
    const savedLocation = localStorage.getItem('userLocation');
    if (savedLocation) {
      const location = JSON.parse(savedLocation);
      return {
        lat: location.lat,
        lng: location.lng
      };
    }
  } catch (e) {
    console.error('Error parsing saved location:', e);
  }
  
  // Usar ubicación por defecto
  return GOOGLE_MAPS_CONFIG.DEFAULT_CENTER;
};

// Función para cargar Google Maps
export const loadGoogleMaps = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if ((window as any).google && (window as any).google.maps) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.src = `https://maps.googleapis.com/maps/api/js?key=${GOOGLE_MAPS_CONFIG.API_KEY}&libraries=drawing,geometry&loading=async`;
    script.async = true;
    script.defer = true;
    
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Error cargando Google Maps'));
    
    document.head.appendChild(script);
  });
};

// Función para verificar si Google Maps está cargado
export const isGoogleMapsLoaded = (): boolean => {
  return !!(window as any).google && !!(window as any).google.maps;
};
