// Configuración de Google Maps para AgroCloud
export const GOOGLE_MAPS_CONFIG = {
  // API Key para desarrollo local
  API_KEY: 'AIzaSyCWz9FKCHBdLqbjhHCPcECww5hs2ugiWA0',
  
  // Configuración por defecto del mapa
  DEFAULT_CENTER: { lat: -34.6118, lng: -58.3960 }, // Buenos Aires
  DEFAULT_ZOOM: 15,
  
  // Estilos del polígono
  POLYGON_STYLES: {
    fillColor: '#4CAF50',
    fillOpacity: 0.3,
    strokeWeight: 2,
    strokeColor: '#4CAF50',
    editable: true,
    draggable: true
  },
  
  // Configuración del Drawing Manager
  DRAWING_MANAGER_OPTIONS: {
    drawingMode: null, // Se establece dinámicamente
    drawingControl: true,
    drawingControlOptions: {
      position: 1, // TOP_CENTER
      drawingModes: ['polygon']
    }
  }
};

// Función para cargar Google Maps dinámicamente
export const loadGoogleMaps = (callback: () => void): void => {
  // Verificar si ya está cargado
  if (window.google && window.google.maps) {
    setTimeout(callback, 100);
    return;
  }

  // Verificar si ya se está cargando
  if (document.querySelector('script[src*="maps.googleapis.com"]')) {
    // Esperar a que termine de cargar
    const checkLoaded = () => {
      if (window.google && window.google.maps) {
        setTimeout(callback, 100);
      } else {
        setTimeout(checkLoaded, 100);
      }
    };
    checkLoaded();
    return;
  }

  // Crear script con configuración mejorada
  const script = document.createElement('script');
  script.src = `https://maps.googleapis.com/maps/api/js?key=${GOOGLE_MAPS_CONFIG.API_KEY}&libraries=drawing,geometry&callback=initGoogleMaps&loading=async&v=weekly`;
  script.async = true;
  script.defer = true;

  // Función global de callback con manejo de errores
  window.initGoogleMaps = () => {
    try {
      // Verificar que Google Maps se cargó correctamente
      if (window.google && window.google.maps) {
        console.log('Google Maps cargado exitosamente');
        setTimeout(callback, 200); // Dar más tiempo para que se inicialice completamente
      } else {
        console.error('Google Maps no se inicializó correctamente');
        throw new Error('Google Maps no se inicializó correctamente');
      }
    } catch (error) {
      console.error('Error en callback de Google Maps:', error);
      throw error;
    }
  };

  // Manejo de errores mejorado
  script.onerror = () => {
    console.error('Error cargando script de Google Maps');
    throw new Error('No se pudo cargar Google Maps');
  };

  script.onload = () => {
    console.log('Script de Google Maps cargado');
  };

  document.head.appendChild(script);
};

// Declaraciones de tipos para TypeScript
declare global {
  interface Window {
    google: any;
    initGoogleMaps: () => void;
  }
}
